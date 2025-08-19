package com.regenerarestudio.regenerapp.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de datos para Material
 * Corresponde al modelo Material del backend Django
 */
public class Material {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private String code;

    @SerializedName("category")
    private MaterialCategory category;

    @SerializedName("unit")
    private String unit;

    @SerializedName("reference_price")
    private Double referencePrice;

    @SerializedName("description")
    private String description;

    @SerializedName("yield_per_unit")
    private Double yieldPerUnit;

    @SerializedName("waste_factor")
    private Double wasteFactor;

    @SerializedName("brand")
    private String brand;

    @SerializedName("color")
    private String color;

    @SerializedName("finish")
    private String finish;

    @SerializedName("is_active")
    private Boolean isActive;

    // Campos específicos para construcción
    @SerializedName("coverage_per_liter")
    private Double coveragePerLiter;

    @SerializedName("density")
    private Double density;

    // Campos específicos para iluminación
    @SerializedName("power_per_meter")
    private Double powerPerMeter;

    @SerializedName("wire_gauge")
    private String wireGauge;

    // Constructor vacío para Gson
    public Material() {
        this.isActive = true;
        this.wasteFactor = 0.1; // 10% de desperdicio por defecto
    }

    // Constructor básico
    public Material(Long id, String name, String code, String unit, Double referencePrice) {
        this();
        this.id = id;
        this.name = name;
        this.code = code;
        this.unit = unit;
        this.referencePrice = referencePrice;
    }

    // Constructor completo
    public Material(Long id, String name, String code, MaterialCategory category,
                    String unit, Double referencePrice, String description,
                    String brand, Boolean isActive) {
        this();
        this.id = id;
        this.name = name;
        this.code = code;
        this.category = category;
        this.unit = unit;
        this.referencePrice = referencePrice;
        this.description = description;
        this.brand = brand;
        this.isActive = isActive;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public MaterialCategory getCategory() { return category; }
    public void setCategory(MaterialCategory category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getReferencePrice() { return referencePrice; }
    public void setReferencePrice(Double referencePrice) { this.referencePrice = referencePrice; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getYieldPerUnit() { return yieldPerUnit; }
    public void setYieldPerUnit(Double yieldPerUnit) { this.yieldPerUnit = yieldPerUnit; }

    public Double getWasteFactor() { return wasteFactor; }
    public void setWasteFactor(Double wasteFactor) { this.wasteFactor = wasteFactor; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getFinish() { return finish; }
    public void setFinish(String finish) { this.finish = finish; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Double getCoveragePerLiter() { return coveragePerLiter; }
    public void setCoveragePerLiter(Double coveragePerLiter) { this.coveragePerLiter = coveragePerLiter; }

    public Double getDensity() { return density; }
    public void setDensity(Double density) { this.density = density; }

    public Double getPowerPerMeter() { return powerPerMeter; }
    public void setPowerPerMeter(Double powerPerMeter) { this.powerPerMeter = powerPerMeter; }

    public String getWireGauge() { return wireGauge; }
    public void setWireGauge(String wireGauge) { this.wireGauge = wireGauge; }

    // Métodos de utilidad
    public String getDisplayName() {
        StringBuilder displayName = new StringBuilder(name);
        if (brand != null && !brand.isEmpty()) {
            displayName.append(" - ").append(brand);
        }
        if (color != null && !color.isEmpty()) {
            displayName.append(" (").append(color).append(")");
        }
        return displayName.toString();
    }

    public String getFormattedPrice() {
        if (referencePrice != null) {
            return String.format("$%.2f/%s", referencePrice, unit);
        }
        return "Precio no disponible";
    }

    public Double getPriceWithWaste(Double basePrice) {
        if (basePrice != null && wasteFactor != null) {
            return basePrice * (1 + wasteFactor);
        }
        return basePrice;
    }

    public Double calculateQuantityNeeded(Double areaOrLength, Integer layers) {
        if (yieldPerUnit == null || areaOrLength == null) {
            return null;
        }

        // Cantidad base necesaria
        int layerCount = layers != null ? layers : 1;
        Double baseQuantity = (areaOrLength * layerCount) / yieldPerUnit;

        // Agregar factor de desperdicio
        if (wasteFactor != null) {
            baseQuantity = baseQuantity * (1 + wasteFactor);
        }

        return Math.ceil(baseQuantity * 100) / 100; // Redondear a 2 decimales hacia arriba
    }

    public String getCategoryType() {
        if (category != null) {
            return category.getCategoryType();
        }
        return "unknown";
    }

    public boolean isConstructionMaterial() {
        return "construction".equals(getCategoryType());
    }

    public boolean isLightingMaterial() {
        return "lighting".equals(getCategoryType());
    }

    @Override
    public String toString() {
        return "Material{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", unit='" + unit + '\'' +
                ", referencePrice=" + referencePrice +
                '}';
    }

    /**
     * Clase anidada para MaterialCategory
     */
    public static class MaterialCategory {

        @SerializedName("id")
        private Long id;

        @SerializedName("name")
        private String name;

        @SerializedName("category_type")
        private String categoryType;

        @SerializedName("description")
        private String description;

        @SerializedName("is_active")
        private Boolean isActive;

        // Constructor vacío
        public MaterialCategory() {}

        // Constructor completo
        public MaterialCategory(Long id, String name, String categoryType, String description) {
            this.id = id;
            this.name = name;
            this.categoryType = categoryType;
            this.description = description;
            this.isActive = true;
        }

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCategoryType() { return categoryType; }
        public void setCategoryType(String categoryType) { this.categoryType = categoryType; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }

        public String getCategoryTypeDisplay() {
            if (categoryType == null) return "Sin categoría";

            switch (categoryType) {
                case "construction": return "Construcción";
                case "lighting": return "Iluminación";
                case "electrical": return "Eléctrico";
                case "plumbing": return "Plomería";
                case "finishes": return "Acabados";
                default: return "Otros";
            }
        }

        @Override
        public String toString() {
            return "MaterialCategory{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", categoryType='" + categoryType + '\'' +
                    '}';
        }
    }
}