from django.contrib import admin
from .models import BudgetItem, RealExpense, ProjectFinancialSummary

@admin.register(BudgetItem)
class BudgetItemAdmin(admin.ModelAdmin):
    list_display = [
        'project', 'description', 'category', 'quantity', 
        'unit', 'unit_price', 'total_price', 'supplier'
    ]
    list_filter = ['project', 'category', 'created_from_calculation']
    search_fields = ['description', 'project__name', 'spaces']
    readonly_fields = ['total_price', 'created_at', 'updated_at']
    
    fieldsets = (
        ('Información del Item', {
            'fields': ('project', 'description', 'category', 'spaces')
        }),
        ('Material y Proveedor', {
            'fields': ('material', 'supplier')
        }),
        ('Cantidades y Precios', {
            'fields': ('quantity', 'unit', 'unit_price', 'total_price')
        }),
        ('Notas y Control', {
            'fields': ('notes', 'created_from_calculation')
        }),
        ('Metadatos', {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    actions = ['copy_to_expenses']
    
    def copy_to_expenses(self, request, queryset):
        copied = 0
        for item in queryset:
            expense = item.copy_to_expense()
            # Necesitarías agregar fecha de compra manualmente
            expense.purchase_date = item.created_at.date()
            expense.save()
            copied += 1
        
        self.message_user(request, f"{copied} items copiados a gastos reales")
    
    copy_to_expenses.short_description = "Copiar a gastos reales"

@admin.register(RealExpense)
class RealExpenseAdmin(admin.ModelAdmin):
    list_display = [
        'project', 'description', 'category', 'quantity',
        'unit_price', 'total_price', 'purchase_date', 'supplier'
    ]
    list_filter = ['project', 'category', 'purchase_date']
    search_fields = ['description', 'project__name', 'invoice_number']
    readonly_fields = ['total_price', 'total_discount', 'savings_vs_budget', 'created_at', 'updated_at']
    
    fieldsets = (
        ('Información del Gasto', {
            'fields': ('project', 'budget_item', 'description', 'category', 'spaces')
        }),
        ('Material y Proveedor', {
            'fields': ('material', 'supplier')
        }),
        ('Cantidades y Precios', {
            'fields': ('quantity', 'unit', 'unit_price')
        }),
        ('Descuentos', {
            'fields': ('discount_percentage', 'discount_amount', 'total_price', 'total_discount')
        }),
        ('Información de Compra', {
            'fields': ('purchase_date', 'invoice_number', 'payment_method')
        }),
        ('Análisis', {
            'fields': ('savings_vs_budget',),
            'classes': ('collapse',)
        }),
        ('Notas y Metadatos', {
            'fields': ('notes', 'created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )

@admin.register(ProjectFinancialSummary)
class ProjectFinancialSummaryAdmin(admin.ModelAdmin):
    list_display = [
        'project', 'total_budget', 'total_expenses', 'balance', 
        'budget_utilization_percentage', 'is_over_budget'
    ]
    list_filter = ['last_updated']  # Cambiado: usar campo real en lugar de propiedad
    search_fields = ['project__name']
    readonly_fields = [
        'total_budget', 'budget_construction', 'budget_lighting', 'budget_others',
        'total_expenses', 'expenses_construction', 'expenses_lighting', 'expenses_others',
        'balance', 'budget_utilization_percentage', 'is_over_budget', 'remaining_budget',
        'last_updated'
    ]
    
    fieldsets = (
        ('Proyecto', {
            'fields': ('project',)
        }),
        ('Totales de Presupuesto', {
            'fields': ('total_budget', 'budget_construction', 'budget_lighting', 'budget_others')
        }),
        ('Totales de Gastos', {
            'fields': ('total_expenses', 'expenses_construction', 'expenses_lighting', 'expenses_others')
        }),
        ('Análisis Financiero', {
            'fields': (
                'balance', 'budget_utilization_percentage', 'is_over_budget', 'remaining_budget'
            )
        }),
        ('Metadatos', {
            'fields': ('last_updated',),
            'classes': ('collapse',)
        }),
    )
    
    actions = ['update_summaries']
    
    def update_summaries(self, request, queryset):
        updated = 0
        for summary in queryset:
            summary.update_summary()
            updated += 1
        
        self.message_user(request, f"{updated} resúmenes financieros actualizados")
    
    update_summaries.short_description = "Actualizar resúmenes financieros"