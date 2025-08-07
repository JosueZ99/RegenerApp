from django.db import models
from django.core.validators import RegexValidator
from apps.materials.models import MaterialCategory

class Supplier(models.Model):
    """
    Proveedores de materiales y servicios
    """
    SUPPLIER_TYPES = [
        ('materials', 'Materiales de Construcción'),
        ('lighting', 'Iluminación'),
        ('electrical', 'Eléctrico'),
        ('services', 'Servicios'),
        ('mixed', 'Mixto'),
    ]
    
    RATING_CHOICES = [
        (1, '⭐ Básico'),
        (2, '⭐⭐ Regular'),
        (3, '⭐⭐⭐ Bueno'),
        (4, '⭐⭐⭐⭐ Muy Bueno'),
        (5, '⭐⭐⭐⭐⭐ Excelente'),
    ]

    # Información básica
    name = models.CharField(
        max_length=150,
        verbose_name="Nombre del Proveedor"
    )
    
    commercial_name = models.CharField(
        max_length=150,
        blank=True,
        verbose_name="Nombre Comercial",
        help_text="Nombre comercial si es diferente al legal"
    )
    
    supplier_type = models.CharField(
        max_length=20,
        choices=SUPPLIER_TYPES,
        verbose_name="Tipo de Proveedor"
    )
    
    # Información de contacto
    contact_person = models.CharField(
        max_length=100,
        blank=True,
        verbose_name="Persona de Contacto"
    )
    
    phone_regex = RegexValidator(
        regex=r'^\+?1?\d{9,15}$',
        message="Número de teléfono debe tener formato: '+999999999'. Hasta 15 dígitos."
    )
    
    phone = models.CharField(
        validators=[phone_regex],
        max_length=17,
        verbose_name="Teléfono Principal"
    )
    
    phone_secondary = models.CharField(
        validators=[phone_regex],
        max_length=17,
        blank=True,
        verbose_name="Teléfono Secundario"
    )
    
    email = models.EmailField(
        blank=True,
        verbose_name="Email"
    )
    
    website = models.URLField(
        blank=True,
        verbose_name="Sitio Web"
    )

    # Ubicación
    address = models.TextField(
        verbose_name="Dirección"
    )
    
    city = models.CharField(
        max_length=100,
        default="Quito",
        verbose_name="Ciudad"
    )
    
    zone = models.CharField(
        max_length=100,
        blank=True,
        verbose_name="Zona/Sector",
        help_text="Ej: Norte, Sur, Cumbayá, etc."
    )

    # Información comercial
    ruc = models.CharField(
        max_length=13,
        blank=True,
        verbose_name="RUC",
        help_text="Registro Único de Contribuyentes"
    )
    
    categories = models.ManyToManyField(
        MaterialCategory,
        blank=True,
        verbose_name="Categorías que Maneja",
        help_text="Categorías de materiales que ofrece este proveedor"
    )

    # Evaluación
    rating = models.IntegerField(
        choices=RATING_CHOICES,
        default=3,
        verbose_name="Calificación"
    )
    
    # Condiciones comerciales
    payment_terms = models.CharField(
        max_length=200,
        blank=True,
        verbose_name="Términos de Pago",
        help_text="Ej: 30 días, contado, etc."
    )
    
    delivery_time = models.CharField(
        max_length=100,
        blank=True,
        verbose_name="Tiempo de Entrega",
        help_text="Tiempo promedio de entrega"
    )
    
    minimum_order = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        null=True, blank=True,
        verbose_name="Pedido Mínimo",
        help_text="Monto mínimo de pedido"
    )
    
    offers_delivery = models.BooleanField(
        default=True,
        verbose_name="Ofrece Entrega a Domicilio"
    )
    
    delivery_cost = models.DecimalField(
        max_digits=8,
        decimal_places=2,
        null=True, blank=True,
        verbose_name="Costo de Entrega",
        help_text="Costo de entrega a domicilio"
    )

    # Notas y observaciones
    notes = models.TextField(
        blank=True,
        verbose_name="Notas",
        help_text="Observaciones adicionales sobre el proveedor"
    )
    
    strengths = models.TextField(
        blank=True,
        verbose_name="Fortalezas",
        help_text="Puntos fuertes del proveedor"
    )
    
    weaknesses = models.TextField(
        blank=True,
        verbose_name="Debilidades",
        help_text="Aspectos a mejorar del proveedor"
    )

    # Estado
    is_active = models.BooleanField(
        default=True,
        verbose_name="Activo"
    )
    
    is_preferred = models.BooleanField(
        default=False,
        verbose_name="Proveedor Preferido"
    )

    # Metadatos
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    last_contact_date = models.DateField(
        null=True, blank=True,
        verbose_name="Última Fecha de Contacto"
    )

    class Meta:
        verbose_name = "Proveedor"
        verbose_name_plural = "Proveedores"
        ordering = ['name']
        db_table = 'suppliers'

    def __str__(self):
        return f"{self.name} ({self.get_supplier_type_display()})"

    @property
    def display_name(self):
        """Retorna el nombre comercial si existe, sino el legal"""
        return self.commercial_name if self.commercial_name else self.name

    @property
    def rating_stars(self):
        """Retorna las estrellas de calificación"""
        return "⭐" * self.rating

    def get_categories_list(self):
        """Retorna lista de categorías que maneja"""
        return [cat.name for cat in self.categories.all()]

    def has_category(self, category_type):
        """Verifica si el proveedor maneja una categoría específica"""
        return self.categories.filter(category_type=category_type).exists()


class SupplierPrice(models.Model):
    """
    Precios de materiales por proveedor
    """
    supplier = models.ForeignKey(
        Supplier,
        on_delete=models.CASCADE,
        related_name='prices'
    )
    
    material = models.ForeignKey(
        'materials.Material',
        on_delete=models.CASCADE,
        related_name='supplier_prices'
    )
    
    price = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name="Precio"
    )
    
    currency = models.CharField(
        max_length=3,
        default='USD',
        verbose_name="Moneda"
    )
    
    # Descuentos y promociones
    discount_percentage = models.DecimalField(
        max_digits=5,
        decimal_places=2,
        default=0,
        verbose_name="Descuento (%)"
    )
    
    bulk_discount_threshold = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        null=True, blank=True,
        verbose_name="Umbral para Descuento por Volumen"
    )
    
    bulk_discount_percentage = models.DecimalField(
        max_digits=5,
        decimal_places=2,
        null=True, blank=True,
        verbose_name="Descuento por Volumen (%)"
    )

    # Validez del precio
    valid_from = models.DateField(
        verbose_name="Válido Desde"
    )
    
    valid_until = models.DateField(
        null=True, blank=True,
        verbose_name="Válido Hasta"
    )
    
    is_current = models.BooleanField(
        default=True,
        verbose_name="Precio Actual"
    )

    # Metadatos
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        verbose_name = "Precio de Proveedor"
        verbose_name_plural = "Precios de Proveedores"
        ordering = ['-created_at']
        db_table = 'supplier_prices'

    def __str__(self):
        return f"{self.supplier.name} - {self.material.name}: ${self.price}"

    @property
    def final_price(self):
        """Calcula el precio final con descuento"""
        if self.discount_percentage > 0:
            discount_amount = self.price * (self.discount_percentage / 100)
            return self.price - discount_amount
        return self.price

    def get_bulk_price(self, quantity_value):
        """Calcula precio con descuento por volumen si aplica"""
        if (self.bulk_discount_threshold and 
            self.bulk_discount_percentage and 
            quantity_value >= self.bulk_discount_threshold):
            
            bulk_discount = self.price * (self.bulk_discount_percentage / 100)
            return self.price - bulk_discount
        return self.final_price