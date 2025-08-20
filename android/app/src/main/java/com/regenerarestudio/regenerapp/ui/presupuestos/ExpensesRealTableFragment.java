package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.regenerarestudio.regenerapp.MainActivity;
import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.databinding.FragmentTableExpensesRealBinding;
import com.regenerarestudio.regenerapp.data.models.BudgetItemCreateUpdateRequest;

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
 * Actualizado con funcionalidad CRUD completa
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
            expenseItems.clear();
            adapter.notifyDataSetChanged();
            showEmptyState();
            return;
        }

        // Convertir datos del backend a objetos ExpenseItem locales
        expenseItems.clear();
        for (Map<String, Object> expenseData : backendData) {
            try {
                ExpenseItem item = convertBackendDataToExpenseItem(expenseData);
                if (item != null) {
                    expenseItems.add(item);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al convertir dato del backend: " + e.getMessage(), e);
            }
        }

        // Actualizar adapter
        adapter.notifyDataSetChanged();

        // Actualizar totales en la UI
        updateTotals();

        Log.d(TAG, "updateExpensesData - Procesados " + expenseItems.size() + " gastos exitosamente");
    }

    /**
     * Convertir datos del backend a ExpenseItem local
     */
    private ExpenseItem convertBackendDataToExpenseItem(Map<String, Object> data) {
        ExpenseItem item = new ExpenseItem();

        // Parsear ID
        Object idObj = data.get("id");
        if (idObj instanceof Number) {
            item.setId(((Number) idObj).longValue());
        }

        // Parsear campos de texto
        item.setMaterialName(parseStringFromObject(data.get("description")));
        item.setMaterialCode(parseStringFromObject(data.get("material_code")));
        item.setUnit(parseStringFromObject(data.get("unit")));
        item.setSupplierName(parseStringFromObject(data.get("supplier_name")));
        item.setInvoiceNumber(parseStringFromObject(data.get("invoice_number")));

        // Parsear campos numéricos
        item.setQuantity(parseDoubleFromObject(data.get("quantity"), 0.0));
        item.setUnitPrice(parseDoubleFromObject(data.get("unit_price"), 0.0));
        item.setDiscountPercentage(parseDoubleFromObject(data.get("discount_percentage"), 0.0));

        // Parsear fecha de compra
        String purchaseDateStr = parseStringFromObject(data.get("purchase_date"));
        if (purchaseDateStr != null) {
            try {
                Date purchaseDate = backendDateFormatter.parse(purchaseDateStr);
                item.setPurchaseDate(purchaseDate);
                item.setDisplayDate(displayDateFormatter.format(purchaseDate));
                item.setDisplayTime(displayTimeFormatter.format(purchaseDate));
            } catch (ParseException e) {
                Log.w(TAG, "Error al parsear fecha de compra: " + purchaseDateStr);
                item.setDisplayDate("Fecha inválida");
                item.setDisplayTime("--:--");
            }
        } else {
            item.setDisplayDate("Sin fecha");
            item.setDisplayTime("--:--");
        }

        return item;
    }

    private double parseDoubleFromObject(Object obj, double defaultValue) {
        if (obj == null) return defaultValue;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Error al convertir string a double: " + obj);
                return defaultValue;
            }
        }
        return defaultValue;
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
    // EVENTOS DE CLICKS - ACTUALIZADO CON CRUD
    // ==========================================

    private void onItemClick(ExpenseItem item) {
        Log.d(TAG, "Click en gasto: " + item.getMaterialName());
        // Al hacer click directo, mostrar detalles
        handleViewExpenseDetails(item);
    }

    /**
     * Manejar click en menú de opciones de cada fila - NUEVO
     */
    private void onItemMenuClick(ExpenseItem item) {
        Log.d(TAG, "Menú solicitado para gasto: " + item.getMaterialName());
        showItemOptionsMenu(item);
    }

    /**
     * Mostrar menú de opciones para un gasto real - NUEVO
     */
    private void showItemOptionsMenu(ExpenseItem item) {
        PopupMenu popupMenu = new PopupMenu(getContext(), binding.rvExpensesReal);
        popupMenu.getMenuInflater().inflate(R.menu.menu_expense_item_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.action_edit_expense) {
                handleEditExpense(item);
                return true;
            } else if (itemId == R.id.action_delete_expense) {
                handleDeleteExpense(item);
                return true;
            } else if (itemId == R.id.action_view_details_expense) {
                handleViewExpenseDetails(item);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    /**
     * Manejar edición de gasto real - NUEVO
     */
    private void handleEditExpense(ExpenseItem item) {
        Log.d(TAG, "handleEditExpense: Editando gasto - " + item.getMaterialName());

        if (getParentFragment() instanceof PresupuestosFragment) {
            PresupuestosFragment parentFragment = (PresupuestosFragment) getParentFragment();

            // Crear objeto para edición con los datos del gasto real
            BudgetItemCreateUpdateRequest editRequest = new BudgetItemCreateUpdateRequest();
            editRequest.setId(item.getId());
            editRequest.setProjectId(getCurrentProjectId());
            editRequest.setDescription(item.getMaterialName());
            editRequest.setQuantity(item.getQuantity());
            editRequest.setUnitPrice(item.getUnitPrice());
            editRequest.setUnit(item.getUnit());
            editRequest.setCategory("construction"); // O determinar la categoría apropiada

            // Campos específicos de gastos reales
            editRequest.setDiscount(item.getDiscountPercentage());
            editRequest.setPurchaseDate(formatDateForBackend(item.getPurchaseDate()));

            // Llamar método del padre para mostrar formulario de gastos reales
            parentFragment.showManualEntryForm(true, editRequest);
        }
    }

    /**
     * Manejar eliminación de gasto real - NUEVO
     */
    private void handleDeleteExpense(ExpenseItem item) {
        Log.d(TAG, "handleDeleteExpense: Eliminando gasto - " + item.getMaterialName());
        showDeleteConfirmationDialog(item);
    }

    /**
     * Manejar visualización de detalles - NUEVO
     */
    private void handleViewExpenseDetails(ExpenseItem item) {
        Log.d(TAG, "handleViewExpenseDetails: Mostrando detalles - " + item.getMaterialName());
        showExpenseDetailsDialog(item);
    }

    /**
     * Mostrar diálogo de confirmación para eliminar gasto - NUEVO
     */
    private void showDeleteConfirmationDialog(ExpenseItem item) {
        double finalAmount = (item.getQuantity() * item.getUnitPrice()) * (1 - item.getDiscountPercentage() / 100);

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Está seguro de que desea eliminar este gasto?\n\n" +
                        "Material: " + item.getMaterialName() + "\n" +
                        "Cantidad: " + item.getQuantity() + " " + item.getUnit() + "\n" +
                        "Total pagado: " + currencyFormat.format(finalAmount))
                .setIcon(R.drawable.ic_warning_24)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteExpenseFromBackend(item);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Eliminar gasto del backend usando el método correcto del ViewModel - NUEVO
     */
    private void deleteExpenseFromBackend(ExpenseItem item) {
        if (getParentFragment() instanceof PresupuestosFragment) {
            PresupuestosFragment parentFragment = (PresupuestosFragment) getParentFragment();
            PresupuestosViewModel viewModel = parentFragment.getPresupuestosViewModel();

            // Usar el método correcto del ViewModel
            viewModel.deleteExpenseReal(item.getId());

            // Mostrar mensaje de confirmación
            Toast.makeText(getContext(), "Eliminando gasto...", Toast.LENGTH_SHORT).show();

            // El ViewModel ya maneja la recarga automática de datos
            Log.d(TAG, "Eliminación solicitada para item ID: " + item.getId());
        }
    }

    /**
     * Mostrar diálogo con detalles del gasto - NUEVO
     */
    private void showExpenseDetailsDialog(ExpenseItem item) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_expense_details, null);

        // Referencias a las vistas del diálogo
        TextView tvMaterial = dialogView.findViewById(R.id.tv_detail_material);
        TextView tvQuantity = dialogView.findViewById(R.id.tv_detail_quantity);
        TextView tvUnitPrice = dialogView.findViewById(R.id.tv_detail_unit_price);
        TextView tvSubtotal = dialogView.findViewById(R.id.tv_detail_subtotal);
        TextView tvDiscount = dialogView.findViewById(R.id.tv_detail_discount);
        TextView tvFinalAmount = dialogView.findViewById(R.id.tv_detail_final_amount);
        TextView tvSupplier = dialogView.findViewById(R.id.tv_detail_supplier);
        TextView tvInvoice = dialogView.findViewById(R.id.tv_detail_invoice);
        TextView tvPurchaseDate = dialogView.findViewById(R.id.tv_detail_purchase_date);

        // Calcular valores
        double subtotal = item.getQuantity() * item.getUnitPrice();
        double discountAmount = subtotal * (item.getDiscountPercentage() / 100);
        double finalAmount = subtotal - discountAmount;

        // Llenar datos
        tvMaterial.setText(item.getMaterialName());
        tvQuantity.setText(item.getQuantity() + " " + item.getUnit());
        tvUnitPrice.setText(currencyFormat.format(item.getUnitPrice()));
        tvSubtotal.setText(currencyFormat.format(subtotal));
        tvDiscount.setText(item.getDiscountPercentage() + "% (" + currencyFormat.format(discountAmount) + ")");
        tvFinalAmount.setText(currencyFormat.format(finalAmount));
        tvSupplier.setText(item.getSupplierName() != null ? item.getSupplierName() : "No especificado");
        tvInvoice.setText(item.getInvoiceNumber() != null ? item.getInvoiceNumber() : "No especificado");
        tvPurchaseDate.setText(item.getDisplayDate());

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Detalles del Gasto")
                .setView(dialogView)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    /**
     * Obtener ID del proyecto actual - NUEVO
     */
    private long getCurrentProjectId() {
        if (getParentFragment() instanceof PresupuestosFragment) {
            return ((PresupuestosFragment) getParentFragment()).getProjectId();
        }
        return -1;
    }

    /**
     * Convertir fecha para el backend - NUEVO
     */
    private String formatDateForBackend(Date date) {
        if (date == null) return null;
        return backendDateFormatter.format(date);
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

        // Constructor vacío
        public ExpenseItem() {}

        // Constructor completo (compatible con adapter)
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

        // Métodos calculados (requeridos por el adapter)
        public double getTotalPrice() {
            double subtotal = quantity * unitPrice;
            double discount = subtotal * (discountPercentage / 100);
            return subtotal - discount;
        }

        public double getDiscountAmount() {
            double subtotal = quantity * unitPrice;
            return subtotal * (discountPercentage / 100);
        }

        // Getters y Setters
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }

        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }

        public String getMaterialCode() { return materialCode; }
        public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

        public double getDiscountPercentage() { return discountPercentage; }
        public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }

        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

        public Date getPurchaseDate() { return purchaseDate; }
        public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }

        public String getDisplayDate() { return displayDate; }
        public void setDisplayDate(String displayDate) { this.displayDate = displayDate; }

        public String getDisplayTime() { return displayTime; }
        public void setDisplayTime(String displayTime) { this.displayTime = displayTime; }

        @Override
        public String toString() {
            return "ExpenseItem{" +
                    "id=" + id +
                    ", materialName='" + materialName + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    ", discountPercentage=" + discountPercentage +
                    ", displayDate='" + displayDate + '\'' +
                    ", displayTime='" + displayTime + '\'' +
                    '}';
        }
    }
}