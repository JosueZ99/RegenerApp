package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.regenerarestudio.regenerapp.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para la tabla de Presupuesto Inicial
 * Maneja la visualización de items en el RecyclerView
 */
public class BudgetInitialAdapter extends RecyclerView.Adapter<BudgetInitialAdapter.BudgetViewHolder> {

    private List<BudgetInitialTableFragment.BudgetItem> budgetItems;
    private final NumberFormat currencyFormat;

    public BudgetInitialAdapter(List<BudgetInitialTableFragment.BudgetItem> budgetItems) {
        this.budgetItems = budgetItems;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget_initial, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetInitialTableFragment.BudgetItem item = budgetItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return budgetItems.size();
    }

    /**
     * Actualizar la lista de items
     */
    public void updateItems(List<BudgetInitialTableFragment.BudgetItem> newItems) {
        this.budgetItems = newItems;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder para items del presupuesto
     */
    public class BudgetViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDescription;
        private TextView tvCategory;
        private TextView tvSpaces;
        private TextView tvQuantity;
        private TextView tvUnit;
        private TextView tvUnitPrice;
        private TextView tvSupplier;
        private TextView tvTotalPrice;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inicializar vistas con IDs correctos del layout item_budget_initial.xml
            tvDescription = itemView.findViewById(R.id.tv_material_name);
            tvCategory = itemView.findViewById(R.id.tv_material_category);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvUnit = itemView.findViewById(R.id.tv_unit);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvSupplier = itemView.findViewById(R.id.tv_supplier_name);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);

            // tvSpaces no existe en este layout específico
            tvSpaces = null;
        }

        public void bind(BudgetInitialTableFragment.BudgetItem item) {
            // Descripción
            if (tvDescription != null) {
                tvDescription.setText(item.getDescription() != null ? item.getDescription() : "Sin descripción");
            }

            // Categoría
            if (tvCategory != null) {
                String categoryText = item.getCategoryDisplay() != null ?
                        item.getCategoryDisplay() :
                        (item.getCategory() != null ? item.getCategory() : "Sin categoría");
                tvCategory.setText(categoryText);
            }

            // Espacios (este campo no existe en el layout actual)
            if (tvSpaces != null) {
                tvSpaces.setText(item.getSpaces() != null && !item.getSpaces().isEmpty() ?
                        item.getSpaces() : "-");
            }

            // Cantidad
            if (tvQuantity != null) {
                double quantity = item.getQuantity();
                // Mostrar sin decimales si es número entero
                if (quantity == Math.floor(quantity)) {
                    tvQuantity.setText(String.valueOf((int) quantity));
                } else {
                    tvQuantity.setText(String.format(Locale.getDefault(), "%.2f", quantity));
                }
            }

            // Unidad
            if (tvUnit != null) {
                tvUnit.setText(item.getUnit() != null ? item.getUnit() : "ud");
            }

            // Precio unitario
            if (tvUnitPrice != null) {
                tvUnitPrice.setText(currencyFormat.format(item.getUnitPrice()));
            }

            // Proveedor
            if (tvSupplier != null) {
                tvSupplier.setText(item.getSupplierName() != null ?
                        item.getSupplierName() : "Sin proveedor");
            }

            // Precio total
            if (tvTotalPrice != null) {
                tvTotalPrice.setText(currencyFormat.format(item.getTotalPrice()));
            }

            // Click listener para item completo (opcional)
            itemView.setOnClickListener(v -> {
                // TODO: Implementar acción al hacer click en item
                // Por ejemplo, mostrar diálogo de edición
            });
        }
    }
}