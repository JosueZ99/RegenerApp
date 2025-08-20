from rest_framework import serializers
from .models import Supplier, SupplierPrice
from apps.materials.serializers import MaterialCategorySerializer

class SupplierListSerializer(serializers.ModelSerializer):
    """
    Serializer básico para lista de proveedores
    """
    supplier_type_display = serializers.CharField(source='get_supplier_type_display', read_only=True)
    display_name = serializers.CharField(read_only=True)
    rating_stars = serializers.CharField(read_only=True)
    categories_list = serializers.SerializerMethodField()
    
    class Meta:
        model = Supplier
        fields = [
            'id', 'name', 'display_name', 'commercial_name',
            'supplier_type', 'supplier_type_display', 'phone',
            'city', 'zone', 'rating', 'rating_stars',
            'is_active', 'is_preferred', 'categories_list'
        ]
    
    def get_categories_list(self, obj):
        """Lista de nombres de categorías que maneja"""
        return obj.get_categories_list()

class SupplierDetailSerializer(serializers.ModelSerializer):
    """
    Serializer completo para detalles del proveedor
    """
    supplier_type_display = serializers.CharField(source='get_supplier_type_display', read_only=True)
    display_name = serializers.CharField(read_only=True)
    rating_stars = serializers.CharField(read_only=True)
    categories = MaterialCategorySerializer(many=True, read_only=True)
    total_materials = serializers.SerializerMethodField()
    
    class Meta:
        model = Supplier
        fields = [
            'id', 'name', 'display_name', 'commercial_name',
            'supplier_type', 'supplier_type_display', 'contact_person',
            'phone', 'phone_secondary', 'email', 'website',
            'address', 'city', 'zone', 'ruc', 'categories',
            'rating', 'rating_stars', 'payment_terms', 'delivery_time',
            'minimum_order', 'offers_delivery', 'delivery_cost',
            'notes', 'strengths', 'weaknesses', 'is_active',
            'is_preferred', 'created_at', 'updated_at',
            'last_contact_date', 'total_materials'
        ]
    
    def get_total_materials(self, obj):
        """Cantidad de materiales con precios de este proveedor"""
        return obj.prices.count()

class SupplierPriceSerializer(serializers.ModelSerializer):
    """
    Serializer para precios de proveedores
    """
    material_name = serializers.CharField(source='material.name', read_only=True)
    material_code = serializers.CharField(source='material.code', read_only=True)
    supplier_name = serializers.CharField(source='supplier.name', read_only=True)
    final_price = serializers.DecimalField(max_digits=10, decimal_places=2, read_only=True)
    
    class Meta:
        model = SupplierPrice
        fields = [
            'id', 'supplier', 'supplier_name', 'material', 
            'material_name', 'material_code', 'price', 'currency',
            'discount_percentage', 'bulk_discount_threshold',
            'bulk_discount_percentage', 'final_price', 'valid_from',
            'valid_until', 'is_current', 'created_at', 'updated_at'
        ]

class SupplierForBudgetSerializer(serializers.ModelSerializer):
    """
    Serializer simplificado para selección en presupuestos
    """
    display_name = serializers.CharField(read_only=True)
    rating_stars = serializers.CharField(read_only=True)
    
    class Meta:
        model = Supplier
        fields = [
            'id', 'name', 'display_name', 'supplier_type',
            'phone', 'city', 'rating', 'rating_stars',
            'delivery_time', 'offers_delivery', 'is_preferred'
        ]

class SupplierCreateUpdateSerializer(serializers.ModelSerializer):
    """
    Serializer para crear/actualizar proveedores
    """
    class Meta:
        model = Supplier
        fields = [
            'name', 'commercial_name', 'supplier_type', 'contact_person',
            'phone', 'phone_secondary', 'email', 'website',
            'address', 'city', 'zone', 'ruc', 'categories',
            'rating', 'payment_terms', 'delivery_time', 'minimum_order',
            'offers_delivery', 'delivery_cost', 'notes', 'strengths',
            'weaknesses', 'is_active', 'is_preferred', 'last_contact_date'
        ]
    
    def validate_phone(self, value):
        """Validar formato de teléfono"""
        if not value.replace('+', '').replace('-', '').replace(' ', '').isdigit():
            raise serializers.ValidationError("Formato de teléfono inválido")
        return value

class SupplierSearchSerializer(serializers.Serializer):
    """
    Serializer para filtros de búsqueda de proveedores
    """
    supplier_type = serializers.ChoiceField(
        choices=['materials', 'lighting', 'electrical', 'services', 'mixed'],
        required=False
    )
    city = serializers.CharField(max_length=100, required=False)
    zone = serializers.CharField(max_length=100, required=False)
    rating_min = serializers.IntegerField(min_value=1, max_value=5, required=False)
    is_preferred = serializers.BooleanField(required=False)
    offers_delivery = serializers.BooleanField(required=False)
    search = serializers.CharField(max_length=100, required=False)

class SupplierMaterialPriceSerializer(serializers.Serializer):
    """
    Serializer para obtener precios de un material por proveedores
    """
    material_id = serializers.IntegerField()
    quantity = serializers.DecimalField(max_digits=10, decimal_places=3, required=False)
    
    def validate_material_id(self, value):
        """Validar que el material existe"""
        from apps.materials.models import Material
        try:
            Material.objects.get(id=value)
        except Material.DoesNotExist:
            raise serializers.ValidationError("El material no existe")
        return value