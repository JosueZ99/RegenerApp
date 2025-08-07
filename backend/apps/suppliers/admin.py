from django.contrib import admin
from .models import Supplier, SupplierPrice

class SupplierPriceInline(admin.TabularInline):
    model = SupplierPrice
    extra = 1
    readonly_fields = ['final_price']

@admin.register(Supplier)
class SupplierAdmin(admin.ModelAdmin):
    list_display = [
        'name', 'supplier_type', 'phone', 'city', 'rating', 
        'is_active', 'is_preferred'
    ]
    list_filter = [
        'supplier_type', 'city', 'rating', 'is_active', 
        'is_preferred', 'offers_delivery'
    ]
    search_fields = ['name', 'commercial_name', 'contact_person', 'phone']
    readonly_fields = ['created_at', 'updated_at', 'rating_stars']
    filter_horizontal = ['categories']
    inlines = [SupplierPriceInline]
    
    fieldsets = (
        ('Información Básica', {
            'fields': ('name', 'commercial_name', 'supplier_type', 'categories')
        }),
        ('Contacto', {
            'fields': ('contact_person', 'phone', 'phone_secondary', 'email', 'website')
        }),
        ('Ubicación', {
            'fields': ('address', 'city', 'zone')
        }),
        ('Información Comercial', {
            'fields': ('ruc', 'rating', 'rating_stars')
        }),
        ('Condiciones Comerciales', {
            'fields': (
                'payment_terms', 'delivery_time', 'minimum_order',
                'offers_delivery', 'delivery_cost'
            )
        }),
        ('Estado', {
            'fields': ('is_active', 'is_preferred')
        }),
        ('Evaluación', {
            'fields': ('notes', 'strengths', 'weaknesses')
        }),
        ('Metadatos', {
            'fields': ('created_at', 'updated_at', 'last_contact_date'),
            'classes': ('collapse',)
        }),
    )

@admin.register(SupplierPrice)
class SupplierPriceAdmin(admin.ModelAdmin):
    list_display = [
        'supplier', 'material', 'price', 'currency', 
        'discount_percentage', 'is_current', 'valid_from'
    ]
    list_filter = [
        'supplier', 'currency', 'is_current', 'valid_from'
    ]
    search_fields = [
        'supplier__name', 'material__name', 'material__code'
    ]
    readonly_fields = ['final_price', 'created_at', 'updated_at']
    
    fieldsets = (
        ('Información Básica', {
            'fields': ('supplier', 'material', 'price', 'currency')
        }),
        ('Descuentos', {
            'fields': (
                'discount_percentage', 'bulk_discount_threshold', 
                'bulk_discount_percentage', 'final_price'
            )
        }),
        ('Validez', {
            'fields': ('valid_from', 'valid_until', 'is_current')
        }),
        ('Metadatos', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )