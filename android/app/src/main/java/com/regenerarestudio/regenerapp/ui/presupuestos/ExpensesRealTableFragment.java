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

import com.regenerarestudio.regenerapp.databinding.FragmentTableExpensesRealBinding;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment para la tabla de Gastos Reales
 * Actualizado para usar datos reales del backend Django - IDs VERIFICADOS
 */
public class ExpensesRealTableFragment extends Fragment {

    private static final String TAG = "ExpensesRealTable";

    private FragmentTableExpensesRealBinding binding;
    private ExpensesRealAdapter adapter;
    private List<ExpenseItem> expenseItems;
    private NumberFormat currencyFormat;
    private SimpleDateFormat backendDateFormatter;
    private SimpleDateFormat displayDateFormatter;
    private SimpleDateFormat displayTimeFormatter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTableExpensesRealBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated - Inicializando ExpensesRealTableFragment");

        // Inicializar formatters
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));
        backendDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        displayDateFormatter = new SimpleDateFormat("dd/MM", Locale.getDefault());
        displayTimeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

        setupRecyclerView();
        setupSearchAndFilter();

        // Mostrar estado inicial (sin datos)
        showEmptyState();
    }

    private void setupRecyclerView() {
        expenseItems = new ArrayList<>();
        adapter = new ExpensesRealAdapter(expenseItems, this::onItemClick, this::onItemMenuClick);

        binding.rvExpensesReal.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpensesReal.setAdapter(adapter);

        Log.d(TAG, "RecyclerView configurado");
    }

    private void setupSearchAndFilter() {
        // Configurar búsqueda
        binding.etSearchExpenses.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // TODO: Implementar lógica de búsqueda en tiempo real
                Log.d(TAG, "Búsqueda activada - TODO: implementar");
            }
        });

        // Configurar filtros
        binding.btnFilterExpenses.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Filtros de gastos - Próximamente", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Filtros solicitados - TODO: implementar");
        });

        binding.btnDateFilter.setOnClickListener(v -> {
            // TODO: Mostrar selector de rango de fechas
            Toast.makeText(requireContext(), "Filtro por fechas - Próximamente", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Filtro de fechas solicitado - TODO: implementar");
        });
    }

    /**
     * Mostrar estado vacío cuando no hay datos
     * IDs VERIFICADOS según fragment_table_expenses_real.xml
     */
    private void showEmptyState() {
        Log.d(TAG, "Mostrando estado vacío");
        // IDs VERIFICADOS en fragment_table_expenses_real.xml
        binding.tvTotalItemsExpenses.setText("0 compras");
        binding.tvSubtotalExpenses.setText(currencyFormat.format(0.0));
        binding.tvTotalDiscounts.setText(currencyFormat.format(0.0));
        binding.tvTotalExpensesReal.setText(currencyFormat.format(0.0));
    }

    /**
     * Método público llamado por PresupuestosFragment para actualizar datos
     * Convierte datos del backend (Map) a objetos ExpenseItem locales
     */
    public void updateExpensesData(List<Map<String, Object>> backendData) {
        Log.d(TAG, "updateExpensesData - Recibidos " +
                (backendData != null ? backendData.size() : 0) + " gastos del backend");

        if (backendData == null || backendData.isEmpty()) {
            // Sin datos del backend
            expenseItems.clear();
            adapter.notifyDataSetChanged();
            showEmptyState();
            return;
        }

        try {
            // Convertir datos del backend a objetos locales
            List<ExpenseItem> newExpenseItems = convertBackendDataToExpenseItems(backendData);

            // Actualizar lista y adapter
            expenseItems.clear();
            expenseItems.addAll(newExpenseItems);
            adapter.notifyDataSetChanged();

            // Actualizar totales
            updateTotals();

            Log.d(TAG, "Datos actualizados exitosamente - " + expenseItems.size() + " gastos");

        } catch (Exception e) {
            Log.e(TAG, "Error al procesar datos del backend", e);
            showError("Error al procesar datos de gastos reales");
        }
    }

    /**
     * Convertir datos del backend (Django JSON) a objetos ExpenseItem
     */
    private List<ExpenseItem> convertBackendDataToExpenseItems(List<Map<String, Object>> backendData) {
        List<ExpenseItem> items = new ArrayList<>();

        for (Map<String, Object> itemData : backendData) {
            try {
                ExpenseItem expenseItem = createExpenseItemFromBackendData(itemData);
                if (expenseItem != null) {
                    items.add(expenseItem);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error al procesar gasto individual: " + itemData, e);
                // Continuar con los demás items
            }
        }

        Log.d(TAG, "Convertidos " + items.size() + " gastos exitosamente");
        return items;
    }

    /**
     * Crear un ExpenseItem desde datos del backend
     */
    private ExpenseItem createExpenseItemFromBackendData(Map<String, Object> itemData) {
        try {
            // Extraer datos del Map (campo por campo del modelo Django RealExpense)
            Long id = parseLongFromObject(itemData.get("id"));
            String description = parseStringFromObject(itemData.get("description"));
            String category = parseStringFromObject(itemData.get("category"));
            Double quantity = parseDoubleFromObject(itemData.get("quantity"));
            String unit = parseStringFromObject(itemData.get("unit"));
            Double unitPrice = parseDoubleFromObject(itemData.get("unit_price"));
            Double discountPercentage = parseDoubleFromObject(itemData.get("discount_percentage"));
            String invoiceNumber = parseStringFromObject(itemData.get("invoice_number"));
            String purchaseDateStr = parseStringFromObject(itemData.get("purchase_date"));

            // Datos del proveedor (pueden venir en un objeto anidado)
            String supplierName = "Sin proveedor";
            Object supplierData = itemData.get("supplier");
            if (supplierData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> supplier = (Map<String, Object>) supplierData;
                supplierName = parseStringFromObject(supplier.get("name"));
            } else if (supplierData instanceof String) {
                supplierName = (String) supplierData;
            }

            // Datos del material (pueden venir en un objeto anidado o como string)
            String materialCode = "SIN-COD";
            Object materialData = itemData.get("material");
            if (materialData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> material = (Map<String, Object>) materialData;
                materialCode = parseStringFromObject(material.get("code"));
                if (materialCode == null) {
                    materialCode = "SIN-COD";
                }
            }

            // Procesar fecha de compra
            Date purchaseDate = new Date(); // Fecha por defecto
            String displayDate = displayDateFormatter.format(purchaseDate);
            String displayTime = displayTimeFormatter.format(purchaseDate);

            if (purchaseDateStr != null && !purchaseDateStr.isEmpty()) {
                try {
                    purchaseDate = backendDateFormatter.parse(purchaseDateStr);
                    displayDate = displayDateFormatter.format(purchaseDate);
                    displayTime = displayTimeFormatter.format(purchaseDate);
                } catch (ParseException e) {
                    Log.w(TAG, "Error al parsear fecha: " + purchaseDateStr, e);
                }
            }

            // Crear el objeto ExpenseItem
            ExpenseItem expenseItem = new ExpenseItem(
                    id != null ? id : 0L,
                    description != null ? description : "Sin descripción",
                    materialCode,
                    quantity != null ? quantity : 0.0,
                    unit != null ? unit : "Unidad",
                    unitPrice != null ? unitPrice : 0.0,
                    discountPercentage != null ? discountPercentage : 0.0,
                    supplierName,
                    invoiceNumber != null ? invoiceNumber : "Sin factura",
                    purchaseDate,
                    displayDate,
                    displayTime
            );

            Log.v(TAG, "Gasto creado: " + expenseItem.getMaterialName() + " - " +
                    currencyFormat.format(expenseItem.getTotalPrice()));

            return expenseItem;

        } catch (Exception e) {
            Log.e(TAG, "Error al crear ExpenseItem desde datos del backend: " + itemData, e);
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
     * IDs VERIFICADOS según fragment_table_expenses_real.xml
     */
    private void updateTotals() {
        double subtotal = 0.0;
        double totalDiscounts = 0.0;

        for (ExpenseItem item : expenseItems) {
            double itemSubtotal = item.getQuantity() * item.getUnitPrice();
            double itemDiscount = itemSubtotal * (item.getDiscountPercentage() / 100);

            subtotal += itemSubtotal;
            totalDiscounts += itemDiscount;
        }

        double total = subtotal - totalDiscounts;

        // Actualizar UI - IDs VERIFICADOS en fragment_table_expenses_real.xml
        binding.tvSubtotalExpenses.setText(currencyFormat.format(subtotal));
        binding.tvTotalDiscounts.setText("-" + currencyFormat.format(totalDiscounts));
        binding.tvTotalExpensesReal.setText(currencyFormat.format(total));
        binding.tvTotalItemsExpenses.setText(expenseItems.size() + " compras");

        Log.d(TAG, String.format("Totales actualizados - Items: %d, Subtotal: %.2f, Descuentos: %.2f, Total: %.2f",
                expenseItems.size(), subtotal, totalDiscounts, total));
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

    private void onItemClick(ExpenseItem item) {
        Log.d(TAG, "Click en gasto: " + item.getMaterialName());
        // TODO: Mostrar detalles del gasto o permitir edición
        Toast.makeText(requireContext(), "Ver detalles: " + item.getMaterialName(), Toast.LENGTH_SHORT).show();
    }

    private void onItemMenuClick(ExpenseItem item) {
        Log.d(TAG, "Menú solicitado para gasto: " + item.getMaterialName());
        // TODO: Mostrar menú contextual (ver factura, editar, eliminar, etc.)
        Toast.makeText(requireContext(), "Menú para: " + item.getMaterialName(), Toast.LENGTH_SHORT).show();
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
     * Clase modelo para items de gastos reales
     * Representa datos locales convertidos desde el backend
     */
    public static class ExpenseItem {
        private long id;
        private String materialName;
        private String materialCode;
        private double quantity;
        private String unit;
        private double unitPrice;
        private double discountPercentage;
        private String supplierName;
        private String invoiceNumber;
        private Date purchaseDate;
        private String displayDate;
        private String displayTime;

        public ExpenseItem(long id, String materialName, String materialCode, double quantity,
                           String unit, double unitPrice, double discountPercentage,
                           String supplierName, String invoiceNumber, Date purchaseDate,
                           String displayDate, String displayTime) {
            this.id = id;
            this.materialName = materialName;
            this.materialCode = materialCode;
            this.quantity = quantity;
            this.unit = unit;
            this.unitPrice = unitPrice;
            this.discountPercentage = discountPercentage;
            this.supplierName = supplierName;
            this.invoiceNumber = invoiceNumber;
            this.purchaseDate = purchaseDate;
            this.displayDate = displayDate;
            this.displayTime = displayTime;
        }

        public double getTotalPrice() {
            double subtotal = quantity * unitPrice;
            double discount = subtotal * (discountPercentage / 100);
            return subtotal - discount;
        }

        public double getDiscountAmount() {
            double subtotal = quantity * unitPrice;
            return subtotal * (discountPercentage / 100);
        }

        // Getters
        public long getId() { return id; }
        public String getMaterialName() { return materialName; }
        public String getMaterialCode() { return materialCode; }
        public double getQuantity() { return quantity; }
        public String getUnit() { return unit; }
        public double getUnitPrice() { return unitPrice; }
        public double getDiscountPercentage() { return discountPercentage; }
        public String getSupplierName() { return supplierName; }
        public String getInvoiceNumber() { return invoiceNumber; }
        public Date getPurchaseDate() { return purchaseDate; }
        public String getDisplayDate() { return displayDate; }
        public String getDisplayTime() { return displayTime; }

        // Setters (para edición)
        public void setQuantity(double quantity) { this.quantity = quantity; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
        public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

        @Override
        public String toString() {
            return "ExpenseItem{" +
                    "id=" + id +
                    ", materialName='" + materialName + '\'' +
                    ", materialCode='" + materialCode + '\'' +
                    ", quantity=" + quantity +
                    ", unit='" + unit + '\'' +
                    ", unitPrice=" + unitPrice +
                    ", discountPercentage=" + discountPercentage +
                    ", supplierName='" + supplierName + '\'' +
                    ", invoiceNumber='" + invoiceNumber + '\'' +
                    ", displayDate='" + displayDate + '\'' +
                    ", displayTime='" + displayTime + '\'' +
                    '}';
        }
    }
}