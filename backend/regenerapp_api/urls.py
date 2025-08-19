"""
URL configuration for regenerapp_api project.
Configuración corregida para RegenerApp APIs
"""
from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    # Panel de administración
    path('admin/', admin.site.urls),
    
    # APIs REST con prefijos específicos por módulo
    path('api/projects/', include('apps.projects.urls', namespace='projects')),
    path('api/materials/', include('apps.materials.urls', namespace='materials')),
    path('api/suppliers/', include('apps.suppliers.urls', namespace='suppliers')),
    path('api/budgets/', include('apps.budgets.urls', namespace='budgets')),
    path('api/calculations/', include('apps.calculations.urls', namespace='calculations')),
    
    # Documentación API (opcional)
    # path('api/schema/', SpectacularAPIView.as_view(), name='schema'),
    # path('api/docs/', SpectacularSwaggerView.as_view(url_name='schema'), name='swagger-ui'),
]

# Servir archivos media y estáticos en desarrollo
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)