package com.regenerarestudio.regenerapp.ui.calculadora;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.regenerarestudio.regenerapp.data.api.ApiClient;
import com.regenerarestudio.regenerapp.data.api.ApiService;
import com.regenerarestudio.regenerapp.data.models.CalculationResponse;

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
}