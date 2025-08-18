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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.regenerarestudio.regenerapp.MainActivity;
import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.databinding.FragmentDashboardBinding;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Fragment del Dashboard - Pantalla principal después de seleccionar proyecto
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/dashboard/DashboardFragment.java
 */
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;

    // Información del proyecto (desde MainActivity)
    private long projectId;
    private String projectName;

    // Widgets del dashboard
    private MaterialCardView cardGoogleDrive;
    private MaterialCardView cardReports;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        getProjectInfo();
        initializeViews();
        setupClickListeners();
        loadProjectData();
        observeViewModel();
    }

    private void getProjectInfo() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            projectId = mainActivity.getSelectedProjectId();
            projectName = mainActivity.getSelectedProjectName();
        }
    }

    private void initializeViews() {
        cardGoogleDrive = binding.cardGoogleDrive;
        cardReports = binding.cardReports;
    }

    private void setupClickListeners() {
        // Google Drive
        cardGoogleDrive.setOnClickListener(v -> openGoogleDrive());

        // Reportes
        cardReports.setOnClickListener(v -> openReports());
    }

    private void loadProjectData() {
        // Cargar datos del proyecto (por ahora simulados)
        // TODO: En el futuro obtener desde API
        loadProjectInfo();
        loadFinancialSummary();
        loadProjectPhases();
    }

    private void loadProjectInfo() {
        // Información básica del proyecto
        binding.tvProjectName.setText(projectName);
        binding.tvProjectClient.setText("Familia Rodríguez Pérez"); // TODO: Obtener desde API
        binding.tvStartDate.setText("15 Ene 2025");
        binding.tvEndDate.setText("30 Abr 2025");

        // Estado del proyecto
        updateProjectStatus("diseño"); // TODO: Obtener desde API
    }

    private void updateProjectStatus(String status) {
        Chip statusChip = binding.chipProjectStatus;

        switch (status.toLowerCase()) {
            case "diseño":
                statusChip.setText("En Diseño");
                statusChip.setChipBackgroundColorResource(R.color.status_design);
                break;
            case "compra":
                statusChip.setText("En Compra");
                statusChip.setChipBackgroundColorResource(R.color.status_purchase);
                break;
            case "instalacion":
                statusChip.setText("En Instalación");
                statusChip.setChipBackgroundColorResource(R.color.status_installation);
                break;
            case "completado":
                statusChip.setText("Completado");
                statusChip.setChipBackgroundColorResource(R.color.status_completed);
                break;
            default:
                statusChip.setText("Estado Desconocido");
                statusChip.setChipBackgroundColorResource(R.color.gray_500);
                break;
        }
    }

    private void loadFinancialSummary() {
        // Datos financieros simulados
        // TODO: En el futuro obtener desde API
        double budgetInitial = 15750.00;
        double expensesReal = 8240.00;
        double balance = budgetInitial - expensesReal;

        // Formatear números como moneda
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));

        binding.tvBudgetInitial.setText(currencyFormat.format(budgetInitial));
        binding.tvExpensesReal.setText(currencyFormat.format(expensesReal));

        // Configurar color del balance según si es positivo o negativo
        if (balance >= 0) {
            binding.tvBalance.setText(currencyFormat.format(balance));
            binding.tvBalance.setTextColor(getResources().getColor(R.color.financial_positive, null));
        } else {
            binding.tvBalance.setText("-" + currencyFormat.format(Math.abs(balance)));
            binding.tvBalance.setTextColor(getResources().getColor(R.color.financial_negative, null));
        }
    }

    private void loadProjectPhases() {
        // Configurar fases según el estado del proyecto
        // TODO: En el futuro obtener desde API

        // Simulación: Proyecto en fase de compra
        updatePhaseStatus("design", "completado");
        updatePhaseStatus("purchase", "en_progreso");
        updatePhaseStatus("installation", "pendiente");
    }

    private void updatePhaseStatus(String phase, String status) {
        Chip chip;

        switch (phase) {
            case "design":
                chip = binding.chipPhaseDesign;
                break;
            case "purchase":
                chip = binding.chipPhasePurchase;
                break;
            case "installation":
                chip = binding.chipPhaseInstallation;
                break;
            default:
                return;
        }

        switch (status) {
            case "completado":
                chip.setText("Completado");
                chip.setChipBackgroundColorResource(R.color.success);
                chip.setTextColor(getResources().getColor(R.color.white, null));
                break;
            case "en_progreso":
                chip.setText("En Progreso");
                chip.setChipBackgroundColorResource(R.color.warning);
                chip.setTextColor(getResources().getColor(R.color.white, null));
                break;
            case "pendiente":
                chip.setText("Pendiente");
                chip.setChipBackgroundColorResource(R.color.gray_300);
                chip.setTextColor(getResources().getColor(R.color.gray_600, null));
                break;
        }
    }

    private void openGoogleDrive() {
        // URL de ejemplo para Google Drive del proyecto
        // TODO: En el futuro obtener desde API la URL real configurada para el proyecto
        String driveUrl = "https://drive.google.com/drive/folders/ejemplo-proyecto-" + projectId;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(driveUrl));

            // Verificar si hay una app que pueda manejar el intent
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Si no hay app, abrir en navegador web
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.setData(Uri.parse("https://drive.google.com"));
                startActivity(webIntent);
                Toast.makeText(requireContext(), "Abriendo Google Drive en navegador", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al abrir Google Drive", Toast.LENGTH_SHORT).show();
        }
    }

    private void openReports() {
        // Navegar a la sección de reportes o exportación
        Toast.makeText(requireContext(), "Función de reportes - Próximamente", Toast.LENGTH_SHORT).show();

        // TODO: En el futuro implementar navegación a fragment de reportes
        // Navigation.findNavController(requireView()).navigate(R.id.nav_reportes);
    }

    private void observeViewModel() {
        // TODO: Implementar observadores cuando se conecte con el backend

        // Observar cambios en datos del proyecto
        // dashboardViewModel.getProjectData().observe(getViewLifecycleOwner(), projectData -> {
        //     updateProjectInfo(projectData);
        // });

        // Observar cambios en datos financieros
        // dashboardViewModel.getFinancialData().observe(getViewLifecycleOwner(), financialData -> {
        //     updateFinancialSummary(financialData);
        // });

        // Observar cambios en fases del proyecto
        // dashboardViewModel.getProjectPhases().observe(getViewLifecycleOwner(), phases -> {
        //     updateProjectPhases(phases);
        // });

        // Observar errores
        // dashboardViewModel.getError().observe(getViewLifecycleOwner(), error -> {
        //     if (error != null && !error.isEmpty()) {
        //         Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
        //     }
        // });
    }

    // Método público para actualizar datos desde la MainActivity si es necesario
    public void refreshDashboard() {
        loadProjectData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Actualizar datos cada vez que se regresa al dashboard
        refreshDashboard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}