package com.regenerarestudio.regenerapp.data.models;

/**
 * Clase para crear/actualizar gastos reales
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/data/models/RealExpenseCreateUpdateRequest.java
 */
public class RealExpenseCreateUpdateRequest {

    private Long id;
    private Long projectId;
    private String description;
    private Double quantity;
    private String unit;
    private Double unitPrice;
    private Double discountPercentage;
    private String supplierName;
    private Long supplierId;
    private String invoiceNumber;
    private String purchaseDate;
    private String category;
    private String spaces;

    // Constructor vacío
    public RealExpenseCreateUpdateRequest() {}

    // Constructor completo
    public RealExpenseCreateUpdateRequest(Long id, Long projectId, String description,
                                          Double quantity, String unit, Double unitPrice,
                                          Double discountPercentage, String supplierName,
                                          String invoiceNumber, String purchaseDate,
                                          String category) {
        this.id = id;
        this.projectId = projectId;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.discountPercentage = discountPercentage;
        this.supplierName = supplierName;
        this.invoiceNumber = invoiceNumber;
        this.purchaseDate = purchaseDate;
        this.category = category;
    }

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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSpaces() {
        return spaces;
    }

    public void setSpaces(String spaces) {
        this.spaces = spaces;
    }

    // Métodos utilitarios
    public boolean isUpdate() {
        return id != null;
    }

    public double getTotalPrice() {
        if (quantity == null || unitPrice == null) return 0.0;

        double total = quantity * unitPrice;

        // Aplicar descuento si existe
        if (discountPercentage != null && discountPercentage > 0) {
            total = total * (1 - discountPercentage / 100);
        }

        return Math.max(0, total); // No permitir totales negativos
    }

    @Override
    public String toString() {
        return "RealExpenseCreateUpdateRequest{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", unitPrice=" + unitPrice +
                ", discountPercentage=" + discountPercentage +
                ", supplierName='" + supplierName + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", purchaseDate='" + purchaseDate + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}