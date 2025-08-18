package com.regenerarestudio.regenerapp.data.models;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modelo actualizado para Proyecto
 * Compatible con backend Django y frontend Android
 * Unifica todas las versiones existentes del modelo Project
 */
public class Project {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("client")
    private String client;

    @SerializedName("location")
    private String location;

    @SerializedName("project_type")
    private String projectType;

    @SerializedName("description")
    private String description;

    @SerializedName("start_date")
    private String startDate; // Formato: "YYYY-MM-DD"

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("actual_end_date")
    private String actualEndDate;

    @SerializedName("current_phase")
    private String currentPhase;

    @SerializedName("status")
    private String status;

    @SerializedName("drive_folder_url")
    private String driveFolderUrl;

    @SerializedName("initial_budget")
    private Double initialBudget;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("is_selected")
    private Boolean isSelected;

    // Campos locales (no enviados al backend)
    private transient String type; // Para compatibilidad con código existente
    private transient Date createdAtDate;
    private transient Date updatedAtDate;

    // Constructor vacío necesario para Gson
    public Project() {
        this.isSelected = false;
    }

    // Constructor simple (compatible con ProyectosViewModel original)
    public Project(int id, String name, String client, String location, String type, String status) {
        this();
        this.id = (long) id;
        this.name = name;
        this.client = client;
        this.location = location;
        this.type = type;
        this.projectType = type; // Sincronizar ambos campos
        this.status = status;
        this.currentPhase = "design"; // Valor por defecto
    }

    // Constructor completo (compatible con ProjectSelectionActivity)
    public Project(long id, String name, String client, String location, String status,
                   String startDate, String endDate, String description) {
        this();
        this.id = id;
        this.name = name;
        this.client = client;
        this.location = location;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.currentPhase = "design"; // Valor por defecto
    }

    // Constructor extendido con URLs y fechas
    public Project(long id, String name, String client, String location, String status,
                   String startDate, String endDate, String description, String driveUrl,
                   Date createdAt, Date updatedAt) {
        this(id, name, client, location, status, startDate, endDate, description);
        this.driveFolderUrl = driveUrl;
        this.createdAtDate = createdAt;
        this.updatedAtDate = updatedAt;
    }

    // Constructor completo para APIs REST (nuevo)
    public Project(Long id, String name, String client, String location, String projectType,
                   String status, String currentPhase, String startDate, String endDate,
                   String description, String driveFolderUrl, Double initialBudget, Boolean isSelected) {
        this();
        this.id = id;
        this.name = name;
        this.client = client;
        this.location = location;
        this.projectType = projectType;
        this.type = projectType; // Sincronizar
        this.status = status;
        this.currentPhase = currentPhase;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.driveFolderUrl = driveFolderUrl;
        this.initialBudget = initialBudget;
        this.isSelected = isSelected;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getProjectType() { return projectType != null ? projectType : type; }
    public void setProjectType(String projectType) {
        this.projectType = projectType;
        this.type = projectType; // Mantener sincronización
    }

    public String getType() { return type != null ? type : projectType; }
    public void setType(String type) {
        this.type = type;
        this.projectType = type; // Mantener sincronización
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(String actualEndDate) { this.actualEndDate = actualEndDate; }

    public String getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(String currentPhase) { this.currentPhase = currentPhase; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDriveFolderUrl() { return driveFolderUrl; }
    public void setDriveFolderUrl(String driveFolderUrl) { this.driveFolderUrl = driveFolderUrl; }

    public Double getInitialBudget() { return initialBudget; }
    public void setInitialBudget(Double initialBudget) { this.initialBudget = initialBudget; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Boolean isSelected() { return isSelected != null ? isSelected : false; }
    public void setSelected(Boolean selected) { this.isSelected = selected; }

    // Métodos de compatibilidad para código existente
    public boolean getSelected() { return isSelected(); }
    public void setSelected(boolean selected) { setSelected((Boolean) selected); }

    public Date getCreatedAtDate() { return createdAtDate; }
    public void setCreatedAtDate(Date createdAtDate) { this.createdAtDate = createdAtDate; }

    public Date getUpdatedAtDate() { return updatedAtDate; }
    public void setUpdatedAtDate(Date updatedAtDate) { this.updatedAtDate = updatedAtDate; }

    // Métodos de utilidad
    public String getDisplayType() {
        String typeToDisplay = getProjectType();
        if (typeToDisplay == null) return "Sin tipo";

        switch (typeToDisplay.toLowerCase()) {
            case "residential": return "Residencial";
            case "commercial": return "Comercial";
            case "institutional": return "Institucional";
            case "industrial": return "Industrial";
            default: return typeToDisplay;
        }
    }

    public String getDisplayStatus() {
        if (status == null) return "Sin estado";

        switch (status.toLowerCase()) {
            case "planning": return "PLANIFICACIÓN";
            case "in_progress": return "EN PROGRESO";
            case "on_hold": return "EN PAUSA";
            case "completed": return "TERMINADO";
            case "budget": return "PRESUPUESTO";
            default: return status.toUpperCase();
        }
    }

    public String getDisplayPhase() {
        if (currentPhase == null) return "Sin fase";

        switch (currentPhase.toLowerCase()) {
            case "design": return "Diseño";
            case "purchase": return "Compra";
            case "installation": return "Instalación";
            case "completed": return "Completado";
            default: return currentPhase;
        }
    }

    public String getFormattedBudget() {
        if (initialBudget != null) {
            return String.format(Locale.US, "$%.2f", initialBudget);
        }
        return "Sin presupuesto";
    }

    public String getFormattedDateRange() {
        if (startDate != null && endDate != null) {
            return formatDate(startDate) + " - " + formatDate(endDate);
        } else if (startDate != null) {
            return "Desde " + formatDate(startDate);
        } else if (endDate != null) {
            return "Hasta " + formatDate(endDate);
        }
        return "Fechas no definidas";
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString; // Retornar la fecha original si no se puede parsear
        }
    }

    public boolean hasGoogleDriveLink() {
        return driveFolderUrl != null && !driveFolderUrl.isEmpty() &&
                driveFolderUrl.startsWith("http");
    }

    public String getShortDescription() {
        if (description == null || description.isEmpty()) {
            return "Sin descripción";
        }
        if (description.length() <= 100) {
            return description;
        }
        return description.substring(0, 97) + "...";
    }

    public boolean isInProgress() {
        return "in_progress".equals(status);
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }

    public boolean isPlanning() {
        return "planning".equals(status);
    }

    public int getProgressPercentage() {
        if (currentPhase == null) return 0;

        switch (currentPhase.toLowerCase()) {
            case "design": return 25;
            case "purchase": return 50;
            case "installation": return 75;
            case "completed": return 100;
            default: return 0;
        }
    }

    // Métodos adicionales para compatibilidad con ProjectAdapter
    public String getFormattedStartDate() {
        return formatDate(startDate);
    }

    public String getFormattedEndDate() {
        return formatDate(endDate);
    }

    public String getStatusDisplayName() {
        return getDisplayStatus();
    }

    public int getStatusColor() {
        if (status == null) return android.R.color.holo_blue_light;

        switch (status.toLowerCase()) {
            case "planning":
                return android.R.color.holo_blue_light;
            case "in_progress":
                return android.R.color.holo_orange_light;
            case "on_hold":
                return android.R.color.holo_red_light;
            case "completed":
                return android.R.color.holo_green_light;
            case "budget":
                return android.R.color.holo_purple;
            default:
                return android.R.color.darker_gray;
        }
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", client='" + client + '\'' +
                ", location='" + location + '\'' +
                ", projectType='" + projectType + '\'' +
                ", status='" + status + '\'' +
                ", currentPhase='" + currentPhase + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;
        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}