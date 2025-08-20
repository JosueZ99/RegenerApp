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

    /**
     * onViewCreated mejorado
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated - Inicializando BudgetInitialTableFragment");

        try {
            initializeViews();
            setupRecyclerView();
            showEmptyState();

            Log.d(TAG, "Inicialización completada exitosamente");
        } catch (Exception e) {
            Log.e(TAG, "Error en onViewCreated: " + e.getMessage(), e);
        }
    }

    /**
     * Inicialización mejorada de vistas
     */
    private void initializeViews() {
        if (binding == null) {
            Log.e(TAG, "Error: binding es null en initializeViews");
            return;
        }

        // Inicializar RecyclerView
        recyclerView = binding.rvBudgetInitial;

        // Verificar que el RecyclerView existe en el layout
        if (recyclerView == null) {
            Log.e(TAG, "Error: No se pudo encontrar rv_budget_initial en el layout");
            return;
        }

        // Inicializar lista de items
        if (budgetItems == null) {
            budgetItems = new ArrayList<>();
        }

        Log.d(TAG, "Vistas inicializadas correctamente");
    }

    /**
     * Configuración mejorada del RecyclerView
     */
    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Iniciando configuración");

        if (binding == null) {
            Log.e(TAG, "setupRecyclerView: binding es null");
            return;
        }

        if (binding.rvBudgetInitial == null) {
            Log.e(TAG, "setupRecyclerView: rvBudgetInitial es null en binding");
            return;
        }

        recyclerView = binding.rvBudgetInitial;
        Log.d(TAG, "setupRecyclerView: RecyclerView obtenido del binding");

        // Verificar que el RecyclerView no sea null
        if (recyclerView == null) {
            Log.e(TAG, "setupRecyclerView: RecyclerView es null después de asignación");
            return;
        }

        // Verificar visibilidad y dimensiones
        Log.d(TAG, "setupRecyclerView: Visibilidad inicial: " + recyclerView.getVisibility());
        recyclerView.post(() -> {
            Log.d(TAG, "setupRecyclerView: Dimensiones RecyclerView - " +
                    "Width: " + recyclerView.getWidth() +
                    ", Height: " + recyclerView.getHeight());
        });

        // Configurar LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        Log.d(TAG, "setupRecyclerView: LayoutManager configurado");

        // Inicializar lista si es necesaria
        if (budgetItems == null) {
            budgetItems = new ArrayList<>();
            Log.d(TAG, "setupRecyclerView: Lista budgetItems inicializada");
        }

        // Crear y configurar adapter
        adapter = new BudgetInitialAdapter(budgetItems);
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "setupRecyclerView: Adapter asignado al RecyclerView");

        // Configuraciones adicionales para mejor rendimiento
        recyclerView.setHasFixedSize(false); // Cambiar a false para debugging
        recyclerView.setItemAnimator(null); // Desactivar animaciones

        // Asegurar visibilidad
        recyclerView.setVisibility(View.VISIBLE);
        Log.d(TAG, "setupRecyclerView: Visibilidad establecida a VISIBLE");

        // Verificar adapter
        RecyclerView.Adapter<?> currentAdapter = recyclerView.getAdapter();
        if (currentAdapter == null) {
            Log.e(TAG, "setupRecyclerView: ERROR - Adapter es null después de asignación");
        } else {
            Log.d(TAG, "setupRecyclerView: Adapter asignado correctamente. Item count: " +
                    currentAdapter.getItemCount());
        }

        Log.d(TAG, "setupRecyclerView: Configuración completada exitosamente");
    }

    /**
     * Método para forzar refresh del RecyclerView - AGREGAR ESTE MÉTODO
     */
    private void forceRecyclerViewRefresh() {
        Log.d(TAG, "forceRecyclerViewRefresh: Forzando actualización");

        if (recyclerView == null) {
            Log.e(TAG, "forceRecyclerViewRefresh: RecyclerView es null");
            return;
        }

        if (adapter == null) {
            Log.e(TAG, "forceRecyclerViewRefresh: Adapter es null");
            return;
        }

        // Verificar estado actual
        Log.d(TAG, "forceRecyclerViewRefresh: Items en adapter: " + adapter.getItemCount());
        Log.d(TAG, "forceRecyclerViewRefresh: Visibilidad RecyclerView: " + recyclerView.getVisibility());

        // Forzar layout
        recyclerView.post(() -> {
            Log.d(TAG, "forceRecyclerViewRefresh: Ejecutando en post()");

            // Verificar dimensiones
            Log.d(TAG, "forceRecyclerViewRefresh: Dimensiones - W:" +
                    recyclerView.getWidth() + " H:" + recyclerView.getHeight());

            // Forzar layout
            recyclerView.requestLayout();

            // Scroll para forzar binding
            recyclerView.scrollToPosition(0);

            Log.d(TAG, "forceRecyclerViewRefresh: Layout y scroll forzados");
        });
    }

    /**
     * Estado vacío mejorado
     */
    private void showEmptyState() {
        Log.d(TAG, "Mostrando estado vacío");

        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE); // Mantener visible
        }

        // Actualizar totales con valores en cero
        if (binding != null) {
            if (binding.tvTotalItemsInitial != null) {
                binding.tvTotalItemsInitial.setText("0 items");
            }
            if (binding.tvTotalBudgetInitial != null) {
                binding.tvTotalBudgetInitial.setText(currencyFormat.format(0.0));
            }
        }
    }

    /**
     * Mostrar contenido - VERSIÓN MEJORADA
     */
    private void showContent() {
        Log.d(TAG, "showContent: Mostrando contenido con " + budgetItems.size() + " items");

        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "showContent: RecyclerView visibilidad establecida a VISIBLE");

            // Verificar en el siguiente frame
            recyclerView.post(() -> {
                Log.d(TAG, "showContent: Verificación post - Visibilidad: " + recyclerView.getVisibility());
                Log.d(TAG, "showContent: Verificación post - Items en adapter: " +
                        (adapter != null ? adapter.getItemCount() : "adapter null"));
            });
        } else {
            Log.e(TAG, "showContent: RecyclerView es null");
        }

        // Asegurar que el binding esté disponible
        if (binding != null && binding.tvTotalItemsInitial != null && binding.tvTotalBudgetInitial != null) {
            Log.d(TAG, "showContent: Binding disponible para actualizar totales");
        } else {
            Log.e(TAG, "showContent: Binding o vistas de totales no disponibles");
        }
    }

    /**
     * MÉTODO PRINCIPAL - Actualizar datos del presupuesto desde el ViewModel
     * VERSIÓN CORREGIDA - Sin threading innecesario
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

        // CORRECCIÓN: Actualizar UI directamente (ya estamos en el hilo principal)
        updateUI(totalAmount);
    }

    /**
     * Actualizar UI - VERSIÓN MEJORADA CON DEBUGGING
     */
    private void updateUI(double totalAmount) {
        Log.d(TAG, "updateUI: Iniciando actualización con monto: " + totalAmount);

        try {
            // Verificar que el fragment aún esté activo
            if (!isAdded() || getContext() == null) {
                Log.w(TAG, "updateUI: Fragment no está activo, saltando actualización");
                return;
            }

            // Notificar al adapter que los datos han cambiado
            if (adapter != null) {
                Log.d(TAG, "updateUI: Actualizando adapter con " + budgetItems.size() + " items");
                adapter.updateItems(budgetItems);

                // Forzar refresh después de un breve delay
                recyclerView.postDelayed(() -> {
                    forceRecyclerViewRefresh();
                }, 100);

            } else {
                Log.e(TAG, "updateUI: ERROR - Adapter es null");
            }

            // Actualizar totales en la UI
            updateTotals(budgetItems.size(), totalAmount);

            // Mostrar el contenido o estado vacío según corresponda
            if (budgetItems.isEmpty()) {
                showEmptyState();
                Log.d(TAG, "updateUI: Mostrando estado vacío");
            } else {
                showContent();
                Log.d(TAG, "updateUI: Mostrando contenido con " + budgetItems.size() + " items");
            }

        } catch (Exception e) {
            Log.e(TAG, "updateUI: Error durante actualización", e);
        }
    }


    /**
     * Actualizar los totales mostrados en la UI - VERSIÓN CORREGIDA
     */
    private void updateTotals(int itemCount, double totalAmount) {
        Log.d(TAG, "Actualizando totales - Items: " + itemCount + ", Monto: " + totalAmount);

        try {
            // Verificar que el binding esté disponible
            if (binding == null) {
                Log.e(TAG, "Error: binding es null al actualizar totales");
                return;
            }

            // Actualizar contador de items
            TextView tvTotalItems = binding.tvTotalItemsInitial;
            if (tvTotalItems != null) {
                String itemsText = itemCount == 1 ? "1 item" : itemCount + " items";
                tvTotalItems.setText(itemsText);
                Log.d(TAG, "Total items actualizado: " + itemsText);
            } else {
                Log.e(TAG, "tvTotalItemsInitial es null");
            }

            // Actualizar total de presupuesto
            TextView tvTotalBudget = binding.tvTotalBudgetInitial;
            if (tvTotalBudget != null) {
                String totalText = currencyFormat.format(totalAmount);
                tvTotalBudget.setText(totalText);
                Log.d(TAG, "Total presupuesto actualizado: " + totalText);
            } else {
                Log.e(TAG, "tvTotalBudgetInitial es null");
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