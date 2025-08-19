package com.regenerarestudio.regenerapp.data.models;

import java.util.List;
import java.util.Map;

/**
 * Modelo para respuesta de cálculos del backend
 * Representa la respuesta unificada de las APIs de calculadoras
 */
public class CalculationResponse {
    private int calculationId;
    private String calculationType;
    private double calculatedQuantity;
    private String unit;
    private Double estimatedCost;
    private Map<String, Object> detailedResults;
    private Map<String, Object> specificDetails;
    private List<MaterialSuggestion> materialSuggestions;

    // Constructores
    public CalculationResponse() {}

    public CalculationResponse(int calculationId, String calculationType,
                               double calculatedQuantity, String unit) {
        this.calculationId = calculationId;
        this.calculationType = calculationType;
        this.calculatedQuantity = calculatedQuantity;
        this.unit = unit;
    }

    // Getters y Setters
    public int getCalculationId() {
        return calculationId;
    }

    public void setCalculationId(int calculationId) {
        this.calculationId = calculationId;
    }

    public String getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    public double getCalculatedQuantity() {
        return calculatedQuantity;
    }

    public void setCalculatedQuantity(double calculatedQuantity) {
        this.calculatedQuantity = calculatedQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public Map<String, Object> getDetailedResults() {
        return detailedResults;
    }

    public void setDetailedResults(Map<String, Object> detailedResults) {
        this.detailedResults = detailedResults;
    }

    public Map<String, Object> getSpecificDetails() {
        return specificDetails;
    }

    public void setSpecificDetails(Map<String, Object> specificDetails) {
        this.specificDetails = specificDetails;
    }

    public List<MaterialSuggestion> getMaterialSuggestions() {
        return materialSuggestions;
    }

    public void setMaterialSuggestions(List<MaterialSuggestion> materialSuggestions) {
        this.materialSuggestions = materialSuggestions;
    }

    // Métodos de conveniencia
    public String getFormattedQuantity() {
        return String.format("%.2f %s", calculatedQuantity, unit);
    }

    public String getFormattedCost() {
        if (estimatedCost != null) {
            return String.format("$%.2f", estimatedCost);
        }
        return "No disponible";
    }

    // Clase interna para sugerencias de materiales
    public static class MaterialSuggestion {
        private int id;
        private String name;
        private String code;
        private Double referencePrice;
        private String unit;

        // Constructores
        public MaterialSuggestion() {}

        public MaterialSuggestion(int id, String name, String code) {
            this.id = id;
            this.name = name;
            this.code = code;
        }

        // Getters y Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Double getReferencePrice() {
            return referencePrice;
        }

        public void setReferencePrice(Double referencePrice) {
            this.referencePrice = referencePrice;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}