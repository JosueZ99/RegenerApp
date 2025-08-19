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

import com.regenerarestudio.regenerapp.databinding.FragmentTableExpensesRealBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment para la tabla de Gastos Reales
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/presupuestos/ExpensesRealTableFragment.java
 */
public class ExpensesRealTableFragment extends Fragment {

    private FragmentTableExpensesRealBinding binding;
    private ExpensesRealAdapter adapter;
    private List<ExpenseItem> expenseItems;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTableExpensesRealBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchAndFilter();
        loadExpensesData();
    }

    private void setupRecyclerView() {
        expenseItems = new ArrayList<>();
        adapter = new ExpensesRealAdapter(expenseItems, this::onItemClick, this::onItemMenuClick);

        binding.rvExpensesReal.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvExpensesReal.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        // Configurar búsqueda
        binding.etSearchExpenses.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // TODO: Implementar lógica de búsqueda en tiempo real
            }
        });

        // Configurar filtros
        binding.btnFilterExpenses.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Filtros de gastos - Próximamente", Toast.LENGTH_SHORT).show();
        });

        binding.btnDateFilter.setOnClickListener(v -> {
            // TODO: Mostrar selector de rango de fechas
            Toast.makeText(requireContext(), "Filtro por fechas - Próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadExpensesData() {
        // Datos simulados - TODO: Obtener desde API
        expenseItems.clear();
        expenseItems.addAll(createSampleExpenseItems());

        adapter.notifyDataSetChanged();
        updateTotals();
    }

    private List<ExpenseItem> createSampleExpenseItems() {
        List<ExpenseItem> items = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        items.add(new ExpenseItem(
                1, "Pintura Látex Interior", "PIN-001", 20.0, "Litros", 14.80, 5.0,
                "Pinturas Cóndor", "#001-001-0001234", new Date(),
                dateFormat.format(new Date()), timeFormat.format(new Date())
        ));

        items.add(new ExpenseItem(
                2, "Panel Gypsum 12mm", "GYP-012", 40.0, "Paneles", 12.50, 2.0,
                "Gyplac Ecuador", "#001-002-0005678", new Date(),
                "24/01", "09:15"
        ));

        items.add(new ExpenseItem(
                3, "Cinta LED 5050", "LED-5050", 15.0, "Metros", 8.00, 6.0,
                "LED Solutions", "#002-001-0002341", new Date(),
                "23/01", "16:30"
        ));

        items.add(new ExpenseItem(
                4, "Perfil Aluminio", "PRF-EMP", 15.0, "Metros", 11.80, 4.0,
                "Perfiles Andinos", "#003-001-0007894", new Date(),
                "22/01", "11:45"
        ));

        items.add(new ExpenseItem(
                5, "Cable THHN 12 AWG", "CAB-12", 50.0, "Metros", 1.75, 5.4,
                "Cablesec", "#001-003-0003456", new Date(),
                "21/01", "14:20"
        ));

        return items;
    }

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

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));
        binding.tvSubtotalExpenses.setText(currencyFormat.format(subtotal));
        binding.tvTotalDiscounts.setText("-" + currencyFormat.format(totalDiscounts));
        binding.tvTotalExpensesReal.setText(currencyFormat.format(total));
        binding.tvTotalItemsExpenses.setText(expenseItems.size() + " compras");
    }

    private void onItemClick(ExpenseItem item) {
        // TODO: Mostrar detalles del gasto o permitir edición
        Toast.makeText(requireContext(), "Ver detalles: " + item.getMaterialName(), Toast.LENGTH_SHORT).show();
    }

    private void onItemMenuClick(ExpenseItem item) {
        // TODO: Mostrar menú contextual (ver factura, editar, eliminar, etc.)
        Toast.makeText(requireContext(), "Menú para: " + item.getMaterialName(), Toast.LENGTH_SHORT).show();
    }

    public void refreshData() {
        loadExpensesData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Clase modelo para items de gastos reales
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
            return (quantity * unitPrice) * (discountPercentage / 100);
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
    }
}