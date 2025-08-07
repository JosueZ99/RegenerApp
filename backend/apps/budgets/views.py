from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.filters import SearchFilter, OrderingFilter
from django_filters.rest_framework import DjangoFilterBackend
from django.db.models import Sum, Count, Q
from django.shortcuts import get_object_or_404
from .models import BudgetItem, RealExpense, ProjectFinancialSummary
from .serializers import (
    BudgetItemListSerializer, BudgetItemDetailSerializer,
    BudgetItemCreateUpdateSerializer, RealExpenseListSerializer,
    RealExpenseDetailSerializer, RealExpenseCreateUpdateSerializer,
    ProjectFinancialSummarySerializer, BudgetSummarySerializer,
    CopyBudgetToExpenseSerializer
)
from apps.projects.models import Project

class BudgetItemViewSet(viewsets.ModelViewSet):
    """
    ViewSet para items del presupuesto inicial
    """
    queryset = BudgetItem.objects.all()
    filter_backends = [SearchFilter, OrderingFilter, DjangoFilterBackend]
    search_fields = ['description', 'spaces', 'notes']
    ordering_fields = ['description', 'category', 'total_price', 'created_at']
    ordering = ['category', 'description']
    filterset_fields = ['project', 'category', 'supplier', 'material', 'created_from_calculation']
    
    def get_serializer_class(self):
        """Retorna serializer según la acción"""
        if self.action == 'list':
            return BudgetItemListSerializer
        elif self.action in ['create', 'update', 'partial_update']:
            return BudgetItemCreateUpdateSerializer
        return BudgetItemDetailSerializer
    
    def get_queryset(self):
        """Personalizar queryset según filtros"""
        queryset = BudgetItem.objects.all()
        
        # Filtro por proyecto seleccionado
        selected_project_only = self.request.query_params.get('selected_project_only')
        if selected_project_only and selected_project_only.lower() == 'true':
            try:
                selected_project = Project.objects.get(is_selected=True)
                queryset = queryset.filter(project=selected_project)
            except Project.DoesNotExist:
                queryset = queryset.none()
        
        # Filtro por rango de precios
        min_price = self.request.query_params.get('min_price')
        max_price = self.request.query_params.get('max_price')
        
        if min_price:
            try:
                queryset = queryset.filter(total_price__gte=float(min_price))
            except ValueError:
                pass
        
        if max_price:
            try:
                queryset = queryset.filter(total_price__lte=float(max_price))
            except ValueError:
                pass
        
        return queryset
    
    def perform_create(self, serializer):
        """Acciones adicionales al crear item"""
        budget_item = serializer.save()
        
        # Actualizar resumen financiero del proyecto
        self._update_financial_summary(budget_item.project)
    
    def perform_update(self, serializer):
        """Acciones adicionales al actualizar item"""
        budget_item = serializer.save()
        self._update_financial_summary(budget_item.project)
    
    def perform_destroy(self, instance):
        """Acciones adicionales al eliminar item"""
        project = instance.project
        instance.delete()
        self._update_financial_summary(project)
    
    def _update_financial_summary(self, project):
        """Actualizar resumen financiero del proyecto"""
        summary, created = ProjectFinancialSummary.objects.get_or_create(project=project)
        summary.update_summary()
    
    @action(detail=True, methods=['post'])
    def copy_to_expense(self, request, pk=None):
        """
        Copiar item del presupuesto a gastos reales
        POST /api/budget-items/{id}/copy_to_expense/
        """
        budget_item = self.get_object()
        
        # Datos adicionales para el gasto
        purchase_date = request.data.get('purchase_date')
        supplier_override = request.data.get('supplier_id')
        discount_percentage = request.data.get('discount_percentage', 0)
        discount_amount = request.data.get('discount_amount', 0)
        notes_override = request.data.get('notes', '')
        
        if not purchase_date:
            return Response(
                {'error': 'Se requiere purchase_date'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Crear gasto real basado en el item del presupuesto
        expense_data = {
            'project': budget_item.project.id,
            'budget_item': budget_item.id,
            'description': budget_item.description,
            'category': budget_item.category,
            'spaces': budget_item.spaces,
            'quantity': budget_item.quantity,
            'unit': budget_item.unit,
            'unit_price': budget_item.unit_price,
            'discount_percentage': discount_percentage,
            'discount_amount': discount_amount,
            'purchase_date': purchase_date,
            'material': budget_item.material.id if budget_item.material else None,
            'supplier': supplier_override or (budget_item.supplier.id if budget_item.supplier else None),
            'notes': notes_override or f"Copiado de presupuesto: {budget_item.description}"
        }
        
        serializer = RealExpenseCreateUpdateSerializer(data=expense_data)
        if serializer.is_valid():
            expense = serializer.save()
            return Response(
                RealExpenseDetailSerializer(expense).data,
                status=status.HTTP_201_CREATED
            )
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=False, methods=['get'])
    def summary_by_category(self, request):
        """
        Resumen del presupuesto por categorías
        GET /api/budget-items/summary_by_category/?project_id=1
        """
        project_id = request.query_params.get('project_id')
        if not project_id:
            return Response(
                {'error': 'Se requiere project_id'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        queryset = self.get_queryset().filter(project_id=project_id)
        
        summary = []
        for category, category_display in BudgetItem.ITEM_CATEGORIES:
            items = queryset.filter(category=category)
            total_amount = items.aggregate(Sum('total_price'))['total_price__sum'] or 0
            items_count = items.count()
            
            summary.append({
                'category': category,
                'category_display': category_display,
                'total_amount': total_amount,
                'items_count': items_count,
                'average_item_cost': total_amount / items_count if items_count > 0 else 0
            })
        
        # Total general
        total_budget = sum(item['total_amount'] for item in summary)
        
        return Response({
            'project_id': project_id,
            'by_category': summary,
            'total_budget': total_budget,
            'total_items': sum(item['items_count'] for item in summary)
        })

class RealExpenseViewSet(viewsets.ModelViewSet):
    """
    ViewSet para gastos reales
    """
    queryset = RealExpense.objects.all()
    filter_backends = [SearchFilter, OrderingFilter, DjangoFilterBackend]
    search_fields = ['description', 'invoice_number', 'notes']
    ordering_fields = ['description', 'total_price', 'purchase_date', 'created_at']
    ordering = ['-purchase_date']
    filterset_fields = ['project', 'category', 'supplier', 'material', 'payment_method']
    
    def get_serializer_class(self):
        """Retorna serializer según la acción"""
        if self.action == 'list':
            return RealExpenseListSerializer
        elif self.action in ['create', 'update', 'partial_update']:
            return RealExpenseCreateUpdateSerializer
        return RealExpenseDetailSerializer
    
    def get_queryset(self):
        """Personalizar queryset según filtros"""
        queryset = RealExpense.objects.all()
        
        # Filtro por proyecto seleccionado
        selected_project_only = self.request.query_params.get('selected_project_only')
        if selected_project_only and selected_project_only.lower() == 'true':
            try:
                selected_project = Project.objects.get(is_selected=True)
                queryset = queryset.filter(project=selected_project)
            except Project.DoesNotExist:
                queryset = queryset.none()
        
        # Filtro por rango de fechas
        date_from = self.request.query_params.get('date_from')
        date_to = self.request.query_params.get('date_to')
        
        if date_from:
            queryset = queryset.filter(purchase_date__gte=date_from)
        if date_to:
            queryset = queryset.filter(purchase_date__lte=date_to)
        
        return queryset
    
    def perform_create(self, serializer):
        """Acciones adicionales al crear gasto"""
        expense = serializer.save()
        self._update_financial_summary(expense.project)
    
    def perform_update(self, serializer):
        """Acciones adicionales al actualizar gasto"""
        expense = serializer.save()
        self._update_financial_summary(expense.project)
    
    def perform_destroy(self, instance):
        """Acciones adicionales al eliminar gasto"""
        project = instance.project
        instance.delete()
        self._update_financial_summary(project)
    
    def _update_financial_summary(self, project):
        """Actualizar resumen financiero del proyecto"""
        summary, created = ProjectFinancialSummary.objects.get_or_create(project=project)
        summary.update_summary()
    
    @action(detail=False, methods=['get'])
    def summary_by_category(self, request):
        """
        Resumen de gastos reales por categorías
        GET /api/real-expenses/summary_by_category/?project_id=1
        """
        project_id = request.query_params.get('project_id')
        if not project_id:
            return Response(
                {'error': 'Se requiere project_id'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        queryset = self.get_queryset().filter(project_id=project_id)
        
        summary = []
        for category, category_display in RealExpense.ITEM_CATEGORIES:
            expenses = queryset.filter(category=category)
            total_spent = expenses.aggregate(Sum('total_price'))['total_price__sum'] or 0
            expenses_count = expenses.count()
            total_discount = sum(expense.total_discount for expense in expenses)
            
            summary.append({
                'category': category,
                'category_display': category_display,
                'total_spent': total_spent,
                'expenses_count': expenses_count,
                'total_discount': total_discount,
                'average_expense': total_spent / expenses_count if expenses_count > 0 else 0
            })
        
        # Total general
        total_expenses = sum(item['total_spent'] for item in summary)
        
        return Response({
            'project_id': project_id,
            'by_category': summary,
            'total_expenses': total_expenses,
            'total_expense_items': sum(item['expenses_count'] for item in summary)
        })

class ProjectFinancialSummaryViewSet(viewsets.ReadOnlyModelViewSet):
    """
    ViewSet de solo lectura para resúmenes financieros
    """
    queryset = ProjectFinancialSummary.objects.all()
    serializer_class = ProjectFinancialSummarySerializer
    filter_backends = [DjangoFilterBackend]
    filterset_fields = ['project']
    
    @action(detail=False, methods=['get'])
    def for_selected_project(self, request):
        """
        Resumen financiero del proyecto seleccionado
        GET /api/financial-summary/for_selected_project/
        """
        try:
            selected_project = Project.objects.get(is_selected=True)
            summary, created = ProjectFinancialSummary.objects.get_or_create(
                project=selected_project
            )
            
            if created or request.query_params.get('refresh') == 'true':
                summary.update_summary()
            
            serializer = ProjectFinancialSummarySerializer(summary)
            return Response(serializer.data)
            
        except Project.DoesNotExist:
            return Response(
                {'error': 'No hay proyecto seleccionado'},
                status=status.HTTP_404_NOT_FOUND
            )
    
    @action(detail=True, methods=['post'])
    def refresh(self, request, pk=None):
        """
        Actualizar resumen financiero
        POST /api/financial-summary/{id}/refresh/
        """
        summary = self.get_object()
        summary.update_summary()
        
        serializer = ProjectFinancialSummarySerializer(summary)
        return Response({
            'message': 'Resumen financiero actualizado',
            'summary': serializer.data
        })

@action(detail=False, methods=['post'])
def copy_multiple_to_expenses(request):
    """
    Copiar múltiples items del presupuesto a gastos reales
    POST /api/budget-items/copy_multiple_to_expenses/
    """
    serializer = CopyBudgetToExpenseSerializer(data=request.data)
    if not serializer.is_valid():
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    budget_item_ids = serializer.validated_data['budget_item_ids']
    purchase_date = serializer.validated_data['purchase_date']
    default_supplier = serializer.validated_data.get('default_supplier')
    
    created_expenses = []
    errors = []
    
    for item_id in budget_item_ids:
        try:
            budget_item = BudgetItem.objects.get(id=item_id)
            
            expense_data = {
                'project': budget_item.project.id,
                'budget_item': budget_item.id,
                'description': budget_item.description,
                'category': budget_item.category,
                'spaces': budget_item.spaces,
                'quantity': budget_item.quantity,
                'unit': budget_item.unit,
                'unit_price': budget_item.unit_price,
                'purchase_date': purchase_date,
                'material': budget_item.material.id if budget_item.material else None,
                'supplier': default_supplier or (budget_item.supplier.id if budget_item.supplier else None),
                'notes': f"Copiado automáticamente de presupuesto"
            }
            
            expense_serializer = RealExpenseCreateUpdateSerializer(data=expense_data)
            if expense_serializer.is_valid():
                expense = expense_serializer.save()
                created_expenses.append(expense.id)
            else:
                errors.append({
                    'budget_item_id': item_id,
                    'errors': expense_serializer.errors
                })
                
        except BudgetItem.DoesNotExist:
            errors.append({
                'budget_item_id': item_id,
                'error': 'Item no encontrado'
            })
    
    return Response({
        'created_expenses_count': len(created_expenses),
        'created_expense_ids': created_expenses,
        'errors_count': len(errors),
        'errors': errors
    })