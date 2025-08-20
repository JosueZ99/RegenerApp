from rest_framework import serializers
from .models import BudgetItem, RealExpense, ProjectFinancialSummary
from apps.materials.serializers import MaterialListSerializer
from apps.suppliers.serializers import SupplierForBudgetSerializer

class BudgetItemListSerializer(serializers.ModelSerializer):
    """
    Serializer para lista de items del presupuesto inicial
    """
    material_name = serializers.CharField(source='material.name', read_only=True)
    supplier_name = serializers.CharField(source='supplier.name', read_only=True)
    category_display = serializers.CharField(source='get_category_display', read_only=True)
    
    class Meta:
        model = BudgetItem
        fields = [
            'id', 'description', 'category', 'category_display',
            'spaces', 'quantity', 'unit', 'unit_price', 'total_price',
            'material', 'material_name', 'supplier', 'supplier_name',
            'notes', 'created_from_calculation', 'created_at', 'updated_at'
        ]

class BudgetItemDetailSerializer(serializers.ModelSerializer):
    """
    Serializer detallado para items del presupuesto
    """
    material = MaterialListSerializer(read_only=True)
    supplier = SupplierForBudgetSerializer(read_only=True)
    category_display = serializers.CharField(source='get_category_display', read_only=True)
    related_expenses_count = serializers.SerializerMethodField()
    
    class Meta:
        model = BudgetItem
        fields = [
            'id', 'project', 'description', 'category', 'category_display',
            'spaces', 'quantity', 'unit', 'unit_price', 'total_price',
            'material', 'supplier', 'notes', 'created_from_calculation',
            'created_at', 'updated_at', 'related_expenses_count'
        ]
    
    def get_related_expenses_count(self, obj):
        """Cantidad de gastos relacionados con este item"""
        return obj.related_expenses.count()

class BudgetItemCreateUpdateSerializer(serializers.ModelSerializer):
    """
    Serializer para crear/actualizar items del presupuesto
    """
    class Meta:
        model = BudgetItem
        fields = [
            'project', 'description', 'category', 'spaces',
            'quantity', 'unit', 'unit_price', 'material',
            'supplier', 'notes'
        ]
    
    def validate(self, data):
        """Validaciones generales"""
        if data['quantity'] <= 0:
            raise serializers.ValidationError({"quantity": "La cantidad debe ser mayor a 0"})
        if data['unit_price'] <= 0:
            raise serializers.ValidationError({"unit_price": "El precio unitario debe ser mayor a 0"})
        return data

class RealExpenseListSerializer(serializers.ModelSerializer):
    """
    Serializer para lista de gastos reales
    """
    material_name = serializers.CharField(source='material.name', read_only=True)
    supplier_name = serializers.CharField(source='supplier.name', read_only=True)
    budget_item_description = serializers.CharField(source='budget_item.description', read_only=True)
    category_display = serializers.CharField(source='get_category_display', read_only=True)
    total_discount = serializers.DecimalField(max_digits=10, decimal_places=2, read_only=True)
    
    class Meta:
        model = RealExpense
        fields = [
            'id', 'description', 'category', 'category_display',
            'spaces', 'quantity', 'unit', 'unit_price',
            'discount_percentage', 'discount_amount', 'total_price',
            'total_discount', 'purchase_date', 'invoice_number',
            'payment_method', 'material', 'material_name',
            'supplier', 'supplier_name', 'budget_item',
            'budget_item_description', 'notes', 'created_at'
        ]

class RealExpenseDetailSerializer(serializers.ModelSerializer):
    """
    Serializer detallado para gastos reales
    """
    material = MaterialListSerializer(read_only=True)
    supplier = SupplierForBudgetSerializer(read_only=True)
    budget_item = BudgetItemListSerializer(read_only=True)
    category_display = serializers.CharField(source='get_category_display', read_only=True)
    total_discount = serializers.DecimalField(max_digits=10, decimal_places=2, read_only=True)
    savings_vs_budget = serializers.DecimalField(max_digits=10, decimal_places=2, read_only=True)
    
    class Meta:
        model = RealExpense
        fields = [
            'id', 'project', 'description', 'category', 'category_display',
            'spaces', 'quantity', 'unit', 'unit_price',
            'discount_percentage', 'discount_amount', 'total_price',
            'total_discount', 'savings_vs_budget', 'purchase_date',
            'invoice_number', 'payment_method', 'material', 'supplier',
            'budget_item', 'notes', 'created_at', 'updated_at'
        ]

class RealExpenseCreateUpdateSerializer(serializers.ModelSerializer):
    """
    Serializer para crear/actualizar gastos reales
    """
    class Meta:
        model = RealExpense
        fields = [
            'project', 'budget_item', 'description', 'category', 'spaces',
            'quantity', 'unit', 'unit_price', 'discount_percentage',
            'discount_amount', 'purchase_date', 'invoice_number',
            'payment_method', 'material', 'supplier', 'notes'
        ]
    
    def validate(self, data):
        """Validaciones generales"""
        if data['quantity'] <= 0:
            raise serializers.ValidationError({"quantity": "La cantidad debe ser mayor a 0"})
        if data['unit_price'] <= 0:
            raise serializers.ValidationError({"unit_price": "El precio unitario debe ser mayor a 0"})
        
        # Validar que los descuentos no sean negativos
        if data.get('discount_percentage', 0) < 0:
            raise serializers.ValidationError({"discount_percentage": "El descuento no puede ser negativo"})
        if data.get('discount_amount', 0) < 0:
            raise serializers.ValidationError({"discount_amount": "El descuento no puede ser negativo"})
        
        return data

class ProjectFinancialSummarySerializer(serializers.ModelSerializer):
    """
    Serializer para resumen financiero del proyecto
    """
    project_name = serializers.CharField(source='project.name', read_only=True)
    budget_utilization_percentage = serializers.DecimalField(
        max_digits=5, decimal_places=2, read_only=True
    )
    is_over_budget = serializers.BooleanField(read_only=True)
    remaining_budget = serializers.DecimalField(
        max_digits=15, decimal_places=2, read_only=True
    )
    
    class Meta:
        model = ProjectFinancialSummary
        fields = [
            'id', 'project', 'project_name', 'total_budget',
            'budget_construction', 'budget_lighting', 'budget_others',
            'total_expenses', 'expenses_construction', 'expenses_lighting',
            'expenses_others', 'balance', 'budget_utilization_percentage',
            'is_over_budget', 'remaining_budget', 'last_updated'
        ]

class BudgetSummarySerializer(serializers.Serializer):
    """
    Serializer para resumen rápido de presupuesto por categorías
    """
    category = serializers.CharField()
    budgeted_amount = serializers.DecimalField(max_digits=12, decimal_places=2)
    spent_amount = serializers.DecimalField(max_digits=12, decimal_places=2)
    remaining_amount = serializers.DecimalField(max_digits=12, decimal_places=2)
    items_count = serializers.IntegerField()
    expenses_count = serializers.IntegerField()

class CopyBudgetToExpenseSerializer(serializers.Serializer):
    """
    Serializer para copiar items del presupuesto a gastos reales
    """
    budget_item_ids = serializers.ListField(
        child=serializers.IntegerField(),
        min_length=1
    )
    purchase_date = serializers.DateField()
    default_supplier = serializers.IntegerField(required=False)
    
    def validate_budget_item_ids(self, value):
        """Validar que todos los items existen"""
        existing_ids = BudgetItem.objects.filter(id__in=value).values_list('id', flat=True)
        missing_ids = set(value) - set(existing_ids)
        if missing_ids:
            raise serializers.ValidationError(f"Items no encontrados: {list(missing_ids)}")
        return value