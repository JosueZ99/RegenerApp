from django.contrib import admin
from .models import (
    CalculationType, Calculation, PaintCalculation, 
    GypsumCalculation, LEDStripCalculation, CableCalculation
)

@admin.register(CalculationType)
class CalculationTypeAdmin(admin.ModelAdmin):
    list_display = ['name', 'code', 'category', 'is_active']
    list_filter = ['category', 'is_active']
    search_fields = ['name', 'code', 'description']
    
    fieldsets = (
        ('Información Básica', {
            'fields': ('name', 'code', 'category', 'description', 'is_active')
        }),
        ('Configuración', {
            'fields': ('input_parameters',)
        }),
    )

@admin.register(Calculation)
class CalculationAdmin(admin.ModelAdmin):
    list_display = [
        'project', 'calculation_type', 'calculated_quantity', 
        'unit', 'estimated_cost', 'added_to_budget', 'created_at'
    ]
    list_filter = [
        'calculation_type', 'added_to_budget', 'created_at'
    ]
    search_fields = ['project__name', 'calculation_type__name']
    readonly_fields = ['created_at']
    
    fieldsets = (
        ('Información Básica', {
            'fields': ('project', 'calculation_type', 'material')
        }),
        ('Datos de Entrada', {
            'fields': ('input_data',)
        }),
        ('Resultados', {
            'fields': ('calculated_quantity', 'unit', 'estimated_cost')
        }),
        ('Resultados Detallados', {
            'fields': ('detailed_results',),
            'classes': ('collapse',)
        }),
        ('Control de Presupuesto', {
            'fields': ('added_to_budget', 'budget_item_created')
        }),
        ('Metadatos', {
            'fields': ('created_at',),
            'classes': ('collapse',)
        }),
    )

@admin.register(PaintCalculation)
class PaintCalculationAdmin(admin.ModelAdmin):
    list_display = [
        'calculation', 'area_to_paint', 'number_of_coats', 
        'paint_type', 'total_liters_needed'
    ]
    search_fields = ['calculation__project__name', 'paint_type']
    
    fieldsets = (
        ('Cálculo Base', {
            'fields': ('calculation',)
        }),
        ('Parámetros de Pintura', {
            'fields': ('area_to_paint', 'number_of_coats', 'paint_type', 'coverage_per_liter')
        }),
        ('Resultados', {
            'fields': ('total_liters_needed', 'gallons_needed')
        }),
    )

@admin.register(GypsumCalculation)
class GypsumCalculationAdmin(admin.ModelAdmin):
    list_display = [
        'calculation', 'area_to_cover', 'thickness', 
        'gypsum_type', 'sheets_needed'
    ]
    search_fields = ['calculation__project__name', 'gypsum_type']
    
    fieldsets = (
        ('Cálculo Base', {
            'fields': ('calculation',)
        }),
        ('Parámetros de Gypsum', {
            'fields': ('area_to_cover', 'thickness', 'gypsum_type')
        }),
        ('Resultados', {
            'fields': ('sheets_needed', 'linear_meters_profile')
        }),
    )

@admin.register(LEDStripCalculation)
class LEDStripCalculationAdmin(admin.ModelAdmin):
    list_display = [
        'calculation', 'total_length', 'power_per_meter', 
        'voltage', 'total_power', 'drivers_needed'
    ]
    search_fields = ['calculation__project__name', 'strip_type', 'voltage']
    
    fieldsets = (
        ('Cálculo Base', {
            'fields': ('calculation',)
        }),
        ('Parámetros de Cinta LED', {
            'fields': ('total_length', 'power_per_meter', 'voltage', 'strip_type')
        }),
        ('Resultados', {
            'fields': ('total_power', 'drivers_needed', 'meters_per_roll', 'rolls_needed')
        }),
    )

@admin.register(CableCalculation)
class CableCalculationAdmin(admin.ModelAdmin):
    list_display = [
        'calculation', 'total_length', 'wire_gauge', 
        'cable_type', 'rolls_needed'
    ]
    search_fields = ['calculation__project__name', 'wire_gauge', 'cable_type']
    
    fieldsets = (
        ('Cálculo Base', {
            'fields': ('calculation',)
        }),
        ('Parámetros de Cable', {
            'fields': ('total_length', 'wire_gauge', 'cable_type', 'installation_type')
        }),
        ('Resultados', {
            'fields': ('rolls_needed', 'meters_per_roll')
        }),
    )