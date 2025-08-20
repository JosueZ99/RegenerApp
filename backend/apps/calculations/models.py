from django.db import models
from django.core.validators import MinValueValidator
from decimal import Decimal
import json
from apps.projects.models import Project
from apps.materials.models import Material

class CalculationType(models.Model):
    """
    Tipos de calculadoras disponibles
    """
    CATEGORY_CHOICES = [
        ('construction', 'Construcción'),
        ('lighting', 'Iluminación'),
    ]
    
    name = models.CharField(
        max_length=100,
        verbose_name="Nombre de la Calculadora"
    )
    
    code = models.CharField(
        max_length=50,
        unique=True,
        verbose_name="Código",
        help_text="Código único para identificar el tipo de cálculo"
    )
    
    category = models.CharField(
        max_length=20,
        choices=CATEGORY_CHOICES,
        verbose_name="Categoría"
    )
    
    description = models.TextField(
        verbose_name="Descripción",
        help_text="Descripción de qué calcula esta calculadora"
    )
    
    is_active = models.BooleanField(
        default=True,
        verbose_name="Activo"
    )

    # Configuración de parámetros esperados (JSON)
    input_parameters = models.JSONField(
        default=dict,
        verbose_name="Parámetros de Entrada",
        help_text="Definición de los parámetros que requiere esta calculadora"
    )

    class Meta:
        verbose_name = "Tipo de Calculadora"
        verbose_name_plural = "Tipos de Calculadoras"
        ordering = ['category', 'name']
        db_table = 'calculation_types'

    def __str__(self):
        return f"{self.get_category_display()} - {self.name}"


class Calculation(models.Model):
    """
    Resultados de cálculos realizados por las calculadoras
    """
    # Relaciones
    project = models.ForeignKey(
        Project,
        on_delete=models.CASCADE,
        related_name='calculations',
        verbose_name="Proyecto"
    )
    
    calculation_type = models.ForeignKey(
        CalculationType,
        on_delete=models.CASCADE,
        verbose_name="Tipo de Cálculo"
    )
    
    material = models.ForeignKey(
        Material,
        on_delete=models.SET_NULL,
        null=True, blank=True,
        verbose_name="Material Principal",
        help_text="Material principal calculado"
    )

    # Parámetros de entrada
    input_data = models.JSONField(
        verbose_name="Datos de Entrada",
        help_text="Parámetros utilizados para el cálculo"
    )

    # Resultados
    calculated_quantity = models.DecimalField(
        max_digits=10,
        decimal_places=3,
        verbose_name="Cantidad Calculada"
    )
    
    unit = models.CharField(
        max_length=20,
        verbose_name="Unidad"
    )
    
    estimated_cost = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        null=True, blank=True,
        verbose_name="Costo Estimado",
        help_text="Costo estimado basado en precios de referencia"
    )

    # Resultados detallados (JSON)
    detailed_results = models.JSONField(
        default=dict,
        verbose_name="Resultados Detallados",
        help_text="Resultados completos del cálculo incluyendo materiales secundarios"
    )

    # Metadatos
    created_at = models.DateTimeField(auto_now_add=True)
    
    # Control de uso
    added_to_budget = models.BooleanField(
        default=False,
        verbose_name="Agregado al Presupuesto",
        help_text="Indica si este cálculo ya fue agregado al presupuesto"
    )
    
    budget_item_created = models.ForeignKey(
        'budgets.BudgetItem',
        on_delete=models.SET_NULL,
        null=True, blank=True,
        verbose_name="Item de Presupuesto Creado",
        help_text="Referencia al item de presupuesto creado desde este cálculo"
    )

    class Meta:
        verbose_name = "Cálculo"
        verbose_name_plural = "Cálculos"
        ordering = ['-created_at']
        db_table = 'calculations'

    def __str__(self):
        return f"{self.project.name} - {self.calculation_type.name} ({self.created_at.strftime('%d/%m/%Y')})"

    def get_input_parameter(self, key, default=None):
        """Obtiene un parámetro específico de entrada"""
        return self.input_data.get(key, default)

    def get_detailed_result(self, key, default=None):
        """Obtiene un resultado específico detallado"""
        return self.detailed_results.get(key, default)


# Modelos específicos para cada tipo de calculadora

class PaintCalculation(models.Model):
    """
    Cálculos específicos para pintura
    """
    calculation = models.OneToOneField(
        Calculation,
        on_delete=models.CASCADE,
        related_name='paint_details'
    )
    
    # Parámetros específicos de pintura
    area_to_paint = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name="Área a Pintar (m²)"
    )
    
    number_of_coats = models.IntegerField(
        default=2,
        validators=[MinValueValidator(1)],
        verbose_name="Número de Capas"
    )
    
    paint_type = models.CharField(
        max_length=50,
        verbose_name="Tipo de Pintura",
        help_text="Ej: Látex, Esmalte, Anticorrosivo"
    )
    
    coverage_per_liter = models.DecimalField(
        max_digits=8,
        decimal_places=2,
        verbose_name="Rendimiento (m²/L)"
    )

    # Resultados específicos
    total_liters_needed = models.DecimalField(
        max_digits=10,
        decimal_places=3,
        verbose_name="Litros Totales Necesarios"
    )
    
    gallons_needed = models.DecimalField(
        max_digits=10,
        decimal_places=3,
        verbose_name="Galones Necesarios"
    )

    class Meta:
        verbose_name = "Cálculo de Pintura"
        verbose_name_plural = "Cálculos de Pintura"
        db_table = 'paint_calculations'


class GypsumCalculation(models.Model):
    """
    Cálculos específicos para gypsum
    """
    calculation = models.OneToOneField(
        Calculation,
        on_delete=models.CASCADE,
        related_name='gypsum_details'
    )
    
    # Parámetros específicos
    area_to_cover = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name="Área a Cubrir (m²)"
    )
    
    thickness = models.DecimalField(
        max_digits=4,
        decimal_places=1,
        verbose_name="Espesor (mm)"
    )
    
    gypsum_type = models.CharField(
        max_length=50,
        verbose_name="Tipo de Gypsum",
        help_text="Ej: Standard, Resistente a humedad, Acústico"
    )

    # Resultados específicos
    sheets_needed = models.IntegerField(
        verbose_name="Planchas Necesarias"
    )
    
    linear_meters_profile = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name="Metros Lineales de Perfil"
    )

    class Meta:
        verbose_name = "Cálculo de Gypsum"
        verbose_name_plural = "Cálculos de Gypsum"
        db_table = 'gypsum_calculations'


class LEDStripCalculation(models.Model):
    """
    Cálculos específicos para cintas LED
    """
    calculation = models.OneToOneField(
        Calculation,
        on_delete=models.CASCADE,
        related_name='led_strip_details'
    )
    
    # Parámetros específicos
    total_length = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name="Longitud Total (m)"
    )
    
    power_per_meter = models.DecimalField(
        max_digits=6,
        decimal_places=2,
        verbose_name="Potencia por Metro (W/m)"
    )
    
    voltage = models.CharField(
        max_length=10,
        verbose_name="Voltaje",
        help_text="Ej: 12V, 24V, 110V"
    )
    
    strip_type = models.CharField(
        max_length=50,
        verbose_name="Tipo de Cinta LED",
        help_text="Ej: SMD5050, SMD3528, COB"
    )

    # Resultados específicos
    total_power = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name="Potencia Total (W)"
    )
    
    drivers_needed = models.IntegerField(
        verbose_name="Drivers Necesarios"
    )
    
    meters_per_roll = models.DecimalField(
        max_digits=6,
        decimal_places=2,
        default=Decimal('5.00'),
        verbose_name="Metros por Rollo"
    )
    
    rolls_needed = models.IntegerField(
        verbose_name="Rollos Necesarios"
    )

    class Meta:
        verbose_name = "Cálculo de Cinta LED"
        verbose_name_plural = "Cálculos de Cintas LED"
        db_table = 'led_strip_calculations'


class CableCalculation(models.Model):
    """
    Cálculos específicos para cables eléctricos
    """
    calculation = models.OneToOneField(
        Calculation,
        on_delete=models.CASCADE,
        related_name='cable_details'
    )
    
    # Parámetros específicos
    total_length = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name="Longitud Total (m)"
    )
    
    wire_gauge = models.CharField(
        max_length=20,
        verbose_name="Calibre del Cable",
        help_text="Ej: 12 AWG, 14 AWG, 2.5mm²"
    )
    
    cable_type = models.CharField(
        max_length=50,
        verbose_name="Tipo de Cable",
        help_text="Ej: THHN, THW, Flexible"
    )
    
    installation_type = models.CharField(
        max_length=50,
        verbose_name="Tipo de Instalación",
        help_text="Ej: Conduit, Bandeja, Directo"
    )

    # Resultados específicos
    rolls_needed = models.IntegerField(
        verbose_name="Rollos Necesarios"
    )
    
    meters_per_roll = models.DecimalField(
        max_digits=6,
        decimal_places=2,
        default=Decimal('100.00'),
        verbose_name="Metros por Rollo"
    )

    class Meta:
        verbose_name = "Cálculo de Cable"
        verbose_name_plural = "Cálculos de Cables"
        db_table = 'cable_calculations'