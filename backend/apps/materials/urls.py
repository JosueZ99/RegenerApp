from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import MaterialCategoryViewSet, MaterialViewSet, MaterialSpecificationViewSet

app_name = 'materials'

router = DefaultRouter()
router.register(r'categories', MaterialCategoryViewSet, basename='materialcategory')
router.register(r'materials', MaterialViewSet, basename='material')
router.register(r'specifications', MaterialSpecificationViewSet, basename='materialspecification')

urlpatterns = [
    path('', include(router.urls)),
]