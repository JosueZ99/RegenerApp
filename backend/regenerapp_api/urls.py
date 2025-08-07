"""
URL configuration for regenerapp_api project.
"""
from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
from rest_framework import permissions
from drf_spectacular.views import SpectacularAPIView, SpectacularSwaggerView, SpectacularRedocView

urlpatterns = [
    # Admin
    path('admin/', admin.site.urls),
    
    # API Documentation
    path('api/schema/', SpectacularAPIView.as_view(), name='schema'),
    path('api/docs/', SpectacularSwaggerView.as_view(url_name='schema'), name='swagger-ui'),
    path('api/redoc/', SpectacularRedocView.as_view(url_name='schema'), name='redoc'),
    
    # API v1 endpoints (con namespaces únicos)
    path('api/v1/', include([
        path('', include('apps.projects.urls', namespace='projects_v1')),
        path('', include('apps.materials.urls', namespace='materials_v1')),
        path('', include('apps.suppliers.urls', namespace='suppliers_v1')),
        path('', include('apps.budgets.urls', namespace='budgets_v1')),
        path('', include('apps.calculations.urls', namespace='calculations_v1')),
    ])),
    
    # API root (con namespaces únicos para compatibilidad)
    path('api/', include([
        path('', include('apps.projects.urls', namespace='projects')),
        path('', include('apps.materials.urls', namespace='materials')),
        path('', include('apps.suppliers.urls', namespace='suppliers')),
        path('', include('apps.budgets.urls', namespace='budgets')),
        path('', include('apps.calculations.urls', namespace='calculations')),
    ])),
]

# Servir archivos media en desarrollo
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)