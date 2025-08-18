package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.regenerarestudio.regenerapp.databinding.FragmentTableBudgetInitialBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment para la tabla de Presupuesto Inicial
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/presupuestos/BudgetInitialTableFragment.java
 */
public class BudgetInitialTableFragment extends Fragment {

    private FragmentTableBudgetInitialBinding binding;
    private BudgetInitialAdapter adapter;
    private List<BudgetItem> budgetItems;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTableBudgetInitialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchAndFilter();
        loadBudgetData();
    }

    private void setupRecyclerView() {
        budgetItems = new ArrayList<>();
        adapter = new BudgetInitialAdapter(budgetItems, this::onItemClick, this::onItemMenuClick);

        binding.rvBudgetInitial.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBudgetInitial.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        // Configurar búsqueda
        binding.etSearchInitial.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // TODO: Implementar lógica de búsqueda en tiempo real
            }
        });

        // Configurar filtro
        binding.btnFilterInitial.setOnClickListener(v -> {
            // TODO: Mostrar dialog de filtros
            Toast.makeText(requireContext(), "Filtros - Próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadBudgetData() {
        // Datos simulados - TODO: Obtener desde API
        budgetItems.clear();
        budgetItems.addAll(createSampleBudgetItems());

        adapter.notifyDataSetChanged();
        updateTotals();
    }

    private List<BudgetItem> createSampleBudgetItems() {
        List<BudgetItem> items = new ArrayList<>();

        items.add(new BudgetItem(
                1, "Pintura Látex Interior", "Construcción",
                25.5, "Litros", 15.50, "Pinturas Cóndor", 4.5
        ));

        items.add(new BudgetItem(
                2, "Panel Gypsum 12mm", "Construcción",
                45.0, "Paneles", 12.80, "Gyplac Ecuador", 4.2
        ));

        items.add(new BudgetItem(
                3, "Cinta LED 5050 Blanco Cálido", "Iluminación",
                15.0, "Metros", 8.50, "LED Solutions", 4.8
        ));

        items.add(new BudgetItem(
                4, "Perfil Aluminio Empotrado", "Iluminación",
                15.0, "Metros", 12.30, "Perfiles Andinos", 4.0
        ));

        items.add(new BudgetItem(
                5, "Cable THHN 12 AWG", "Eléctrico",
                50.0, "Metros", 1.85, "Cablesec", 4.3
        ));

        items.add(new BudgetItem(
                6, "Empaste Interior Premium", "Construcción",
                8.5, "Sacos", 18.75, "Materiales Quito", 4.1
        ));

        return items;
    }

    private void updateTotals() {
        double total = 0.0;
        for (BudgetItem item : budgetItems) {
            total += item.getTotalPrice();
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));
        binding.tvTotalBudgetInitial.setText(currencyFormat.format(total));
        binding.tvTotalItemsInitial.setText(budgetItems.size() + " items");
    }

    private void onItemClick(BudgetItem item) {
        // TODO: Mostrar detalles del item o permitir edición
        Toast.makeText(requireContext(), "Editar: " + item.getMaterialName(), Toast.LENGTH_SHORT).show();
    }

    private void onItemMenuClick(BudgetItem item) {
        // TODO: Mostrar menú contextual (editar, eliminar, duplicar, etc.)
        Toast.makeText(requireContext(), "Menú para: " + item.getMaterialName(), Toast.LENGTH_SHORT).show();
    }

    public void refreshData() {
        loadBudgetData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Clase modelo para items del presupuesto inicial
     */
    public static class BudgetItem {
        private long id;
        private String materialName;
        private String category;
        private double quantity;
        private String unit;
        private double unitPrice;
        private String supplierName;
        private double supplierRating;

        public BudgetItem(long id, String materialName, String category, double quantity,
                          String unit, double unitPrice, String supplierName, double supplierRating) {
            this.id = id;
            this.materialName = materialName;
            this.category = category;
            this.quantity = quantity;
            this.unit = unit;
            this.unitPrice = unitPrice;
            this.supplierName = supplierName;
            this.supplierRating = supplierRating;
        }

        public double getTotalPrice() {
            return quantity * unitPrice;
        }

        // Getters
        public long getId() { return id; }
        public String getMaterialName() { return materialName; }
        public String getCategory() { return category; }
        public double getQuantity() { return quantity; }
        public String getUnit() { return unit; }
        public double getUnitPrice() { return unitPrice; }
        public String getSupplierName() { return supplierName; }
        public double getSupplierRating() { return supplierRating; }

        // Setters (para edición)
        public void setQuantity(double quantity) { this.quantity = quantity; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    }
}