package com.regenerarestudio.regenerapp.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase para traducir etiquetas de cálculos a español
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/utils/CalculationTranslations.java
 */
public class CalculationTranslations {

    private static final Map<String, String> TRANSLATIONS = new HashMap<>();

    static {
        // Traducciones generales
        TRANSLATIONS.put("total_length", "Longitud Total");
        TRANSLATIONS.put("base_length", "Longitud Base");
        TRANSLATIONS.put("area_to_paint", "Área a Pintar");
        TRANSLATIONS.put("area_to_cover", "Área a Cubrir");
        TRANSLATIONS.put("number_of_coats", "Número de Capas");
        TRANSLATIONS.put("paint_type", "Tipo de Pintura");
        TRANSLATIONS.put("coverage_per_liter", "Rendimiento por Litro");
        TRANSLATIONS.put("total_liters_needed", "Litros Totales Necesarios");
        TRANSLATIONS.put("gallons_needed", "Galones Necesarios");

        // Traducciones de perfiles
        TRANSLATIONS.put("profile_type", "Tipo de Perfil");
        TRANSLATIONS.put("profile_size", "Tamaño del Perfil");
        TRANSLATIONS.put("finish_type", "Tipo de Acabado");
        TRANSLATIONS.put("profiles_needed", "Perfiles Necesarios");
        TRANSLATIONS.put("accessories_count", "Cantidad de Accesorios");
        TRANSLATIONS.put("standard_length", "Longitud Estándar");
        TRANSLATIONS.put("waste_factor_applied", "Factor de Desperdicio");

        // Traducciones de gypsum
        TRANSLATIONS.put("area_to_cover", "Área a Cubrir");
        TRANSLATIONS.put("thickness", "Espesor");
        TRANSLATIONS.put("gypsum_type", "Tipo de Gypsum");
        TRANSLATIONS.put("sheets_needed", "Planchas Necesarias");
        TRANSLATIONS.put("linear_meters_profile", "Metros Lineales de Perfil");
        TRANSLATIONS.put("sheet_area", "Área por Plancha");
        TRANSLATIONS.put("sheets_base", "Planchas Base");

        // Traducciones de empaste
        TRANSLATIONS.put("empaste_type", "Tipo de Empaste");
        TRANSLATIONS.put("coverage_per_kg", "Rendimiento por Kg");
        TRANSLATIONS.put("kg_base", "Kilogramos Base");
        TRANSLATIONS.put("kg_per_sack", "Kg por Saco");
        TRANSLATIONS.put("sacks_needed", "Sacos Necesarios");

        // Traducciones de LED
        TRANSLATIONS.put("power_per_meter", "Potencia por Metro");
        TRANSLATIONS.put("voltage", "Voltaje");
        TRANSLATIONS.put("strip_type", "Tipo de Cinta");
        TRANSLATIONS.put("total_power", "Potencia Total");
        TRANSLATIONS.put("drivers_needed", "Drivers Necesarios");
        TRANSLATIONS.put("meters_per_roll", "Metros por Rollo");
        TRANSLATIONS.put("rolls_needed", "Rollos Necesarios");

        // Traducciones de cables
        TRANSLATIONS.put("wire_gauge", "Calibre del Cable");
        TRANSLATIONS.put("cable_type", "Tipo de Cable");
        TRANSLATIONS.put("installation_type", "Tipo de Instalación");

        // Traducciones de costos
        TRANSLATIONS.put("estimated_cost", "Costo Estimado");
        TRANSLATIONS.put("total_cost", "Costo Total");
        TRANSLATIONS.put("unit_cost", "Costo Unitario");

        // Traducciones de resultados
        TRANSLATIONS.put("total_area", "Área Total");
        TRANSLATIONS.put("calculated_quantity", "Cantidad Calculada");
        TRANSLATIONS.put("base_liters_needed", "Litros Base Necesarios");
        TRANSLATIONS.put("gallons_equivalent", "Equivalente en Galones");
    }

    /**
     * Traduce una clave de detalle de cálculo al español
     * @param key Clave en inglés
     * @return Traducción en español o versión formateada si no existe traducción
     */
    public static String translateDetailKey(String key) {
        String translation = TRANSLATIONS.get(key);
        if (translation != null) {
            return translation;
        }

        // Fallback: convertir formato snake_case a título
        return formatFallbackLabel(key);
    }

    /**
     * Formato de respaldo para claves no traducidas
     */
    private static String formatFallbackLabel(String key) {
        return key.replace("_", " ")
                .substring(0, 1).toUpperCase() +
                key.replace("_", " ").substring(1).toLowerCase();
    }

    /**
     * Formatea el valor según el tipo de clave
     * @param key Clave del campo
     * @param value Valor a formatear
     * @return Valor formateado con unidades apropiadas
     */
    public static String formatDetailValue(String key, Object value) {
        if (value == null) return "No disponible";

        if (value instanceof Number) {
            double numValue = ((Number) value).doubleValue();

            // Áreas
            if (key.contains("area")) {
                return String.format("%.2f m²", numValue);
            }
            // Longitudes y metros
            else if (key.contains("length") || key.contains("meters") || key.contains("linear")) {
                return String.format("%.2f m", numValue);
            }
            // Costos y precios
            else if (key.contains("cost") || key.contains("price")) {
                return String.format("$%.2f", numValue);
            }
            // Porcentajes y factores
            else if (key.contains("percentage") || key.contains("factor")) {
                return String.format("%.1f%%", numValue * 100);
            }
            // Potencia
            else if (key.contains("power")) {
                return String.format("%.2f W", numValue);
            }
            // Voltaje
            else if (key.contains("voltage")) {
                return value.toString() + "V";
            }
            // Espesores
            else if (key.contains("thickness")) {
                return String.format("%.1f mm", numValue);
            }
            // Rendimiento
            else if (key.contains("coverage")) {
                if (key.contains("liter")) {
                    return String.format("%.1f m²/L", numValue);
                } else if (key.contains("kg")) {
                    return String.format("%.1f m²/kg", numValue);
                }
            }
            // Cantidades enteras (sin decimales)
            else if (key.contains("needed") || key.contains("count") || key.contains("coats") ||
                    key.equals("sheets_base") || key.equals("accessories_count")) {
                return String.format("%.0f", numValue);
            }
            // Valores generales con decimales
            else {
                return String.format("%.2f", numValue);
            }
        }

        // Para strings, retornar como está
        return value.toString();
    }

    /**
     * Verifica si una clave representa un costo
     */
    public static boolean isCostField(String key) {
        return key.contains("cost") || key.contains("price");
    }

    /**
     * Verifica si una clave representa una cantidad
     */
    public static boolean isQuantityField(String key) {
        return key.contains("needed") || key.contains("quantity") ||
                key.equals("profiles_needed") || key.equals("sheets_needed") ||
                key.equals("sacks_needed") || key.equals("rolls_needed");
    }
}