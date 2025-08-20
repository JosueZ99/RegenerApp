package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.regenerarestudio.regenerapp.data.api.ApiClient;
import com.regenerarestudio.regenerapp.data.api.ApiService;
import com.regenerarestudio.regenerapp.data.responses.PaginatedResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel actualizado para manejar presupuestos usando APIs REST - SIN RESUMEN FINANCIERO
 * El resumen financiero ahora solo está en el Dashboard - CORREGIDO
 */
public class PresupuestosViewModel extends AndroidViewModel {

    private static final String TAG = "PresupuestosViewModel";

    // API Service
    private final ApiService apiService;

    // LiveData para presupuesto inicial
    private final MutableLiveData<List<Map<String, Object>>> budgetInitialLiveData = new MutableLiveData<>();

    // LiveData para gastos reales
    private final MutableLiveData<List<Map<String, Object>>> expensesRealLiveData = new MutableLiveData<>();

    // Estados de carga (REMOVIDO: isLoadingSummaryLiveData)
    private final MutableLiveData<Boolean> isLoadingBudgetLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingExpensesLiveData = new MutableLiveData<>();

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
        errorLiveData.setValue(null);

        Log.d(TAG, "PresupuestosViewModel inicializado - SIN resumen financiero");
    }

    // ==========================================
    // GETTERS PARA LIVEDATA (REMOVIDO: getFinancialSummary, getIsLoadingSummary)
    // ==========================================

    public LiveData<List<Map<String, Object>>> getBudgetInitial() {
        return budgetInitialLiveData;
    }

    public LiveData<List<Map<String, Object>>> getExpensesReal() {
        return expensesRealLiveData;
    }

    public LiveData<Boolean> getIsLoadingBudget() {
        return isLoadingBudgetLiveData;
    }

    public LiveData<Boolean> getIsLoadingExpenses() {
        return isLoadingExpensesLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    // ==========================================
    // MÉTODOS PRINCIPALES (SIMPLIFICADOS)
    // ==========================================

    /**
     * Cargar todos los datos de presupuestos para un proyecto - SOLO TABLAS
     */
    public void loadAllBudgetData(Long projectId) {
        if (projectId == null || projectId <= 0) {
            Log.e(TAG, "ID de proyecto inválido: " + projectId);
            errorLiveData.setValue("Error: ID de proyecto inválido");
            return;
        }

        currentProjectId = projectId;

        Log.d(TAG, "Cargando datos de presupuestos para proyecto: " + projectId);

        // Solo cargar presupuesto inicial y gastos reales
        loadBudgetInitial(projectId);
        loadExpensesReal(projectId);
    }

    /**
     * Cargar presupuesto inicial del proyecto
     * URL: /api/budgets/budget-items/?project={projectId}
     */
    public void loadBudgetInitial(Long projectId) {
        if (projectId == null) {
            errorLiveData.setValue("Error: ID de proyecto es null");
            return;
        }

        Log.d(TAG, "Cargando presupuesto inicial para proyecto: " + projectId);

        isLoadingBudgetLiveData.setValue(true);
        errorLiveData.setValue(null);

        Call<PaginatedResponse<Map<String, Object>>> call = apiService.getInitialBudget(projectId);

        call.enqueue(new Callback<PaginatedResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Map<String, Object>>> call,
                                   @NonNull Response<PaginatedResponse<Map<String, Object>>> response) {
                isLoadingBudgetLiveData.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    PaginatedResponse<Map<String, Object>> paginatedResponse = response.body();
                    List<Map<String, Object>> budgetItems = paginatedResponse.getResults();

                    if (budgetItems != null) {
                        Log.d(TAG, "Presupuesto inicial cargado exitosamente. Items: " + budgetItems.size());
                        budgetInitialLiveData.setValue(budgetItems);
                    } else {
                        Log.w(TAG, "Lista de presupuesto inicial es null");
                        budgetInitialLiveData.setValue(new ArrayList<>());
                    }
                } else {
                    String error = "Error al cargar presupuesto inicial: " + response.code();
                    Log.e(TAG, error);
                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                isLoadingBudgetLiveData.setValue(false);
                String error = "Error de conexión al cargar presupuesto inicial: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Cargar gastos reales del proyecto - CORREGIDO
     * URL: /api/budgets/real-expenses/?project={projectId}
     */
    public void loadExpensesReal(Long projectId) {
        if (projectId == null) {
            errorLiveData.setValue("Error: ID de proyecto es null");
            return;
        }

        Log.d(TAG, "Cargando gastos reales para proyecto: " + projectId);

        isLoadingExpensesLiveData.setValue(true);
        errorLiveData.setValue(null);

        Call<PaginatedResponse<Map<String, Object>>> call = apiService.getExpenses(projectId);

        call.enqueue(new Callback<PaginatedResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Map<String, Object>>> call,
                                   @NonNull Response<PaginatedResponse<Map<String, Object>>> response) {
                isLoadingExpensesLiveData.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    PaginatedResponse<Map<String, Object>> paginatedResponse = response.body();
                    List<Map<String, Object>> expenses = paginatedResponse.getResults();

                    if (expenses != null) {
                        Log.d(TAG, "Gastos reales cargados exitosamente. Items: " + expenses.size());
                        expensesRealLiveData.setValue(expenses);
                    } else {
                        Log.w(TAG, "Lista de gastos reales es null");
                        expensesRealLiveData.setValue(new ArrayList<>());
                    }
                } else {
                    String error = "Error al cargar gastos reales: " + response.code();
                    Log.e(TAG, error);
                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                isLoadingExpensesLiveData.setValue(false);
                String error = "Error de conexión al cargar gastos reales: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    // ==========================================
    // MÉTODOS CRUD (SIN REFRESH DE RESUMEN FINANCIERO) - CORREGIDOS
    // ==========================================

    /**
     * Agregar item al presupuesto inicial - SIN REFRESH AUTOMÁTICO DE RESUMEN - CORREGIDO
     * POST /api/budgets/budget-items/
     */
    public void addItemToBudget(Map<String, Object> budgetItem) {
        Log.d(TAG, "Agregando item al presupuesto");

        isLoadingBudgetLiveData.setValue(true);
        errorLiveData.setValue(null);

        Call<Map<String, Object>> call = apiService.addToBudget(budgetItem);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                isLoadingBudgetLiveData.setValue(false);

                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Item agregado al presupuesto exitosamente");

                    // Recargar presupuesto inicial
                    if (currentProjectId != null) {
                        loadBudgetInitial(currentProjectId);
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
     * Actualizar item del presupuesto inicial - SIN REFRESH AUTOMÁTICO DE RESUMEN
     * PUT /api/budgets/budget-items/{id}/
     */
    public void updateBudgetItem(Long budgetItemId, Map<String, Object> budgetItem) {
        Log.d(TAG, "Actualizando item del presupuesto ID: " + budgetItemId);

        isLoadingBudgetLiveData.setValue(true);
        errorLiveData.setValue(null);

        Call<Map<String, Object>> call = apiService.updateBudgetItem(budgetItemId, budgetItem);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                isLoadingBudgetLiveData.setValue(false);

                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Item del presupuesto actualizado exitosamente");

                    // Recargar presupuesto inicial
                    if (currentProjectId != null) {
                        loadBudgetInitial(currentProjectId);
                    }
                } else {
                    String error = "Error al actualizar item del presupuesto: " + response.code();
                    Log.e(TAG, error);
                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                isLoadingBudgetLiveData.setValue(false);
                String error = "Error de conexión al actualizar item: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Eliminar item del presupuesto inicial - SIN REFRESH AUTOMÁTICO DE RESUMEN
     * DELETE /api/budgets/budget-items/{id}/
     */
    public void deleteBudgetItem(Long budgetItemId) {
        Log.d(TAG, "Eliminando item del presupuesto ID: " + budgetItemId);

        isLoadingBudgetLiveData.setValue(true);
        errorLiveData.setValue(null);

        Call<Void> call = apiService.deleteBudgetItem(budgetItemId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                isLoadingBudgetLiveData.setValue(false);

                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Item del presupuesto eliminado exitosamente");

                    // Recargar presupuesto inicial
                    if (currentProjectId != null) {
                        loadBudgetInitial(currentProjectId);
                    }
                } else {
                    String error = "Error al eliminar item del presupuesto: " + response.code();
                    Log.e(TAG, error);
                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                isLoadingBudgetLiveData.setValue(false);
                String error = "Error de conexión al eliminar item: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Agregar gasto real - SIN REFRESH AUTOMÁTICO DE RESUMEN - CORREGIDO
     * POST /api/budgets/real-expenses/
     */
    public void addExpenseReal(Map<String, Object> expense) {
        Log.d(TAG, "Agregando gasto real");

        isLoadingExpensesLiveData.setValue(true);
        errorLiveData.setValue(null);

        Call<Map<String, Object>> call = apiService.addExpense(expense);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                isLoadingExpensesLiveData.setValue(false);

                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Gasto agregado exitosamente");

                    // Recargar gastos reales
                    if (currentProjectId != null) {
                        loadExpensesReal(currentProjectId);
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
     * Actualizar gasto real - SIN REFRESH AUTOMÁTICO DE RESUMEN - CORREGIDO
     * PUT /api/budgets/real-expenses/{id}/
     */
    public void updateExpenseReal(Long expenseId, Map<String, Object> expense) {
        Log.d(TAG, "Actualizando gasto real ID: " + expenseId);

        isLoadingExpensesLiveData.setValue(true);
        errorLiveData.setValue(null);

        Call<Map<String, Object>> call = apiService.updateExpense(expenseId, expense);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                isLoadingExpensesLiveData.setValue(false);

                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Gasto actualizado exitosamente");

                    // Recargar gastos reales
                    if (currentProjectId != null) {
                        loadExpensesReal(currentProjectId);
                    }
                } else {
                    String error = "Error al actualizar gasto real: " + response.code();
                    Log.e(TAG, error);
                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                isLoadingExpensesLiveData.setValue(false);
                String error = "Error de conexión al actualizar gasto: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Eliminar gasto real - SIN REFRESH AUTOMÁTICO DE RESUMEN - CORREGIDO
     * DELETE /api/budgets/real-expenses/{id}/
     */
    public void deleteExpenseReal(Long expenseId) {
        Log.d(TAG, "Eliminando gasto real ID: " + expenseId);

        isLoadingExpensesLiveData.setValue(true);
        errorLiveData.setValue(null);

        Call<Void> call = apiService.deleteExpense(expenseId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                isLoadingExpensesLiveData.setValue(false);

                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Gasto eliminado exitosamente");

                    // Recargar gastos reales
                    if (currentProjectId != null) {
                        loadExpensesReal(currentProjectId);
                    }
                } else {
                    String error = "Error al eliminar gasto real: " + response.code();
                    Log.e(TAG, error);
                    errorLiveData.setValue(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                isLoadingExpensesLiveData.setValue(false);
                String error = "Error de conexión al eliminar gasto: " + t.getMessage();
                Log.e(TAG, error, t);
                errorLiveData.setValue(error);
            }
        });
    }

    /**
     * Refrescar todos los datos - SIMPLIFICADO
     */
    public void refreshAllData() {
        if (currentProjectId != null) {
            Log.d(TAG, "Refrescando todos los datos para proyecto: " + currentProjectId);
            loadAllBudgetData(currentProjectId);
        } else {
            Log.w(TAG, "No hay proyecto seleccionado para refrescar");
        }
    }

    /**
     * Limpiar datos cuando se cambie de proyecto
     */
    public void clearData() {
        Log.d(TAG, "Limpiando datos de presupuestos");

        budgetInitialLiveData.setValue(null);
        expensesRealLiveData.setValue(null);
        errorLiveData.setValue(null);
        currentProjectId = null;
    }
}