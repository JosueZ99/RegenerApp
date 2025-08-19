package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.os.Bundle;
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
import java.util.Locale;

/**
 * Fragment del Sistema de Presupuestos con tablas interactivas
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/presupuestos/PresupuestosFragment.java
 */
public class PresupuestosFragment extends Fragment {

    private FragmentPresupuestosBinding binding;
    private PresupuestosViewModel presupuestosViewModel;

    // Información del proyecto
    private long projectId;
    private String projectName;

    // Datos financieros
    private double totalBudget = 0.0;
    private double totalExpenses = 0.0;
    private double totalBalance = 0.0;

    // Adapter para ViewPager
    private BudgetPagerAdapter pagerAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPresupuestosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presupuestosViewModel = new ViewModelProvider(this).get(PresupuestosViewModel.class);

        getProjectInfo();
        setupViewPager();
        setupFloatingActionButtons();
        loadFinancialData();
        observeViewModel();
    }

    private void getProjectInfo() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            projectId = mainActivity.getSelectedProjectId();
            projectName = mainActivity.getSelectedProjectName();
        }
    }

    private void setupViewPager() {
        pagerAdapter = new BudgetPagerAdapter(this);
        binding.viewPagerBudget.setAdapter(pagerAdapter);

        // Configurar TabLayout con ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPagerBudget,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(getString(R.string.presupuesto_inicial));
                            tab.setIcon(R.drawable.ic_assignment_24);
                            break;
                        case 1:
                            tab.setText(getString(R.string.gastos_reales));
                            tab.setIcon(R.drawable.ic_receipt_24);
                            break;
                    }
                }).attach();
    }

    private void setupFloatingActionButtons() {
        // FAB Agregar Item
        binding.fabAddItem.setOnClickListener(v -> showAddItemDialog());

        // FAB Copiar a Gastos Reales
        binding.fabCopyToExpenses.setOnClickListener(v -> showCopyToExpensesDialog());

        // Mostrar FABs solo cuando sea apropiado
        binding.layoutFabActions.setVisibility(View.VISIBLE);
    }

    private void showAddItemDialog() {
        // Determinar en qué tab estamos
        int currentTab = binding.viewPagerBudget.getCurrentItem();
        String dialogTitle = currentTab == 0 ? "Añadir al Presupuesto Inicial" : "Registrar Gasto Real";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(dialogTitle)
                .setMessage("¿Qué deseas hacer?")
                .setPositiveButton("Desde Calculadora", (dialog, which) -> {
                    // TODO: Navegar a calculadora con bandera de "añadir al presupuesto"
                    Toast.makeText(requireContext(), "Ir a Calculadora", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Manual", (dialog, which) -> {
                    // TODO: Abrir formulario manual
                    showManualEntryForm(currentTab == 1);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showCopyToExpensesDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Copiar a Gastos Reales")
                .setMessage("¿Deseas copiar todos los items del presupuesto inicial a gastos reales? " +
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
        // TODO: Implementar lógica para copiar presupuesto a gastos
        Toast.makeText(requireContext(), "Items copiados a gastos reales", Toast.LENGTH_SHORT).show();

        // Cambiar a la tab de gastos reales
        binding.viewPagerBudget.setCurrentItem(1, true);

        // Actualizar datos
        loadFinancialData();
    }

    private void loadFinancialData() {
        // Datos simulados - TODO: Obtener desde API
        totalBudget = 15750.00;
        totalExpenses = 8240.00;
        totalBalance = totalBudget - totalExpenses;

        updateFinancialSummary();
    }

    private void updateFinancialSummary() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));

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

    private void observeViewModel() {
        // TODO: Implementar observadores cuando se conecte con el backend

        // Observar cambios en datos financieros
        // presupuestosViewModel.getFinancialData().observe(getViewLifecycleOwner(), data -> {
        //     updateFinancialData(data);
        // });

        // Observar errores
        // presupuestosViewModel.getError().observe(getViewLifecycleOwner(), error -> {
        //     if (error != null && !error.isEmpty()) {
        //         Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
        //     }
        // });
    }

    // Método público para actualizar desde otras partes de la app
    public void refreshBudgetData() {
        loadFinancialData();
        // También refrescar los fragments internos
        if (pagerAdapter != null) {
            // TODO: Notificar a los fragments internos que actualicen
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshBudgetData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

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