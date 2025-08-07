from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.filters import SearchFilter, OrderingFilter
from django_filters.rest_framework import DjangoFilterBackend
from django.db.models import Q, Avg, Count
from django.db import models
from .models import Supplier, SupplierPrice
from .serializers import (
    SupplierListSerializer, SupplierDetailSerializer, 
    SupplierForBudgetSerializer, SupplierCreateUpdateSerializer,
    SupplierPriceSerializer, SupplierSearchSerializer,
    SupplierMaterialPriceSerializer
)

class SupplierViewSet(viewsets.ModelViewSet):
    """
    ViewSet para gestión de proveedores
    """
    queryset = Supplier.objects.filter(is_active=True)
    filter_backends = [SearchFilter, OrderingFilter, DjangoFilterBackend]
    search_fields = ['name', 'commercial_name', 'contact_person', 'city']
    ordering_fields = ['name', 'rating', 'city', 'created_at']
    ordering = ['name']
    filterset_fields = ['supplier_type', 'city', 'rating', 'is_preferred', 'offers_delivery']
    
    def get_serializer_class(self):
        """Retorna serializer según la acción"""
        if self.action == 'list':
            return SupplierListSerializer
        elif self.action in ['create', 'update', 'partial_update']:
            return SupplierCreateUpdateSerializer
        elif self.action == 'for_budget':
            return SupplierForBudgetSerializer
        return SupplierDetailSerializer
    
    def get_queryset(self):
        """Personalizar queryset según filtros"""
        queryset = Supplier.objects.filter(is_active=True)
        
        # Filtro por zona
        zone = self.request.query_params.get('zone')
        if zone:
            queryset = queryset.filter(zone__icontains=zone)
        
        # Filtro por rating mínimo
        min_rating = self.request.query_params.get('min_rating')
        if min_rating:
            try:
                queryset = queryset.filter(rating__gte=int(min_rating))
            except ValueError:
                pass
        
        # Filtro por categoría que maneja
        category_type = self.request.query_params.get('category_type')
        if category_type:
            queryset = queryset.filter(categories__category_type=category_type).distinct()
        
        return queryset
    
    @action(detail=False, methods=['get'])
    def for_budget(self, request):
        """
        Proveedores simplificados para selección en presupuestos
        GET /api/suppliers/for_budget/
        """
        suppliers = self.get_queryset().order_by('-is_preferred', 'name')
        serializer = SupplierForBudgetSerializer(suppliers, many=True)
        return Response(serializer.data)
    
    @action(detail=False, methods=['get'])
    def by_category(self, request):
        """
        Proveedores agrupados por tipo
        GET /api/suppliers/by_category/
        """
        result = {}
        
        for supplier_type, display_name in Supplier.SUPPLIER_TYPES:
            suppliers = self.get_queryset().filter(supplier_type=supplier_type)
            serializer = SupplierListSerializer(suppliers, many=True)
            result[supplier_type] = {
                'type_name': display_name,
                'count': suppliers.count(),
                'suppliers': serializer.data
            }
        
        return Response(result)
    
    @action(detail=True, methods=['get'])
    def materials(self, request, pk=None):
        """
        Materiales que maneja este proveedor con precios
        GET /api/suppliers/{id}/materials/
        """
        supplier = self.get_object()
        prices = supplier.prices.filter(is_current=True).select_related('material')
        serializer = SupplierPriceSerializer(prices, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['post'])
    def add_material_price(self, request, pk=None):
        """
        Agregar precio de material a proveedor
        POST /api/suppliers/{id}/add_material_price/
        """
        supplier = self.get_object()
        
        data = request.data.copy()
        data['supplier'] = supplier.id
        
        serializer = SupplierPriceSerializer(data=data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=False, methods=['post'])
    def compare_prices(self, request):
        """
        Comparar precios de un material entre proveedores
        POST /api/suppliers/compare_prices/
        Body: {"material_id": 1, "quantity": 100}
        """
        serializer = SupplierMaterialPriceSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        
        material_id = serializer.validated_data['material_id']
        quantity = serializer.validated_data.get('quantity', 1)
        
        # Obtener precios actuales del material
        prices = SupplierPrice.objects.filter(
            material_id=material_id,
            is_current=True,
            supplier__is_active=True
        ).select_related('supplier', 'material')
        
        comparison_data = []
        
        for price in prices:
            # Calcular precio considerando descuentos por volumen
            final_price = price.get_bulk_price(quantity)
            total_cost = final_price * quantity
            
            # Calcular costo de entrega si aplica
            delivery_cost = 0
            if price.supplier.offers_delivery and price.supplier.delivery_cost:
                delivery_cost = float(price.supplier.delivery_cost)
            
            comparison_data.append({
                'supplier': SupplierForBudgetSerializer(price.supplier).data,
                'price_info': {
                    'unit_price': float(price.price),
                    'final_unit_price': float(final_price),
                    'total_cost': float(total_cost),
                    'delivery_cost': delivery_cost,
                    'total_with_delivery': float(total_cost) + delivery_cost,
                    'discount_applied': float(price.discount_percentage),
                    'bulk_discount': price.bulk_discount_percentage if quantity >= (price.bulk_discount_threshold or 0) else 0,
                    'currency': price.currency
                }
            })
        
        # Ordenar por precio total (incluyendo entrega)
        comparison_data.sort(key=lambda x: x['price_info']['total_with_delivery'])
        
        return Response({
            'material_id': material_id,
            'quantity_requested': quantity,
            'suppliers_count': len(comparison_data),
            'best_price': comparison_data[0] if comparison_data else None,
            'all_prices': comparison_data
        })
    
    @action(detail=False, methods=['get'])
    def stats(self, request):
        """
        Estadísticas de proveedores
        GET /api/suppliers/stats/
        """
        total_suppliers = self.get_queryset().count()
        
        by_type = {}
        for supplier_type, display_name in Supplier.SUPPLIER_TYPES:
            count = self.get_queryset().filter(supplier_type=supplier_type).count()
            by_type[supplier_type] = {
                'count': count,
                'label': display_name
            }
        
        by_city = list(
            self.get_queryset()
            .values('city')
            .annotate(count=models.Count('id'))
            .order_by('-count')[:10]
        )
        
        avg_rating = self.get_queryset().aggregate(
            avg_rating=Avg('rating')
        )['avg_rating'] or 0
        
        return Response({
            'total_suppliers': total_suppliers,
            'by_type': by_type,
            'by_city': by_city,
            'average_rating': round(avg_rating, 2),
            'preferred_count': self.get_queryset().filter(is_preferred=True).count(),
            'with_delivery': self.get_queryset().filter(offers_delivery=True).count()
        })

class SupplierPriceViewSet(viewsets.ModelViewSet):
    """
    ViewSet para precios de proveedores
    """
    queryset = SupplierPrice.objects.all()
    serializer_class = SupplierPriceSerializer
    filter_backends = [DjangoFilterBackend, OrderingFilter]
    filterset_fields = ['supplier', 'material', 'is_current', 'currency']
    ordering_fields = ['price', 'created_at', 'valid_from']
    ordering = ['-created_at']
    
    def get_queryset(self):
        """Personalizar queryset según filtros"""
        queryset = SupplierPrice.objects.all()
        
        # Solo precios actuales por defecto
        current_only = self.request.query_params.get('current_only', 'true')
        if current_only.lower() == 'true':
            queryset = queryset.filter(is_current=True)
        
        # Filtrar por proveedor activo
        active_suppliers_only = self.request.query_params.get('active_suppliers_only', 'true')
        if active_suppliers_only.lower() == 'true':
            queryset = queryset.filter(supplier__is_active=True)
        
        return queryset