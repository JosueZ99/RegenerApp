# RegenerApp - Sistema de Gestión de Proyectos Arquitectónicos

![Version](https://img.shields.io/badge/version-2.0-blue.svg)
![Android](https://img.shields.io/badge/Android-API%2030+-green.svg)
![Django](https://img.shields.io/badge/Django-REST%20Framework-red.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)

## 🏗️ Descripción

RegenerApp es una aplicación móvil Android nativa diseñada para optimizar la gestión de proyectos arquitectónicos en **Regenerar Estudio**, especializado en iluminación arquitectónica.

### ✨ Características Principales

- **Selección obligatoria de proyectos** como sistema de login
- **Calculadoras especializadas** para construcción e iluminación
- **Sistema dual de presupuestos** (inicial vs gastos reales)
- **Dashboard centralizado** con métricas del proyecto
- **Integración Google Drive** para gestión de archivos
- **Control de tres fases**: Diseño → Compra → Instalación

## 🏛️ Arquitectura del Sistema

```
📱 Android App (Frontend)
    ↕️ Retrofit2 + JSON (HTTPS)
🌐 Django REST API (Backend)
    ↕️ Django ORM
🗄️ PostgreSQL (Base de Datos)
```

## 📁 Estructura del Proyecto

```
RegenerApp/
├── android/                    # 📱 Aplicación Android nativa
│   ├── app/                   # Código fuente de la app
│   ├── build.gradle.kts       # Configuración de build
│   └── ...                    # Archivos de configuración Android
│
├── backend/                   # 🌐 API REST con Django
│   ├── regenerapp_api/        # Proyecto Django principal
│   ├── requirements.txt       # Dependencias Python
│   ├── Dockerfile            # Imagen Docker del backend
│   └── manage.py             # Comando principal Django
│
├── database/                  # 🗄️ Esquemas y migraciones
│   ├── schema.sql            # Esquema inicial
│   └── fixtures/             # Datos de prueba
│
├── deployment/                # 🚀 Configuración de despliegue
│   ├── azure/                # Configuraciones Azure
│   └── scripts/              # Scripts de automatización
│
├── docs/                      # 📖 Documentación
│   ├── api/                  # Documentación de APIs
│   └── user-manual/          # Manual de usuario
│
├── docker-compose.yml         # 🐳 Orquestación de contenedores
└── README.md                  # Este archivo
```

## 🚀 Configuración para Desarrollo

### Prerrequisitos

- **Android Studio** Arctic Fox o superior
- **Docker** y **Docker Compose**
- **Python 3.9+** (para desarrollo local del backend)
- **PostgreSQL 13+** (o usar Docker)

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/RegenerApp.git
cd RegenerApp
```

### 2. Configurar Backend (Django)

```bash
cd backend
python -m venv venv
source venv/bin/activate  # Linux/Mac
# venv\Scripts\activate   # Windows

pip install -r requirements.txt
python manage.py migrate
python manage.py loaddata fixtures/initial_data.json
python manage.py runserver 0.0.0.0:8000
```

### 3. Configurar Frontend (Android)

```bash
cd android
# Abrir en Android Studio
# O desde línea de comandos:
./gradlew assembleDebug
```

### 4. Desarrollo con Docker Compose

```bash
# Levantar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

## 🌐 URLs del Sistema

- **API Backend**: http://localhost:8000/api/
- **Admin Django**: http://localhost:8000/admin/
- **Documentación API**: http://localhost:8000/api/docs/
- **App Android**: Instalación directa en dispositivo

## 🎯 Tech Stack

### Frontend (Android)
- **Lenguaje**: Java
- **UI**: Material Design 3
- **Arquitectura**: MVVM
- **Networking**: Retrofit2 + Gson
- **Navegación**: Navigation Component

### Backend (Django)
- **Framework**: Django REST Framework
- **Base de Datos**: PostgreSQL
- **Autenticación**: No requerida (app compartida)
- **APIs**: RESTful con paginación

### DevOps
- **Contenerización**: Docker + Docker Compose
- **Nube**: Azure Container Instances
- **Proxy**: Nginx

## 👥 Equipo de Desarrollo

**Desarrollador**: Joab Josue Zambrano Zambrano  
**Email**: jjzambranoz@puce.edu.ec  
**Cliente**: Regenerar Estudio - Arq. Juan Fernando Terán  

**Institución**: Pontificia Universidad Católica del Ecuador  
**Programa**: Tecnología Superior en Desarrollo de Software  

## 📜 Licencia

Proyecto desarrollado para Regenerar Estudio.  
Todos los derechos reservados © 2025
