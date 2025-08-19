package com.regenerarestudio.regenerapp.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de datos para Proveedor
 * Corresponde al modelo Supplier del backend Django
 */
public class Supplier {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("commercial_name")
    private String commercialName;

    @SerializedName("supplier_type")
    private String supplierType;

    @SerializedName("contact_person")
    private String contactPerson;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("whatsapp_number")
    private String whatsappNumber;

    @SerializedName("address")
    private String address;

    @SerializedName("city")
    private String city;

    @SerializedName("zone")
    private String zone;

    @SerializedName("rating")
    private Integer rating;

    @SerializedName("payment_terms")
    private String paymentTerms;

    @SerializedName("delivery_time")
    private String deliveryTime;

    @SerializedName("is_preferred")
    private Boolean isPreferred;

    @SerializedName("is_active")
    private Boolean isActive;

    @SerializedName("website")
    private String website;

    @SerializedName("notes")
    private String notes;

    // Constructor vacío para Gson
    public Supplier() {}

    // Constructor completo
    public Supplier(Long id, String name, String commercialName, String supplierType,
                    String contactPerson, String phone, String email, String address,
                    String city, String zone, Integer rating, Boolean isPreferred) {
        this.id = id;
        this.name = name;
        this.commercialName = commercialName;
        this.supplierType = supplierType;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.city = city;
        this.zone = zone;
        this.rating = rating;
        this.isPreferred = isPreferred;
        this.isActive = true;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCommercialName() { return commercialName; }
    public void setCommercialName(String commercialName) { this.commercialName = commercialName; }

    public String getSupplierType() { return supplierType; }
    public void setSupplierType(String supplierType) { this.supplierType = supplierType; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    public Boolean getIsPreferred() { return isPreferred; }
    public void setIsPreferred(Boolean isPreferred) { this.isPreferred = isPreferred; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Métodos de utilidad
    public String getFormattedPhone() {
        if (phone != null && !phone.isEmpty()) {
            return phone;
        }
        return whatsappNumber;
    }

    public String getWhatsappUrl() {
        if (whatsappNumber != null && !whatsappNumber.isEmpty()) {
            // Limpiar el número (quitar + y espacios)
            String cleanNumber = whatsappNumber.replaceAll("[^0-9]", "");
            return "https://wa.me/" + cleanNumber;
        }
        return null;
    }

    public String getDisplayName() {
        return commercialName != null && !commercialName.isEmpty() ? commercialName : name;
    }

    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        if (address != null && !address.isEmpty()) {
            fullAddress.append(address);
        }
        if (city != null && !city.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(city);
        }
        if (zone != null && !zone.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(" - ");
            fullAddress.append(zone);
        }
        return fullAddress.toString();
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", commercialName='" + commercialName + '\'' +
                ", supplierType='" + supplierType + '\'' +
                ", phone='" + phone + '\'' +
                ", city='" + city + '\'' +
                ", rating=" + rating +
                '}';
    }
}