from django.db import models
from django.core.validators import URLValidator
from datetime import date

class Project(models.Model):
    """
    Modelo principal para proyectos arquitectónicos
    """
    PHASE_CHOICES = [
        ('design', 'Diseño'),
        ('purchase', 'Compra'), 
        ('installation', 'Instalación'),
        ('completed', 'Completado'),
    ]
    
    STATUS_CHOICES = [
        ('planning', 'PLANIFICACIÓN'),
        ('in_progress', 'EN PROGRESO'),
        ('on_hold', 'EN PAUSA'),
        ('completed', 'TERMINADO'),
        ('budget', 'PRESUPUESTO'),
    ]
    
    PROJECT_TYPES = [
        ('residential', 'Residencial'),
        ('commercial', 'Comercial'),
        ('institutional', 'Institucional'),
        ('industrial', 'Industrial'),
    ]

    # Información básica
    name = models.CharField(
        max_length=200, 
        verbose_name="Nombre del Proyecto",
        help_text="Nombre descriptivo del proyecto"
    )
    
    client = models.CharField(
        max_length=150,
        verbose_name="Cliente",
        help_text="Nombre del cliente o empresa"
    )
    
    location = models.CharField(
        max_length=200,
        verbose_name="Ubicación",
        help_text="Dirección o zona del proyecto"
    )
    
    project_type = models.CharField(
        max_length=20,
        choices=PROJECT_TYPES,
        default='residential',
        verbose_name="Tipo de Proyecto"
    )
    
    description = models.TextField(
        blank=True,
        verbose_name="Descripción",
        help_text="Descripción detallada del proyecto"
    )

    # Fechas
    start_date = models.DateField(
        verbose_name="Fecha de Inicio",
        help_text="Fecha de inicio del proyecto"
    )
    
    end_date = models.DateField(
        null=True, blank=True,
        verbose_name="Fecha de Finalización",
        help_text="Fecha planificada de finalización"
    )
    
    actual_end_date = models.DateField(
        null=True, blank=True,
        verbose_name="Fecha Real de Finalización"
    )

    # Estado y fase
    current_phase = models.CharField(
        max_length=20,
        choices=PHASE_CHOICES,
        default='design',
        verbose_name="Fase Actual"
    )
    
    status = models.CharField(
        max_length=20,
        choices=STATUS_CHOICES,
        default='planning',
        verbose_name="Estado del Proyecto"
    )

    # Integración Google Drive
    drive_folder_url = models.URLField(
        blank=True,
        validators=[URLValidator()],
        verbose_name="URL Carpeta Google Drive",
        help_text="Link a la carpeta de Google Drive del proyecto"
    )

    # Presupuesto inicial
    initial_budget = models.DecimalField(
        max_digits=12, 
        decimal_places=2,
        null=True, blank=True,
        verbose_name="Presupuesto Inicial",
        help_text="Presupuesto inicial estimado del proyecto"
    )

    # Metadatos
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    # Selección actual (para el sistema de "login")
    is_selected = models.BooleanField(
        default=False,
        verbose_name="Proyecto Seleccionado",
        help_text="Indica si este proyecto está actualmente seleccionado en la app"
    )

    class Meta:
        verbose_name = "Proyecto"
        verbose_name_plural = "Proyectos"
        ordering = ['-created_at']
        db_table = 'projects'

    def __str__(self):
        return f"{self.name} - {self.client}"

    def save(self, *args, **kwargs):
        # Solo un proyecto puede estar seleccionado a la vez
        if self.is_selected:
            Project.objects.filter(is_selected=True).update(is_selected=False)
        super().save(*args, **kwargs)

    @property
    def duration_days(self):
        """Calcula la duración del proyecto en días"""
        if self.end_date:
            return (self.end_date - self.start_date).days
        return None

    @property
    def is_overdue(self):
        """Verifica si el proyecto está atrasado"""
        if self.end_date and self.status != 'completed':
            return date.today() > self.end_date
        return False

    @property
    def progress_percentage(self):
        """Calcula el porcentaje de progreso basado en la fase"""
        phase_progress = {
            'design': 25,
            'purchase': 50,
            'installation': 75,
            'completed': 100,
        }
        return phase_progress.get(self.current_phase, 0)

    def get_phase_display_with_icon(self):
        """Retorna la fase con emoji para la UI"""
        phase_icons = {
            'design': '🎨 Diseño',
            'purchase': '🛒 Compra',
            'installation': '🔧 Instalación',
            'completed': '✅ Completado',
        }
        return phase_icons.get(self.current_phase, self.get_current_phase_display())

    def advance_to_next_phase(self):
        """Avanza el proyecto a la siguiente fase"""
        phase_order = ['design', 'purchase', 'installation', 'completed']
        current_index = phase_order.index(self.current_phase)
        
        if current_index < len(phase_order) - 1:
            self.current_phase = phase_order[current_index + 1]
            
            # Actualizar estado si se completa
            if self.current_phase == 'completed':
                self.status = 'completed'
                self.actual_end_date = date.today()
            
            self.save()
            return True
        return False