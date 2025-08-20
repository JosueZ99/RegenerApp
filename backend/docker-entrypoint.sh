#!/bin/bash
# ===================================
# DOCKER ENTRYPOINT - RegenerApp Backend
# Script de inicialización para contenedor Django
# ===================================

set -e

echo "🚀 Iniciando RegenerApp Backend..."

# Función para esperar PostgreSQL
wait_for_postgres() {
    echo "⏳ Esperando PostgreSQL en $DB_HOST:$DB_PORT..."
    
    while ! nc -z $DB_HOST $DB_PORT; do
        echo "PostgreSQL no está listo - esperando..."
        sleep 2
    done
    
    echo "✅ PostgreSQL está listo!"
}

# Función para ejecutar migraciones
run_migrations() {
    echo "📦 Ejecutando migraciones de Django..."
    python manage.py makemigrations --noinput
    python manage.py migrate --noinput
    echo "✅ Migraciones completadas!"
}

# Función para recolectar archivos estáticos
collect_static() {
    echo "📁 Recolectando archivos estáticos..."
    python manage.py collectstatic --noinput --clear
    echo "✅ Archivos estáticos recolectados!"
}

# Función para crear superusuario (solo si no existe)
create_superuser() {
    echo "👤 Verificando superusuario..."
    python manage.py shell << EOF
from django.contrib.auth import get_user_model
User = get_user_model()

if not User.objects.filter(username='admin').exists():
    User.objects.create_superuser(
        username='admin',
        email='admin@regenerarestudio.com',
        password='regenerapp2025'
    )
    print("✅ Superusuario 'admin' creado!")
else:
    print("ℹ️  Superusuario ya existe")
EOF
}

# Función para cargar datos iniciales
load_initial_data() {
    echo "🗃️  Cargando datos iniciales..."
    python manage.py setup_initial_data
    echo "✅ Datos iniciales cargados!"
}

# Función principal
main() {
    # Esperar base de datos
    wait_for_postgres
    
    # Ejecutar migraciones
    run_migrations
    
    # Recolectar archivos estáticos
    collect_static
    
    # Crear superusuario
    create_superuser
    
    # Cargar datos iniciales (solo en primera ejecución)
    if [ "$LOAD_INITIAL_DATA" = "true" ]; then
        load_initial_data
    fi
    
    echo "🎉 RegenerApp Backend inicializado correctamente!"
    echo "🌐 Servidor disponible en: http://0.0.0.0:8000"
    
    # Ejecutar comando pasado como argumentos
    exec "$@"
}

# Ejecutar función principal
main "$@"