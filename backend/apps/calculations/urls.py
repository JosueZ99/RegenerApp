from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import CalculationTypeViewSet, CalculationViewSet

app_name = 'calculations'

router = DefaultRouter()
router.register(r'types', CalculationTypeViewSet, basename='calculationtype')
router.register(r'calculations', CalculationViewSet, basename='calculation')

urlpatterns = [
    # Rutas del router básico
    path('', include(router.urls)),
    
    # Rutas específicas para calculadoras - CORREGIDAS
    path('calculate/paint/', CalculationViewSet.as_view({'post': 'calculate_paint'}), name='calculate-paint'),
    path('calculate/gypsum/', CalculationViewSet.as_view({'post': 'calculate_gypsum'}), name='calculate-gypsum'),
    path('calculate/empaste/', CalculationViewSet.as_view({'post': 'calculate_empaste'}), name='calculate-empaste'),
    path('calculate/led/', CalculationViewSet.as_view({'post': 'calculate_led_strip'}), name='calculate-led'),
    path('calculate/profiles/', CalculationViewSet.as_view({'post': 'calculate_profiles'}), name='calculate-profiles'),
    path('calculate/cables/', CalculationViewSet.as_view({'post': 'calculate_cable'}), name='calculate-cables'),
]