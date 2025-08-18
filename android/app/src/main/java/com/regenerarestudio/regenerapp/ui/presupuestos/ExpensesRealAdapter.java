package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.regenerarestudio.regenerapp.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para la tabla de Gastos Reales
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/presupuestos/ExpensesRealAdapter.java
 */
public class ExpensesRealAdapter extends RecyclerView.Adapter<ExpensesRealAdapter.ExpenseViewHolder> {

    private List<ExpensesRealTableFragment.ExpenseItem> expenseItems;
    private OnItemClickListener onItemClickListener;
    private OnItemMenuClickListener onItemMenuClickListener;
    private NumberFormat currencyFormat;

    public interface OnItemClickListener {
        void onItemClick(ExpensesRealTableFragment.ExpenseItem item);
    }

    public interface OnItemMenuClickListener {
        void onItemMenuClick(ExpensesRealTableFragment.ExpenseItem item);
    }

    public ExpensesRealAdapter(List<ExpensesRealTableFragment.ExpenseItem> expenseItems,
                               OnItemClickListener onItemClickListener,
                               OnItemMenuClickListener onItemMenuClickListener) {
        this.expenseItems = expenseItems;
        this.onItemClickListener = onItemClickListener;
        this.onItemMenuClickListener = onItemMenuClickListener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_real, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpensesRealTableFragment.ExpenseItem item = expenseItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return expenseItems.size();
    }

    public void updateItems(List<ExpensesRealTableFragment.ExpenseItem> newItems) {
        this.expenseItems = newItems;
        notifyDataSetChanged();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMaterialNameExpense;
        private TextView tvMaterialCode;
        private TextView tvQuantityExpense;
        private TextView tvUnitExpense;
        private TextView tvUnitPriceExpense;
        private TextView tvDiscountPercentage;
        private TextView tvDiscountAmount;
        private TextView tvSupplierNameExpense;
        private TextView tvInvoiceNumber;
        private TextView tvPurchaseDate;
        private TextView tvPurchaseTime;
        private TextView tvTotalPriceExpense;
        private ImageView iconPriceComparison;
        private TextView tvSavingsAmount;
        private ImageView btnMenuExpenseItem;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMaterialNameExpense = itemView.findViewById(R.id.tv_material_name_expense);
            tvMaterialCode = itemView.findViewById(R.id.tv_material_code);
            tvQuantityExpense = itemView.findViewById(R.id.tv_quantity_expense);
            tvUnitExpense = itemView.findViewById(R.id.tv_unit_expense);
            tvUnitPriceExpense = itemView.findViewById(R.id.tv_unit_price_expense);
            tvDiscountPercentage = itemView.findViewById(R.id.tv_discount_percentage);
            tvDiscountAmount = itemView.findViewById(R.id.tv_discount_amount);
            tvSupplierNameExpense = itemView.findViewById(R.id.tv_supplier_name_expense);
            tvInvoiceNumber = itemView.findViewById(R.id.tv_invoice_number);
            tvPurchaseDate = itemView.findViewById(R.id.tv_purchase_date);
            tvPurchaseTime = itemView.findViewById(R.id.tv_purchase_time);
            tvTotalPriceExpense = itemView.findViewById(R.id.tv_total_price_expense);
            iconPriceComparison = itemView.findViewById(R.id.icon_price_comparison);
            tvSavingsAmount = itemView.findViewById(R.id.tv_savings_amount);
            btnMenuExpenseItem = itemView.findViewById(R.id.btn_menu_expense_item);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(expenseItems.get(position));
                    }
                }
            });

            btnMenuExpenseItem.setOnClickListener(v -> {
                if (onItemMenuClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemMenuClickListener.onItemMenuClick(expenseItems.get(position));
                    }
                }
            });
        }

        public void bind(ExpensesRealTableFragment.ExpenseItem item) {
            // Información básica
            tvMaterialNameExpense.setText(item.getMaterialName());
            tvMaterialCode.setText(item.getMaterialCode());

            // Cantidad y unidad
            tvQuantityExpense.setText(String.valueOf((int) item.getQuantity()));
            tvUnitExpense.setText(getShortUnit(item.getUnit()));

            // Precios
            tvUnitPriceExpense.setText(currencyFormat.format(item.getUnitPrice()));
            tvTotalPriceExpense.setText(currencyFormat.format(item.getTotalPrice()));

            // Descuentos
            tvDiscountPercentage.setText(String.format("%.0f%%", item.getDiscountPercentage()));
            tvDiscountAmount.setText(currencyFormat.format(item.getDiscountAmount()));

            // Proveedor
            tvSupplierNameExpense.setText(getShortSupplierName(item.getSupplierName()));
            tvInvoiceNumber.setText(item.getInvoiceNumber());

            // Fechas
            tvPurchaseDate.setText(item.getDisplayDate());
            tvPurchaseTime.setText(item.getDisplayTime());

            // Configurar indicador de ahorro/sobrecosto
            configureComparisonIndicator(item);
        }

        private String getShortUnit(String unit) {
            switch (unit.toLowerCase()) {
                case "litros":
                    return "L";
                case "metros":
                    return "m";
                case "paneles":
                    return "pcs";
                case "sacos":
                    return "scs";
                default:
                    return unit.length() > 4 ? unit.substring(0, 3) : unit;
            }
        }

        private String getShortSupplierName(String supplierName) {
            if (supplierName.length() > 10) {
                String[] words = supplierName.split(" ");
                return words[0];
            }
            return supplierName;
        }

        private void configureComparisonIndicator(ExpensesRealTableFragment.ExpenseItem item) {
            // Simulación de comparación con presupuesto inicial
            // TODO: En el futuro, obtener precio presupuestado real desde API
            double budgetedPrice = item.getUnitPrice() * 1.15; // Simulación: 15% más caro presupuestado
            double actualPrice = item.getUnitPrice();
            double savings = (budgetedPrice - actualPrice) * item.getQuantity();

            if (savings > 0) {
                // Ahorró dinero
                iconPriceComparison.setImageResource(R.drawable.ic_arrow_downward_24);
                iconPriceComparison.setImageTintList(
                        itemView.getContext().getColorStateList(R.color.success)
                );
                tvSavingsAmount.setText(currencyFormat.format(savings));
                tvSavingsAmount.setTextColor(
                        itemView.getContext().getColor(R.color.success)
                );
            } else if (savings < 0) {
                // Gastó más de lo presupuestado
                iconPriceComparison.setImageResource(R.drawable.ic_arrow_upward_24);
                iconPriceComparison.setImageTintList(
                        itemView.getContext().getColorStateList(R.color.error)
                );
                tvSavingsAmount.setText("+" + currencyFormat.format(Math.abs(savings)));
                tvSavingsAmount.setTextColor(
                        itemView.getContext().getColor(R.color.error)
                );
            } else {
                // Precio exacto
                iconPriceComparison.setImageResource(R.drawable.ic_remove_24);
                iconPriceComparison.setImageTintList(
                        itemView.getContext().getColorStateList(R.color.gray_500)
                );
                tvSavingsAmount.setText("$0.00");
                tvSavingsAmount.setTextColor(
                        itemView.getContext().getColor(R.color.gray_500)
                );
            }
        }
    }
}