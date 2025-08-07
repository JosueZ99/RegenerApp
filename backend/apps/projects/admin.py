from django.contrib import admin
from .models import Project

@admin.register(Project)
class ProjectAdmin(admin.ModelAdmin):
    list_display = [
        'name', 'client', 'location', 'project_type', 
        'current_phase', 'status', 'start_date', 'is_selected'
    ]
    list_filter = [
        'project_type', 'current_phase', 'status', 'start_date'
    ]
    search_fields = ['name', 'client', 'location']
    readonly_fields = ['created_at', 'updated_at', 'progress_percentage']
    
    fieldsets = (
        ('Información Básica', {
            'fields': ('name', 'client', 'location', 'project_type', 'description')
        }),
        ('Fechas', {
            'fields': ('start_date', 'end_date', 'actual_end_date')
        }),
        ('Estado y Fase', {
            'fields': ('current_phase', 'status', 'is_selected')
        }),
        ('Presupuesto e Integración', {
            'fields': ('initial_budget', 'drive_folder_url')
        }),
        ('Metadatos', {
            'fields': ('created_at', 'updated_at', 'progress_percentage'),
            'classes': ('collapse',)
        }),
    )
    
    actions = ['mark_as_selected', 'advance_phase']
    
    def mark_as_selected(self, request, queryset):
        # Solo seleccionar uno a la vez
        if queryset.count() > 1:
            self.message_user(request, "Solo se puede seleccionar un proyecto a la vez", level='error')
            return
        
        # Deseleccionar todos
        Project.objects.update(is_selected=False)
        # Seleccionar el elegido
        queryset.update(is_selected=True)
        self.message_user(request, f"Proyecto seleccionado: {queryset.first().name}")
    
    mark_as_selected.short_description = "Seleccionar proyecto"
    
    def advance_phase(self, request, queryset):
        advanced = 0
        for project in queryset:
            if project.advance_to_next_phase():
                advanced += 1
        
        self.message_user(request, f"{advanced} proyectos avanzaron a la siguiente fase")
    
    advance_phase.short_description = "Avanzar a siguiente fase"