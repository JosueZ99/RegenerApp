package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.regenerarestudio.regenerapp.MainActivity;
import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.databinding.FragmentPresupuestosBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment del Sistema de Presupuestos con tablas interactivas
 * Actualizado para usar APIs reales del backend - TabLayout ID CORREGIDO
 */
public class PresupuestosFragment extends Fragment {

    private static final String TAG = "PresupuestosFragment";

    private FragmentPresupuestosBinding binding;
    private PresupuestosViewModel presupuestosViewModel;

    // Información del proyecto
    private long projectId;
    private String projectName;

    // Datos financieros (ahora vienen del backend)
    private double totalBudget = 0.0;
    private double totalExpenses = 0.0;
    private double totalBalance = 0.0;

    // Adapter para ViewPager
    private BudgetPagerAdapter pagerAdapter;

    // Formatters
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPresupuestosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated - Inicializando PresupuestosFragment");

        // Inicializar ViewModel
        presupuestosViewModel = new ViewModelProvider(this).get(PresupuestosViewModel.class);

        // Configurar UI
        getProjectInfo();
        setupViewPager();
        setupFloatingActionButtons();

        // Observar datos del ViewModel
        observeViewModel();

        // Cargar datos si hay proyecto seleccionado
        if (projectId > 0) {
            loadAllBudgetData();
        } else {
            Log.w(TAG, "No hay proyecto seleccionado");
            showError("No hay proyecto seleccionado");
        }
    }

    private void getProjectInfo() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            projectId = mainActivity.getSelectedProjectId();
            projectName = mainActivity.getSelectedProjectName();

            Log.d(TAG, "Proyecto seleccionado - ID: " + projectId + ", Nombre: " + projectName);
        } else {
            Log.e(TAG, "Error: MainActivity no encontrada");
        }
    }

    /**
     * CORREGIDO: Usar ID correcto del TabLayout según fragment_presupuestos.xml
     */
    private void setupViewPager() {
        pagerAdapter = new BudgetPagerAdapter(this);
        binding.viewPagerBudget.setAdapter(pagerAdapter);

        // Configurar TabLayout con ViewPager - ID CORREGIDO: tab_layout (no tabLayoutBudget)
        new TabLayoutMediator(binding.tabLayout, binding.viewPagerBudget, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.presupuesto_inicial));
                    break;
                case 1:
                    tab.setText(getString(R.string.gastos_reales));
                    break;
            }
        }).attach();

        Log.d(TAG, "ViewPager y TabLayout configurados");
    }

    private void setupFloatingActionButtons() {
        // FAB Agregar Item
        binding.fabAddItem.setOnClickListener(v -> {
            int currentTab = binding.viewPagerBudget.getCurrentItem();
            boolean isExpense = (currentTab == 1); // 1 = Gastos Reales
            showManualEntryForm(isExpense);
        });

        // FAB Copiar a Gastos Reales
        binding.fabCopyToExpenses.setOnClickListener(v -> showCopyToExpensesDialog());

        // Mostrar/ocultar FABs según la tab activa
        binding.viewPagerBudget.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateFabVisibility(position);
            }
        });

        // Estado inicial
        updateFabVisibility(0);

        Log.d(TAG, "Floating Action Buttons configurados");
    }

    private void updateFabVisibility(int tabPosition) {
        if (tabPosition == 0) {
            // Presupuesto Inicial: mostrar FAB copiar
            binding.fabCopyToExpenses.setVisibility(View.VISIBLE);
            binding.fabAddItem.setImageResource(R.drawable.ic_add_24);
        } else {
            // Gastos Reales: ocultar FAB copiar
            binding.fabCopyToExpenses.setVisibility(View.GONE);
            binding.fabAddItem.setImageResource(R.drawable.ic_add_24);
        }
    }

    /**
     * Cargar todos los datos de presupuestos del backend
     */
    private void loadAllBudgetData() {
        if (projectId <= 0) {
            Log.e(TAG, "ID de proyecto inválido: " + projectId);
            showError("Error: ID de proyecto inválido");
            return;
        }

        Log.d(TAG, "Cargando todos los datos de presupuestos para proyecto: " + projectId);
        presupuestosViewModel.loadAllBudgetData(projectId);
    }

    /**
     * Observar todos los LiveData del ViewModel
     */
    private void observeViewModel() {
        Log.d(TAG, "Configurando observadores del ViewModel");

        // Observar resumen financiero
        presupuestosViewModel.getFinancialSummary().observe(getViewLifecycleOwner(), financialSummary -> {
            Log.d(TAG, "Observer: Resumen financiero recibido");
            if (financialSummary != null) {
                updateFinancialSummary(financialSummary);
            }
        });

        // Observar presupuesto inicial
        presupuestosViewModel.getBudgetInitial().observe(getViewLifecycleOwner(), budgetItems -> {
            Log.d(TAG, "Observer: Presupuesto inicial recibido - Items: " +
                    (budgetItems != null ? budgetItems.size() : 0));

            // Notificar al fragment de la tabla inicial
            if (pagerAdapter != null) {
                Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f0");
                if (currentFragment instanceof BudgetInitialTableFragment) {
                    ((BudgetInitialTableFragment) currentFragment).updateBudgetData(budgetItems);
                }
            }
        });

        // Observar gastos reales
        presupuestosViewModel.getExpensesReal().observe(getViewLifecycleOwner(), expenses -> {
            Log.d(TAG, "Observer: Gastos reales recibidos - Items: " +
                    (expenses != null ? expenses.size() : 0));

            // Notificar al fragment de la tabla de gastos
            if (pagerAdapter != null) {
                Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f1");
                if (currentFragment instanceof ExpensesRealTableFragment) {
                    ((ExpensesRealTableFragment) currentFragment).updateExpensesData(expenses);
                }
            }
        });

        // Observar estados de carga
        presupuestosViewModel.getIsLoadingBudget().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "Observer: Estado de carga presupuesto - " + isLoading);
            updateLoadingState(isLoading);
        });

        presupuestosViewModel.getIsLoadingExpenses().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "Observer: Estado de carga gastos - " + isLoading);
            updateLoadingState(isLoading);
        });

        presupuestosViewModel.getIsLoadingSummary().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "Observer: Estado de carga resumen - " + isLoading);
            updateLoadingState(isLoading);
        });

        // Observar errores
        presupuestosViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Observer: Error recibido - " + error);
                showError(error);
            }
        });
    }

    /**
     * Actualizar resumen financiero con datos del backend
     */
    private void updateFinancialSummary(Map<String, Object> financialSummary) {
        try {
            // Extraer valores del Map (vienen del backend Django)
            Object totalBudgetObj = financialSummary.get("total_budget");
            Object totalExpensesObj = financialSummary.get("total_expenses");
            Object balanceObj = financialSummary.get("balance");

            // Convertir a double de forma segura
            totalBudget = parseDoubleFromObject(totalBudgetObj);
            totalExpenses = parseDoubleFromObject(totalExpensesObj);
            totalBalance = parseDoubleFromObject(balanceObj);

            Log.d(TAG, String.format("Resumen financiero actualizado - Budget: %.2f, Expenses: %.2f, Balance: %.2f",
                    totalBudget, totalExpenses, totalBalance));

            // Actualizar UI
            updateFinancialDisplays();

        } catch (Exception e) {
            Log.e(TAG, "Error al procesar resumen financiero", e);
            showError("Error al procesar datos financieros");
        }
    }

    /**
     * Convertir Object a double de forma segura
     */
    private double parseDoubleFromObject(Object obj) {
        if (obj == null) return 0.0;

        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }

        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Error al convertir string a double: " + obj);
                return 0.0;
            }
        }

        return 0.0;
    }

    /**
     * Actualizar displays financieros en la UI
     */
    private void updateFinancialDisplays() {
        // Actualizar totales
        binding.tvTotalBudget.setText(currencyFormat.format(totalBudget));
        binding.tvTotalExpenses.setText(currencyFormat.format(totalExpenses));

        // Configurar balance con color según si es positivo o negativo
        if (totalBalance >= 0) {
            binding.tvBalanceRealTime.setText(currencyFormat.format(totalBalance));
            binding.tvBalanceRealTime.setTextColor(getResources().getColor(R.color.white, null));
        } else {
            binding.tvBalanceRealTime.setText("-" + currencyFormat.format(Math.abs(totalBalance)));
            binding.tvBalanceRealTime.setTextColor(getResources().getColor(R.color.error, null));
        }

        // Actualizar barra de progreso
        updateProgressBar();
    }

    private void updateProgressBar() {
        LinearProgressIndicator progressBar = binding.progressBudget;

        if (totalBudget > 0) {
            int percentage = (int) ((totalExpenses / totalBudget) * 100);
            progressBar.setProgress(percentage);

            // Configurar color según el porcentaje
            int colorRes;
            if (percentage <= 50) {
                colorRes = R.color.success;
            } else if (percentage <= 85) {
                colorRes = R.color.warning;
            } else {
                colorRes = R.color.error;
            }

            progressBar.setIndicatorColor(getResources().getColor(colorRes, null));

            // Actualizar texto de porcentaje
            String percentageText = percentage + "% del presupuesto utilizado";
            binding.tvBudgetPercentage.setText(percentageText);
        } else {
            progressBar.setProgress(0);
            binding.tvBudgetPercentage.setText("0% del presupuesto utilizado");
        }
    }

    /**
     * Actualizar estado de carga en la UI
     */
    private void updateLoadingState(Boolean isLoading) {
        if (isLoading != null && isLoading) {
            // Mostrar indicadores de carga
            if (binding.progressBudget != null) {
                binding.progressBudget.setVisibility(View.VISIBLE);
            }
        } else {
            // Ocultar indicadores de carga
            if (binding.progressBudget != null) {
                binding.progressBudget.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Mostrar error en la UI
     */
    private void showError(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, "Error mostrado: " + message);
    }

    // ==========================================
    // MÉTODOS DE ACCIONES DE USUARIO
    // ==========================================

    private void showCopyToExpensesDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Copiar presupuesto a gastos reales")
                .setMessage("¿Deseas copiar todos los items del presupuesto inicial a gastos reales?\n\n" +
                        "Esto te permitirá registrar las compras reales.")
                .setPositiveButton(getString(R.string.btn_copiar_gastos), (dialog, which) -> {
                    copyBudgetToExpenses();
                })
                .setNegativeButton(getString(R.string.btn_cancelar), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showManualEntryForm(boolean isExpense) {
        // TODO: Implementar formulario manual para agregar items
        String message = isExpense ? "Formulario de Gasto Real" : "Formulario de Presupuesto";
        Toast.makeText(requireContext(), message + " - Próximamente", Toast.LENGTH_SHORT).show();
    }

    private void copyBudgetToExpenses() {
        // TODO: Implementar lógica para copiar presupuesto a gastos usando API
        Toast.makeText(requireContext(), "Función de copiado - Próximamente", Toast.LENGTH_SHORT).show();

        // Cambiar a la tab de gastos reales
        binding.viewPagerBudget.setCurrentItem(1, true);

        // Recargar datos
        refreshBudgetData();
    }

    // ==========================================
    // MÉTODOS PÚBLICOS
    // ==========================================

    /**
     * Método público para refrescar datos desde otras partes de la app
     */
    public void refreshBudgetData() {
        Log.d(TAG, "Refrescando datos de presupuestos");
        if (projectId > 0) {
            presupuestosViewModel.refreshAllData();
        } else {
            Log.w(TAG, "No se puede refrescar: no hay proyecto seleccionado");
        }
    }

    /**
     * Método para agregar item desde otras partes (ej: calculadora)
     */
    public void addItemFromCalculation(Map<String, Object> budgetItem) {
        Log.d(TAG, "Agregando item desde calculadora");
        presupuestosViewModel.addItemToBudget(budgetItem);
    }

    // ==========================================
    // CICLO DE VIDA
    // ==========================================

    @Override
    public void onResume() {
        super.onResume();
        // Refrescar datos cuando el fragment vuelve a ser visible
        refreshBudgetData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Limpiar datos del ViewModel cuando se destruye la vista
        if (presupuestosViewModel != null) {
            presupuestosViewModel.clearData();
        }

        binding = null;
        Log.d(TAG, "Vista destruida y datos limpiados");
    }

    // ==========================================
    // ADAPTER PARA VIEWPAGER
    // ==========================================

    /**
     * Adapter para ViewPager2 que maneja las dos tablas de presupuesto
     */
    private static class BudgetPagerAdapter extends FragmentStateAdapter {

        public BudgetPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new BudgetInitialTableFragment();
                case 1:
                    return new ExpensesRealTableFragment();
                default:
                    return new BudgetInitialTableFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Presupuesto Inicial y Gastos Reales
        }
    }
}