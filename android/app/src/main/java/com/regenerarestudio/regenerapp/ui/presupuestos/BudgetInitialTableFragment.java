package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.databinding.FragmentTableBudgetInitialBinding;

// IMPORTS CORREGIDOS Y AGREGADOS
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.regenerarestudio.regenerapp.data.models.BudgetItemCreateUpdateRequest;
import com.regenerarestudio.regenerapp.MainActivity;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para mostrar la tabla de Presupuesto Inicial
 * VERSIÓN CORREGIDA - Actualiza UI correctamente con CRUD completo
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
     * Configuración mejorada del RecyclerView con callbacks
     */
    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Iniciando configuración");

        if (binding == null || binding.rvBudgetInitial == null) {
            Log.e(TAG, "setupRecyclerView: binding o RecyclerView es null");
            return;
        }

        recyclerView = binding.rvBudgetInitial;
        Log.d(TAG, "setupRecyclerView: RecyclerView obtenido del binding");

        // Configurar LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Inicializar lista si es necesaria
        if (budgetItems == null) {
            budgetItems = new ArrayList<>();
            Log.d(TAG, "setupRecyclerView: Lista budgetItems inicializada");
        }

        // Crear adapter con callbacks implementados
        adapter = new BudgetInitialAdapter(
                budgetItems,
                this::onItemClick,        // Callback para click en item
                new BudgetInitialAdapter.OnItemMenuClickListener() {
                    @Override
                    public void onEditItem(BudgetItem item, int position) {
                        handleEditItem(item, position);
                    }

                    @Override
                    public void onDeleteItem(BudgetItem item, int position) {
                        handleDeleteItem(item, position);
                    }

                    @Override
                    public void onCopyItemToExpenses(BudgetItem item, int position) {
                        handleCopyItemToExpenses(item, position);
                    }
                }
        );

        recyclerView.setAdapter(adapter);
        Log.d(TAG, "setupRecyclerView: Adapter asignado al RecyclerView con callbacks");

        // Configuraciones adicionales
        recyclerView.setHasFixedSize(false);
        recyclerView.setItemAnimator(null);
        recyclerView.setVisibility(View.VISIBLE);

        Log.d(TAG, "setupRecyclerView: Configuración completada exitosamente");
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    /**
     * Obtener ID del proyecto actual desde MainActivity
     */
    private long getCurrentProjectId() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            return activity.getSelectedProjectId();
        }
        return -1L;
    }

    // ==========================================
    // CALLBACKS DEL ADAPTER
    // ==========================================

    /**
     * Callback para click en item completo
     */
    private void onItemClick(BudgetItem item, int position) {
        Log.d(TAG, "onItemClick: Item clickeado - " + item.getDescription() + " en posición " + position);

        // Por ahora solo mostrar info, podrías expandir para mostrar detalles
        Toast.makeText(getContext(),
                "Item: " + item.getDescription(),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Manejar edición de item
     */
    private void handleEditItem(BudgetItem item, int position) {
        Log.d(TAG, "handleEditItem: Editando item - " + item.getDescription());

        // Llamar al método del fragment padre para mostrar formulario de edición
        if (getParentFragment() instanceof PresupuestosFragment) {
            PresupuestosFragment parentFragment = (PresupuestosFragment) getParentFragment();

            // Convertir BudgetItem a BudgetItemCreateUpdateRequest para edición
            BudgetItemCreateUpdateRequest editRequest = new BudgetItemCreateUpdateRequest();
            editRequest.setId(item.getId());
            editRequest.setProjectId(item.getProjectId());
            editRequest.setDescription(item.getDescription());
            editRequest.setQuantity(item.getQuantity());
            editRequest.setUnitPrice(item.getUnitPrice());
            editRequest.setSpaces(item.getSpaces());
            editRequest.setCategory(item.getCategory());
            editRequest.setUnit(item.getUnit());
            editRequest.setSupplierId(item.getSupplierId());

            // Llamar método del padre para mostrar formulario
            parentFragment.showManualEntryForm(false, editRequest);
        }
    }

    /**
     * Manejar eliminación de item
     */
    private void handleDeleteItem(BudgetItem item, int position) {
        Log.d(TAG, "handleDeleteItem: Eliminando item - " + item.getDescription());
        showDeleteConfirmationDialog(item, position);
    }

    /**
     * Manejar copia de item a gastos
     */
    private void handleCopyItemToExpenses(BudgetItem item, int position) {
        Log.d(TAG, "handleCopyItemToExpenses: Copiando item a gastos - " + item.getDescription());
        showCopyToExpensesDialog(item);
    }

    // ==========================================
    // MÉTODOS DE ACCIÓN
    // ==========================================

    /**
     * Mostrar diálogo de confirmación para eliminar item
     */
    private void showDeleteConfirmationDialog(BudgetItem item, int position) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de que desea eliminar este item?\n\n" +
                        "Material: " + item.getDescription() + "\n" +
                        "Total: " + currencyFormat.format(item.getQuantity() * item.getUnitPrice()))
                .setIcon(R.drawable.ic_warning_24)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteItemFromBudget(item, position);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Eliminar item del presupuesto llamando al ViewModel
     */
    private void deleteItemFromBudget(BudgetItem item, int position) {
        Log.d(TAG, "deleteItemFromBudget: Eliminando item ID " + item.getId());

        // Obtener referencia al ViewModel del fragment padre
        if (getParentFragment() instanceof PresupuestosFragment) {
            PresupuestosFragment parentFragment = (PresupuestosFragment) getParentFragment();
            PresupuestosViewModel viewModel = parentFragment.getPresupuestosViewModel();

            if (viewModel != null) {
                viewModel.deleteBudgetItem(item.getId());

                // Mostrar mensaje de éxito
                Toast.makeText(getContext(),
                        "Item eliminado: " + item.getDescription(),
                        Toast.LENGTH_SHORT).show();

                Log.d(TAG, "deleteItemFromBudget: Eliminación solicitada al ViewModel");
            } else {
                Log.e(TAG, "deleteItemFromBudget: ViewModel es null");
                Toast.makeText(getContext(), "Error: No se pudo eliminar el item", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Mostrar diálogo para copiar item individual a gastos reales
     */
    private void showCopyToExpensesDialog(BudgetItem item) {
        // Calcular total del item
        double totalAmount = item.getQuantity() * item.getUnitPrice();

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Copiar a gastos reales")
                .setMessage("¿Desea copiar este item a gastos reales?\n\n" +
                        "Material: " + item.getDescription() + "\n" +
                        "Cantidad: " + item.getQuantity() + " " + item.getUnit() + "\n" +
                        "Total: " + currencyFormat.format(totalAmount) + "\n\n" +
                        "Nota: Podrá modificar el precio y agregar descuentos después.")
                .setIcon(R.drawable.ic_copy_24)
                .setPositiveButton("Copiar", (dialog, which) -> {
                    copyItemToExpenses(item);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Copiar item individual a gastos reales - VERSIÓN CORREGIDA
     */
    private void copyItemToExpenses(BudgetItem item) {
        Log.d(TAG, "copyItemToExpenses: Copiando item - " + item.getDescription());

        // Crear Map para gasto real basado en el item de presupuesto
        Map<String, Object> expenseMap = new HashMap<>();
        expenseMap.put("project", item.getProjectId());
        expenseMap.put("description", item.getDescription());
        expenseMap.put("quantity", item.getQuantity());
        expenseMap.put("unit_price", item.getUnitPrice());
        expenseMap.put("unit", item.getUnit());
        expenseMap.put("category", item.getCategory());
        expenseMap.put("spaces", item.getSpaces());
        expenseMap.put("supplier", item.getSupplierId());

        // Campos específicos para gastos (valores por defecto)
        expenseMap.put("discount", 0.0); // Sin descuento inicial

        // Fecha actual como fecha de compra por defecto
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        expenseMap.put("purchase_date", sdf.format(new Date()));

        // Obtener ViewModel y crear gasto real
        if (getParentFragment() instanceof PresupuestosFragment) {
            PresupuestosFragment parentFragment = (PresupuestosFragment) getParentFragment();
            PresupuestosViewModel viewModel = parentFragment.getPresupuestosViewModel();

            if (viewModel != null) {
                viewModel.addExpenseReal(expenseMap); // 🟢 MÉTODO CORREGIDO

                // Mostrar mensaje de éxito
                Toast.makeText(getContext(),
                        "Item copiado a gastos reales: " + item.getDescription(),
                        Toast.LENGTH_LONG).show();

                Log.d(TAG, "copyItemToExpenses: Item copiado exitosamente");
            } else {
                Log.e(TAG, "copyItemToExpenses: ViewModel es null");
                Toast.makeText(getContext(), "Error: No se pudo copiar el item", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    /**
     * Obtener referencia al ViewModel del fragment padre
     */
    public PresupuestosViewModel getPresupuestosViewModel() {
        if (getParentFragment() instanceof PresupuestosFragment) {
            return ((PresupuestosFragment) getParentFragment()).getPresupuestosViewModel();
        }
        return null;
    }

    // ==========================================
    // MÉTODOS DE ESTADO DE UI
    // ==========================================

    /**
     * Mostrar estado vacío
     */
    private void showEmptyState() {
        Log.d(TAG, "showEmptyState: Mostrando estado vacío");

        if (adapter != null) {
            adapter.updateItems(new ArrayList<>());
        }

        updateTotalsInUI(0, 0.0);
    }

    /**
     * Mostrar contenido con datos
     */
    private void showContent() {
        Log.d(TAG, "showContent: Mostrando contenido con datos");

        if (recyclerView != null) {
            recyclerView.post(() -> {
                Log.d(TAG, "showContent: RecyclerView - Items en adapter: " +
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
                Log.e(TAG, "updateUI: Adapter es null, no se puede actualizar");
            }

            // Actualizar totales
            updateTotalsInUI(budgetItems.size(), totalAmount);

            // Mostrar contenido
            showContent();

            Log.d(TAG, "updateUI: Actualización completada exitosamente");

        } catch (Exception e) {
            Log.e(TAG, "updateUI: Error durante actualización: " + e.getMessage(), e);
        }
    }

    /**
     * Forzar refresh del RecyclerView
     */
    private void forceRecyclerViewRefresh() {
        if (recyclerView != null && adapter != null) {
            Log.d(TAG, "forceRecyclerViewRefresh: Forzando refresh");
            adapter.notifyDataSetChanged();
            recyclerView.invalidate();
            recyclerView.requestLayout();
        }
    }

    /**
     * Actualizar totales en la UI
     */
    private void updateTotalsInUI(int itemCount, double totalAmount) {
        Log.d(TAG, "updateTotalsInUI: Items: " + itemCount + ", Total: " + totalAmount);

        try {
            // Verificar que binding no sea null
            if (binding == null) {
                Log.e(TAG, "updateTotalsInUI: Binding es null");
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
     * Crear BudgetItem desde Map del backend - VERSIÓN CORREGIDA
     */
    private BudgetItem createBudgetItemFromMap(Map<String, Object> itemData) {
        BudgetItem item = new BudgetItem();

        // ID
        item.setId(parseLongFromObject(itemData.get("id")));

        // PROJECT ID
        item.setProjectId(getCurrentProjectId());

        // Descripción
        item.setDescription(parseStringFromObject(itemData.get("description")));

        // Categoría
        item.setCategory(parseStringFromObject(itemData.get("category")));
        item.setCategoryDisplay(parseStringFromObject(itemData.get("category_display")));

        // Espacios
        item.setSpaces(parseStringFromObject(itemData.get("spaces")));

        // Cantidad - usando método auxiliar
        Double quantity = parseDoubleFromObject(itemData.get("quantity"));
        item.setQuantity(quantity != null ? quantity : 1.0);

        // Unidad
        item.setUnit(parseStringFromObject(itemData.get("unit")));

        // Precio unitario - usando método auxiliar
        Double unitPrice = parseDoubleFromObject(itemData.get("unit_price"));
        item.setUnitPrice(unitPrice != null ? unitPrice : 0.0);

        // Precio total - usando método auxiliar
        Double totalPrice = parseDoubleFromObject(itemData.get("total_price"));
        if (totalPrice != null) {
            item.setTotalPrice(totalPrice);
        } else {
            // Calcular si no viene del backend
            item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
        }

        // Proveedor
        item.setSupplierName(parseStringFromObject(itemData.get("supplier_name")));
        item.setSupplierId(parseLongFromObject(itemData.get("supplier")));

        // Notas
        item.setNotes(parseStringFromObject(itemData.get("notes")));

        // Log para debug
        Log.d(TAG, "Item creado: " + item.getDescription() +
                " - Cantidad: " + item.getQuantity() +
                " - Precio unitario: " + item.getUnitPrice() +
                " - Total: " + item.getTotalPrice());

        return item;
    }

    // ==========================================
    // MÉTODOS DE CONVERSIÓN SEGUROS
    // ==========================================

    /**
     * Convierte un objeto a Long de manera segura
     * Maneja tanto Number como String
     */
    private Long parseLongFromObject(Object obj) {
        if (obj == null) return null;

        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }

        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Error al convertir string a long: " + obj);
                return null;
            }
        }

        return null;
    }

    /**
     * Convierte un objeto a Double de manera segura
     * Maneja tanto Number como String
     */
    private Double parseDoubleFromObject(Object obj) {
        if (obj == null) return null;

        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }

        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Error al convertir string a double: " + obj);
                return null;
            }
        }

        return null;
    }

    /**
     * Convierte un objeto a String de manera segura
     */
    private String parseStringFromObject(Object obj) {
        if (obj == null) return null;
        return obj.toString();
    }

    /**
     * Clase modelo para items del presupuesto - VERSIÓN CORREGIDA CON PROJECT ID
     */
    public static class BudgetItem {
        private Long id;
        private Long projectId; // 🟢 AGREGADO
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

        // 🟢 MÉTODOS AGREGADOS PARA PROJECT ID
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }

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