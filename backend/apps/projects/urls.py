from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import ProjectViewSet

app_name = 'projects'

router = DefaultRouter()
router.register(r'projects', ProjectViewSet, basename='project')

urlpatterns = [
    path('', include(router.urls)),
]

# URLs adicionales sin router si las necesitas
# urlpatterns += [
#     path('custom-endpoint/', custom_view, name='custom-endpoint'),
# ]