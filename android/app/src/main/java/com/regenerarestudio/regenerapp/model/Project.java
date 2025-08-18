package com.regenerarestudio.regenerapp.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Clase modelo unificada para Proyectos
 * Compatible con ambas versiones existentes y preparada para APIs REST
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/model/Project.java
 */
public class Project {
    // Campos de la versión simple (model/Project.java original)
    private long id;
    private String name;
    private String client;
    private String location;
    private String type;
    private String status;
    private boolean isSelected;

    // Campos adicionales de la versión completa (ui/proyectos/Project.java)
    private String startDate;
    private String endDate;
    private String description;
    private String driveUrl;
    private Date createdAt;
    private Date updatedAt;

    // Campos adicionales para compatibilidad con backend Django
    private String projectType;
    private String currentPhase;
    private String actualEndDate;
    private double initialBudget;

    // Constructor simple (compatible con ProyectosViewModel)
    public Project(int id, String name, String client, String location, String type, String status) {
        this.id = id;
        this.name = name;
        this.client = client;
        this.location = location;
        this.type = type;
        this.projectType = type; // Sincronizar type y projectType
        this.status = status;
        this.isSelected = false;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Constructor completo (compatible con ProjectSelectionActivity)
    public Project(long id, String name, String client, String location, String status,
                   String startDate, String endDate, String description) {
        this.id = id;
        this.name = name;
        this.client = client;
        this.location = location;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.isSelected = false;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Constructor completo con URLs y fechas (versión extendida)
    public Project(long id, String name, String client, String location, String status,
                   String startDate, String endDate, String description, String driveUrl,
                   Date createdAt, Date updatedAt) {
        this(id, name, client, location, status, startDate, endDate, description);
        this.driveUrl = driveUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor para APIs REST (futuro)
    public Project(long id, String name, String client, String location, String projectType,
                   String status, String currentPhase, String startDate, String endDate,
                   String description, String driveUrl, double initialBudget) {
        this.id = id;
        this.name = name;
        this.client = client;
        this.location = location;
        this.type = projectType;
        this.projectType = projectType;
        this.status = status;
        this.currentPhase = currentPhase;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.driveUrl = driveUrl;
        this.initialBudget = initialBudget;
        this.isSelected = false;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters - Versión simple
    public long getId() { return id; }
    public String getName() { return name; }
    public String getClient() { return client; }
    public String getLocation() { return location; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public boolean isSelected() { return isSelected; }

    // Getters - Versión completa
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getDescription() { return description; }
    public String getDriveUrl() { return driveUrl; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    // Getters - Campos adicionales para APIs
    public String getProjectType() { return projectType != null ? projectType : type; }
    public String getCurrentPhase() { return currentPhase; }
    public String getActualEndDate() { return actualEndDate; }
    public double getInitialBudget() { return initialBudget; }

    // Setters
    public void setSelected(boolean selected) { this.isSelected = selected; }
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setClient(String client) { this.client = client; }
    public void setLocation(String location) { this.location = location; }
    public void setType(String type) {
        this.type = type;
        this.projectType = type; // Mantener sincronizado
    }
    public void setStatus(String status) { this.status = status; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setDescription(String description) { this.description = description; }
    public void setDriveUrl(String driveUrl) { this.driveUrl = driveUrl; }
    public void setProjectType(String projectType) {
        this.projectType = projectType;
        this.type = projectType; // Mantener sincronizado
    }
    public void setCurrentPhase(String currentPhase) { this.currentPhase = currentPhase; }
    public void setActualEndDate(String actualEndDate) { this.actualEndDate = actualEndDate; }
    public void setInitialBudget(double initialBudget) { this.initialBudget = initialBudget; }

    // Métodos de utilidad
    public String getFormattedCreatedDate() {
        if (createdAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(createdAt);
        }
        return "";
    }

    public String getFormattedStartDate() {
        if (startDate == null || startDate.isEmpty()) return "No definida";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "EC"));
            Date date = inputFormat.parse(startDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            return startDate; // Retornar tal como está si no se puede parsear
        }
    }

    public String getFormattedEndDate() {
        if (endDate == null || endDate.isEmpty()) return "No definida";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "EC"));
            Date date = inputFormat.parse(endDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            return endDate; // Retornar tal como está si no se puede parsear
        }
    }

    public boolean hasGoogleDriveLink() {
        return driveUrl != null && !driveUrl.trim().isEmpty();
    }

    // Métodos para ProjectAdapter (los que faltaban)
    public String getStatusDisplayName() {
        switch (status.toLowerCase()) {
            case "diseño":
            case "design":
                return "En Diseño";
            case "compra":
            case "purchase":
                return "En Compra";
            case "instalacion":
            case "installation":
                return "En Instalación";
            case "completado":
            case "completed":
                return "Completado";
            case "pausado":
            case "paused":
                return "Pausado";
            default:
                return "Estado Desconocido";
        }
    }

    public int getStatusColor() {
        switch (status.toLowerCase()) {
            case "diseño":
            case "design":
                return com.regenerarestudio.regenerapp.R.color.status_design;
            case "compra":
            case "purchase":
                return com.regenerarestudio.regenerapp.R.color.status_purchase;
            case "instalacion":
            case "installation":
                return com.regenerarestudio.regenerapp.R.color.status_installation;
            case "completado":
            case "completed":
                return com.regenerarestudio.regenerapp.R.color.status_completed;
            case "pausado":
            case "paused":
                return com.regenerarestudio.regenerapp.R.color.status_paused;
            default:
                return com.regenerarestudio.regenerapp.R.color.gray_500;
        }
    }

    public boolean isActive() {
        return !status.equalsIgnoreCase("completado") && !status.equalsIgnoreCase("pausado");
    }

    public double getProgressPercentage() {
        // Calcular progreso basado en el estado
        switch (status.toLowerCase()) {
            case "diseño":
            case "design":
                return 25.0;
            case "compra":
            case "purchase":
                return 60.0;
            case "instalacion":
            case "installation":
                return 90.0;
            case "completado":
            case "completed":
                return 100.0;
            case "pausado":
            case "paused":
                return 0.0;
            default:
                return 0.0;
        }
    }

    // Método para compatibilidad con el toString anterior
    @Override
    public String toString() {
        return name;
    }

    // Método equals para comparaciones
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Project project = (Project) obj;
        return id == project.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}