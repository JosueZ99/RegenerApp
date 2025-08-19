package com.regenerarestudio.regenerapp.ui.dashboard;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.regenerarestudio.regenerapp.data.api.ApiClient;
import com.regenerarestudio.regenerapp.data.api.ApiService;
import com.regenerarestudio.regenerapp.data.models.Project;
import com.regenerarestudio.regenerapp.data.network.NetworkStateManager;
import com.regenerarestudio.regenerapp.data.responses.DashboardResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel actualizado para Dashboard - Conectado con APIs REST
 * Maneja datos del proyecto seleccionado, resumen financiero y estadísticas
 */
public class DashboardViewModel extends AndroidViewModel {

    private static final String TAG = "DashboardViewModel";

    // API Service
    private final ApiService apiService;

    // LiveData para datos del dashboard
    private final MutableLiveData<DashboardResponse> dashboardDataLiveData = new MutableLiveData<>();
    private final MutableLiveData<Project> projectDataLiveData = new MutableLiveData<>();
    private final MutableLiveData<DashboardResponse.FinancialSummary> financialSummaryLiveData = new MutableLiveData<>();

    // Estado de carga
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    // Estado de red
    private final NetworkStateManager.NetworkStateLiveData networkState = new NetworkStateManager.NetworkStateLiveData();

    // ID del proyecto actual
    private Long currentProjectId = null;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.apiService = ApiClient.getApiService();

        // Inicializar valores por defecto
        isLoadingLiveData.setValue(false);
        errorLiveData.setValue(null);

        Log.d(TAG, "DashboardViewModel inicializado");
    }

    /**
     * Cargar datos del dashboard para un proyecto específico
     */
    public void loadDashboardData(Long projectId) {
        if (projectId == null || projectId <= 0) {
            Log.e(TAG, "ID de proyecto inválido: " + projectId);
            errorLiveData.setValue("Error: ID de proyecto inválido");
            return;
        }

        // Si es el mismo proyecto, no recargar
        if (projectId.equals(currentProjectId)) {
            Log.d(TAG, "Los datos del proyecto " + projectId + " ya están cargados");
            return;
        }

        currentProjectId = projectId;

        Log.d(TAG, "Cargando datos del dashboard para proyecto: " + projectId);

        // Mostrar indicador de carga
        isLoadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        // Llamada a la API
        Call<DashboardResponse> call = apiService.getDashboard(projectId);

        call.enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<DashboardResponse> call, @NonNull Response<DashboardResponse> response) {
                isLoadingLiveData.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Datos del dashboard cargados exitosamente");

                    DashboardResponse dashboardData = response.body();

                    // Actualizar LiveData
                    dashboardDataLiveData.setValue(dashboardData);

                    if (dashboardData.getProject() != null) {
                        projectDataLiveData.setValue(dashboardData.getProject());
                    }

                    if (dashboardData.getFinancialSummary() != null) {
                        financialSummaryLiveData.setValue(dashboardData.getFinancialSummary());
                    }

                    errorLiveData.setValue(null);

                } else {
                    String errorMsg = "Error al cargar dashboard: " + response.code();
                    Log.e(TAG, errorMsg);

                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer error body: " + e.getMessage());
                    }

                    errorLiveData.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardResponse> call, @NonNull Throwable t) {
                isLoadingLiveData.setValue(false);

                String errorMsg = "Error de conexión: " + t.getMessage();
                Log.e(TAG, errorMsg, t);

                errorLiveData.setValue(errorMsg);
            }
        });
    }

    /**
     * Refrescar datos del dashboard (forzar recarga)
     */
    public void refreshDashboard() {
        if (currentProjectId != null) {
            // Forzar recarga limpiando el ID actual
            Long projectId = currentProjectId;
            currentProjectId = null;
            loadDashboardData(projectId);
        } else {
            Log.w(TAG, "No hay proyecto seleccionado para refrescar");
        }
    }

    /**
     * Limpiar datos del dashboard
     */
    public void clearDashboard() {
        Log.d(TAG, "Limpiando datos del dashboard");

        currentProjectId = null;
        dashboardDataLiveData.setValue(null);
        projectDataLiveData.setValue(null);
        financialSummaryLiveData.setValue(null);
        errorLiveData.setValue(null);
        isLoadingLiveData.setValue(false);
    }

    // ==========================================
    // GETTERS PARA LIVEDATA
    // ==========================================

    /**
     * Obtener todos los datos del dashboard
     */
    public LiveData<DashboardResponse> getDashboardData() {
        return dashboardDataLiveData;
    }

    /**
     * Obtener datos del proyecto
     */
    public LiveData<Project> getProjectData() {
        return projectDataLiveData;
    }

    /**
     * Obtener resumen financiero
     */
    public LiveData<DashboardResponse.FinancialSummary> getFinancialSummary() {
        return financialSummaryLiveData;
    }

    /**
     * Obtener estado de carga
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    /**
     * Obtener errores
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * Obtener estado de red
     */
    public LiveData<NetworkStateManager.NetworkState> getNetworkState() {
        return networkState.getNetworkState();
    }

    // ==========================================
    // MÉTODOS DE UTILIDAD
    // ==========================================

    /**
     * Verificar si hay datos cargados
     */
    public boolean hasData() {
        return dashboardDataLiveData.getValue() != null;
    }

    /**
     * Obtener ID del proyecto actual
     */
    public Long getCurrentProjectId() {
        return currentProjectId;
    }

    /**
     * Verificar si está cargando
     */
    public boolean isLoading() {
        Boolean loading = isLoadingLiveData.getValue();
        return loading != null && loading;
    }

    /**
     * Obtener último error
     */
    public String getLastError() {
        return errorLiveData.getValue();
    }

    /**
     * Limpiar errores
     */
    public void clearError() {
        errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "DashboardViewModel destruido");
    }
}