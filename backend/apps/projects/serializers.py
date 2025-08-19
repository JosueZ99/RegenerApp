from rest_framework import serializers
from .models import Project

class ProjectListSerializer(serializers.ModelSerializer):
    """
    Serializer para lista de proyectos (pantalla de selección)
    """
    phase_display = serializers.CharField(source='get_phase_display_with_icon', read_only=True)
    status_display = serializers.CharField(source='get_status_display', read_only=True)
    duration_days = serializers.IntegerField(read_only=True)
    is_overdue = serializers.BooleanField(read_only=True)
    progress_percentage = serializers.IntegerField(read_only=True)
    
    class Meta:
        model = Project
        fields = [
            'id', 'name', 'client', 'location', 'project_type',
            'start_date', 'end_date', 'current_phase', 'status',
            'phase_display', 'status_display', 'duration_days',
            'is_overdue', 'progress_percentage', 'is_selected',
            'created_at'
        ]

class ProjectDetailSerializer(serializers.ModelSerializer):
    """
    Serializer completo para detalles del proyecto (Dashboard)
    """
    phase_display = serializers.CharField(source='get_phase_display_with_icon', read_only=True)
    status_display = serializers.CharField(source='get_status_display', read_only=True)
    project_type_display = serializers.CharField(source='get_project_type_display', read_only=True)
    duration_days = serializers.IntegerField(read_only=True)
    is_overdue = serializers.BooleanField(read_only=True)
    progress_percentage = serializers.IntegerField(read_only=True)
    
    # Contadores relacionados
    total_budget_items = serializers.SerializerMethodField()
    total_expenses = serializers.SerializerMethodField()
    
    class Meta:
        model = Project
        fields = [
            'id', 'name', 'client', 'location', 'project_type',
            'project_type_display', 'description', 'start_date', 
            'end_date', 'actual_end_date', 'current_phase', 'status',
            'phase_display', 'status_display', 'drive_folder_url',
            'initial_budget', 'duration_days', 'is_overdue',
            'progress_percentage', 'is_selected', 'created_at',
            'updated_at', 'total_budget_items', 'total_expenses'
        ]
    
    def get_total_budget_items(self, obj):
        """Número total de items en el presupuesto"""
        return obj.budget_items.count()
    
    def get_total_expenses(self, obj):
        """Número total de gastos reales"""
        return obj.real_expenses.count()

class ProjectCreateUpdateSerializer(serializers.ModelSerializer):
    """
    Serializer para crear/actualizar proyectos
    """
    class Meta:
        model = Project
        fields = [
            'name', 'client', 'location', 'project_type',
            'description', 'start_date', 'end_date',
            'drive_folder_url', 'initial_budget'
        ]
    
    def validate_end_date(self, value):
        """Validar que la fecha de fin sea posterior al inicio"""
        if value and self.initial_data.get('start_date'):
            start_date = serializers.DateField().to_internal_value(
                self.initial_data['start_date']
            )
            if value <= start_date:
                raise serializers.ValidationError(
                    "La fecha de finalización debe ser posterior a la fecha de inicio"
                )
        return value

class ProjectSelectionSerializer(serializers.Serializer):
    """
    Serializer para manejar la selección de proyecto
    """
    project_id = serializers.IntegerField()
    
    def validate_project_id(self, value):
        """Validar que el proyecto existe"""
        try:
            Project.objects.get(id=value)
        except Project.DoesNotExist:
            raise serializers.ValidationError("El proyecto no existe")
        return value