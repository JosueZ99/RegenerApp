package com.regenerarestudio.regenerapp.data.responses;

import com.google.gson.annotations.SerializedName;
import com.regenerarestudio.regenerapp.data.models.Project;
import java.util.List;

/**
 * Respuesta del dashboard con datos del proyecto
 */
public class DashboardResponse {
    @SerializedName("project")
    private Project project;

    @SerializedName("financial_summary")
    private FinancialSummary financialSummary;

    @SerializedName("phase_progress")
    private PhaseProgress phaseProgress;

    @SerializedName("recent_activities")
    private List<Activity> recentActivities;

    @SerializedName("notifications")
    private List<Notification> notifications;

    // Constructor vacío
    public DashboardResponse() {}

    // Getters y Setters
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public FinancialSummary getFinancialSummary() { return financialSummary; }
    public void setFinancialSummary(FinancialSummary financialSummary) { this.financialSummary = financialSummary; }

    public PhaseProgress getPhaseProgress() { return phaseProgress; }
    public void setPhaseProgress(PhaseProgress phaseProgress) { this.phaseProgress = phaseProgress; }

    public List<Activity> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<Activity> recentActivities) { this.recentActivities = recentActivities; }

    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }

    /**
     * Resumen financiero del proyecto
     */
    public static class FinancialSummary {
        @SerializedName("initial_budget")
        private Double initialBudget;

        @SerializedName("total_expenses")
        private Double totalExpenses;

        @SerializedName("remaining_budget")
        private Double remainingBudget;

        @SerializedName("budget_percentage_used")
        private Double budgetPercentageUsed;

        @SerializedName("total_items_budgeted")
        private Integer totalItemsBudgeted;

        @SerializedName("total_items_purchased")
        private Integer totalItemsPurchased;

        // Constructor vacío
        public FinancialSummary() {}

        // Getters y Setters
        public Double getInitialBudget() { return initialBudget; }
        public void setInitialBudget(Double initialBudget) { this.initialBudget = initialBudget; }

        public Double getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(Double totalExpenses) { this.totalExpenses = totalExpenses; }

        public Double getRemainingBudget() { return remainingBudget; }
        public void setRemainingBudget(Double remainingBudget) { this.remainingBudget = remainingBudget; }

        public Double getBudgetPercentageUsed() { return budgetPercentageUsed; }
        public void setBudgetPercentageUsed(Double budgetPercentageUsed) { this.budgetPercentageUsed = budgetPercentageUsed; }

        public Integer getTotalItemsBudgeted() { return totalItemsBudgeted; }
        public void setTotalItemsBudgeted(Integer totalItemsBudgeted) { this.totalItemsBudgeted = totalItemsBudgeted; }

        public Integer getTotalItemsPurchased() { return totalItemsPurchased; }
        public void setTotalItemsPurchased(Integer totalItemsPurchased) { this.totalItemsPurchased = totalItemsPurchased; }

        public String getFormattedInitialBudget() {
            return initialBudget != null ? String.format("$%.2f", initialBudget) : "$0.00";
        }

        public String getFormattedTotalExpenses() {
            return totalExpenses != null ? String.format("$%.2f", totalExpenses) : "$0.00";
        }

        public String getFormattedRemainingBudget() {
            return remainingBudget != null ? String.format("$%.2f", remainingBudget) : "$0.00";
        }

        public boolean isOverBudget() {
            return remainingBudget != null && remainingBudget < 0;
        }
    }

    /**
     * Progreso de las fases del proyecto
     */
    public static class PhaseProgress {
        @SerializedName("current_phase")
        private String currentPhase;

        @SerializedName("design_completed")
        private Boolean designCompleted;

        @SerializedName("purchase_completed")
        private Boolean purchaseCompleted;

        @SerializedName("installation_completed")
        private Boolean installationCompleted;

        @SerializedName("overall_progress_percentage")
        private Double overallProgressPercentage;

        // Constructor vacío
        public PhaseProgress() {}

        // Getters y Setters
        public String getCurrentPhase() { return currentPhase; }
        public void setCurrentPhase(String currentPhase) { this.currentPhase = currentPhase; }

        public Boolean getDesignCompleted() { return designCompleted; }
        public void setDesignCompleted(Boolean designCompleted) { this.designCompleted = designCompleted; }

        public Boolean getPurchaseCompleted() { return purchaseCompleted; }
        public void setPurchaseCompleted(Boolean purchaseCompleted) { this.purchaseCompleted = purchaseCompleted; }

        public Boolean getInstallationCompleted() { return installationCompleted; }
        public void setInstallationCompleted(Boolean installationCompleted) { this.installationCompleted = installationCompleted; }

        public Double getOverallProgressPercentage() { return overallProgressPercentage; }
        public void setOverallProgressPercentage(Double overallProgressPercentage) { this.overallProgressPercentage = overallProgressPercentage; }

        public String getCurrentPhaseDisplay() {
            if (currentPhase == null) return "No definida";

            switch (currentPhase) {
                case "design": return "Diseño";
                case "purchase": return "Compra";
                case "installation": return "Instalación";
                case "completed": return "Completado";
                default: return "Desconocida";
            }
        }

        public int getProgressAsInt() {
            return overallProgressPercentage != null ? overallProgressPercentage.intValue() : 0;
        }
    }

    /**
     * Actividad reciente del proyecto
     */
    public static class Activity {
        @SerializedName("id")
        private Long id;

        @SerializedName("type")
        private String type;

        @SerializedName("description")
        private String description;

        @SerializedName("timestamp")
        private String timestamp;

        @SerializedName("icon")
        private String icon;

        // Constructor vacío
        public Activity() {}

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    /**
     * Notificación para el proyecto
     */
    public static class Notification {
        @SerializedName("id")
        private Long id;

        @SerializedName("type")
        private String type;

        @SerializedName("title")
        private String title;

        @SerializedName("message")
        private String message;

        @SerializedName("priority")
        private String priority;

        @SerializedName("is_read")
        private Boolean isRead;

        @SerializedName("created_at")
        private String createdAt;

        // Constructor vacío
        public Notification() {}

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public Boolean getIsRead() { return isRead; }
        public void setIsRead(Boolean isRead) { this.isRead = isRead; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public boolean isHighPriority() {
            return "high".equals(priority) || "urgent".equals(priority);
        }
    }
}