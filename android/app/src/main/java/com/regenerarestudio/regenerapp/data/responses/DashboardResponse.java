package com.regenerarestudio.regenerapp.data.responses;

import com.google.gson.annotations.SerializedName;
import com.regenerarestudio.regenerapp.data.models.Project;
import java.util.List;
import java.util.Map;

/**
 * Respuesta del dashboard que coincide exactamente con el backend Django
 * GET /api/projects/{id}/dashboard/
 */
public class DashboardResponse {

    @SerializedName("project")
    private Project project;

    @SerializedName("financial_summary")
    private FinancialSummary financialSummary;

    @SerializedName("statistics")
    private Statistics statistics;

    // Constructor vacío
    public DashboardResponse() {}

    // Getters y Setters
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public FinancialSummary getFinancialSummary() { return financialSummary; }
    public void setFinancialSummary(FinancialSummary financialSummary) { this.financialSummary = financialSummary; }

    public Statistics getStatistics() { return statistics; }
    public void setStatistics(Statistics statistics) { this.statistics = statistics; }

    /**
     * Resumen financiero del proyecto - Coincide con backend Django
     */
    public static class FinancialSummary {
        @SerializedName("total_budget")
        private Double totalBudget;

        @SerializedName("total_expenses")
        private Double totalExpenses;

        @SerializedName("balance")
        private Double balance;

        @SerializedName("budget_utilization_percentage")
        private Double budgetUtilizationPercentage;

        @SerializedName("is_over_budget")
        private Boolean isOverBudget;

        @SerializedName("remaining_budget")
        private Double remainingBudget;

        @SerializedName("budget_by_category")
        private BudgetByCategory budgetByCategory;

        // Constructor vacío
        public FinancialSummary() {}

        // Getters y Setters
        public Double getTotalBudget() { return totalBudget; }
        public void setTotalBudget(Double totalBudget) { this.totalBudget = totalBudget; }

        public Double getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(Double totalExpenses) { this.totalExpenses = totalExpenses; }

        public Double getBalance() { return balance; }
        public void setBalance(Double balance) { this.balance = balance; }

        public Double getBudgetUtilizationPercentage() { return budgetUtilizationPercentage; }
        public void setBudgetUtilizationPercentage(Double budgetUtilizationPercentage) {
            this.budgetUtilizationPercentage = budgetUtilizationPercentage;
        }

        public Boolean getIsOverBudget() { return isOverBudget; }
        public void setIsOverBudget(Boolean isOverBudget) { this.isOverBudget = isOverBudget; }

        public Double getRemainingBudget() { return remainingBudget; }
        public void setRemainingBudget(Double remainingBudget) { this.remainingBudget = remainingBudget; }

        public BudgetByCategory getBudgetByCategory() { return budgetByCategory; }
        public void setBudgetByCategory(BudgetByCategory budgetByCategory) {
            this.budgetByCategory = budgetByCategory;
        }

        // Métodos de utilidad
        public Double getBudgetPercentageUsed() { return budgetUtilizationPercentage; }
        public Double getInitialBudget() { return totalBudget; }
    }

    /**
     * Presupuesto por categorías
     */
    public static class BudgetByCategory {
        @SerializedName("construction")
        private CategoryBudget construction;

        @SerializedName("lighting")
        private CategoryBudget lighting;

        @SerializedName("others")
        private CategoryBudget others;

        // Constructor vacío
        public BudgetByCategory() {}

        // Getters y Setters
        public CategoryBudget getConstruction() { return construction; }
        public void setConstruction(CategoryBudget construction) { this.construction = construction; }

        public CategoryBudget getLighting() { return lighting; }
        public void setLighting(CategoryBudget lighting) { this.lighting = lighting; }

        public CategoryBudget getOthers() { return others; }
        public void setOthers(CategoryBudget others) { this.others = others; }
    }

    /**
     * Presupuesto de una categoría específica
     */
    public static class CategoryBudget {
        @SerializedName("budgeted")
        private Double budgeted;

        @SerializedName("spent")
        private Double spent;

        // Constructor vacío
        public CategoryBudget() {}

        // Getters y Setters
        public Double getBudgeted() { return budgeted; }
        public void setBudgeted(Double budgeted) { this.budgeted = budgeted; }

        public Double getSpent() { return spent; }
        public void setSpent(Double spent) { this.spent = spent; }

        // Métodos de utilidad
        public Double getRemaining() {
            if (budgeted != null && spent != null) {
                return budgeted - spent;
            }
            return 0.0;
        }

        public Double getPercentageUsed() {
            if (budgeted != null && spent != null && budgeted > 0) {
                return (spent / budgeted) * 100;
            }
            return 0.0;
        }
    }

    /**
     * Estadísticas adicionales del proyecto
     */
    public static class Statistics {
        @SerializedName("budget_items_count")
        private Integer budgetItemsCount;

        @SerializedName("expenses_count")
        private Integer expensesCount;

        @SerializedName("calculations_count")
        private Integer calculationsCount;

        @SerializedName("recent_expenses")
        private List<RecentExpense> recentExpenses;

        // Constructor vacío
        public Statistics() {}

        // Getters y Setters
        public Integer getBudgetItemsCount() { return budgetItemsCount; }
        public void setBudgetItemsCount(Integer budgetItemsCount) { this.budgetItemsCount = budgetItemsCount; }

        public Integer getExpensesCount() { return expensesCount; }
        public void setExpensesCount(Integer expensesCount) { this.expensesCount = expensesCount; }

        public Integer getCalculationsCount() { return calculationsCount; }
        public void setCalculationsCount(Integer calculationsCount) { this.calculationsCount = calculationsCount; }

        public List<RecentExpense> getRecentExpenses() { return recentExpenses; }
        public void setRecentExpenses(List<RecentExpense> recentExpenses) { this.recentExpenses = recentExpenses; }
    }

    /**
     * Gasto reciente para mostrar en dashboard
     */
    public static class RecentExpense {
        @SerializedName("description")
        private String description;

        @SerializedName("total_price")
        private Double totalPrice;

        @SerializedName("purchase_date")
        private String purchaseDate;

        @SerializedName("category")
        private String category;

        // Constructor vacío
        public RecentExpense() {}

        // Getters y Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

        public String getPurchaseDate() { return purchaseDate; }
        public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    // Métodos de utilidad para toda la respuesta
    public boolean hasFinancialData() {
        return financialSummary != null;
    }

    public boolean hasStatistics() {
        return statistics != null;
    }

    public boolean isValidResponse() {
        return project != null;
    }
}