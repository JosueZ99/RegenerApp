package com.regenerarestudio.regenerapp.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.regenerarestudio.regenerapp.MainActivity;
import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.databinding.FragmentDashboardBinding;
import com.regenerarestudio.regenerapp.data.models.Project;
import com.regenerarestudio.regenerapp.data.responses.DashboardResponse;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment del Dashboard - Versión completa con todos los elementos financieros
 * Muestra información detallada del proyecto seleccionado desde el backend
 */
public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;

    // Información del proyecto (desde MainActivity)
    private long projectId;
    private String projectName;

    // Widgets del dashboard
    private MaterialCardView cardGoogleDrive;
    private MaterialCardView cardReports;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Formatters
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));
    private final SimpleDateFormat inputDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat outputDateFormatter = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "EC"));

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        android.util.Log.d(TAG, "onCreateView - Layout completo cargado");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        initializeViews();
        getProjectInfo();
        setupSwipeRefresh();
        setupClickListeners();
        observeViewModel();

        // Cargar datos del proyecto
        loadProjectData();
    }

    private void initializeViews() {
        cardGoogleDrive = binding.cardGoogleDrive;
        cardReports = binding.cardReports;
        swipeRefreshLayout = binding.swipeRefreshLayout;

        android.util.Log.d(TAG, "Views inicializadas - Layout completo");

        // Establecer textos de carga iniciales
        setLoadingTexts();
    }

    private void setLoadingTexts() {
        // Información del proyecto
        binding.tvProjectName.setText("Cargando proyecto...");
        binding.tvProjectClient.setText("Cargando cliente...");
        binding.tvStartDate.setText("--");
        binding.tvEndDate.setText("--");
        binding.chipProjectStatus.setText("Cargando...");

        // Información financiera
        binding.tvInitialBudget.setText("$0.00");
        binding.tvTotalExpenses.setText("$0.00");
        binding.tvRemainingBudget.setText("$0.00");
        binding.tvBudgetPercentage.setText("0.0%");
        binding.progressBarBudget.setProgress(0);

        // Categorías
        binding.tvConstructionBudget.setText("Cargando...");
        binding.tvLightingBudget.setText("Cargando...");
        binding.tvOthersBudget.setText("Cargando...");

        // Estadísticas
        binding.tvBudgetItemsCount.setText("0");
        binding.tvExpensesCount.setText("0");
        binding.tvCalculationsCount.setText("0");

        // Ocultar elementos opcionales
        binding.tvProjectLocation.setVisibility(View.GONE);
        binding.tvProjectDescription.setVisibility(View.GONE);
        binding.chipOverBudget.setVisibility(View.GONE);

        android.util.Log.d(TAG, "Textos de carga establecidos");
    }

    private void getProjectInfo() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            projectId = mainActivity.getSelectedProjectId();
            projectName = mainActivity.getSelectedProjectName();
            android.util.Log.d(TAG, "Proyecto desde MainActivity: ID=" + projectId + ", Nombre=" + projectName);
        }
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                android.util.Log.d(TAG, "SwipeRefresh activado");
                refreshDashboardData();
            });

            // Configurar colores del refresh
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.primary_color,
                    R.color.primary_variant,
                    android.R.color.holo_blue_bright
            );
        }
    }

    private void setupClickListeners() {
        cardGoogleDrive.setOnClickListener(v -> openGoogleDrive());
        cardReports.setOnClickListener(v -> openReports());
    }

    private void loadProjectData() {
        if (projectId > 0) {
            android.util.Log.d(TAG, "Cargando datos para proyecto ID: " + projectId);
            dashboardViewModel.loadDashboardData(projectId);
        } else {
            android.util.Log.e(TAG, "ID de proyecto inválido: " + projectId);
            showError("Error: No hay proyecto seleccionado");
        }
    }

    private void observeViewModel() {
        // Observar datos del proyecto
        dashboardViewModel.getProjectData().observe(getViewLifecycleOwner(), project -> {
            android.util.Log.d(TAG, "Observer: Datos de proyecto recibidos");
            if (project != null) {
                updateProjectInfo(project);
            }
        });

        // Observar resumen financiero
        dashboardViewModel.getFinancialSummary().observe(getViewLifecycleOwner(), financialSummary -> {
            android.util.Log.d(TAG, "Observer: Resumen financiero recibido");
            if (financialSummary != null) {
                updateFinancialSummary(financialSummary);
            }
        });

        // Observar datos completos del dashboard
        dashboardViewModel.getDashboardData().observe(getViewLifecycleOwner(), dashboardData -> {
            android.util.Log.d(TAG, "Observer: Datos completos del dashboard recibidos");
            if (dashboardData != null) {
                updateDashboardStatistics(dashboardData);
            }
        });

        // Observar estado de carga
        dashboardViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(isLoading);
            }

            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observar errores
        dashboardViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                android.util.Log.e(TAG, "Error observado: " + error);
                showError(error);
                dashboardViewModel.clearError();
            }
        });

        // Observar estado de red
        dashboardViewModel.getNetworkState().observe(getViewLifecycleOwner(), networkState -> {
            if (networkState != null && networkState.hasNoNetwork()) {
                showError("Sin conexión a internet");
            }
        });

        android.util.Log.d(TAG, "Observers configurados para layout completo");
    }

    private void updateProjectInfo(Project project) {
        if (project == null) return;

        android.util.Log.d(TAG, "=== ACTUALIZANDO INFORMACIÓN DEL PROYECTO ===");
        android.util.Log.d(TAG, "Proyecto: " + project.getName());
        android.util.Log.d(TAG, "Cliente: " + project.getClient());
        android.util.Log.d(TAG, "Ubicación: " + project.getLocation());
        android.util.Log.d(TAG, "Descripción: " + project.getDescription());

        // Información básica
        binding.tvProjectName.setText(project.getName());
        binding.tvProjectClient.setText(project.getClient());

        // Ubicación
        if (project.getLocation() != null && !project.getLocation().isEmpty()) {
            binding.tvProjectLocation.setText(project.getLocation());
            binding.tvProjectLocation.setVisibility(View.VISIBLE);
        } else {
            binding.tvProjectLocation.setVisibility(View.GONE);
        }

        // Descripción
        if (project.getDescription() != null && !project.getDescription().isEmpty()) {
            binding.tvProjectDescription.setText(project.getDescription());
            binding.tvProjectDescription.setVisibility(View.VISIBLE);
        } else {
            binding.tvProjectDescription.setVisibility(View.GONE);
        }

        // Fechas
        if (project.getStartDate() != null && !project.getStartDate().isEmpty()) {
            String formattedStartDate = formatDateString(project.getStartDate());
            binding.tvStartDate.setText(formattedStartDate);
            android.util.Log.d(TAG, "Fecha inicio formateada: " + formattedStartDate);
        }

        if (project.getEndDate() != null && !project.getEndDate().isEmpty()) {
            String formattedEndDate = formatDateString(project.getEndDate());
            binding.tvEndDate.setText(formattedEndDate);
            android.util.Log.d(TAG, "Fecha fin formateada: " + formattedEndDate);
        }

        // Estado del proyecto
        updateProjectStatus(project.getCurrentPhase());

        android.util.Log.d(TAG, "=== INFORMACIÓN DEL PROYECTO ACTUALIZADA ===");
    }

    private void updateProjectStatus(String phase) {
        if (phase == null) return;

        Chip statusChip = binding.chipProjectStatus;
        String statusText;
        int colorResource;

        switch (phase.toLowerCase()) {
            case "design":
                statusText = "En Diseño";
                colorResource = android.R.color.holo_blue_light;
                break;
            case "purchase":
                statusText = "En Compra";
                colorResource = android.R.color.holo_orange_light;
                break;
            case "installation":
                statusText = "En Instalación";
                colorResource = android.R.color.holo_purple;
                break;
            case "completed":
                statusText = "Completado";
                colorResource = android.R.color.holo_green_light;
                break;
            default:
                statusText = "Sin definir";
                colorResource = android.R.color.darker_gray;
                break;
        }

        statusChip.setText(statusText);
        statusChip.setChipBackgroundColorResource(colorResource);

        android.util.Log.d(TAG, "Estado actualizado a: " + statusText);
    }

    private void updateFinancialSummary(DashboardResponse.FinancialSummary financialSummary) {
        if (financialSummary == null) return;

        android.util.Log.d(TAG, "=== ACTUALIZANDO RESUMEN FINANCIERO ===");

        // Obtener valores con null safety
        Double totalBudget = financialSummary.getTotalBudget() != null ? financialSummary.getTotalBudget() : 0.0;
        Double totalExpenses = financialSummary.getTotalExpenses() != null ? financialSummary.getTotalExpenses() : 0.0;
        Double remainingBudget = financialSummary.getRemainingBudget() != null ? financialSummary.getRemainingBudget() : 0.0;
        Double percentageUsed = financialSummary.getBudgetUtilizationPercentage() != null ? financialSummary.getBudgetUtilizationPercentage() : 0.0;

        android.util.Log.d(TAG, "Presupuesto total: " + totalBudget);
        android.util.Log.d(TAG, "Total gastado: " + totalExpenses);
        android.util.Log.d(TAG, "Balance restante: " + remainingBudget);
        android.util.Log.d(TAG, "Porcentaje usado: " + percentageUsed + "%");

        // Actualizar valores principales
        binding.tvInitialBudget.setText(currencyFormatter.format(totalBudget));
        binding.tvTotalExpenses.setText(currencyFormatter.format(totalExpenses));
        binding.tvRemainingBudget.setText(currencyFormatter.format(remainingBudget));
        binding.tvBudgetPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", percentageUsed));

        // Actualizar colores según el estado del presupuesto
        if (remainingBudget >= 0) {
            binding.tvRemainingBudget.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
        } else {
            binding.tvRemainingBudget.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        }

        // Actualizar barra de progreso
        binding.progressBarBudget.setProgress(percentageUsed.intValue());

        // Indicador de sobrecosto
        Boolean isOverBudget = financialSummary.getIsOverBudget();
        if (isOverBudget != null && isOverBudget) {
            binding.chipOverBudget.setVisibility(View.VISIBLE);
            android.util.Log.d(TAG, "ALERTA: Proyecto con sobrecosto");
        } else {
            binding.chipOverBudget.setVisibility(View.GONE);
        }

        // Actualizar categorías
        updateCategoriesBudget(financialSummary);

        android.util.Log.d(TAG, "=== RESUMEN FINANCIERO ACTUALIZADO ===");
    }

    private void updateCategoriesBudget(DashboardResponse.FinancialSummary financialSummary) {
        if (financialSummary.getBudgetByCategory() != null) {
            DashboardResponse.BudgetByCategory categories = financialSummary.getBudgetByCategory();

            // Construcción
            if (categories.getConstruction() != null) {
                DashboardResponse.CategoryBudget construction = categories.getConstruction();
                String constructionText = String.format(Locale.getDefault(),
                        "Presupuesto: %s | Gastado: %s",
                        currencyFormatter.format(construction.getBudgeted() != null ? construction.getBudgeted() : 0.0),
                        currencyFormatter.format(construction.getSpent() != null ? construction.getSpent() : 0.0)
                );
                binding.tvConstructionBudget.setText(constructionText);
            }

            // Iluminación
            if (categories.getLighting() != null) {
                DashboardResponse.CategoryBudget lighting = categories.getLighting();
                String lightingText = String.format(Locale.getDefault(),
                        "Presupuesto: %s | Gastado: %s",
                        currencyFormatter.format(lighting.getBudgeted() != null ? lighting.getBudgeted() : 0.0),
                        currencyFormatter.format(lighting.getSpent() != null ? lighting.getSpent() : 0.0)
                );
                binding.tvLightingBudget.setText(lightingText);
            }

            // Otros
            if (categories.getOthers() != null) {
                DashboardResponse.CategoryBudget others = categories.getOthers();
                String othersText = String.format(Locale.getDefault(),
                        "Presupuesto: %s | Gastado: %s",
                        currencyFormatter.format(others.getBudgeted() != null ? others.getBudgeted() : 0.0),
                        currencyFormatter.format(others.getSpent() != null ? others.getSpent() : 0.0)
                );
                binding.tvOthersBudget.setText(othersText);
            }
        }
    }

    private void updateDashboardStatistics(DashboardResponse dashboardData) {
        if (dashboardData.getStatistics() != null) {
            DashboardResponse.Statistics stats = dashboardData.getStatistics();

            android.util.Log.d(TAG, "=== ACTUALIZANDO ESTADÍSTICAS ===");
            android.util.Log.d(TAG, "Items presupuestados: " + stats.getBudgetItemsCount());
            android.util.Log.d(TAG, "Gastos realizados: " + stats.getExpensesCount());
            android.util.Log.d(TAG, "Cálculos realizados: " + stats.getCalculationsCount());

            // Actualizar contadores
            binding.tvBudgetItemsCount.setText(String.valueOf(stats.getBudgetItemsCount() != null ? stats.getBudgetItemsCount() : 0));
            binding.tvExpensesCount.setText(String.valueOf(stats.getExpensesCount() != null ? stats.getExpensesCount() : 0));
            binding.tvCalculationsCount.setText(String.valueOf(stats.getCalculationsCount() != null ? stats.getCalculationsCount() : 0));

            android.util.Log.d(TAG, "=== ESTADÍSTICAS ACTUALIZADAS ===");
        }
    }

    private String formatDateString(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Fecha no definida";
        }

        try {
            Date date = inputDateFormatter.parse(dateString);
            return outputDateFormatter.format(date);
        } catch (ParseException e) {
            android.util.Log.w(TAG, "Error parseando fecha: " + dateString, e);
            return dateString;
        }
    }

    private void openGoogleDrive() {
        try {
            Project project = dashboardViewModel.getProjectData().getValue();
            if (project != null && project.getDriveFolderUrl() != null && !project.getDriveFolderUrl().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(project.getDriveFolderUrl()));
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "URL de Google Drive no configurada para este proyecto", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al abrir Google Drive", Toast.LENGTH_SHORT).show();
        }
    }

    private void openReports() {
        Toast.makeText(requireContext(), "Función de reportes - Próximamente", Toast.LENGTH_SHORT).show();
    }

    private void refreshDashboardData() {
        if (dashboardViewModel != null) {
            android.util.Log.d(TAG, "Refrescando datos del dashboard...");
            dashboardViewModel.refreshDashboard();
        }
    }

    private void showError(String message) {
        android.util.Log.e(TAG, "Error en dashboard: " + message);
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .setAction("Reintentar", v -> loadProjectData())
                    .show();
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    public void refreshDashboard() {
        refreshDashboardData();
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d(TAG, "onResume - Fragment visible");
        if (dashboardViewModel != null && dashboardViewModel.hasData()) {
            refreshDashboardData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        android.util.Log.d(TAG, "onDestroyView - Limpiando binding");
        binding = null;
    }
}