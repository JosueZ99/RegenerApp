from rest_framework import serializers
from .models import MaterialCategory, Material, MaterialSpecification

class MaterialCategorySerializer(serializers.ModelSerializer):
    """
    Serializer para categorías de materiales
    """
    category_type_display = serializers.CharField(source='get_category_type_display', read_only=True)
    materials_count = serializers.SerializerMethodField()
    
    class Meta:
        model = MaterialCategory
        fields = [
            'id', 'name', 'category_type', 'category_type_display',
            'description', 'is_active', 'materials_count'
        ]
    
    def get_materials_count(self, obj):
        """Cantidad de materiales activos en esta categoría"""
        return obj.material_set.filter(is_active=True).count()

class MaterialSpecificationSerializer(serializers.ModelSerializer):
    """
    Serializer para especificaciones de materiales
    """
    class Meta:
        model = MaterialSpecification
        fields = [
            'id', 'specification_type', 'value', 'is_default'
        ]

class MaterialListSerializer(serializers.ModelSerializer):
    """
    Serializer básico para lista de materiales
    """
    category_name = serializers.CharField(source='category.name', read_only=True)
    category_type = serializers.CharField(source='category.category_type', read_only=True)
    unit_display = serializers.CharField(source='get_unit_display', read_only=True)
    
    class Meta:
        model = Material
        fields = [
            'id', 'name', 'code', 'category_name', 'category_type',
            'unit', 'unit_display', 'reference_price', 'is_active'
        ]

class MaterialDetailSerializer(serializers.ModelSerializer):
    """
    Serializer completo para detalles de materiales
    """
    category = MaterialCategorySerializer(read_only=True)
    specifications = MaterialSpecificationSerializer(many=True, read_only=True)
    unit_display = serializers.CharField(source='get_unit_display', read_only=True)
    category_type = serializers.CharField(source='category.category_type', read_only=True)
    price_with_waste = serializers.SerializerMethodField()
    
    class Meta:
        model = Material
        fields = [
            'id', 'name', 'code', 'category', 'category_type',
            'description', 'unit', 'unit_display', 'yield_per_unit',
            'waste_factor', 'reference_price', 'price_with_waste',
            'last_price_update', 'is_active', 'created_at', 'updated_at',
            # Campos específicos por tipo
            'coverage_per_liter', 'density', 'power_per_meter', 'wire_gauge',
            'specifications'
        ]
    
    def get_price_with_waste(self, obj):
        """Precio incluyendo factor de desperdicio"""
        return obj.get_price_with_waste()

class MaterialForCalculatorSerializer(serializers.ModelSerializer):
    """
    Serializer específico para materiales en calculadoras
    """
    category_type = serializers.CharField(source='category.category_type', read_only=True)
    unit_display = serializers.CharField(source='get_unit_display', read_only=True)
    
    class Meta:
        model = Material
        fields = [
            'id', 'name', 'code', 'category_type', 'unit', 'unit_display',
            'yield_per_unit', 'waste_factor', 'reference_price',
            # Campos específicos para cálculos
            'coverage_per_liter', 'power_per_meter', 'wire_gauge'
        ]
    
    def to_representation(self, instance):
        """
        Personalizar la representación para calculadoras
        """
        data = super().to_representation(instance)
        
        # Asegurar que todos los campos necesarios estén presentes
        data['has_price'] = instance.reference_price is not None
        data['price_display'] = f"${instance.reference_price:.2f}" if instance.reference_price else "Sin precio"
        
        return data

class MaterialCreateUpdateSerializer(serializers.ModelSerializer):
    """
    Serializer para crear/actualizar materiales
    """
    class Meta:
        model = Material
        fields = [
            'name', 'code', 'category', 'description', 'unit',
            'yield_per_unit', 'waste_factor', 'reference_price',
            'coverage_per_liter', 'density', 'power_per_meter', 
            'wire_gauge', 'is_active'
        ]
    
    def validate_code(self, value):
        """Validar que el código sea único"""
        if Material.objects.filter(code=value).exclude(id=self.instance.id if self.instance else None).exists():
            raise serializers.ValidationError("Ya existe un material con este código")
        return value

class MaterialSearchSerializer(serializers.Serializer):
    """
    Serializer para filtros de búsqueda de materiales
    """
    category_type = serializers.ChoiceField(
        choices=['construction', 'lighting', 'electrical'], 
        required=False
    )
    category_id = serializers.IntegerField(required=False)
    search = serializers.CharField(max_length=100, required=False)
    is_active = serializers.BooleanField(default=True)