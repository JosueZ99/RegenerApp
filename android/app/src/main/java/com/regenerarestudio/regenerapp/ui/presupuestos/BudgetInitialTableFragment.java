package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.databinding.FragmentTableBudgetInitialBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment para mostrar la tabla de Presupuesto Inicial
 * VERSIÓN CORREGIDA - Actualiza UI correctamente
 */
public class BudgetInitialTableFragment extends Fragment {

    private static final String TAG = "BudgetInitialTable";

    private FragmentTableBudgetInitialBinding binding;
    private BudgetInitialAdapter adapter;
    private List<BudgetItem> budgetItems;

    // Estados de UI
    private RecyclerView recyclerView;

    // Formatters
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTableBudgetInitialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated - Inicializando BudgetInitialTableFragment");

        initializeViews();
        setupRecyclerView();
        showEmptyState();
    }

    private void initializeViews() {
        recyclerView = binding.rvBudgetInitial;
        budgetItems = new ArrayList<>();

        Log.d(TAG, "Vistas inicializadas");
    }

    private void setupRecyclerView() {
        Log.d(TAG, "RecyclerView configurado");

        // Configurar LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Crear y configurar adapter
        adapter = new BudgetInitialAdapter(budgetItems);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "Adapter configurado con " + budgetItems.size() + " items");
    }

    /**
     * Mostrar estado vacío cuando no hay datos
     */
    private void showEmptyState() {
        Log.d(TAG, "Mostrando estado vacío");

        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE); // Mantener visible pero sin datos
        }
    }

    /**
     * Mostrar contenido cuando hay datos
     */
    private void showContent() {
        Log.d(TAG, "Mostrando contenido con datos");

        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * MÉTODO PRINCIPAL - Actualizar datos del presupuesto desde el ViewModel
     * Este método es llamado por PresupuestosFragment cuando llegan datos del backend
     */
    public void updateBudgetData(List<Map<String, Object>> budgetData) {
        if (budgetData == null) {
            Log.w(TAG, "updateBudgetData - Datos recibidos son null");
            showEmptyState();
            return;
        }

        Log.d(TAG, "updateBudgetData - Recibidos " + budgetData.size() + " items del backend");

        // Limpiar lista actual
        budgetItems.clear();

        // Procesar datos del backend
        double totalAmount = 0.0;
        for (Map<String, Object> itemData : budgetData) {
            try {
                BudgetItem item = createBudgetItemFromMap(itemData);
                budgetItems.add(item);
                totalAmount += item.getTotalPrice();

                Log.v(TAG, "Item creado: " + item.getDescription() + " - " +
                        currencyFormat.format(item.getTotalPrice()));

            } catch (Exception e) {
                Log.e(TAG, "Error procesando item del presupuesto: " + e.getMessage(), e);
            }
        }

        Log.d(TAG, "Convertidos " + budgetItems.size() + " items exitosamente");

        // Hacer la variable final para usar en lambda
        final double finalTotalAmount = totalAmount;

        // Actualizar UI en el hilo principal
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                updateUI(finalTotalAmount);
            });
        }
    }

    /**
     * Actualizar la interfaz de usuario
     */
    private void updateUI(double totalAmount) {
        // Notificar al adapter que los datos han cambiado
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            Log.d(TAG, "Adapter notificado de cambios");
        }

        // Actualizar totales en la UI
        updateTotals(budgetItems.size(), totalAmount);

        if (budgetItems.isEmpty()) {
            Log.d(TAG, "UI actualizada - No hay items para mostrar");
        } else {
            Log.d(TAG, "Datos actualizados exitosamente - " + budgetItems.size() + " items");
        }
    }

    /**
     * Actualizar los totales mostrados en la UI
     */
    private void updateTotals(int itemCount, double totalAmount) {
        Log.d(TAG, "Totales actualizados - Items: " + itemCount + ", Monto: " + totalAmount);

        try {
            // Usar los IDs correctos del layout fragment_table_budget_initial.xml

            // Actualizar contador de items
            TextView tvTotalItems = binding.tvTotalItemsInitial;
            if (tvTotalItems != null) {
                String itemsText = itemCount == 1 ? "1 item" : itemCount + " items";
                tvTotalItems.setText(itemsText);
            }

            // Actualizar total de presupuesto
            TextView tvTotalBudget = binding.tvTotalBudgetInitial;
            if (tvTotalBudget != null) {
                tvTotalBudget.setText(currencyFormat.format(totalAmount));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error actualizando totales en UI: " + e.getMessage(), e);
        }
    }

    /**
     * Crear BudgetItem desde Map del backend
     */
    private BudgetItem createBudgetItemFromMap(Map<String, Object> itemData) {
        BudgetItem item = new BudgetItem();

        // ID
        if (itemData.get("id") instanceof Number) {
            item.setId(((Number) itemData.get("id")).longValue());
        }

        // Descripción
        item.setDescription((String) itemData.get("description"));

        // Categoría
        item.setCategory((String) itemData.get("category"));
        item.setCategoryDisplay((String) itemData.get("category_display"));

        // Espacios
        item.setSpaces((String) itemData.get("spaces"));

        // Cantidad
        if (itemData.get("quantity") instanceof String) {
            try {
                item.setQuantity(Double.parseDouble((String) itemData.get("quantity")));
            } catch (NumberFormatException e) {
                item.setQuantity(1.0);
            }
        } else if (itemData.get("quantity") instanceof Number) {
            item.setQuantity(((Number) itemData.get("quantity")).doubleValue());
        }

        // Unidad
        item.setUnit((String) itemData.get("unit"));

        // Precio unitario
        if (itemData.get("unit_price") instanceof String) {
            try {
                item.setUnitPrice(Double.parseDouble((String) itemData.get("unit_price")));
            } catch (NumberFormatException e) {
                item.setUnitPrice(0.0);
            }
        } else if (itemData.get("unit_price") instanceof Number) {
            item.setUnitPrice(((Number) itemData.get("unit_price")).doubleValue());
        }

        // Precio total
        if (itemData.get("total_price") instanceof String) {
            try {
                item.setTotalPrice(Double.parseDouble((String) itemData.get("total_price")));
            } catch (NumberFormatException e) {
                item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
            }
        } else if (itemData.get("total_price") instanceof Number) {
            item.setTotalPrice(((Number) itemData.get("total_price")).doubleValue());
        }

        // Proveedor
        item.setSupplierName((String) itemData.get("supplier_name"));
        if (itemData.get("supplier") instanceof Number) {
            item.setSupplierId(((Number) itemData.get("supplier")).longValue());
        }

        // Notas
        item.setNotes((String) itemData.get("notes"));

        return item;
    }

    /**
     * Clase modelo para items del presupuesto
     */
    public static class BudgetItem {
        private Long id;
        private String description;
        private String category;
        private String categoryDisplay;
        private String spaces;
        private Double quantity;
        private String unit;
        private Double unitPrice;
        private Double totalPrice;
        private String supplierName;
        private Long supplierId;
        private String notes;

        // Constructor vacío
        public BudgetItem() {}

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getCategoryDisplay() { return categoryDisplay; }
        public void setCategoryDisplay(String categoryDisplay) { this.categoryDisplay = categoryDisplay; }

        public String getSpaces() { return spaces; }
        public void setSpaces(String spaces) { this.spaces = spaces; }

        public Double getQuantity() { return quantity != null ? quantity : 1.0; }
        public void setQuantity(Double quantity) { this.quantity = quantity; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public Double getUnitPrice() { return unitPrice != null ? unitPrice : 0.0; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

        public Double getTotalPrice() { return totalPrice != null ? totalPrice : 0.0; }
        public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

        public Long getSupplierId() { return supplierId; }
        public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "Vista destruida");
    }
}