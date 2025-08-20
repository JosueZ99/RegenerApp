from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import SupplierViewSet, SupplierPriceViewSet

app_name = 'suppliers'

router = DefaultRouter()
router.register(r'suppliers', SupplierViewSet, basename='supplier')
router.register(r'prices', SupplierPriceViewSet, basename='supplierprice')

urlpatterns = [
    path('', include(router.urls)),
]