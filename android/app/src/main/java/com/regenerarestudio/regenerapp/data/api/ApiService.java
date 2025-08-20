package com.regenerarestudio.regenerapp.data.api;

import com.regenerarestudio.regenerapp.data.models.Material;
import com.regenerarestudio.regenerapp.data.models.Project;
import com.regenerarestudio.regenerapp.data.models.Supplier;
import com.regenerarestudio.regenerapp.data.responses.ApiResponse;
import com.regenerarestudio.regenerapp.data.responses.DashboardResponse;
import com.regenerarestudio.regenerapp.data.responses.PaginatedResponse;
import com.regenerarestudio.regenerapp.data.responses.ProjectSelectionResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Interfaz de servicios API para comunicación con backend Django
 * Define todas las llamadas REST disponibles
 */
public interface ApiService {

    // ==========================================
    // PROYECTOS - Projects APIs - CORREGIDO
    // ==========================================

    /**
     * Obtener lista de proyectos con filtros opcionales
     * GET /api/projects/projects/
     */
    @GET("projects/projects/")
    Call<PaginatedResponse<Project>> getProjects(
            @Query("search") String search,
            @Query("project_type") String projectType,
            @Query("status") String status,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("only_selected") Boolean onlySelected
    );

    /**
     * Obtener un proyecto específico por ID
     * GET /api/projects/projects/{id}/
     */
    @GET("projects/projects/{id}/")
    Call<Project> getProject(@Path("id") Long projectId);

    /**
     * Seleccionar un proyecto (marca como seleccionado)
     * POST /api/projects/projects/{id}/select_project/
     */
    @POST("projects/projects/{id}/select_project/")
    Call<ProjectSelectionResponse> selectProject(@Path("id") Long projectId);

    /**
     * Obtener datos del dashboard para un proyecto - CORREGIDO
     * GET /api/projects/projects/{id}/dashboard/
     * Retorna Map<String, Object> para mayor flexibilidad
     */
    @GET("projects/projects/{id}/dashboard/")
    Call<Map<String, Object>> getDashboard(@Path("id") Long projectId);

    /**
     * Obtener datos del dashboard (versión con DashboardResponse si la necesitas)
     * GET /api/projects/projects/{id}/dashboard/
     */
    @GET("projects/projects/{id}/dashboard/")
    Call<DashboardResponse> getDashboardTyped(@Path("id") Long projectId);

    /**
     * Crear un nuevo proyecto
     * POST /api/projects/projects/
     */
    @POST("projects/projects/")
    Call<Project> createProject(@Body Project project);

    /**
     * Actualizar un proyecto existente
     * PUT /api/projects/projects/{id}/
     */
    @PUT("projects/projects/{id}/")
    Call<Project> updateProject(@Path("id") Long projectId, @Body Project project);

    // ==========================================
    // MATERIALES - Materials APIs
    // ==========================================

    /**
     * Obtener lista de categorías de materiales
     * GET /api/materials/categories/
     */
    @GET("materials/categories/")
    Call<PaginatedResponse<Material.MaterialCategory>> getMaterialCategories(
            @Query("category_type") String categoryType
    );

    /**
     * Obtener lista de materiales con filtros
     * GET /api/materials/materials/
     */
    @GET("materials/materials/")
    Call<PaginatedResponse<Material>> getMaterials(
            @Query("search") String search,
            @Query("category") Long categoryId,
            @Query("unit") String unit,
            @Query("category_type") String categoryType
    );

    /**
     * Obtener un material específico por ID
     * GET /api/materials/materials/{id}/
     */
    @GET("materials/materials/{id}/")
    Call<Material> getMaterial(@Path("id") Long materialId);

    /**
     * Obtener materiales para calculadora específica
     * GET /api/materials/materials/for_calculator/
     */
    @GET("materials/materials/for_calculator/")
    Call<PaginatedResponse<Material>> getMaterialsForCalculator(
            @Query("calculator_type") String calculatorType
    );

    /**
     * Buscar materiales por código o nombre
     * GET /api/materials/materials/search/
     */
    @GET("materials/materials/search/")
    Call<PaginatedResponse<Material>> searchMaterials(
            @Query("q") String query,
            @Query("limit") Integer limit
    );

    // ==========================================
    // PROVEEDORES - Suppliers APIs
    // ==========================================

    /**
     * Obtener lista de proveedores con filtros
     * GET /api/suppliers/suppliers/
     */
    @GET("suppliers/suppliers/")
    Call<PaginatedResponse<Supplier>> getSuppliers(
            @Query("search") String search,
            @Query("supplier_type") String supplierType,
            @Query("city") String city,
            @Query("is_preferred") Boolean isPreferred,
            @Query("is_active") Boolean isActive
    );

    /**
     * Obtener un proveedor específico por ID
     * GET /api/suppliers/suppliers/{id}/
     */
    @GET("suppliers/suppliers/{id}/")
    Call<Supplier> getSupplier(@Path("id") Long supplierId);

    /**
     * Obtener proveedores por categoría de material
     * GET /api/suppliers/suppliers/by_category/
     */
    @GET("suppliers/suppliers/by_category/")
    Call<PaginatedResponse<Supplier>> getSuppliersByCategory(
            @Query("category_type") String categoryType
    );

    /**
     * Obtener precios de un proveedor para materiales específicos
     * GET /api/suppliers/prices/
     */
    @GET("suppliers/prices/")
    Call<PaginatedResponse<Map<String, Object>>> getSupplierPrices(
            @Query("supplier") Long supplierId,
            @Query("material") Long materialId
    );

// ==========================================
// PRESUPUESTOS - Budgets APIs - URLS CORREGIDAS CON PAGINATEDRESPONSE
// ==========================================

    /**
     * Obtener presupuesto inicial de un proyecto - CORREGIDO PARA USAR PAGINATEDRESPONSE
     * GET /api/budgets/budget-items/?project={projectId}
     */
    @GET("budgets/budget-items/")
    Call<PaginatedResponse<Map<String, Object>>> getInitialBudget(
            @Query("project") Long projectId
    );

    /**
     * Obtener gastos reales de un proyecto - CORREGIDO PARA USAR PAGINATEDRESPONSE
     * GET /api/budgets/real-expenses/?project={projectId}
     */
    @GET("budgets/real-expenses/")
    Call<PaginatedResponse<Map<String, Object>>> getExpenses(
            @Query("project") Long projectId
    );

    /**
     * Obtener resumen financiero usando dashboard - MÉTODO OPTIMIZADO
     * Usa el endpoint de dashboard que ya incluye financial_summary
     * GET /api/projects/projects/{projectId}/dashboard/
     */
    default Call<Map<String, Object>> getBudgetSummary(Long projectId) {
        return getDashboard(projectId);
    }

    /**
     * Obtener resumen financiero usando endpoint específico - ALTERNATIVO
     * GET /api/budgets/financial-summary/?project={projectId}
     */
    @GET("budgets/financial-summary/")
    Call<List<Map<String, Object>>> getFinancialSummaryList(
            @Query("project") Long projectId
    );

    /**
     * Agregar item al presupuesto inicial
     * POST /api/budgets/budget-items/
     */
    @POST("budgets/budget-items/")
    Call<Map<String, Object>> addToBudget(@Body Map<String, Object> budgetItem);

    /**
     * Agregar gasto real
     * POST /api/budgets/real-expenses/
     */
    @POST("budgets/real-expenses/")
    Call<Map<String, Object>> addExpense(@Body Map<String, Object> expense);

    /**
     * Actualizar item del presupuesto inicial
     * PUT /api/budgets/budget-items/{id}/
     */
    @PUT("budgets/budget-items/{id}/")
    Call<Map<String, Object>> updateBudgetItem(
            @Path("id") Long budgetItemId,
            @Body Map<String, Object> budgetItem
    );

    /**
     * Eliminar item del presupuesto inicial
     * DELETE /api/budgets/budget-items/{id}/
     */
    @DELETE("budgets/budget-items/{id}/")
    Call<Void> deleteBudgetItem(@Path("id") Long budgetItemId);

    /**
     * Actualizar gasto real
     * PUT /api/budgets/real-expenses/{id}/
     */
    @PUT("budgets/real-expenses/{id}/")
    Call<Map<String, Object>> updateExpense(
            @Path("id") Long expenseId,
            @Body Map<String, Object> expense
    );

    /**
     * Eliminar gasto real
     * DELETE /api/budgets/real-expenses/{id}/
     */
    @DELETE("budgets/real-expenses/{id}/")
    Call<Void> deleteExpense(@Path("id") Long expenseId);

    /**
     * Copiar item del presupuesto a gastos reales
     * POST /api/budgets/budget-items/{id}/copy_to_expense/
     */
    @POST("budgets/budget-items/{id}/copy_to_expense/")
    Call<Map<String, Object>> copyBudgetItemToExpense(
            @Path("id") Long budgetItemId,
            @Body Map<String, Object> expenseData
    );

    /**
     * Copiar múltiples items del presupuesto a gastos reales
     * POST /api/budgets/budget-items/copy-multiple-to-expenses/
     */
    @POST("budgets/budget-items/copy-multiple-to-expenses/")
    Call<Map<String, Object>> copyMultipleBudgetItemsToExpenses(
            @Body Map<String, Object> requestData
    );

    /**
     * Obtener resumen del presupuesto por categorías
     * GET /api/budgets/budget-items/summary_by_category/?project_id={projectId}
     */
    @GET("budgets/budget-items/summary_by_category/")
    Call<Map<String, Object>> getBudgetSummaryByCategory(
            @Query("project_id") Long projectId
    );

    /**
     * Obtener resumen de gastos reales por categorías
     * GET /api/budgets/real-expenses/summary_by_category/?project_id={projectId}
     */
    @GET("budgets/real-expenses/summary_by_category/")
    Call<Map<String, Object>> getExpensesSummaryByCategory(
            @Query("project_id") Long projectId
    );

    // ==========================================
    // CALCULADORAS - Calculations APIs
    // ==========================================

    /**
     * Obtener tipos de calculadora disponibles
     * GET /api/calculations/types/
     */
    @GET("calculations/types/")
    Call<List<Map<String, Object>>> getCalculationTypes();

    /**
     * Realizar cálculo de pintura
     * POST /api/calculations/calculate/paint/
     */
    @POST("calculations/calculate/paint/")
    Call<Map<String, Object>> calculatePaint(@Body Map<String, Object> parameters);

    /**
     * Realizar cálculo de gypsum
     * POST /api/calculations/calculate/gypsum/
     */
    @POST("calculations/calculate/gypsum/")
    Call<Map<String, Object>> calculateGypsum(@Body Map<String, Object> parameters);

    /**
     * Realizar cálculo de empaste
     * POST /api/calculations/calculate/empaste/
     */
    @POST("calculations/calculate/empaste/")
    Call<Map<String, Object>> calculateEmpaste(@Body Map<String, Object> parameters);

    /**
     * Realizar cálculo de cintas LED
     * POST /api/calculations/calculate/led/
     */
    @POST("calculations/calculate/led/")
    Call<Map<String, Object>> calculateLED(@Body Map<String, Object> parameters);

    /**
     * Realizar cálculo de perfiles
     * POST /api/calculations/calculate/profiles/
     */
    @POST("calculations/calculate/profiles/")
    Call<Map<String, Object>> calculateProfiles(@Body Map<String, Object> parameters);

    /**
     * Realizar cálculo de cables
     * POST /api/calculations/calculate/cables/
     */
    @POST("calculations/calculate/cables/")
    Call<Map<String, Object>> calculateCables(@Body Map<String, Object> parameters);

    /**
     * Obtener historial de cálculos de un proyecto
     * GET /api/calculations/calculations/
     */
    @GET("calculations/calculations/")
    Call<List<Map<String, Object>>> getCalculationHistory(
            @Query("project") Long projectId,
            @Query("calculation_type") String calculationType
    );

    /**
     * Agregar cálculo al presupuesto inicial
     * POST /api/calculations/calculations/{id}/add_to_budget/
     */
    @POST("calculations/calculations/{id}/add_to_budget/")
    Call<Map<String, Object>> addCalculationToBudget(
            @Path("id") int calculationId,
            @Body Map<String, Object> parameters
    );

    // ==========================================
    // FINANCIAL SUMMARY APIs - NUEVAS
    // ==========================================

    /**
     * Obtener resumen financiero del proyecto seleccionado
     * GET /api/budgets/financial-summary/for_selected_project/
     */
    @GET("budgets/financial-summary/for_selected_project/")
    Call<Map<String, Object>> getFinancialSummaryForSelectedProject();

    /**
     * Refrescar/actualizar resumen financiero específico
     * POST /api/budgets/financial-summary/{id}/refresh/
     */
    @POST("budgets/financial-summary/{id}/refresh/")
    Call<Map<String, Object>> refreshFinancialSummary(@Path("id") Long summaryId);

    // ==========================================
    // UTILIDADES GENERALES
    // ==========================================

    /**
     * Verificar estado del servidor
     * GET /api/health/
     */
    @GET("health/")
    Call<Map<String, Object>> getServerHealth();

    /**
     * Obtener configuración general de la app
     * GET /api/config/
     */
    @GET("config/")
    Call<Map<String, Object>> getAppConfig();
}