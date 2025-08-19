package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.regenerarestudio.regenerapp.data.api.ApiClient;
import com.regenerarestudio.regenerapp.data.api.ApiService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel actualizado para manejar presupuestos usando APIs REST - URLs CORREGIDAS
 * Usa las URLs reales del backend Django
 */
public class PresupuestosViewModel extends AndroidViewModel {

    private static final String TAG = "PresupuestosViewModel";

    // API Service
    private final ApiService apiService;

    // LiveData para presupuesto inicial
    private final MutableLiveData<List<Map<String, Object>>> budgetInitialLiveData = new MutableLiveData<>();

    // LiveData para gastos reales
    private final MutableLiveData<List<Map<String, Object>>> expensesRealLiveData = new MutableLiveData<>();

    // LiveData para resumen financiero
    private final MutableLiveData<Map<String, Object>> financialSummaryLiveData = new MutableLiveData<>();

    // Estados de carga
    private final MutableLiveData<Boolean> isLoadingBudgetLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingExpensesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingSummaryLiveData = new MutableLiveData<>();

    // Estados de error
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    // Proyecto actual
    private Long currentProjectId;

    public PresupuestosViewModel(@NonNull Application application) {
        super(application);

        // Inicializar API service
        this.apiService = ApiClient.getApiService();

        // Inicializar estados
        isLoadingBudgetLiveData.setValue(false);
        isLoadingExpensesLiveData.setValue(false);
        isLoadingSummaryLiveData.setValue(false);
        errorLiveData.setValue(null);

        Log.d(TAG, "PresupuestosViewModel inicializado con URLs corregidas del backend");
    }

    // ==========================================
    // GETTERS PARA LIVEDATA
    // ==========================================

    public LiveData<List<Map<String, Object>>> getBudgetInitial() {
        return budgetInitialLiveData;
    }

    public LiveData<List<Map<String, Object>>> getExpensesReal() {
        return expensesRealLiveData;
    }

    public LiveData<Map<String, Object>> getFinancialSummary() {
        return financialSummaryLiveData;
    }

    public LiveData<Boolean> getIsLoadingBudget() {
        return isLoadingBudgetLiveData;
    }

    public LiveData<Boolean> getIsLoadingExpenses() {
        return isLoadingExpensesLiveData;
    }

    public LiveData<Boolean> getIsLoadingSummary() {
        return isLoadingSummaryLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    // ==========================================
    // MÉTODOS PRINCIPALES
    // ==========================================

    /**
     * Cargar todos los datos de presupuestos para un proyecto
     */
    public void loadAllBudgetData(Long projectId) {
        if (projectId == null || projectId <= 0) {
            Log.e(TAG, "ID de proyecto inválido: " + projectId);
            errorLiveData.setValue("Error: ID de proyecto inválido");
            return;
        }

        currentProjectId = projectId;

        Log.d(TAG, "Cargando todos los datos de presupuestos para proyecto: " + projectId);

        // Cargar datos en paralelo usando URLs corregidas
        loadBudgetInitial(projectId);
        loadExpensesReal(projectId);
        loadFinancialSummary(projectId);
    }

    /**
     * Cargar presupuesto inicial del proyecto
     * URL CORREGIDA: /api/budgets/budget-items/?project={projectId}
     */
    public void loadBudgetInitial(Long projectId) {
        if (projectId == null) {
            errorLiveData.setValue("Error: ID de proyecto es null");
            return;
        }

        Log.d(TAG, "Cargando presupuesto inicial para proyecto: " + projectId);

        isLoadingBudgetLiveData.setValue(true);
        errorLiveData.setValue(null);

        // URL CORREGIDA
        Call<List<Map<String, Object>>> call = apiService.getInitialBudget(projectId);

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call,
                                   @NonNull Response<List<Map<String, Object>>> response) {
                isLoadingBudgetLiveData.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Presupuesto inicial cargado exitosamente. Items: " + response.body().size());
                    budgetInitialLiveData.setValue(response.body());
                } else {
                    String error = "Error al cargar presupuesto inicial: " + response.code();
                    Log.e(TAG, error);

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error del servidor: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer mensaje de error", e);
                    }

                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                isLoadingBudgetLiveData.setValue(false);

                String error = "Error de conexión al cargar presupuesto inicial: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Cargar gastos reales del proyecto
     * URL CORREGIDA: /api/budgets/real-expenses/?project={projectId}
     */
    public void loadExpensesReal(Long projectId) {
        if (projectId == null) {
            errorLiveData.setValue("Error: ID de proyecto es null");
            return;
        }

        Log.d(TAG, "Cargando gastos reales para proyecto: " + projectId);

        isLoadingExpensesLiveData.setValue(true);
        errorLiveData.setValue(null);

        // URL CORREGIDA
        Call<List<Map<String, Object>>> call = apiService.getExpenses(projectId);

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call,
                                   @NonNull Response<List<Map<String, Object>>> response) {
                isLoadingExpensesLiveData.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Gastos reales cargados exitosamente. Items: " + response.body().size());
                    expensesRealLiveData.setValue(response.body());
                } else {
                    String error = "Error al cargar gastos reales: " + response.code();
                    Log.e(TAG, error);

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error del servidor: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer mensaje de error", e);
                    }

                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call, @NonNull Throwable t) {
                isLoadingExpensesLiveData.setValue(false);

                String error = "Error de conexión al cargar gastos reales: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Cargar resumen financiero del proyecto
     * URL OPTIMIZADA: Usar dashboard que ya incluye financial_summary
     * /api/projects/{projectId}/dashboard/
     */
    public void loadFinancialSummary(Long projectId) {
        if (projectId == null) {
            errorLiveData.setValue("Error: ID de proyecto es null");
            return;
        }

        Log.d(TAG, "Cargando resumen financiero para proyecto: " + projectId);

        isLoadingSummaryLiveData.setValue(true);
        errorLiveData.setValue(null);

        // URL OPTIMIZADA: Usar dashboard que ya incluye toda la información financiera
        Call<Map<String, Object>> call = apiService.getDashboard(projectId);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                isLoadingSummaryLiveData.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Dashboard cargado exitosamente");

                    // Extraer financial_summary del dashboard response
                    Map<String, Object> dashboardData = response.body();
                    Object financialSummaryObj = dashboardData.get("financial_summary");

                    if (financialSummaryObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> financialSummary = (Map<String, Object>) financialSummaryObj;
                        financialSummaryLiveData.setValue(financialSummary);
                        Log.d(TAG, "Resumen financiero extraído del dashboard exitosamente");
                    } else {
                        Log.w(TAG, "financial_summary no encontrado en dashboard response");
                        errorLiveData.setValue("Error: resumen financiero no disponible");
                    }
                } else {
                    String error = "Error al cargar resumen financiero: " + response.code();
                    Log.e(TAG, error);

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error del servidor: " + errorBody);
                            error += " - " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer mensaje de error", e);
                    }

                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                isLoadingSummaryLiveData.setValue(false);

                String error = "Error de conexión al cargar resumen financiero: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Refrescar todos los datos
     */
    public void refreshAllData() {
        if (currentProjectId != null) {
            Log.d(TAG, "Refrescando todos los datos para proyecto: " + currentProjectId);
            loadAllBudgetData(currentProjectId);
        } else {
            Log.w(TAG, "No hay proyecto seleccionado para refrescar");
            errorLiveData.setValue("No hay proyecto seleccionado");
        }
    }

    /**
     * Agregar item al presupuesto inicial
     * URL CORREGIDA: /api/budgets/budget-items/
     */
    public void addItemToBudget(Map<String, Object> budgetItem) {
        Log.d(TAG, "Agregando item al presupuesto inicial");

        isLoadingBudgetLiveData.setValue(true);
        errorLiveData.setValue(null);

        // URL CORREGIDA
        Call<Map<String, Object>> call = apiService.addToBudget(budgetItem);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                isLoadingBudgetLiveData.setValue(false);

                if (response.isSuccessful()) {
                    Log.d(TAG, "Item agregado al presupuesto exitosamente");
                    // Recargar presupuesto inicial
                    if (currentProjectId != null) {
                        loadBudgetInitial(currentProjectId);
                        loadFinancialSummary(currentProjectId); // Actualizar resumen también
                    }
                } else {
                    String error = "Error al agregar item al presupuesto: " + response.code();
                    Log.e(TAG, error);
                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                isLoadingBudgetLiveData.setValue(false);

                String error = "Error de conexión al agregar item: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Agregar gasto real
     * URL CORREGIDA: /api/budgets/real-expenses/
     */
    public void addExpense(Map<String, Object> expense) {
        Log.d(TAG, "Agregando gasto real");

        isLoadingExpensesLiveData.setValue(true);
        errorLiveData.setValue(null);

        // URL CORREGIDA
        Call<Map<String, Object>> call = apiService.addExpense(expense);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                isLoadingExpensesLiveData.setValue(false);

                if (response.isSuccessful()) {
                    Log.d(TAG, "Gasto agregado exitosamente");
                    // Recargar gastos reales
                    if (currentProjectId != null) {
                        loadExpensesReal(currentProjectId);
                        loadFinancialSummary(currentProjectId); // Actualizar resumen también
                    }
                } else {
                    String error = "Error al agregar gasto: " + response.code();
                    Log.e(TAG, error);
                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                isLoadingExpensesLiveData.setValue(false);

                String error = "Error de conexión al agregar gasto: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Limpiar datos cuando se cambie de proyecto
     */
    public void clearData() {
        Log.d(TAG, "Limpiando datos de presupuestos");

        budgetInitialLiveData.setValue(null);
        expensesRealLiveData.setValue(null);
        financialSummaryLiveData.setValue(null);
        errorLiveData.setValue(null);
        currentProjectId = null;
    }
}