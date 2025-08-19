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
            // Campos principales
            if (responseBody.containsKey("calculation_id")) {
                result.setCalculationId(((Number) responseBody.get("calculation_id")).intValue());
            }

            if (responseBody.containsKey("calculation_type")) {
                result.setCalculationType((String) responseBody.get("calculation_type"));
            }

            if (responseBody.containsKey("calculated_quantity")) {
                result.setCalculatedQuantity(((Number) responseBody.get("calculated_quantity")).doubleValue());
            }

            if (responseBody.containsKey("unit")) {
                result.setUnit((String) responseBody.get("unit"));
            }

            if (responseBody.containsKey("estimated_cost") && responseBody.get("estimated_cost") != null) {
                result.setEstimatedCost(((Number) responseBody.get("estimated_cost")).doubleValue());
            }

            // Detalles específicos y resultados detallados
            if (responseBody.containsKey("detailed_results")) {
                result.setDetailedResults((Map<String, Object>) responseBody.get("detailed_results"));
            }

            if (responseBody.containsKey("specific_details")) {
                result.setSpecificDetails((Map<String, Object>) responseBody.get("specific_details"));
            }

        } catch (Exception e) {
            errorMessage.setValue("Error al procesar respuesta: " + e.getMessage());
        }

        return result;
    }

    /**
     * Agregar cálculo al presupuesto
     */
    public void addCalculationToBudget(int calculationId, Integer supplierId, String spaces, String notes) {
        isLoading.setValue(true);

        Map<String, Object> parameters = new HashMap<>();

        if (supplierId != null) {
            parameters.put("supplier_id", supplierId);
        }
        if (spaces != null && !spaces.trim().isEmpty()) {
            parameters.put("spaces", spaces);
        }
        if (notes != null && !notes.trim().isEmpty()) {
            parameters.put("notes", notes);
        }

        // Llamada para agregar al presupuesto (calculationId va en la URL)
        Call<Map<String, Object>> call = apiService.addCalculationToBudget(calculationId, parameters);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                isLoading.setValue(false);

                if (response.isSuccessful()) {
                    isAddedToBudget.setValue(true);
                } else {
                    String error = "Error al agregar al presupuesto: " + response.code();
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
        Call<List<Map<String, Object>>> call = apiService.getSupplierPrices(null, materialId);

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SupplierWithPrice> providers = parseSupplierPrices(response.body());
                    callback.onProvidersLoaded(providers);
                } else {
                    String error = "Error cargando proveedores: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            error += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            // Ignorar errores de parsing del error body
                        }
                    }
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
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
                SupplierWithPrice supplier = new SupplierWithPrice();

                // Información del proveedor
                Map<String, Object> supplierData = (Map<String, Object>) priceData.get("supplier");
                if (supplierData != null) {
                    supplier.setSupplierId(((Number) supplierData.get("id")).longValue());
                    supplier.setSupplierName((String) supplierData.get("name"));
                    supplier.setCommercialName((String) supplierData.get("commercial_name"));
                    supplier.setDeliveryTime((String) supplierData.getOrDefault("delivery_time", "No especificado"));

                    if (supplierData.get("rating") != null) {
                        supplier.setRating(((Number) supplierData.get("rating")).doubleValue());
                    }

                    if (supplierData.get("is_preferred") != null) {
                        supplier.setPreferred((Boolean) supplierData.get("is_preferred"));
                    }

                    supplier.setLocation((String) supplierData.getOrDefault("location", ""));
                    supplier.setPhone((String) supplierData.getOrDefault("phone", ""));
                    supplier.setWhatsappUrl((String) supplierData.getOrDefault("whatsapp_url", ""));
                }

                // Información del precio
                if (priceData.get("price") != null) {
                    supplier.setPrice(((Number) priceData.get("price")).doubleValue());
                }

                supplier.setCurrency((String) priceData.getOrDefault("currency", "USD"));

                if (priceData.get("discount_percentage") != null) {
                    supplier.setDiscountPercentage(((Number) priceData.get("discount_percentage")).doubleValue());
                }

                suppliers.add(supplier);

            } catch (Exception e) {
                // Log error but continue processing other suppliers
                Log.e("CalculadoraViewModel", "Error parsing supplier price data", e);
            }
        }

        // Ordenar por precio (menor a mayor)
        suppliers.sort((s1, s2) -> Double.compare(s1.getFinalPrice(), s2.getFinalPrice()));

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