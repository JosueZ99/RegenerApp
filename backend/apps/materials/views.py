from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.filters import SearchFilter, OrderingFilter
from django_filters.rest_framework import DjangoFilterBackend
from django.db.models import Q
from .models import MaterialCategory, Material, MaterialSpecification
from .serializers import (
    MaterialCategorySerializer, MaterialListSerializer, 
    MaterialDetailSerializer, MaterialForCalculatorSerializer,
    MaterialCreateUpdateSerializer, MaterialSearchSerializer, MaterialSpecificationSerializer
)

class MaterialCategoryViewSet(viewsets.ReadOnlyModelViewSet):
    """
    ViewSet de solo lectura para categorías de materiales
    """
    queryset = MaterialCategory.objects.filter(is_active=True)
    serializer_class = MaterialCategorySerializer
    filter_backends = [SearchFilter, OrderingFilter]
    search_fields = ['name', 'description']
    ordering = ['category_type', 'name']
    filterset_fields = ['category_type']

class MaterialViewSet(viewsets.ModelViewSet):
    """
    ViewSet para gestión de materiales
    """
    queryset = Material.objects.filter(is_active=True)
    filter_backends = [SearchFilter, OrderingFilter, DjangoFilterBackend]
    search_fields = ['name', 'code', 'description']
    ordering_fields = ['name', 'code', 'reference_price', 'created_at']
    ordering = ['category__category_type', 'name']
    filterset_fields = ['category', 'unit']
    
    def get_serializer_class(self):
        """Retorna serializer según la acción"""
        if self.action == 'list':
            return MaterialListSerializer
        elif self.action in ['create', 'update', 'partial_update']:
            return MaterialCreateUpdateSerializer
        elif self.action == 'for_calculator':
            return MaterialForCalculatorSerializer
        return MaterialDetailSerializer
    
    def get_queryset(self):
        """Personalizar queryset según filtros"""
        queryset = Material.objects.filter(is_active=True)
        
        # Filtro por tipo de categoría
        category_type = self.request.query_params.get('category_type')
        if category_type:
            queryset = queryset.filter(category__category_type=category_type)
        
        # Filtro por materiales con precio
        has_price = self.request.query_params.get('has_price')
        if has_price and has_price.lower() == 'true':
            queryset = queryset.filter(reference_price__isnull=False)
        
        return queryset
    
    @action(detail=False, methods=['get'])
    def for_calculator(self, request):
        """
        Materiales específicos para calculadoras
        GET /api/materials/for_calculator/?category_type=construction
        """
        category_type = request.query_params.get('category_type')
        
        if not category_type:
            return Response(
                {'error': 'Se requiere el parámetro category_type'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        materials = self.get_queryset().filter(
            category__category_type=category_type
        )
        
        serializer = MaterialForCalculatorSerializer(materials, many=True)
        return Response(serializer.data)
    
    @action(detail=False, methods=['get'])
    def by_category(self, request):
        """
        Materiales agrupados por categoría
        GET /api/materials/by_category/
        """
        categories = MaterialCategory.objects.filter(is_active=True)
        result = {}
        
        for category in categories:
            materials = self.get_queryset().filter(category=category)
            serializer = MaterialListSerializer(materials, many=True)
            result[category.category_type] = {
                'category_info': MaterialCategorySerializer(category).data,
                'materials': serializer.data
            }
        
        return Response(result)
    
    @action(detail=True, methods=['get'])
    def suppliers(self, request, pk=None):
        """
        Proveedores que manejan este material
        GET /api/materials/{id}/suppliers/
        """
        material = self.get_object()
        supplier_prices = material.supplier_prices.filter(
            is_current=True,
            supplier__is_active=True
        ).select_related('supplier')
        
        from apps.suppliers.serializers import SupplierPriceSerializer
        serializer = SupplierPriceSerializer(supplier_prices, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['post'])
    def calculate_quantity(self, request, pk=None):
        """
        Calcular cantidad necesaria de material
        POST /api/materials/{id}/calculate_quantity/
        Body: {"area_or_length": 100, "layers": 2}
        """
        material = self.get_object()
        
        area_or_length = request.data.get('area_or_length')
        layers = request.data.get('layers', 1)
        
        if not area_or_length:
            return Response(
                {'error': 'Se requiere area_or_length'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        try:
            area_or_length = float(area_or_length)
            layers = int(layers)
        except (ValueError, TypeError):
            return Response(
                {'error': 'Valores numéricos inválidos'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Verificar que el material tenga rendimiento configurado
        if not material.yield_per_unit:
            return Response(
                {'error': 'Material no tiene rendimiento por unidad configurado'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Calcular cantidad base necesaria
        base_quantity = (area_or_length * layers) / float(material.yield_per_unit)
        
        # Aplicar factor de desperdicio (usar 0.05 si es None)
        waste_factor = material.waste_factor or 0.05
        quantity_needed = base_quantity * (1 + float(waste_factor))
        quantity_needed = round(quantity_needed, 2)
        
        # Calcular costo estimado
        estimated_cost = None
        if material.reference_price:
            estimated_cost = float(quantity_needed) * float(material.reference_price)
        
        return Response({
            'material': MaterialDetailSerializer(material).data,
            'input': {
                'area_or_length': area_or_length,
                'layers': layers
            },
            'result': {
                'quantity_needed': quantity_needed,
                'unit': material.unit,
                'estimated_cost': estimated_cost,
                'waste_factor_applied': float(waste_factor)
            }
        })

class MaterialSpecificationViewSet(viewsets.ModelViewSet):
    """
    ViewSet para especificaciones de materiales
    """
    queryset = MaterialSpecification.objects.all()
    serializer_class = MaterialSpecificationSerializer
    filter_backends = [DjangoFilterBackend]
    filterset_fields = ['material', 'specification_type', 'is_default']
    
    def get_queryset(self):
        """Filtrar por material si se proporciona"""
        queryset = MaterialSpecification.objects.all()
        material_id = self.request.query_params.get('material_id')
        
        if material_id:
            queryset = queryset.filter(material_id=material_id)
        
        return queryset