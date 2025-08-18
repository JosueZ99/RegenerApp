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
 * Adapter para la tabla de Presupuesto Inicial
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/presupuestos/BudgetInitialAdapter.java
 */
public class BudgetInitialAdapter extends RecyclerView.Adapter<BudgetInitialAdapter.BudgetViewHolder> {

    private List<BudgetInitialTableFragment.BudgetItem> budgetItems;
    private OnItemClickListener onItemClickListener;
    private OnItemMenuClickListener onItemMenuClickListener;
    private NumberFormat currencyFormat;

    public interface OnItemClickListener {
        void onItemClick(BudgetInitialTableFragment.BudgetItem item);
    }

    public interface OnItemMenuClickListener {
        void onItemMenuClick(BudgetInitialTableFragment.BudgetItem item);
    }

    public BudgetInitialAdapter(List<BudgetInitialTableFragment.BudgetItem> budgetItems,
                                OnItemClickListener onItemClickListener,
                                OnItemMenuClickListener onItemMenuClickListener) {
        this.budgetItems = budgetItems;
        this.onItemClickListener = onItemClickListener;
        this.onItemMenuClickListener = onItemMenuClickListener;
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

    public void updateItems(List<BudgetInitialTableFragment.BudgetItem> newItems) {
        this.budgetItems = newItems;
        notifyDataSetChanged();
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMaterialName;
        private TextView tvMaterialCategory;
        private TextView tvQuantity;
        private TextView tvUnit;
        private TextView tvUnitPrice;
        private TextView tvSupplierName;
        private TextView tvSupplierRating;
        private TextView tvTotalPrice;
        private ImageView btnMenuItemClick;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMaterialName = itemView.findViewById(R.id.tv_material_name);
            tvMaterialCategory = itemView.findViewById(R.id.tv_material_category);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvUnit = itemView.findViewById(R.id.tv_unit);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvSupplierName = itemView.findViewById(R.id.tv_supplier_name);
            tvSupplierRating = itemView.findViewById(R.id.tv_supplier_rating);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            btnMenuItemClick = itemView.findViewById(R.id.btn_menu_item);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(budgetItems.get(position));
                    }
                }
            });

            btnMenuItemClick.setOnClickListener(v -> {
                if (onItemMenuClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemMenuClickListener.onItemMenuClick(budgetItems.get(position));
                    }
                }
            });
        }

        public void bind(BudgetInitialTableFragment.BudgetItem item) {
            tvMaterialName.setText(item.getMaterialName());
            tvMaterialCategory.setText(item.getCategory());

            // Configurar cantidad y unidad
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvUnit.setText(item.getUnit());

            // Configurar precios
            tvUnitPrice.setText(currencyFormat.format(item.getUnitPrice()));
            tvTotalPrice.setText(currencyFormat.format(item.getTotalPrice()));

            // Configurar proveedor
            tvSupplierName.setText(item.getSupplierName());
            tvSupplierRating.setText(String.valueOf(item.getSupplierRating()));

            // Configurar colores según la categoría
            setCategoryColor(item.getCategory());
        }

        private void setCategoryColor(String category) {
            int colorRes;
            switch (category.toLowerCase()) {
                case "construcción":
                    colorRes = R.color.status_design;
                    break;
                case "iluminación":
                    colorRes = R.color.warning;
                    break;
                case "eléctrico":
                    colorRes = R.color.success;
                    break;
                default:
                    colorRes = R.color.gray_500;
                    break;
            }

            tvMaterialCategory.setBackgroundTintList(
                    itemView.getContext().getColorStateList(colorRes)
            );
        }
    }
}