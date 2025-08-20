package com.regenerarestudio.regenerapp.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper para traducciones de categorías de presupuesto y gastos
 * Centraliza las traducciones de las categorías del backend al español
 */
public class BudgetCategoryTranslations {

    private static final Map<String, String> CATEGORY_TRANSLATIONS = new HashMap<>();

    static {
        // Traducciones de categorías principales
        CATEGORY_TRANSLATIONS.put("construction", "Construcción");
        CATEGORY_TRANSLATIONS.put("lighting", "Iluminación");
        CATEGORY_TRANSLATIONS.put("electrical", "Eléctrico");
        CATEGORY_TRANSLATIONS.put("others", "Otros");
        CATEGORY_TRANSLATIONS.put("materials", "Materiales");
        CATEGORY_TRANSLATIONS.put("labor", "Mano de Obra");
        CATEGORY_TRANSLATIONS.put("services", "Servicios");

        // Variaciones en mayúsculas/minúsculas
        CATEGORY_TRANSLATIONS.put("Construction", "Construcción");
        CATEGORY_TRANSLATIONS.put("Lighting", "Iluminación");
        CATEGORY_TRANSLATIONS.put("Electrical", "Eléctrico");
        CATEGORY_TRANSLATIONS.put("Others", "Otros");
        CATEGORY_TRANSLATIONS.put("Materials", "Materiales");
        CATEGORY_TRANSLATIONS.put("Labor", "Mano de Obra");
        CATEGORY_TRANSLATIONS.put("Services", "Servicios");
    }

    /**
     * Traduce una categoría del inglés al español
     * @param category Categoría en inglés (ej: "lighting")
     * @return Categoría en español (ej: "Iluminación")
     */
    public static String translateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "Sin Categoría";
        }

        // Buscar traducción exacta
        String translation = CATEGORY_TRANSLATIONS.get(category.trim());

        if (translation != null) {
            return translation;
        }

        // Buscar traducción insensible a mayúsculas
        translation = CATEGORY_TRANSLATIONS.get(category.toLowerCase().trim());
        if (translation != null) {
            return translation;
        }

        // Fallback: capitalizar primera letra
        return capitalizeFirst(category.trim());
    }

    /**
     * Capitaliza la primera letra de una cadena
     */
    private static String capitalizeFirst(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    /**
     * Verifica si una categoría es válida
     */
    public static boolean isValidCategory(String category) {
        return category != null && CATEGORY_TRANSLATIONS.containsKey(category.trim());
    }

    /**
     * Obtiene todas las categorías disponibles en español
     */
    public static String[] getAllCategoriesInSpanish() {
        return new String[]{
                "Construcción",
                "Iluminación",
                "Eléctrico",
                "Materiales",
                "Mano de Obra",
                "Servicios",
                "Otros"
        };
    }

    /**
     * Obtiene todas las categorías disponibles en inglés (para envío al backend)
     */
    public static String[] getAllCategoriesInEnglish() {
        return new String[]{
                "construction",
                "lighting",
                "electrical",
                "materials",
                "labor",
                "services",
                "others"
        };
    }

    /**
     * Convierte una categoría del español al inglés (para envío al backend)
     */
    public static String translateToEnglish(String spanishCategory) {
        if (spanishCategory == null) return null;

        // Buscar la clave por valor
        for (Map.Entry<String, String> entry : CATEGORY_TRANSLATIONS.entrySet()) {
            if (entry.getValue().equals(spanishCategory.trim())) {
                return entry.getKey().toLowerCase();
            }
        }

        // Fallback
        return spanishCategory.toLowerCase();
    }
}