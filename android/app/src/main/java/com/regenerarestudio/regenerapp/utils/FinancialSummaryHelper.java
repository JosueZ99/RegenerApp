package com.regenerarestudio.regenerapp.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.regenerarestudio.regenerapp.data.api.ApiService;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper para actualizar el resumen financiero
 * Combina las dos APIs necesarias:
 * 1. GET /api/budgets/financial-summary/for_selected_project/ (obtener ID)
 * 2. POST /api/budgets/financial-summary/{id}/refresh/ (refrescar)
 */
public class FinancialSummaryHelper {

    private static final String TAG = "FinancialSummaryHelper";

    /**
     * Callback para notificar el resultado del refresh
     */
    public interface RefreshCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Refrescar resumen financiero del proyecto seleccionado
     * Este método automáticamente:
     * 1. Obtiene el ID del resumen financiero del proyecto seleccionado
     * 2. Llama al refresh de ese resumen específico
     */
    public static void refreshSelectedProjectSummary(ApiService apiService, RefreshCallback callback) {
        Log.d(TAG, "Iniciando refresh del resumen financiero...");

        // Paso 1: Obtener el ID del resumen financiero del proyecto seleccionado
        Call<Map<String, Object>> getIdCall = apiService.getFinancialSummaryForSelectedProject();

        getIdCall.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> summaryData = response.body();

                    // Extraer el ID del resumen financiero
                    Object idObj = summaryData.get("id");
                    if (idObj instanceof Number) {
                        Long summaryId = ((Number) idObj).longValue();
                        Log.d(TAG, "ID del resumen financiero obtenido: " + summaryId);

                        // Paso 2: Refrescar el resumen financiero usando el ID
                        refreshFinancialSummaryById(apiService, summaryId, callback);

                    } else {
                        String error = "No se pudo obtener el ID del resumen financiero";
                        Log.e(TAG, error);
                        if (callback != null) callback.onError(error);
                    }
                } else {
                    String error = "Error al obtener resumen financiero: " + response.code();
                    Log.e(TAG, error);
                    if (callback != null) callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                String error = "Error de conexión al obtener resumen financiero: " + t.getMessage();
                Log.e(TAG, error, t);
                if (callback != null) callback.onError(error);
            }
        });
    }

    /**
     * Refrescar resumen financiero por ID específico
     */
    private static void refreshFinancialSummaryById(ApiService apiService, Long summaryId, RefreshCallback callback) {
        Log.d(TAG, "Refrescando resumen financiero ID: " + summaryId);

        Call<Map<String, Object>> refreshCall = apiService.refreshFinancialSummary(summaryId);

        refreshCall.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Resumen financiero actualizado exitosamente");
                    if (callback != null) callback.onSuccess();
                } else {
                    String error = "Error al refrescar resumen: " + response.code();
                    Log.e(TAG, error);
                    if (callback != null) callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                String error = "Error de conexión al refrescar: " + t.getMessage();
                Log.e(TAG, error, t);
                if (callback != null) callback.onError(error);
            }
        });
    }
}