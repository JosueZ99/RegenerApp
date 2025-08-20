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
 * SIMPLIFICADO - SIN resumen financiero (solo en Dashboard)
 */
public class PresupuestosFragment extends Fragment {

    private static final String TAG = "PresupuestosFragment";

    private FragmentPresupuestosBinding binding;
    private PresupuestosViewModel presupuestosViewModel;

    // Información del proyecto
    private long projectId;
    private String projectName;

    // Adapter para ViewPager
    private BudgetPagerAdapter pagerAdapter;

    // Formatters
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPresupuestosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel
        presupuestosViewModel = new ViewModelProvider(this).get(PresupuestosViewModel.class);

        // Obtener datos del proyecto desde MainActivity
        getProjectDataFromActivity();

        // Configurar componentes de la UI
        setupViewPager();
        setupFabButtons();
        observeViewModel();

        // Cargar datos iniciales
        loadAllBudgetData();

        Log.d(TAG, "PresupuestosFragment configurado correctamente - SIN resumen financiero");
    }

    // ==========================================
    // CONFIGURACIÓN DE LA UI
    // ==========================================

    /**
     * Obtener datos del proyecto desde MainActivity
     */
    private void getProjectDataFromActivity() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();

            projectId = activity.getSelectedProjectId();
            projectName = activity.getSelectedProjectName();

            Log.d(TAG, "Datos del proyecto obtenidos - ID: " + projectId + ", Nombre: " + projectName);

            if (projectId <= 0) {
                Log.e(TAG, "Error: No hay proyecto seleccionado");
                showError("Error: No hay proyecto seleccionado");
                return;
            }
        } else {
            Log.e(TAG, "Error: Activity no es instanceof MainActivity");
            showError("Error: No se pudo obtener datos del proyecto");
        }
    }

    /**
     * Configurar ViewPager2 y TabLayout para las dos tablas
     */
    private void setupViewPager() {
        Log.d(TAG, "Configurando ViewPager y TabLayout");

        // Crear adapter
        pagerAdapter = new BudgetPagerAdapter(this);
        binding.viewPagerBudget.setAdapter(pagerAdapter);

        // Conectar TabLayout con ViewPager2
        new TabLayoutMediator(binding.tabLayoutBudget, binding.viewPagerBudget,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(getString(R.string.presupuesto_inicial));
                            break;
                        case 1:
                            tab.setText(getString(R.string.gastos_reales));
                            break;
                    }
                }).attach();

        // Listener para cambios de tab
        binding.tabLayoutBudget.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                updateFabVisibility(tab.getPosition());
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                // No necesario
            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                // No necesario
            }
        });

        // Configurar visibilidad inicial de FABs
        updateFabVisibility(0);
    }

    /**
     * Configurar Floating Action Buttons
     */
    private void setupFabButtons() {
        Log.d(TAG, "Configurando FAB buttons");

        // FAB Agregar Item
        binding.fabAddItem.setOnClickListener(v -> {
            int currentTab = binding.viewPagerBudget.getCurrentItem();
            boolean isExpense = currentTab == 1; // Tab 1 = Gastos Reales
            showManualEntryForm(isExpense);
        });

        // FAB Copiar a Gastos Reales
        binding.fabCopyToExpenses.setOnClickListener(v -> showCopyConfirmationDialog());

        // Mostrar FABs
        binding.layoutFabActions.setVisibility(View.VISIBLE);
    }

    /**
     * Actualizar visibilidad de FABs según el tab seleccionado
     */
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
     * Observar todos los LiveData del ViewModel - SIN RESUMEN FINANCIERO
     */
    private void observeViewModel() {
        Log.d(TAG, "Configurando observadores del ViewModel");

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

        // Observar errores
        presupuestosViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Observer: Error recibido - " + error);
                showError(error);
            }
        });
    }

    // ==========================================
    // MÉTODOS DE UI Y MANEJO DE EVENTOS
    // ==========================================

    /**
     * Actualizar estado de carga visual
     */
    private void updateLoadingState(boolean isLoading) {
        // Aquí puedes agregar indicadores de carga si es necesario
        Log.d(TAG, "Estado de carga actualizado: " + isLoading);
    }

    /**
     * Mostrar error al usuario
     */
    private void showError(String error) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, "Error mostrado al usuario: " + error);
    }

    /**
     * Mostrar confirmación para copiar presupuesto a gastos
     */
    private void showCopyConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Copiar Presupuesto")
                .setMessage("¿Desea copiar todos los items del presupuesto inicial a gastos reales?")
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