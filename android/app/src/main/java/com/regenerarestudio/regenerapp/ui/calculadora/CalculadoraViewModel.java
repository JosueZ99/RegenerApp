package com.regenerarestudio.regenerapp.ui.calculadora;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.regenerarestudio.regenerapp.data.api.ApiClient;
import com.regenerarestudio.regenerapp.data.api.ApiService;
import com.regenerarestudio.regenerapp.data.models.CalculationResponse;
import com.regenerarestudio.regenerapp.data.models.Supplier;
import com.regenerarestudio.regenerapp.data.models.SupplierWithPrice;
import com.regenerarestudio.regenerapp.data.responses.PaginatedResponse;

import java.util.ArrayList;
import java.util.List;


import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel para calculadoras - Versión simplificada y funcional
 */
public class CalculadoraViewModel extends ViewModel {

    private final ApiService apiService;

    // LiveData para observar desde el Fragment
    private final MutableLiveData<CalculationResponse> calculationResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAddedToBudget = new MutableLiveData<>();

    public CalculadoraViewModel() {
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        isLoading.setValue(false);
    }

    // Getters para LiveData
    public LiveData<CalculationResponse> getCalculationResult() {
        return calculationResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsAddedToBudget() {
        return isAddedToBudget;
    }

    /**
     * Realizar cálculo según el tipo seleccionado
     */
    public void performCalculation(String calculationTypeCode, Map<String, Object> parameters, long projectId) {
        isLoading.setValue(true);

        // Agregar project_id a los parámetros
        parameters.put("project_id", projectId);

        Call<Map<String, Object>> call = getCalculationCall(calculationTypeCode, parameters);

        if (call == null) {
            errorMessage.setValue("Tipo de calculadora no soportado: " + calculationTypeCode);
            isLoading.setValue(false);
            return;
        }

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    CalculationResponse result = parseResponse(response.body());
                    calculationResult.setValue(result);
                } else {
                    String error = "Error en el cálculo: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            error += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            error += " - " + e.getMessage();
                        }
                    }
                    errorMessage.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Obtener la llamada API correcta según el tipo de calculadora
     */
    private Call<Map<String, Object>> getCalculationCall(String calculationTypeCode, Map<String, Object> parameters) {
        switch (calculationTypeCode.toLowerCase()) {
            case "paint":
                return apiService.calculatePaint(parameters);
            case "gypsum":
                return apiService.calculateGypsum(parameters);
            case "empaste":
                return apiService.calculateEmpaste(parameters);
            case "led_strip":
                return apiService.calculateLED(parameters);
            case "profiles":
                return apiService.calculateProfiles(parameters);
            case "cable":
                return apiService.calculateCables(parameters);
            default:
                return null;
        }
    }

    /**
     * Parsear respuesta del backend a CalculationResponse
     */
    private CalculationResponse parseResponse(Map<String, Object> responseBody) {
        CalculationResponse result = new CalculationResponse();

        try {
            Log.d("CalculadoraViewModel", "=== PARSEANDO RESPUESTA DEL BACKEND ===");

            // Campos principales
            if (responseBody.containsKey("calculation_id")) {
                result.setCalculationId(((Number) responseBody.get("calculation_id")).intValue());
                Log.d("CalculadoraViewModel", "Calculation ID: " + result.getCalculationId());
            }

            if (responseBody.containsKey("calculation_type")) {
                result.setCalculationType((String) responseBody.get("calculation_type"));
                Log.d("CalculadoraViewModel", "Calculation Type: " + result.getCalculationType());
            }

            if (responseBody.containsKey("calculated_quantity")) {
                result.setCalculatedQuantity(((Number) responseBody.get("calculated_quantity")).doubleValue());
                Log.d("CalculadoraViewModel", "Calculated Quantity: " + result.getCalculatedQuantity());
            }

            if (responseBody.containsKey("unit")) {
                result.setUnit((String) responseBody.get("unit"));
                Log.d("CalculadoraViewModel", "Unit: " + result.getUnit());
            }

            if (responseBody.containsKey("estimated_cost") && responseBody.get("estimated_cost") != null) {
                result.setEstimatedCost(((Number) responseBody.get("estimated_cost")).doubleValue());
                Log.d("CalculadoraViewModel", "Estimated Cost: " + result.getEstimatedCost());
            }

            // Detalles específicos y resultados detallados
            if (responseBody.containsKey("detailed_results")) {
                result.setDetailedResults((Map<String, Object>) responseBody.get("detailed_results"));
                Log.d("CalculadoraViewModel", "Detailed Results: " + result.getDetailedResults().size() + " elementos");
            }

            if (responseBody.containsKey("specific_details")) {
                result.setSpecificDetails((Map<String, Object>) responseBody.get("specific_details"));
                Log.d("CalculadoraViewModel", "Specific Details: " + result.getSpecificDetails().size() + " elementos");
            }

            // *** PARSING DE MATERIAL SUGGESTIONS - PARTE CRÍTICA ***
            if (responseBody.containsKey("material_suggestions")) {
                Object suggestionsObj = responseBody.get("material_suggestions");
                Log.d("CalculadoraViewModel", "Material suggestions object: " + suggestionsObj);

                if (suggestionsObj instanceof List) {
                    List<Object> suggestionsList = (List<Object>) suggestionsObj;
                    Log.d("CalculadoraViewModel", "Material suggestions list size: " + suggestionsList.size());

                    List<CalculationResponse.MaterialSuggestion> materialSuggestions = new ArrayList<>();

                    for (Object suggestionObj : suggestionsList) {
                        if (suggestionObj instanceof Map) {
                            Map<String, Object> suggestionMap = (Map<String, Object>) suggestionObj;

                            CalculationResponse.MaterialSuggestion suggestion = new CalculationResponse.MaterialSuggestion();

                            // Parsing seguro del ID (puede venir como Double o Integer)
                            if (suggestionMap.containsKey("id")) {
                                Object idObj = suggestionMap.get("id");
                                if (idObj instanceof Number) {
                                    suggestion.setId(((Number) idObj).intValue());
                                } else if (idObj instanceof String) {
                                    try {
                                        suggestion.setId(Integer.parseInt((String) idObj));
                                    } catch (NumberFormatException e) {
                                        Log.w("CalculadoraViewModel", "No se pudo parsear ID: " + idObj);
                                    }
                                }
                            }

                            // Parsing de strings simples
                            if (suggestionMap.containsKey("name")) {
                                suggestion.setName((String) suggestionMap.get("name"));
                            }

                            if (suggestionMap.containsKey("code")) {
                                suggestion.setCode((String) suggestionMap.get("code"));
                            }

                            if (suggestionMap.containsKey("unit")) {
                                suggestion.setUnit((String) suggestionMap.get("unit"));
                            }

                            // Parsing seguro del precio (puede venir como String o Number)
                            if (suggestionMap.containsKey("reference_price") && suggestionMap.get("reference_price") != null) {
                                Object priceObj = suggestionMap.get("reference_price");
                                try {
                                    if (priceObj instanceof Number) {
                                        suggestion.setReferencePrice(((Number) priceObj).doubleValue());
                                    } else if (priceObj instanceof String) {
                                        suggestion.setReferencePrice(Double.parseDouble((String) priceObj));
                                    }
                                    Log.d("CalculadoraViewModel", "Precio parseado: " + suggestion.getReferencePrice());
                                } catch (NumberFormatException e) {
                                    Log.w("CalculadoraViewModel", "No se pudo parsear precio: " + priceObj);
                                }
                            }

                            materialSuggestions.add(suggestion);
                            Log.d("CalculadoraViewModel", "Material suggestion agregada: " + suggestion.getName() + " (ID: " + suggestion.getId() + ", Precio: $" + suggestion.getReferencePrice() + ")");
                        }
                    }

                    result.setMaterialSuggestions(materialSuggestions);
                    Log.d("CalculadoraViewModel", "Total material suggestions parseadas: " + materialSuggestions.size());

                } else {
                    Log.w("CalculadoraViewModel", "material_suggestions no es una lista: " + suggestionsObj.getClass().getSimpleName());
                }
            } else {
                Log.w("CalculadoraViewModel", "No se encontró 'material_suggestions' en la respuesta");
                Log.d("CalculadoraViewModel", "Claves disponibles en la respuesta: " + responseBody.keySet());
            }

        } catch (Exception e) {
            Log.e("CalculadoraViewModel", "Error al procesar respuesta: " + e.getMessage(), e);
            errorMessage.setValue("Error al procesar respuesta: " + e.getMessage());
        }

        return result;
    }

    /**
     * Agregar cálculo al presupuesto
     */
    public void addCalculationToBudget(int calculationId, Integer supplierId, Double unitPriceOverride, String spaces, String notes) {
        isLoading.setValue(true);

        Map<String, Object> parameters = new HashMap<>();

        if (supplierId != null) {
            parameters.put("supplier_id", supplierId);
        }
        if (unitPriceOverride != null) {
            parameters.put("unit_price_override", unitPriceOverride);
        }
        if (spaces != null && !spaces.trim().isEmpty()) {
            parameters.put("spaces", spaces);
        }
        if (notes != null && !notes.trim().isEmpty()) {
            parameters.put("notes", notes);
        }

        Log.d("CalculadoraViewModel", "=== ENVIANDO AL PRESUPUESTO ===");
        Log.d("CalculadoraViewModel", "Parámetros enviados: " + parameters.toString());

        // Llamada para agregar al presupuesto (calculationId va en la URL)
        Call<Map<String, Object>> call = apiService.addCalculationToBudget(calculationId, parameters);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                isLoading.setValue(false);

                if (response.isSuccessful()) {
                    Log.d("CalculadoraViewModel", "Cálculo agregado al presupuesto exitosamente");
                    isAddedToBudget.setValue(true);
                } else {
                    String error = "Error al agregar al presupuesto: " + response.code();

                    // Intentar obtener el mensaje de error del servidor
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("CalculadoraViewModel", "Error del servidor: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e("CalculadoraViewModel", "Error al leer respuesta de error: " + e.getMessage());
                    }

                    errorMessage.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de conexión al agregar al presupuesto: " + t.getMessage());
            }
        });
    }

    /**
     * Limpiar resultados
     */
    public void clearResults() {
        calculationResult.setValue(null);
        errorMessage.setValue(null);
        isAddedToBudget.setValue(false);
    }

    /**
     * Interfaz para callback de carga de proveedores
     */
    public interface ProvidersCallback {
        void onProvidersLoaded(List<SupplierWithPrice> providers);
        void onError(String error);
    }

    /**
     * Cargar proveedores que tienen precios para un material específico
     * @param materialId ID del material
     * @param callback Callback para el resultado
     */
    public void loadProvidersForMaterial(Long materialId, ProvidersCallback callback) {
        Log.d("CalculadoraViewModel", "=== CARGANDO PROVEEDORES PARA MATERIAL ID: " + materialId + " ===");

        Call<PaginatedResponse<Map<String, Object>>> call = apiService.getSupplierPrices(null, materialId);

        call.enqueue(new Callback<PaginatedResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Map<String, Object>>> call,
                                   Response<PaginatedResponse<Map<String, Object>>> response) {

                Log.d("CalculadoraViewModel", "Respuesta de proveedores: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    PaginatedResponse<Map<String, Object>> paginatedResponse = response.body();

                    Log.d("CalculadoraViewModel", "Total proveedores encontrados: " + paginatedResponse.getCount());
                    Log.d("CalculadoraViewModel", "Proveedores en results: " +
                            (paginatedResponse.getResults() != null ? paginatedResponse.getResults().size() : "null"));

                    if (paginatedResponse.getResults() != null && !paginatedResponse.getResults().isEmpty()) {
                        List<SupplierWithPrice> providers = parseSupplierPrices(paginatedResponse.getResults());
                        Log.d("CalculadoraViewModel", "Proveedores parseados: " + providers.size());
                        callback.onProvidersLoaded(providers);
                    } else {
                        Log.w("CalculadoraViewModel", "No se encontraron proveedores para el material ID: " + materialId);
                        callback.onProvidersLoaded(new ArrayList<>()); // Lista vacía
                    }
                } else {
                    String error = "Error cargando proveedores: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            error += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("CalculadoraViewModel", "Error leyendo error body", e);
                        }
                    }
                    Log.e("CalculadoraViewModel", error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Map<String, Object>>> call, Throwable t) {
                String errorMsg = "Error de conexión: " + t.getMessage();
                Log.e("CalculadoraViewModel", errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Cargar proveedores por categoría de material
     * @param categoryType Tipo de categoría ("construction", "lighting", "electrical")
     * @param callback Callback para el resultado
     */
    public void loadProvidersByCategory(String categoryType, ProvidersCallback callback) {
        Call<PaginatedResponse<Supplier>> call = apiService.getSuppliersByCategory(categoryType);

        call.enqueue(new Callback<PaginatedResponse<Supplier>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<Supplier>> call,
                                   Response<PaginatedResponse<Supplier>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SupplierWithPrice> providers = convertSuppliersToSupplierWithPrice(
                            response.body().getResults()
                    );
                    callback.onProvidersLoaded(providers);
                } else {
                    callback.onError("Error cargando proveedores por categoría: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<Supplier>> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Parsear respuesta de precios de proveedores del backend
     */
    private List<SupplierWithPrice> parseSupplierPrices(List<Map<String, Object>> pricesData) {
        List<SupplierWithPrice> suppliers = new ArrayList<>();

        for (Map<String, Object> priceData : pricesData) {
            try {
                Log.d("CalculadoraViewModel", "Parseando proveedor: " + priceData);

                SupplierWithPrice supplier = new SupplierWithPrice();

                // Información del proveedor (formato directo, no anidado)
                if (priceData.containsKey("supplier")) {
                    Object supplierIdObj = priceData.get("supplier");
                    if (supplierIdObj instanceof Number) {
                        supplier.setSupplierId(((Number) supplierIdObj).longValue());
                    }
                }

                if (priceData.containsKey("supplier_name")) {
                    supplier.setSupplierName((String) priceData.get("supplier_name"));
                    supplier.setCommercialName((String) priceData.get("supplier_name")); // Usar mismo nombre
                }

                // Tiempo de entrega por defecto (se puede mejorar después)
                supplier.setDeliveryTime("2-3 días");
                supplier.setRating(4.0); // Rating por defecto
                supplier.setPreferred(false);
                supplier.setLocation("Quito, Ecuador");
                supplier.setPhone("");
                supplier.setWhatsappUrl("");

                // Información del precio (conversión segura)
                if (priceData.containsKey("price")) {
                    Object priceObj = priceData.get("price");
                    try {
                        if (priceObj instanceof String) {
                            supplier.setPrice(Double.parseDouble((String) priceObj));
                        } else if (priceObj instanceof Number) {
                            supplier.setPrice(((Number) priceObj).doubleValue());
                        }
                    } catch (NumberFormatException e) {
                        Log.w("CalculadoraViewModel", "Error parseando precio: " + priceObj);
                        supplier.setPrice(0.0);
                    }
                }

                // Moneda
                supplier.setCurrency((String) priceData.getOrDefault("currency", "USD"));

                // Descuento
                if (priceData.containsKey("discount_percentage")) {
                    Object discountObj = priceData.get("discount_percentage");
                    try {
                        if (discountObj instanceof String) {
                            supplier.setDiscountPercentage(Double.parseDouble((String) discountObj));
                        } else if (discountObj instanceof Number) {
                            supplier.setDiscountPercentage(((Number) discountObj).doubleValue());
                        }
                    } catch (NumberFormatException e) {
                        Log.w("CalculadoraViewModel", "Error parseando descuento: " + discountObj);
                        supplier.setDiscountPercentage(0.0);
                    }
                }

                suppliers.add(supplier);
                Log.d("CalculadoraViewModel", "Proveedor parseado: " + supplier.getSupplierName() + " - $" + supplier.getPrice());

            } catch (Exception e) {
                Log.e("CalculadoraViewModel", "Error parsing supplier price data", e);
            }
        }

        // Ordenar por precio (menor a mayor)
        suppliers.sort((s1, s2) -> Double.compare(s1.getFinalPrice(), s2.getFinalPrice()));

        Log.d("CalculadoraViewModel", "Total proveedores parseados exitosamente: " + suppliers.size());
        return suppliers;
    }

    /**
     * Convertir lista de Supplier a SupplierWithPrice (sin precios específicos)
     */
    private List<SupplierWithPrice> convertSuppliersToSupplierWithPrice(List<Supplier> suppliers) {
        List<SupplierWithPrice> result = new ArrayList<>();

        for (Supplier supplier : suppliers) {
            SupplierWithPrice supplierWithPrice = new SupplierWithPrice();
            supplierWithPrice.setSupplierId(supplier.getId());
            supplierWithPrice.setSupplierName(supplier.getName());
            supplierWithPrice.setCommercialName(supplier.getCommercialName());
            supplierWithPrice.setDeliveryTime(supplier.getDeliveryTime());
            supplierWithPrice.setRating(supplier.getRating());
            supplierWithPrice.setPreferred(supplier.getIsPreferred());
            supplierWithPrice.setLocation(supplier.getCity());
            supplierWithPrice.setPhone(supplier.getPhone());
            supplierWithPrice.setWhatsappUrl(supplier.getWhatsappUrl());

            // Precio por defecto si no se encuentra precio específico
            supplierWithPrice.setPrice(0.0);
            supplierWithPrice.setCurrency("USD");

            result.add(supplierWithPrice);
        }

        return result;
    }

    /**
     * Recalcular costo estimado para una cantidad específica con proveedor seleccionado
     * @param quantity Cantidad calculada
     * @param supplierId ID del proveedor
     * @param materialId ID del material
     * @return Costo estimado
     */
    public Double calculateEstimatedCost(double quantity, Long supplierId, Long materialId) {
        // En este método podrías implementar lógica adicional como:
        // - Aplicar descuentos por volumen
        // - Incluir costos de envío
        // - Agregar margen de ganancia

        // Por ahora, retorna un cálculo simple basado en cantidad * precio
        // Este método se podría extender para hacer una llamada al backend si es necesario
        return null; // Se calcula en el frontend cuando se selecciona el proveedor
    }
}