package com.regenerarestudio.regenerapp.data.models;

/**
 * Modelo que combina información de proveedor con precios de materiales
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/data/models/SupplierWithPrice.java
 */
public class SupplierWithPrice {

    private long supplierId;
    private String supplierName;
    private String commercialName;
    private double price;
    private String currency;
    private double discountPercentage;
    private String deliveryTime;
    private double rating;
    private boolean isPreferred;
    private String location;
    private String phone;
    private String whatsappUrl;

    // Constructor vacío
    public SupplierWithPrice() {}

    // Constructor completo
    public SupplierWithPrice(long supplierId, String supplierName, String commercialName,
                             double price, String currency, double discountPercentage,
                             String deliveryTime, double rating, boolean isPreferred,
                             String location, String phone, String whatsappUrl) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.commercialName = commercialName;
        this.price = price;
        this.currency = currency;
        this.discountPercentage = discountPercentage;
        this.deliveryTime = deliveryTime;
        this.rating = rating;
        this.isPreferred = isPreferred;
        this.location = location;
        this.phone = phone;
        this.whatsappUrl = whatsappUrl;
    }

    // Getters y Setters
    public long getSupplierId() { return supplierId; }
    public void setSupplierId(long supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getCommercialName() { return commercialName; }
    public void setCommercialName(String commercialName) { this.commercialName = commercialName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public boolean isPreferred() { return isPreferred; }
    public void setPreferred(boolean preferred) { isPreferred = preferred; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWhatsappUrl() { return whatsappUrl; }
    public void setWhatsappUrl(String whatsappUrl) { this.whatsappUrl = whatsappUrl; }

    // Métodos utilitarios
    public String getDisplayName() {
        return commercialName != null && !commercialName.isEmpty() ? commercialName : supplierName;
    }

    public double getFinalPrice() {
        return price * (1 - discountPercentage / 100);
    }

    public String getFormattedPrice() {
        return String.format("$%.2f", getFinalPrice());
    }

    public String getFormattedRating() {
        return String.format("%.1f ⭐", rating);
    }

    public String getRatingStars() {
        int stars = (int) Math.round(rating);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) {
            sb.append("⭐");
        }
        return sb.toString();
    }

    public boolean hasDiscount() {
        return discountPercentage > 0;
    }

    public String getDiscountText() {
        if (hasDiscount()) {
            return String.format("%.0f%% descuento", discountPercentage);
        }
        return "";
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)",
                getDisplayName(),
                getFormattedPrice(),
                deliveryTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SupplierWithPrice that = (SupplierWithPrice) obj;
        return supplierId == that.supplierId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(supplierId);
    }
}