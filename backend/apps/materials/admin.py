from django.contrib import admin
from .models import MaterialCategory, Material, MaterialSpecification

@admin.register(MaterialCategory)
class MaterialCategoryAdmin(admin.ModelAdmin):
    list_display = ['name', 'category_type', 'is_active']
    list_filter = ['category_type', 'is_active']
    search_fields = ['name', 'description']

class MaterialSpecificationInline(admin.TabularInline):
    model = MaterialSpecification
    extra = 1

@admin.register(Material)
class MaterialAdmin(admin.ModelAdmin):
    list_display = [
        'code', 'name', 'category', 'unit', 'reference_price', 
        'yield_per_unit', 'is_active'
    ]
    list_filter = ['category', 'unit', 'is_active', 'category__category_type']
    search_fields = ['name', 'code', 'description']
    readonly_fields = ['created_at', 'updated_at']
    inlines = [MaterialSpecificationInline]
    
    fieldsets = (
        ('Información Básica', {
            'fields': ('name', 'code', 'category', 'description', 'is_active')
        }),
        ('Especificaciones Técnicas', {
            'fields': ('unit', 'yield_per_unit', 'waste_factor')
        }),
        ('Precios', {
            'fields': ('reference_price', 'last_price_update')
        }),
        ('Construcción', {
            'fields': ('coverage_per_liter', 'density'),
            'classes': ('collapse',)
        }),
        ('Iluminación', {
            'fields': ('power_per_meter', 'wire_gauge'),
            'classes': ('collapse',)
        }),
        ('Metadatos', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )

@admin.register(MaterialSpecification)
class MaterialSpecificationAdmin(admin.ModelAdmin):
    list_display = ['material', 'specification_type', 'value', 'is_default']
    list_filter = ['specification_type', 'is_default']
    search_fields = ['material__name', 'specification_type', 'value']