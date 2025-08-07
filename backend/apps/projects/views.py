from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.filters import SearchFilter, OrderingFilter
from django_filters.rest_framework import DjangoFilterBackend
from django.shortcuts import get_object_or_404
from django.db.models import Q
from django.utils import timezone
from .models import Project
from .serializers import (
    ProjectListSerializer, ProjectDetailSerializer, 
    ProjectCreateUpdateSerializer, ProjectSelectionSerializer
)

class ProjectViewSet(viewsets.ModelViewSet):
    """
    ViewSet para gestión de proyectos
    """
    queryset = Project.objects.all()
    filter_backends = [SearchFilter, OrderingFilter, DjangoFilterBackend]
    search_fields = ['name', 'client', 'location']
    ordering_fields = ['name', 'start_date', 'created_at', 'status']
    ordering = ['-created_at']
    filterset_fields = ['project_type', 'status', 'current_phase']
    
    def get_serializer_class(self):
        """Retorna serializer según la acción"""
        if self.action == 'list':
            return ProjectListSerializer
        elif self.action in ['create', 'update', 'partial_update']:
            return ProjectCreateUpdateSerializer
        return ProjectDetailSerializer
    
    def get_queryset(self):
        """Personalizar queryset según filtros"""
        queryset = Project.objects.all()
        
        # Filtro por fechas
        start_date = self.request.query_params.get('start_date')
        end_date = self.request.query_params.get('end_date')
        
        if start_date:
            queryset = queryset.filter(start_date__gte=start_date)
        if end_date:
            queryset = queryset.filter(start_date__lte=end_date)
        
        # Filtro por proyecto seleccionado
        only_selected = self.request.query_params.get('only_selected')
        if only_selected and only_selected.lower() == 'true':
            queryset = queryset.filter(is_selected=True)
        
        return queryset
    
    @action(detail=True, methods=['post'])
    def select_project(self, request, pk=None):
        """
        Endpoint para seleccionar un proyecto (sistema de "login")
        POST /api/projects/{id}/select_project/
        """
        project = self.get_object()
        
        # Deseleccionar todos los proyectos
        Project.objects.update(is_selected=False)
        
        # Seleccionar el proyecto actual
        project.is_selected = True
        project.save()
        
        serializer = ProjectDetailSerializer(project)
        return Response({
            'message': f'Proyecto "{project.name}" seleccionado correctamente',
            'project': serializer.data
        })
    
    @action(detail=False, methods=['get'])
    def selected(self, request):
        """
        Obtiene el proyecto actualmente seleccionado
        GET /api/projects/selected/
        """
        try:
            project = Project.objects.get(is_selected=True)
            serializer = ProjectDetailSerializer(project)
            return Response(serializer.data)
        except Project.DoesNotExist:
            return Response(
                {'message': 'No hay ningún proyecto seleccionado'},
                status=status.HTTP_404_NOT_FOUND
            )
    
    @action(detail=True, methods=['get'])
    def dashboard(self, request, pk=None):
        """
        Datos específicos para el dashboard del proyecto
        GET /api/projects/{id}/dashboard/
        """
        project = self.get_object()
        
        # Datos básicos del proyecto
        project_data = ProjectDetailSerializer(project).data
        
        # Resumen financiero
        try:
            financial_summary = project.financial_summary
            financial_data = {
                'total_budget': float(financial_summary.total_budget),
                'total_expenses': float(financial_summary.total_expenses),
                'balance': float(financial_summary.balance),
                'budget_utilization_percentage': float(financial_summary.budget_utilization_percentage),
                'is_over_budget': financial_summary.is_over_budget,
                'remaining_budget': float(financial_summary.remaining_budget),
                'budget_by_category': {
                    'construction': {
                        'budgeted': float(financial_summary.budget_construction),
                        'spent': float(financial_summary.expenses_construction)
                    },
                    'lighting': {
                        'budgeted': float(financial_summary.budget_lighting),
                        'spent': float(financial_summary.expenses_lighting)
                    },
                    'others': {
                        'budgeted': float(financial_summary.budget_others),
                        'spent': float(financial_summary.expenses_others)
                    }
                }
            }
        except:
            # Si no existe resumen financiero, crear uno vacío
            from apps.budgets.models import ProjectFinancialSummary
            financial_summary = ProjectFinancialSummary.objects.create(project=project)
            financial_summary.update_summary()
            financial_data = {
                'total_budget': 0.0,
                'total_expenses': 0.0,
                'balance': 0.0,
                'budget_utilization_percentage': 0.0,
                'is_over_budget': False,
                'remaining_budget': 0.0,
                'budget_by_category': {
                    'construction': {'budgeted': 0.0, 'spent': 0.0},
                    'lighting': {'budgeted': 0.0, 'spent': 0.0},
                    'others': {'budgeted': 0.0, 'spent': 0.0}
                }
            }
        
        # Estadísticas adicionales
        stats = {
            'budget_items_count': project.budget_items.count(),
            'expenses_count': project.real_expenses.count(),
            'calculations_count': project.calculations.count(),
            'recent_expenses': project.real_expenses.order_by('-purchase_date')[:5].values(
                'description', 'total_price', 'purchase_date', 'category'
            )
        }
        
        return Response({
            'project': project_data,
            'financial_summary': financial_data,
            'statistics': stats
        })
    
    @action(detail=True, methods=['post'])
    def advance_phase(self, request, pk=None):
        """
        Avanzar proyecto a la siguiente fase
        POST /api/projects/{id}/advance_phase/
        """
        project = self.get_object()
        
        if project.advance_to_next_phase():
            serializer = ProjectDetailSerializer(project)
            return Response({
                'message': f'Proyecto avanzado a fase: {project.get_current_phase_display()}',
                'project': serializer.data
            })
        else:
            return Response(
                {'message': 'El proyecto ya está en la fase final'},
                status=status.HTTP_400_BAD_REQUEST
            )
    
    @action(detail=False, methods=['get'])
    def stats(self, request):
        """
        Estadísticas generales de proyectos
        GET /api/projects/stats/
        """
        total_projects = Project.objects.count()
        by_status = {}
        by_phase = {}
        
        for status_choice in Project.STATUS_CHOICES:
            count = Project.objects.filter(status=status_choice[0]).count()
            by_status[status_choice[0]] = {
                'count': count,
                'label': status_choice[1]
            }
        
        for phase_choice in Project.PHASE_CHOICES:
            count = Project.objects.filter(current_phase=phase_choice[0]).count()
            by_phase[phase_choice[0]] = {
                'count': count,
                'label': phase_choice[1]
            }
        
        return Response({
            'total_projects': total_projects,
            'by_status': by_status,
            'by_phase': by_phase,
            'overdue_projects': Project.objects.filter(
                end_date__lt=timezone.now().date(),
                status__in=['planning', 'in_progress']
            ).count()
        })