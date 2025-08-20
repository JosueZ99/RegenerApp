package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.regenerarestudio.regenerapp.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para la tabla de Presupuesto Inicial
 * ACTUALIZADO: Incluye menú contextual y callbacks
 */
public class BudgetInitialAdapter extends RecyclerView.Adapter<BudgetInitialAdapter.BudgetViewHolder> {

    private static final String TAG = "BudgetInitialAdapter";
    private List<BudgetInitialTableFragment.BudgetItem> budgetItems;
    private final NumberFormat currencyFormat;

    // Interfaces de callback
    private OnItemClickListener onItemClickListener;
    private OnItemMenuClickListener onItemMenuClickListener;

    /**
     * Interface para clicks en items
     */
    public interface OnItemClickListener {
        void onItemClick(BudgetInitialTableFragment.BudgetItem item, int position);
    }

    /**
     * Interface para opciones del menú contextual
     */
    public interface OnItemMenuClickListener {
        void onEditItem(BudgetInitialTableFragment.BudgetItem item, int position);
        void onDeleteItem(BudgetInitialTableFragment.BudgetItem item, int position);
        void onCopyItemToExpenses(BudgetInitialTableFragment.BudgetItem item, int position);
    }

    public BudgetInitialAdapter(List<BudgetInitialTableFragment.BudgetItem> budgetItems,
                                OnItemClickListener onItemClickListener,
                                OnItemMenuClickListener onItemMenuClickListener) {
        this.budgetItems = budgetItems != null ? budgetItems : new ArrayList<>();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));
        this.onItemClickListener = onItemClickListener;
        this.onItemMenuClickListener = onItemMenuClickListener;

        Log.d(TAG, "BudgetInitialAdapter creado con " + this.budgetItems.size() + " items");
    }

    // Constructor de compatibilidad para código existente
    public BudgetInitialAdapter(List<BudgetInitialTableFragment.BudgetItem> budgetItems) {
        this(budgetItems, null, null);
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Creando ViewHolder");

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget_initial, parent, false);

        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        if (budgetItems == null || position >= budgetItems.size()) {
            Log.e(TAG, "onBindViewHolder: Posición inválida " + position + " / " +
                    (budgetItems != null ? budgetItems.size() : "null"));
            return;
        }

        BudgetInitialTableFragment.BudgetItem item = budgetItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        int count = budgetItems != null ? budgetItems.size() : 0;
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    /**
     * Actualizar lista de items
     */
    public void updateItems(List<BudgetInitialTableFragment.BudgetItem> newItems) {
        Log.d(TAG, "updateItems: Actualizando desde " +
                (budgetItems != null ? budgetItems.size() : 0) +
                " a " + (newItems != null ? newItems.size() : 0) + " items");

        this.budgetItems = newItems != null ? newItems : new ArrayList<>();

        notifyDataSetChanged();
        Log.d(TAG, "updateItems: notifyDataSetChanged() llamado");
    }

    /**
     * ViewHolder con menú contextual implementado
     */
    public class BudgetViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDescription;
        private TextView tvCategory;
        private TextView tvQuantity;
        private TextView tvUnit;
        private TextView tvUnitPrice;
        private TextView tvSupplier;
        private TextView tvTotalPrice;
        private ImageView btnMenuMore;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);

            Log.d(TAG, "BudgetViewHolder: Inicializando ViewHolder");

            // Inicializar vistas
            tvDescription = itemView.findViewById(R.id.tv_material_name);
            tvCategory = itemView.findViewById(R.id.tv_material_category);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvUnit = itemView.findViewById(R.id.tv_unit);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvSupplier = itemView.findViewById(R.id.tv_supplier_name);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            btnMenuMore = itemView.findViewById(R.id.btn_menu_item);

            // Verificar que todas las vistas se encontraron
            if (tvDescription == null) Log.e(TAG, "ERROR: tv_material_name no encontrado");
            if (tvCategory == null) Log.e(TAG, "ERROR: tv_material_category no encontrado");
            if (tvQuantity == null) Log.e(TAG, "ERROR: tv_quantity no encontrado");
            if (tvUnit == null) Log.e(TAG, "ERROR: tv_unit no encontrado");
            if (tvUnitPrice == null) Log.e(TAG, "ERROR: tv_unit_price no encontrado");
            if (tvSupplier == null) Log.e(TAG, "ERROR: tv_supplier_name no encontrado");
            if (tvTotalPrice == null) Log.e(TAG, "ERROR: tv_total_price no encontrado");
            if (btnMenuMore == null) Log.e(TAG, "ERROR: btn_menu_item no encontrado");

            // Configurar click del item completo
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(budgetItems.get(position), position);
                }
            });

            // Configurar click del menú "more vertical"
            if (btnMenuMore != null) {
                btnMenuMore.setOnClickListener(v -> showContextMenu(v, getAdapterPosition()));
            }
        }

        public void bind(BudgetInitialTableFragment.BudgetItem item, int position) {
            if (item == null) {
                Log.e(TAG, "bind: Item es null en posición " + position);
                return;
            }

            Log.d(TAG, "bind: Vinculando item en posición " + position + " - " + item.getDescription());

            // Establecer valores con verificaciones de null
            if (tvDescription != null) {
                tvDescription.setText(item.getDescription() != null ? item.getDescription() : "Sin descripción");
            }

            if (tvCategory != null) {
                tvCategory.setText(item.getCategory() != null ? item.getCategory() : "Sin categoría");
            }

            if (tvQuantity != null) {
                String quantityText = String.valueOf(item.getQuantity());
                tvQuantity.setText(quantityText);
            }

            if (tvUnit != null) {
                tvUnit.setText(item.getUnit() != null ? item.getUnit() : "unidad");
            }

            if (tvUnitPrice != null) {
                String priceText = currencyFormat.format(item.getUnitPrice());
                tvUnitPrice.setText(priceText);
            }

            if (tvSupplier != null) {
                tvSupplier.setText(item.getSupplierName() != null ? item.getSupplierName() : "Sin proveedor");
            }

            if (tvTotalPrice != null) {
                double totalPrice = item.getQuantity() * item.getUnitPrice();
                String totalText = currencyFormat.format(totalPrice);
                tvTotalPrice.setText(totalText);
            }

            // El menú more ya está configurado en el constructor
        }

        /**
         * Mostrar menú contextual para un item
         */
        private void showContextMenu(View anchorView, int position) {
            if (position == RecyclerView.NO_POSITION || onItemMenuClickListener == null) {
                Log.w(TAG, "showContextMenu: Posición inválida o listener nulo");
                return;
            }

            BudgetInitialTableFragment.BudgetItem item = budgetItems.get(position);

            // Crear PopupMenu
            PopupMenu popupMenu = new PopupMenu(anchorView.getContext(), anchorView);
            popupMenu.getMenuInflater().inflate(R.menu.menu_budget_item_context, popupMenu.getMenu());

            // Configurar listeners
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.action_edit_item) {
                    Log.d(TAG, "Menú: Editar item - " + item.getDescription());
                    onItemMenuClickListener.onEditItem(item, position);
                    return true;

                } else if (itemId == R.id.action_delete_item) {
                    Log.d(TAG, "Menú: Eliminar item - " + item.getDescription());
                    onItemMenuClickListener.onDeleteItem(item, position);
                    return true;

                } else if (itemId == R.id.action_copy_to_expenses) {
                    Log.d(TAG, "Menú: Copiar a gastos - " + item.getDescription());
                    onItemMenuClickListener.onCopyItemToExpenses(item, position);
                    return true;
                }

                return false;
            });

            // Mostrar menú
            popupMenu.show();
        }
    }

    /**
     * Métodos para actualizar callbacks externamente
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemMenuClickListener(OnItemMenuClickListener listener) {
        this.onItemMenuClickListener = listener;
    }
}