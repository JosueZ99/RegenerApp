package com.regenerarestudio.regenerapp.ui.proyectos;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Clase modelo para Proyectos
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/proyectos/Project.java
 */
public class Project {
    private long id;
    private String name;
    private String client;
    private String location;
    private String status;
    private String startDate;
    private String endDate;
    private String description;
    private String driveUrl;
    private Date createdAt;
    private Date updatedAt;

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
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Constructor completo
    public Project(long id, String name, String client, String location, String status,
                   String startDate, String endDate, String description, String driveUrl,
                   Date createdAt, Date updatedAt) {
        this(id, name, client, location, status, startDate, endDate, description);
        this.driveUrl = driveUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getClient() { return client; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getDescription() { return description; }
    public String getDriveUrl() { return driveUrl; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setClient(String client) { this.client = client; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(String status) { this.status = status; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setDescription(String description) { this.description = description; }
    public void setDriveUrl(String driveUrl) { this.driveUrl = driveUrl; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Métodos utilitarios
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

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", client='" + client + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

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