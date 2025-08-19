from django.db import models
from django.core.validators import MinValueValidator
from decimal import Decimal
from apps.projects.models import Project
from apps.materials.models import Material
from apps.suppliers.models import Supplier

class BudgetItem(models.Model):
    """
    Elementos del presupuesto inicial del proyecto
    """
    ITEM_CATEGORIES = [
        ('construction', 'Construcción'),
        ('lighting', 'Iluminación'),
        ('electrical', 'Eléctrico'),
        ('labor', 'Mano de Obra'),
        ('others', 'Otros'),
    ]

    # Relaciones
    project = models.ForeignKey(
        Project,
        on_delete=models.CASCADE,
        related_name='budget_items',
        verbose_name="Proyecto"
    )
    
    material = models.ForeignKey(
        Material,
        on_delete=models.SET_NULL,
        null=True, blank=True,
        verbose_name="Material",
        help_text="Material asociado (opcional si es servicio)"
    )
    
    supplier = models.ForeignKey(
        Supplier,
        on_delete=models.SET_NULL,
        null=True, blank=True,
        verbose_name="Proveedor Seleccionado"
    )

    # Información del item
    description = models.CharField(
        max_length=300,
        verbose_name="Descripción del Item",
        help_text="Descripción detallada del material o servicio"
    )
    
    category = models.CharField(
        max_length=20,
        choices=ITEM_CATEGORIES,
        verbose_name="Categoría"
    )
    
    spaces = models.CharField(
        max_length=200,
        blank=True,
        verbose_name="Espacios",
        help_text="Espacios o áreas donde se aplicará"
    )

    # Cantidades y precios
    quantity = models.DecimalField(
        max_digits=10,
        decimal_places=3,
        validators=[MinValueValidator(Decimal('0.001'))],
        verbose_name="Cantidad"
    )
    
    unit = models.CharField(
        max_length=20,
        verbose_name="Unidad",
        help_text="Unidad de medida"
    )
    
    unit_price = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        validators=[MinValueValidator(Decimal('0.01'))],
        verbose_name="Precio Unitario"
    )
    
    total_price = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        verbose_name="Precio Total",
        help_text="Se calcula automáticamente: cantidad × precio unitario"
    )

    # Metadatos
    notes = models.TextField(
        blank=True,
        verbose_name="Notas",
        help_text="Observaciones adicionales"
    )
    
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    # Control de origen
    created_from_calculation = models.BooleanField(
        default=False,
        verbose_name="Creado desde Calculadora",
        help_text="Indica si este item fue creado automáticamente desde una calculadora"
    )

    class Meta:
        verbose_name = "Item de Presupuesto"
        verbose_name_plural = "Items de Presupuesto"
        ordering = ['category', 'description']
        db_table = 'budget_items'

    def __str__(self):
        return f"{self.project.name} - {self.description}"

    def save(self, *args, **kwargs):
        # Calcular precio total automáticamente
        self.total_price = self.quantity * self.unit_price
        super().save(*args, **kwargs)

    def copy_to_expense(self):
        """
        Copia este item del presupuesto a gastos reales
        """
        expense = RealExpense(
            project=self.project,
            budget_item=self,
            material=self.material,
            supplier=self.supplier,
            description=self.description,
            category=self.category,
            spaces=self.spaces,
            quantity=self.quantity,
            unit=self.unit,
            unit_price=self.unit_price,
            # total_price se calculará automáticamente
            notes=f"Copiado de presupuesto: {self.description}"
        )
        return expense  # No se guarda automáticamente


class RealExpense(models.Model):
    """
    Gastos reales del proyecto (Tabla 2 del sistema dual)
    """
    ITEM_CATEGORIES = [
        ('construction', 'Construcción'),
        ('lighting', 'Iluminación'),
        ('electrical', 'Eléctrico'),
        ('labor', 'Mano de Obra'),
        ('others', 'Otros'),
    ]

    # Relaciones
    project = models.ForeignKey(
        Project,
        on_delete=models.CASCADE,
        related_name='real_expenses',
        verbose_name="Proyecto"
    )
    
    budget_item = models.ForeignKey(
        BudgetItem,
        on_delete=models.SET_NULL,
        null=True, blank=True,
        related_name='related_expenses',
        verbose_name="Item de Presupuesto Relacionado",
        help_text="Item del presupuesto del cual proviene este gasto"
    )
    
    material = models.ForeignKey(
        Material,
        on_delete=models.SET_NULL,
        null=True, blank=True,
        verbose_name="Material"
    )
    
    supplier = models.ForeignKey(
        Supplier,
        on_delete=models.SET_NULL,
        null=True, blank=True,
        verbose_name="Proveedor"
    )

    # Información del gasto
    description = models.CharField(
        max_length=300,
        verbose_name="Descripción del Gasto"
    )
    
    category = models.CharField(
        max_length=20,
        choices=ITEM_CATEGORIES,
        verbose_name="Categoría"
    )
    
    spaces = models.CharField(
        max_length=200,
        blank=True,
        verbose_name="Espacios"
    )

    # Cantidades y precios
    quantity = models.DecimalField(
        max_digits=10,
        decimal_places=3,
        validators=[MinValueValidator(Decimal('0.001'))],
        verbose_name="Cantidad"
    )
    
    unit = models.CharField(
        max_length=20,
        verbose_name="Unidad"
    )
    
    unit_price = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        validators=[MinValueValidator(Decimal('0.01'))],
        verbose_name="Precio Unitario"
    )
    
    # Descuentos específicos para gastos reales
    discount_percentage = models.DecimalField(
        max_digits=5,
        decimal_places=2,
        default=Decimal('0.00'),
        validators=[MinValueValidator(Decimal('0.00'))],
        verbose_name="Descuento (%)"
    )
    
    discount_amount = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        default=Decimal('0.00'),
        validators=[MinValueValidator(Decimal('0.00'))],
        verbose_name="Descuento (Monto Fijo)"
    )
    
    total_price = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        verbose_name="Precio Total",
        help_text="Precio final después de descuentos"
    )

    # Información de compra
    purchase_date = models.DateField(
        verbose_name="Fecha de Compra"
    )
    
    invoice_number = models.CharField(
        max_length=50,
        blank=True,
        verbose_name="Número de Factura"
    )
    
    payment_method = models.CharField(
        max_length=50,
        blank=True,
        verbose_name="Método de Pago",
        help_text="Efectivo, tarjeta, transferencia, etc."
    )

    # Metadatos
    notes = models.TextField(
        blank=True,
        verbose_name="Notas"
    )
    
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = "Gasto Real"
        verbose_name_plural = "Gastos Reales"
        ordering = ['-purchase_date', 'category']
        db_table = 'real_expenses'

    def __str__(self):
        return f"{self.project.name} - {self.description} ({self.purchase_date})"

    def save(self, *args, **kwargs):
        # Calcular precio total con descuentos
        base_total = self.quantity * self.unit_price
        
        # Aplicar descuento porcentual
        if self.discount_percentage > 0:
            discount = base_total * (self.discount_percentage / 100)
            base_total -= discount
        
        # Aplicar descuento por monto fijo
        if self.discount_amount > 0:
            base_total -= self.discount_amount
        
        # Asegurar que no sea negativo
        self.total_price = max(base_total, Decimal('0.00'))
        
        super().save(*args, **kwargs)

    @property
    def total_discount(self):
        """Calcula el descuento total aplicado"""
        base_total = self.quantity * self.unit_price
        return base_total - self.total_price

    @property
    def savings_vs_budget(self):
        """
        Calcula el ahorro/sobrecosto vs presupuesto inicial
        Retorna valor positivo si se ahorró, negativo si se gastó más
        """
        if self.budget_item:
            budgeted_total = self.budget_item.total_price
            return budgeted_total - self.total_price
        return None


class ProjectFinancialSummary(models.Model):
    """
    Resumen financiero del proyecto (se actualiza automáticamente)
    """
    project = models.OneToOneField(
        Project,
        on_delete=models.CASCADE,
        related_name='financial_summary',
        verbose_name="Proyecto"
    )

    # Totales de presupuesto
    total_budget = models.DecimalField(
        max_digits=15,
        decimal_places=2,
        default=Decimal('0.00'),
        verbose_name="Total Presupuestado"
    )
    
    budget_construction = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        default=Decimal('0.00'),
        verbose_name="Presupuesto Construcción"
    )
    
    budget_lighting = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        default=Decimal('0.00'),
        verbose_name="Presupuesto Iluminación"
    )
    
    budget_others = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        default=Decimal('0.00'),
        verbose_name="Presupuesto Otros"
    )

    # Totales de gastos reales
    total_expenses = models.DecimalField(
        max_digits=15,
        decimal_places=2,
        default=Decimal('0.00'),
        verbose_name="Total Gastado"
    )
    
    expenses_construction = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        default=Decimal('0.00'),
        verbose_name="Gastos Construcción"
    )
    
    expenses_lighting = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        default=Decimal('0.00'),
        verbose_name="Gastos Iluminación"
    )
    
    expenses_others = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        default=Decimal('0.00'),
        verbose_name="Gastos Otros"
    )

    # Balance
    balance = models.DecimalField(
        max_digits=15,
        decimal_places=2,
        default=Decimal('0.00'),
        verbose_name="Balance",
        help_text="Presupuesto - Gastos (positivo = ahorro, negativo = sobrecosto)"
    )

    # Metadatos
    last_updated = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = "Resumen Financiero"
        verbose_name_plural = "Resúmenes Financieros"
        db_table = 'project_financial_summaries'

    def __str__(self):
        return f"Resumen Financiero - {self.project.name}"

    @property
    def budget_utilization_percentage(self):
        """Porcentaje de presupuesto utilizado"""
        if self.total_budget > 0:
            return (self.total_expenses / self.total_budget) * 100
        return 0

    @property
    def is_over_budget(self):
        """True si se excedió el presupuesto"""
        return self.balance < 0

    @property
    def remaining_budget(self):
        """Presupuesto restante disponible"""
        return max(self.balance, Decimal('0.00'))

    def update_summary(self):
        """
        Actualiza el resumen basado en los items actuales del proyecto
        """
        # Calcular totales de presupuesto por categoría
        budget_items = self.project.budget_items.all()
        
        self.budget_construction = sum(
            item.total_price for item in budget_items.filter(category='construction')
        ) or Decimal('0.00')
        
        self.budget_lighting = sum(
            item.total_price for item in budget_items.filter(category='lighting')
        ) or Decimal('0.00')
        
        self.budget_others = sum(
            item.total_price for item in budget_items.exclude(
                category__in=['construction', 'lighting']
            )
        ) or Decimal('0.00')
        
        self.total_budget = self.budget_construction + self.budget_lighting + self.budget_others

        # Calcular totales de gastos reales por categoría
        expenses = self.project.real_expenses.all()
        
        self.expenses_construction = sum(
            expense.total_price for expense in expenses.filter(category='construction')
        ) or Decimal('0.00')
        
        self.expenses_lighting = sum(
            expense.total_price for expense in expenses.filter(category='lighting')
        ) or Decimal('0.00')
        
        self.expenses_others = sum(
            expense.total_price for expense in expenses.exclude(
                category__in=['construction', 'lighting']
            )
        ) or Decimal('0.00')
        
        self.total_expenses = self.expenses_construction + self.expenses_lighting + self.expenses_others

        # Calcular balance
        self.balance = self.total_budget - self.total_expenses

        self.save()