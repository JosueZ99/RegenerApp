from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import BudgetItemViewSet, RealExpenseViewSet, ProjectFinancialSummaryViewSet, copy_multiple_to_expenses

app_name = 'budgets'

router = DefaultRouter()
router.register(r'budget-items', BudgetItemViewSet, basename='budgetitem')
router.register(r'real-expenses', RealExpenseViewSet, basename='realexpense')
router.register(r'financial-summary', ProjectFinancialSummaryViewSet, basename='projectfinancialsummary')

urlpatterns = [
    path('', include(router.urls)),
    path('budget-items/copy-multiple-to-expenses/', copy_multiple_to_expenses, name='copy-multiple-to-expenses'),
]