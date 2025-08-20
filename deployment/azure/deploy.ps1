# ===================================
# SCRIPT DE DESPLIEGUE AZURE - RegenerApp
# Automatizacion para Azure Container Instances
# ===================================

param(
    [Parameter(Mandatory=$true)]
    [string]$ResourceGroupName,
    
    [Parameter(Mandatory=$false)]
    [string]$Location = "East US",
    
    [Parameter(Mandatory=$false)]
    [string]$ContainerInstanceName = "regenerapp-container",
    
    [Parameter(Mandatory=$false)]
    [string]$RegistryName = "regenerappregistry"
)

Write-Host "Iniciando despliegue de RegenerApp en Azure" -ForegroundColor Green

# ==========================================
# CONFIGURACION
# ==========================================
$ErrorActionPreference = "Stop"
$imageTag = "latest"
$backendImage = "$RegistryName.azurecr.io/regenerapp-backend:$imageTag"

# ==========================================
# 1. VERIFICAR AZURE CLI
# ==========================================
Write-Host "Verificando Azure CLI..." -ForegroundColor Yellow

try {
    az --version | Out-Null
    Write-Host "Azure CLI instalado correctamente" -ForegroundColor Green
} catch {
    Write-Error "Azure CLI no esta instalado. Instalalo desde: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
    exit 1
}

# ==========================================
# 2. LOGIN A AZURE
# ==========================================
Write-Host "Verificando autenticacion Azure..." -ForegroundColor Yellow

$accountInfo = az account show 2>$null | ConvertFrom-Json
if (-not $accountInfo) {
    Write-Host "Iniciando sesion en Azure..." -ForegroundColor Yellow
    az login
} else {
    Write-Host "Ya autenticado como: $($accountInfo.user.name)" -ForegroundColor Green
}

# ==========================================
# 3. CREAR RESOURCE GROUP
# ==========================================
Write-Host "Creando Resource Group..." -ForegroundColor Yellow

$rgExists = az group exists --name $ResourceGroupName | ConvertFrom-Json
if (-not $rgExists) {
    Write-Host "Creando Resource Group: $ResourceGroupName" -ForegroundColor Yellow
    az group create --name $ResourceGroupName --location $Location
    Write-Host "Resource Group creado" -ForegroundColor Green
} else {
    Write-Host "Resource Group ya existe" -ForegroundColor Green
}

# ==========================================
# 4. CREAR AZURE CONTAINER REGISTRY
# ==========================================
Write-Host "Configurando Azure Container Registry..." -ForegroundColor Yellow

$acrExists = az acr show --name $RegistryName --resource-group $ResourceGroupName 2>$null
if (-not $acrExists) {
    Write-Host "Creando Azure Container Registry: $RegistryName" -ForegroundColor Yellow
    az acr create --resource-group $ResourceGroupName --name $RegistryName --sku Basic --admin-enabled true
    Write-Host "Azure Container Registry creado" -ForegroundColor Green
} else {
    Write-Host "Azure Container Registry ya existe" -ForegroundColor Green
}

# ==========================================
# 5. BUILD Y PUSH IMAGEN DOCKER
# ==========================================
Write-Host "Construyendo y subiendo imagen Docker..." -ForegroundColor Yellow

# Cambiar al directorio del proyecto
Set-Location -Path "..\.."

# Build de la imagen
Write-Host "Construyendo imagen Docker..." -ForegroundColor Yellow
az acr build --registry $RegistryName --image "regenerapp-backend:$imageTag" "./backend"

Write-Host "Imagen construida y subida a ACR" -ForegroundColor Green

# ==========================================
# 6. OBTENER CREDENCIALES ACR
# ==========================================
Write-Host "Obteniendo credenciales del registry..." -ForegroundColor Yellow

$acrCredentials = az acr credential show --name $RegistryName | ConvertFrom-Json
$acrServer = az acr show --name $RegistryName --resource-group $ResourceGroupName --query "loginServer" --output tsv

# ==========================================
# 7. CREAR AZURE CONTAINER INSTANCE
# ==========================================
Write-Host "Desplegando Container Instance..." -ForegroundColor Yellow

# Crear archivo YAML temporal para Container Instance
$yamlContent = @"
apiVersion: 2019-12-01
location: $Location
name: $ContainerInstanceName
properties:
  containers:
  - name: regenerapp-backend
    properties:
      image: $backendImage
      resources:
        requests:
          cpu: 1
          memoryInGb: 1
      ports:
      - port: 8000
        protocol: TCP
      environmentVariables:
      - name: DB_HOST
        value: regenerapp-db
      - name: DB_NAME
        value: regenerapp_db
      - name: DB_USER
        value: postgres
      - name: DB_PASSWORD
        secureValue: RegenerApp2025!
      - name: SECRET_KEY
        secureValue: django-super-secret-key-regenerapp-production-2025
      - name: DEBUG
        value: "False"
      - name: ALLOWED_HOSTS
        value: "*"
  - name: regenerapp-db
    properties:
      image: postgres:13-alpine
      resources:
        requests:
          cpu: 0.5
          memoryInGb: 1
      ports:
      - port: 5432
        protocol: TCP
      environmentVariables:
      - name: POSTGRES_DB
        value: regenerapp_db
      - name: POSTGRES_USER
        value: postgres
      - name: POSTGRES_PASSWORD
        secureValue: RegenerApp2025!
  osType: Linux
  ipAddress:
    type: Public
    ports:
    - protocol: TCP
      port: 8000
    - protocol: TCP
      port: 80
    dnsNameLabel: regenerapp-$((Get-Random -Minimum 1000 -Maximum 9999))
  imageRegistryCredentials:
  - server: $acrServer
    username: $($acrCredentials.username)
    password: $($acrCredentials.passwords[0].value)
tags: {}
type: Microsoft.ContainerInstance/containerGroups
"@

# Guardar YAML temporal
$yamlFile = "container-instance.yaml"
$yamlContent | Out-File -FilePath $yamlFile -Encoding UTF8

try {
    # Desplegar Container Instance
    az container create --resource-group $ResourceGroupName --file $yamlFile
    
    Write-Host "Container Instance desplegado exitosamente!" -ForegroundColor Green
    
    # Obtener información del despliegue
    $containerInfo = az container show --resource-group $ResourceGroupName --name $ContainerInstanceName | ConvertFrom-Json
    $publicIP = $containerInfo.ipAddress.ip
    $fqdn = $containerInfo.ipAddress.fqdn
    
    Write-Host "" -ForegroundColor White
    Write-Host "DESPLIEGUE COMPLETADO!" -ForegroundColor Green
    Write-Host "===========================================" -ForegroundColor White
    Write-Host "URL de la API: http://$fqdn:8000" -ForegroundColor Cyan
    Write-Host "Admin Panel: http://$fqdn:8000/admin" -ForegroundColor Cyan
    Write-Host "IP Publica: $publicIP" -ForegroundColor Cyan
    Write-Host "===========================================" -ForegroundColor White
    Write-Host ""
    Write-Host "IMPORTANTE: Actualiza tu app Android con esta URL" -ForegroundColor Yellow
    
} finally {
    # Limpiar archivo temporal
    if (Test-Path $yamlFile) {
        Remove-Item $yamlFile
    }
}

Write-Host "Para actualizar tu app Android:" -ForegroundColor Yellow
Write-Host "   1. Abre ApiClient.java" -ForegroundColor White
Write-Host "   2. Cambia BASE_URL a: http://$fqdn:8000/" -ForegroundColor White
Write-Host "   3. Recompila y reinstala la APK" -ForegroundColor White