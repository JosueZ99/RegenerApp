from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CalculationTypeViewSet, CalculationViewSet

app_name = 'calculations'

router = DefaultRouter()
router.register(r'types', CalculationTypeViewSet, basename='calculationtype')
router.register(r'calculations', CalculationViewSet, basename='calculation')

urlpatterns = [
    path('', include(router.urls)),
]