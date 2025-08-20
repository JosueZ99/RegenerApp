from rest_framework import serializers
from .models import (
    CalculationType, Calculation, PaintCalculation, 
    GypsumCalculation, LEDStripCalculation, CableCalculation
)
from apps.materials.serializers import MaterialListSerializer
from decimal import Decimal

class CalculationTypeSerializer(serializers.ModelSerializer):
    """
    Serializer para tipos de calculadoras
    """
    category_display = serializers.CharField(source='get_category_display', read_only=True)
    
    class Meta:
        model = CalculationType
        fields = [
            'id', 'name', 'code', 'category', 'category_display',
            'description', 'is_active', 'input_parameters'
        ]

class CalculationListSerializer(serializers.ModelSerializer):
    """
    Serializer para lista de cálculos
    """
    calculation_type_name = serializers.CharField(source='calculation_type.name', read_only=True)
    material_name = serializers.CharField(source='material.name', read_only=True)
    project_name = serializers.CharField(source='project.name', read_only=True)
    
    class Meta:
        model = Calculation
        fields = [
            'id', 'project', 'project_name', 'calculation_type',
            'calculation_type_name', 'material', 'material_name',
            'calculated_quantity', 'unit', 'estimated_cost',
            'added_to_budget', 'created_at'
        ]

class CalculationDetailSerializer(serializers.ModelSerializer):
    """
    Serializer detallado para cálculos
    """
    calculation_type = CalculationTypeSerializer(read_only=True)
    material = MaterialListSerializer(read_only=True)
    project_name = serializers.CharField(source='project.name', read_only=True)
    budget_item_created_id = serializers.IntegerField(source='budget_item_created.id', read_only=True)
    
    class Meta:
        model = Calculation
        fields = [
            'id', 'project', 'project_name', 'calculation_type',
            'material', 'input_data', 'calculated_quantity', 'unit',
            'estimated_cost', 'detailed_results', 'added_to_budget',
            'budget_item_created', 'budget_item_created_id', 'created_at'
        ]

# Serializers específicos para cada tipo de cálculo

class PaintCalculationSerializer(serializers.ModelSerializer):
    """
    Serializer para cálculos de pintura
    """
    calculation = CalculationDetailSerializer(read_only=True)
    
    class Meta:
        model = PaintCalculation
        fields = [
            'calculation', 'area_to_paint', 'number_of_coats',
            'paint_type', 'coverage_per_liter', 'total_liters_needed',
            'gallons_needed'
        ]

class GypsumCalculationSerializer(serializers.ModelSerializer):
    """
    Serializer para cálculos de gypsum
    """
    calculation = CalculationDetailSerializer(read_only=True)
    
    class Meta:
        model = GypsumCalculation
        fields = [
            'calculation', 'area_to_cover', 'thickness',
            'gypsum_type', 'sheets_needed', 'linear_meters_profile'
        ]

class LEDStripCalculationSerializer(serializers.ModelSerializer):
    """
    Serializer para cálculos de cintas LED
    """
    calculation = CalculationDetailSerializer(read_only=True)
    
    class Meta:
        model = LEDStripCalculation
        fields = [
            'calculation', 'total_length', 'power_per_meter',
            'voltage', 'strip_type', 'total_power', 'drivers_needed',
            'meters_per_roll', 'rolls_needed'
        ]

class CableCalculationSerializer(serializers.ModelSerializer):
    """
    Serializer para cálculos de cables
    """
    calculation = CalculationDetailSerializer(read_only=True)
    
    class Meta:
        model = CableCalculation
        fields = [
            'calculation', 'total_length', 'wire_gauge',
            'cable_type', 'installation_type', 'rolls_needed',
            'meters_per_roll'
        ]

class EmpasteCalculationRequestSerializer(serializers.Serializer):
    """
    Serializer para request de cálculo de empaste
    """
    project_id = serializers.IntegerField()
    area_to_cover = serializers.DecimalField(max_digits=10, decimal_places=2, min_value=0.01)
    empaste_type = serializers.CharField(max_length=50)
    number_of_layers = serializers.IntegerField(min_value=1, max_value=10)
    material_id = serializers.IntegerField(required=False)
    
    def validate_project_id(self, value):
        """Validar que el proyecto existe"""
        from apps.projects.models import Project
        try:
            Project.objects.get(id=value)
        except Project.DoesNotExist:
            raise serializers.ValidationError("El proyecto no existe")
        return value
    
# Serializers para entrada de datos (requests)

class PaintCalculationRequestSerializer(serializers.Serializer):
    """
    Serializer para request de cálculo de pintura
    """
    project_id = serializers.IntegerField()
    area_to_paint = serializers.DecimalField(max_digits=10, decimal_places=2, min_value=0.01)
    number_of_coats = serializers.IntegerField(min_value=1, max_value=10)
    paint_type = serializers.CharField(max_length=50)
    coverage_per_liter = serializers.DecimalField(max_digits=8, decimal_places=2, min_value=0.1)
    material_id = serializers.IntegerField(required=False)
    
    def validate_project_id(self, value):
        """Validar que el proyecto existe"""
        from apps.projects.models import Project
        try:
            Project.objects.get(id=value)
        except Project.DoesNotExist:
            raise serializers.ValidationError("El proyecto no existe")
        return value

class GypsumCalculationRequestSerializer(serializers.Serializer):
    """
    Serializer para request de cálculo de gypsum
    """
    project_id = serializers.IntegerField()
    area_to_cover = serializers.DecimalField(max_digits=10, decimal_places=2, min_value=0.01)
    thickness = serializers.DecimalField(max_digits=4, decimal_places=1, min_value=1.0)
    gypsum_type = serializers.CharField(max_length=50)
    material_id = serializers.IntegerField(required=False)

class LEDStripCalculationRequestSerializer(serializers.Serializer):
    """
    Serializer para request de cálculo de cintas LED - CORREGIDO
    """
    project_id = serializers.IntegerField()
    total_length = serializers.DecimalField(max_digits=10, decimal_places=2, min_value=0.01)
    power_per_meter = serializers.DecimalField(max_digits=6, decimal_places=2, min_value=0.1)
    voltage = serializers.CharField(max_length=10)
    strip_type = serializers.CharField(max_length=50)
    # CORREGIDO: Default como Decimal en lugar de float
    meters_per_roll = serializers.DecimalField(
        max_digits=6, 
        decimal_places=2, 
        default=Decimal('5.00')  # Era: default=5.0
    )
    material_id = serializers.IntegerField(required=False)
    
    def validate_project_id(self, value):
        """Validar que el proyecto existe"""
        from apps.projects.models import Project
        try:
            Project.objects.get(id=value)
        except Project.DoesNotExist:
            raise serializers.ValidationError("El proyecto no existe")
        return value

class CableCalculationRequestSerializer(serializers.Serializer):
    """
    Serializer para request de cálculo de cables - CORREGIDO
    """
    project_id = serializers.IntegerField()
    total_length = serializers.DecimalField(max_digits=10, decimal_places=2, min_value=0.01)
    wire_gauge = serializers.CharField(max_length=20)
    cable_type = serializers.CharField(max_length=50)
    installation_type = serializers.CharField(max_length=50)
    # CORREGIDO: Default como Decimal en lugar de float
    meters_per_roll = serializers.DecimalField(
        max_digits=6, 
        decimal_places=2, 
        default=Decimal('100.00')  # Era: default=100.0
    )
    material_id = serializers.IntegerField(required=False)
    
    def validate_project_id(self, value):
        """Validar que el proyecto existe"""
        from apps.projects.models import Project
        try:
            Project.objects.get(id=value)
        except Project.DoesNotExist:
            raise serializers.ValidationError("El proyecto no existe")
        return value

class AddCalculationToBudgetSerializer(serializers.Serializer):
    """
    Serializer para agregar cálculo al presupuesto
    El calculation_id viene en la URL, no en el body
    """
    supplier_id = serializers.IntegerField(required=False)
    unit_price_override = serializers.DecimalField(
        max_digits=10, decimal_places=2, required=False
    )
    spaces = serializers.CharField(max_length=200, required=False)
    notes = serializers.CharField(max_length=500, required=False)
    
    def validate_supplier_id(self, value):
        """Validar que el proveedor existe si se proporciona"""
        if value:
            from apps.suppliers.models import Supplier
            try:
                Supplier.objects.get(id=value)
            except Supplier.DoesNotExist:
                raise serializers.ValidationError("El proveedor no existe")
        return value
    
    def validate_supplier_id(self, value):
        """Validar que el proveedor existe si se proporciona"""
        if value:
            from apps.suppliers.models import Supplier
            try:
                Supplier.objects.get(id=value)
            except Supplier.DoesNotExist:
                raise serializers.ValidationError("El proveedor no existe")
        return value

class CalculationResultSerializer(serializers.Serializer):
    """
    Serializer para respuesta unificada de cálculos
    """
    calculation_id = serializers.IntegerField()
    calculation_type = serializers.CharField()
    calculated_quantity = serializers.DecimalField(max_digits=10, decimal_places=3)
    unit = serializers.CharField()
    estimated_cost = serializers.DecimalField(max_digits=10, decimal_places=2)
    detailed_results = serializers.DictField()
    specific_details = serializers.DictField()
    material_suggestions = serializers.ListField(child=serializers.DictField())