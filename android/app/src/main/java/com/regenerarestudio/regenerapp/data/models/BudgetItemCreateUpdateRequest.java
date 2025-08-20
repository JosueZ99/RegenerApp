package com.regenerarestudio.regenerapp.data.models;

/**
 * Modelo para request de crear/actualizar items de presupuesto
 * Usado tanto para presupuesto inicial como gastos reales
 */
public class BudgetItemCreateUpdateRequest {

    private Long id; // null para crear, con valor para actualizar
    private Long projectId;
    private String description;
    private Double quantity;
    private Double unitPrice;
    private String spaces;
    private String category;
    private String unit;
    private Long supplierId;

    // Campos específicos para gastos reales
    private Double discount; // Porcentaje o monto fijo
    private String purchaseDate; // Formato: yyyy-MM-dd

    // Constructores
    public BudgetItemCreateUpdateRequest() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getSpaces() {
        return spaces;
    }

    public void setSpaces(String spaces) {
        this.spaces = spaces;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    // Métodos utilitarios
    public boolean isForExpenses() {
        return discount != null || purchaseDate != null;
    }

    public boolean isUpdate() {
        return id != null;
    }

    public double getTotalPrice() {
        if (quantity == null || unitPrice == null) return 0.0;

        double total = quantity * unitPrice;

        // Aplicar descuento si existe
        if (discount != null && discount > 0) {
            if (discount > 1) {
                // Es un monto fijo
                total -= discount;
            } else {
                // Es un porcentaje
                total = total * (1 - discount);
            }
        }

        return Math.max(0, total); // No permitir totales negativos
    }

    @Override
    public String toString() {
        return "BudgetItemRequest{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", category='" + category + '\'' +
                ", supplierId=" + supplierId +
                ", isExpense=" + isForExpenses() +
                '}';
    }
}