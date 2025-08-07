from django.db import models

class MaterialCategory(models.Model):
    """
    Categorías de materiales (Construcción, Iluminación, etc.)
    """
    CATEGORY_TYPES = [
        ('construction', 'Construcción'),
        ('lighting', 'Iluminación'),
        ('electrical', 'Eléctrico'),
        ('plumbing', 'Plomería'),
        ('finishes', 'Acabados'),
        ('others', 'Otros'),
    ]
    
    name = models.CharField(
        max_length=100,
        verbose_name="Nombre de Categoría"
    )
    
    category_type = models.CharField(
        max_length=20,
        choices=CATEGORY_TYPES,
        verbose_name="Tipo de Categoría"
    )
    
    description = models.TextField(
        blank=True,
        verbose_name="Descripción"
    )
    
    is_active = models.BooleanField(
        default=True,
        verbose_name="Activo"
    )

    class Meta:
        verbose_name = "Categoría de Material"
        verbose_name_plural = "Categorías de Materiales"
        ordering = ['category_type', 'name']
        db_table = 'material_categories'

    def __str__(self):
        return f"{self.get_category_type_display()} - {self.name}"


class Material(models.Model):
    """
    Catálogo de materiales para construcción e iluminación
    """
    UNIT_CHOICES = [
        # Construcción
        ('kg', 'Kilogramos'),
        ('saco', 'Sacos'),
        ('m2', 'Metros Cuadrados'),
        ('m3', 'Metros Cúbicos'),
        ('ml', 'Metros Lineales'),
        ('litros', 'Litros'),
        ('galones', 'Galones'),
        ('unidad', 'Unidades'),
        
        # Iluminación específico
        ('metros', 'Metros'),
        ('rollo', 'Rollos'),
        ('pieza', 'Piezas'),
        ('par', 'Pares'),
        ('kit', 'Kits'),
    ]

    # Información básica
    name = models.CharField(
        max_length=200,
        verbose_name="Nombre del Material"
    )
    
    code = models.CharField(
        max_length=50,
        unique=True,
        verbose_name="Código del Material",
        help_text="Código único para identificar el material"
    )
    
    category = models.ForeignKey(
        MaterialCategory,
        on_delete=models.CASCADE,
        verbose_name="Categoría"
    )
    
    description = models.TextField(
        blank=True,
        verbose_name="Descripción"
    )

    # Especificaciones técnicas
    unit = models.CharField(
        max_length=20,
        choices=UNIT_CHOICES,
        verbose_name="Unidad de Medida"
    )
    
    # Para calculadoras
    yield_per_unit = models.DecimalField(
        max_digits=10,
        decimal_places=4,
        null=True, blank=True,
        verbose_name="Rendimiento por Unidad",
        help_text="Cuánto rinde cada unidad (ej: m2 por saco de cemento)"
    )
    
    waste_factor = models.DecimalField(
        max_digits=5,
        decimal_places=4,
        default=0.05,
        verbose_name="Factor de Desperdicio",
        help_text="Porcentaje de desperdicio típico (0.05 = 5%)"
    )

    # Precios de referencia
    reference_price = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        null=True, blank=True,
        verbose_name="Precio de Referencia",
        help_text="Precio promedio de referencia"
    )
    
    last_price_update = models.DateTimeField(
        null=True, blank=True,
        verbose_name="Última Actualización de Precio"
    )

    # Metadatos
    is_active = models.BooleanField(
        default=True,
        verbose_name="Activo"
    )
    
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    # Campos específicos para tipos de materiales
    
    # Para pintura
    coverage_per_liter = models.DecimalField(
        max_digits=8,
        decimal_places=2,
        null=True, blank=True,
        verbose_name="Cobertura por Litro (m²)",
        help_text="Para pintura: metros cuadrados que cubre un litro"
    )
    
    # Para materiales de construcción
    density = models.DecimalField(
        max_digits=8,
        decimal_places=3,
        null=True, blank=True,
        verbose_name="Densidad",
        help_text="Densidad del material (kg/m³)"
    )
    
    # Para iluminación - cintas LED
    power_per_meter = models.DecimalField(
        max_digits=6,
        decimal_places=2,
        null=True, blank=True,
        verbose_name="Potencia por Metro (W/m)",
        help_text="Para cintas LED: watts por metro"
    )
    
    # Para cables
    wire_gauge = models.CharField(
        max_length=20,
        blank=True,
        verbose_name="Calibre",
        help_text="Calibre del cable eléctrico"
    )

    class Meta:
        verbose_name = "Material"
        verbose_name_plural = "Materiales"
        ordering = ['category', 'name']
        db_table = 'materials'

    def __str__(self):
        return f"{self.code} - {self.name}"

    @property
    def category_type(self):
        """Retorna el tipo de categoría"""
        return self.category.category_type

    def get_price_with_waste(self, base_price=None):
        """Calcula precio incluyendo factor de desperdicio"""
        price = base_price or self.reference_price
        if price:
            return price * (1 + self.waste_factor)
        return None

    def calculate_quantity_needed(self, area_or_length, layers=1):
        """
        Calcula cantidad necesaria basada en área/longitud
        """
        if not self.yield_per_unit:
            return None
        
        # Cantidad base necesaria
        base_quantity = (area_or_length * layers) / self.yield_per_unit
        
        # Agregar factor de desperdicio
        final_quantity = base_quantity * (1 + self.waste_factor)
        
        return round(final_quantity, 2)


class MaterialSpecification(models.Model):
    """
    Especificaciones adicionales para materiales
    (colores, acabados, dimensiones, etc.)
    """
    material = models.ForeignKey(
        Material,
        on_delete=models.CASCADE,
        related_name='specifications'
    )
    
    specification_type = models.CharField(
        max_length=50,
        verbose_name="Tipo de Especificación",
        help_text="Ej: color, acabado, dimensión, voltaje"
    )
    
    value = models.CharField(
        max_length=100,
        verbose_name="Valor"
    )
    
    is_default = models.BooleanField(
        default=False,
        verbose_name="Por Defecto"
    )

    class Meta:
        verbose_name = "Especificación de Material"
        verbose_name_plural = "Especificaciones de Materiales"
        unique_together = ['material', 'specification_type', 'value']
        db_table = 'material_specifications'

    def __str__(self):
        return f"{self.material.name} - {self.specification_type}: {self.value}"