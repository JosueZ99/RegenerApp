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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.regenerarestudio.regenerapp.databinding.FragmentTableBudgetInitialBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment para la tabla de Presupuesto Inicial
 * Actualizado para usar datos reales del backend Django - IDs CORREGIDOS
 */
public class BudgetInitialTableFragment extends Fragment {

    private static final String TAG = "BudgetInitialTable";

    private FragmentTableBudgetInitialBinding binding;
    private BudgetInitialAdapter adapter;
    private List<BudgetItem> budgetItems;
    private NumberFormat currencyFormat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTableBudgetInitialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated - Inicializando BudgetInitialTableFragment");

        // Inicializar formatters
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));

        setupRecyclerView();
        setupSearchAndFilter();

        // Mostrar estado inicial (sin datos)
        showEmptyState();
    }

    private void setupRecyclerView() {
        budgetItems = new ArrayList<>();
        adapter = new BudgetInitialAdapter(budgetItems, this::onItemClick, this::onItemMenuClick);

        binding.rvBudgetInitial.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBudgetInitial.setAdapter(adapter);

        Log.d(TAG, "RecyclerView configurado");
    }

    private void setupSearchAndFilter() {
        // Configurar búsqueda
        binding.etSearchInitial.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // TODO: Implementar lógica de búsqueda en tiempo real
                Log.d(TAG, "Búsqueda activada - TODO: implementar");
            }
        });

        // Configurar filtro
        binding.btnFilterInitial.setOnClickListener(v -> {
            // TODO: Mostrar dialog de filtros
            Toast.makeText(requireContext(), "Filtros - Próximamente", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Filtros solicitados - TODO: implementar");
        });
    }

    /**
     * Mostrar estado vacío cuando no hay datos
     * CORREGIDO: Usar IDs correctos del layout XML
     */
    private void showEmptyState() {
        Log.d(TAG, "Mostrando estado vacío");
        // IDs CORREGIDOS según fragment_table_budget_initial.xml
        binding.tvTotalItemsInitial.setText("0 items");
        binding.tvTotalBudgetInitial.setText(currencyFormat.format(0.0));
    }

    /**
     * Método público llamado por PresupuestosFragment para actualizar datos
     * Convierte datos del backend (Map) a objetos BudgetItem locales
     */
    public void updateBudgetData(List<Map<String, Object>> backendData) {
        Log.d(TAG, "updateBudgetData - Recibidos " +
                (backendData != null ? backendData.size() : 0) + " items del backend");

        if (backendData == null || backendData.isEmpty()) {
            // Sin datos del backend
            budgetItems.clear();
            adapter.notifyDataSetChanged();
            showEmptyState();
            return;
        }

        try {
            // Convertir datos del backend a objetos locales
            List<BudgetItem> newBudgetItems = convertBackendDataToBudgetItems(backendData);

            // Actualizar lista y adapter
            budgetItems.clear();
            budgetItems.addAll(newBudgetItems);
            adapter.notifyDataSetChanged();

            // Actualizar totales
            updateTotals();

            Log.d(TAG, "Datos actualizados exitosamente - " + budgetItems.size() + " items");

        } catch (Exception e) {
            Log.e(TAG, "Error al procesar datos del backend", e);
            showError("Error al procesar datos del presupuesto inicial");
        }
    }

    /**
     * Convertir datos del backend (Django JSON) a objetos BudgetItem
     */
    private List<BudgetItem> convertBackendDataToBudgetItems(List<Map<String, Object>> backendData) {
        List<BudgetItem> items = new ArrayList<>();

        for (Map<String, Object> itemData : backendData) {
            try {
                BudgetItem budgetItem = createBudgetItemFromBackendData(itemData);
                if (budgetItem != null) {
                    items.add(budgetItem);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error al procesar item individual: " + itemData, e);
                // Continuar con los demás items
            }
        }

        Log.d(TAG, "Convertidos " + items.size() + " items exitosamente");
        return items;
    }

    /**
     * Crear un BudgetItem desde datos del backend
     */
    private BudgetItem createBudgetItemFromBackendData(Map<String, Object> itemData) {
        try {
            // Extraer datos del Map (campo por campo del modelo Django)
            Long id = parseLongFromObject(itemData.get("id"));
            String description = parseStringFromObject(itemData.get("description"));
            String category = parseStringFromObject(itemData.get("category"));
            Double quantity = parseDoubleFromObject(itemData.get("quantity"));
            String unit = parseStringFromObject(itemData.get("unit"));
            Double unitPrice = parseDoubleFromObject(itemData.get("unit_price"));

            // Datos del proveedor (pueden venir en un objeto anidado)
            String supplierName = "Sin proveedor";
            Double supplierRating = 0.0;

            Object supplierData = itemData.get("supplier");
            if (supplierData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> supplier = (Map<String, Object>) supplierData;
                supplierName = parseStringFromObject(supplier.get("name"));
                supplierRating = parseDoubleFromObject(supplier.get("rating"));
            } else if (supplierData instanceof String) {
                supplierName = (String) supplierData;
            }

            // Crear el objeto BudgetItem
            BudgetItem budgetItem = new BudgetItem(
                    id != null ? id : 0L,
                    description != null ? description : "Sin descripción",
                    category != null ? category : "Sin categoría",
                    quantity != null ? quantity : 0.0,
                    unit != null ? unit : "Unidad",
                    unitPrice != null ? unitPrice : 0.0,
                    supplierName,
                    supplierRating != null ? supplierRating : 0.0
            );

            Log.v(TAG, "Item creado: " + budgetItem.getMaterialName() + " - " +
                    currencyFormat.format(budgetItem.getTotalPrice()));

            return budgetItem;

        } catch (Exception e) {
            Log.e(TAG, "Error al crear BudgetItem desde datos del backend: " + itemData, e);
            return null;
        }
    }

    // ==========================================
    // MÉTODOS DE CONVERSIÓN SEGUROS
    // ==========================================

    private Long parseLongFromObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
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

    private Double parseDoubleFromObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
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

    private String parseStringFromObject(Object obj) {
        if (obj == null) return null;
        return obj.toString();
    }

    /**
     * Actualizar totales mostrados en la UI
     * CORREGIDO: Usar IDs correctos del layout XML
     */
    private void updateTotals() {
        int totalItems = budgetItems.size();
        double totalAmount = 0.0;

        for (BudgetItem item : budgetItems) {
            totalAmount += item.getTotalPrice();
        }

        // Actualizar UI - IDs CORREGIDOS según fragment_table_budget_initial.xml
        binding.tvTotalItemsInitial.setText(totalItems + " items");
        binding.tvTotalBudgetInitial.setText(currencyFormat.format(totalAmount));

        Log.d(TAG, String.format("Totales actualizados - Items: %d, Monto: %.2f",
                totalItems, totalAmount));
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
    // EVENTOS DE CLICKS
    // ==========================================

    private void onItemClick(BudgetItem item) {
        Log.d(TAG, "Click en item: " + item.getMaterialName());
        Toast.makeText(requireContext(), "Seleccionado: " + item.getMaterialName(), Toast.LENGTH_SHORT).show();
    }

    private void onItemMenuClick(BudgetItem item) {
        Log.d(TAG, "Menú solicitado para item: " + item.getMaterialName());
        Toast.makeText(requireContext(), "Menú para: " + item.getMaterialName(), Toast.LENGTH_SHORT).show();
        // TODO: Implementar menú contextual (editar, eliminar, etc.)
    }

    // ==========================================
    // MÉTODOS PÚBLICOS
    // ==========================================

    /**
     * Método público para refrescar datos
     */
    public void refreshData() {
        Log.d(TAG, "refreshData solicitado - delegando a fragment padre");
        // Los datos vienen del fragment padre (PresupuestosFragment)
        // que a su vez los obtiene del ViewModel
        if (getParentFragment() instanceof PresupuestosFragment) {
            ((PresupuestosFragment) getParentFragment()).refreshBudgetData();
        }
    }

    // ==========================================
    // CICLO DE VIDA
    // ==========================================

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "Vista destruida");
    }

    // ==========================================
    // CLASE MODELO LOCAL
    // ==========================================

    /**
     * Clase modelo para items del presupuesto inicial
     * Representa datos locales convertidos desde el backend
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

        @Override
        public String toString() {
            return "BudgetItem{" +
                    "id=" + id +
                    ", materialName='" + materialName + '\'' +
                    ", category='" + category + '\'' +
                    ", quantity=" + quantity +
                    ", unit='" + unit + '\'' +
                    ", unitPrice=" + unitPrice +
                    ", supplierName='" + supplierName + '\'' +
                    ", supplierRating=" + supplierRating +
                    '}';
        }
    }
}