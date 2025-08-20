package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.regenerarestudio.regenerapp.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para la tabla de Presupuesto Inicial
 * Maneja la visualización de items en el RecyclerView
 */
public class BudgetInitialAdapter extends RecyclerView.Adapter<BudgetInitialAdapter.BudgetViewHolder> {

    private static final String TAG = "BudgetInitialAdapter";
    private List<BudgetInitialTableFragment.BudgetItem> budgetItems;
    private final NumberFormat currencyFormat;

    public BudgetInitialAdapter(List<BudgetInitialTableFragment.BudgetItem> budgetItems) {
        this.budgetItems = budgetItems != null ? budgetItems : new ArrayList<>();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));
        Log.d(TAG, "Constructor: Adaptador creado con " + this.budgetItems.size() + " items");
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Creando ViewHolder para posición");

        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_budget_initial, parent, false);

            if (view == null) {
                Log.e(TAG, "onCreateViewHolder: Vista es null");
                throw new RuntimeException("No se pudo inflar item_budget_initial");
            }

            Log.d(TAG, "onCreateViewHolder: Vista creada correctamente");
            return new BudgetViewHolder(view);

        } catch (Exception e) {
            Log.e(TAG, "onCreateViewHolder: Error creando ViewHolder", e);
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Binding item en posición " + position +
                " de " + budgetItems.size() + " items totales");

        if (position < 0 || position >= budgetItems.size()) {
            Log.e(TAG, "onBindViewHolder: Posición inválida: " + position);
            return;
        }

        BudgetInitialTableFragment.BudgetItem item = budgetItems.get(position);
        if (item == null) {
            Log.e(TAG, "onBindViewHolder: Item es null en posición " + position);
            return;
        }

        Log.d(TAG, "onBindViewHolder: Bindeando item: " + item.getDescription());
        holder.bind(item);
        Log.d(TAG, "onBindViewHolder: Item bindeado exitosamente");
    }

    @Override
    public int getItemCount() {
        int count = budgetItems.size();
        Log.d(TAG, "getItemCount: Retornando " + count + " items");
        return count;
    }

    /**
     * Actualizar la lista de items - VERSIÓN CON DEBUGGING
     */
    public void updateItems(List<BudgetInitialTableFragment.BudgetItem> newItems) {
        Log.d(TAG, "updateItems: Iniciando actualización");
        Log.d(TAG, "updateItems: Items anteriores: " +
                (this.budgetItems != null ? this.budgetItems.size() : 0));
        Log.d(TAG, "updateItems: Items nuevos: " +
                (newItems != null ? newItems.size() : 0));

        if (newItems == null) {
            this.budgetItems = new ArrayList<>();
            Log.d(TAG, "updateItems: newItems era null, creando lista vacía");
        } else {
            this.budgetItems = new ArrayList<>(newItems);
            Log.d(TAG, "updateItems: Lista actualizada con " + this.budgetItems.size() + " items");

            // Log de cada item para debugging
            for (int i = 0; i < this.budgetItems.size(); i++) {
                BudgetInitialTableFragment.BudgetItem item = this.budgetItems.get(i);
                Log.v(TAG, "updateItems: Item " + i + ": " +
                        (item != null ? item.getDescription() : "null"));
            }
        }

        // Notificar cambios en el hilo principal
        notifyDataSetChanged();
        Log.d(TAG, "updateItems: notifyDataSetChanged() llamado");

        // Verificar que se llamó correctamente
        Log.d(TAG, "updateItems: Verificación final - getItemCount(): " + getItemCount());
    }

    /**
     * ViewHolder mejorado con debugging extensivo
     */
    public class BudgetViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDescription;
        private TextView tvCategory;
        private TextView tvQuantity;
        private TextView tvUnit;
        private TextView tvUnitPrice;
        private TextView tvSupplier;
        private TextView tvTotalPrice;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);

            Log.d(TAG, "BudgetViewHolder: Inicializando ViewHolder");

            // Inicializar vistas con verificación detallada
            tvDescription = itemView.findViewById(R.id.tv_material_name);
            tvCategory = itemView.findViewById(R.id.tv_material_category);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvUnit = itemView.findViewById(R.id.tv_unit);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvSupplier = itemView.findViewById(R.id.tv_supplier_name);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);

            // Verificar que todas las vistas se encontraron
            Log.d(TAG, "ViewHolder - tv_material_name: " + (tvDescription != null));
            Log.d(TAG, "ViewHolder - tv_material_category: " + (tvCategory != null));
            Log.d(TAG, "ViewHolder - tv_quantity: " + (tvQuantity != null));
            Log.d(TAG, "ViewHolder - tv_unit: " + (tvUnit != null));
            Log.d(TAG, "ViewHolder - tv_unit_price: " + (tvUnitPrice != null));
            Log.d(TAG, "ViewHolder - tv_supplier_name: " + (tvSupplier != null));
            Log.d(TAG, "ViewHolder - tv_total_price: " + (tvTotalPrice != null));

            if (tvDescription == null) Log.e(TAG, "ERROR: tv_material_name no encontrado en layout");
            if (tvCategory == null) Log.e(TAG, "ERROR: tv_material_category no encontrado en layout");
            if (tvQuantity == null) Log.e(TAG, "ERROR: tv_quantity no encontrado en layout");
            if (tvUnit == null) Log.e(TAG, "ERROR: tv_unit no encontrado en layout");
            if (tvUnitPrice == null) Log.e(TAG, "ERROR: tv_unit_price no encontrado en layout");
            if (tvSupplier == null) Log.e(TAG, "ERROR: tv_supplier_name no encontrado en layout");
            if (tvTotalPrice == null) Log.e(TAG, "ERROR: tv_total_price no encontrado en layout");
        }

        public void bind(BudgetInitialTableFragment.BudgetItem item) {
            Log.d(TAG, "bind: Iniciando bind para item: " +
                    (item != null ? item.getDescription() : "null"));

            if (item == null) {
                Log.e(TAG, "bind: Item es null");
                return;
            }

            try {
                // Descripción
                if (tvDescription != null) {
                    String description = item.getDescription() != null ? item.getDescription() : "Sin descripción";
                    tvDescription.setText(description);
                    Log.d(TAG, "bind: Descripción asignada: " + description);
                } else {
                    Log.e(TAG, "bind: tvDescription es null");
                }

                // Categoría
                if (tvCategory != null) {
                    String categoryText = item.getCategoryDisplay() != null ?
                            item.getCategoryDisplay() :
                            (item.getCategory() != null ? item.getCategory() : "Sin categoría");
                    tvCategory.setText(categoryText);
                    Log.d(TAG, "bind: Categoría asignada: " + categoryText);
                } else {
                    Log.e(TAG, "bind: tvCategory es null");
                }

                // Cantidad
                if (tvQuantity != null) {
                    double quantity = item.getQuantity();
                    String quantityText;
                    if (quantity == Math.floor(quantity)) {
                        quantityText = String.valueOf((int) quantity);
                    } else {
                        quantityText = String.format(Locale.getDefault(), "%.2f", quantity);
                    }
                    tvQuantity.setText(quantityText);
                    Log.d(TAG, "bind: Cantidad asignada: " + quantityText);
                } else {
                    Log.e(TAG, "bind: tvQuantity es null");
                }

                // Unidad
                if (tvUnit != null) {
                    String unit = item.getUnit() != null ? item.getUnit() : "ud";
                    tvUnit.setText(unit);
                    Log.d(TAG, "bind: Unidad asignada: " + unit);
                } else {
                    Log.e(TAG, "bind: tvUnit es null");
                }

                // Precio unitario
                if (tvUnitPrice != null) {
                    String unitPrice = currencyFormat.format(item.getUnitPrice());
                    tvUnitPrice.setText(unitPrice);
                    Log.d(TAG, "bind: Precio unitario asignado: " + unitPrice);
                } else {
                    Log.e(TAG, "bind: tvUnitPrice es null");
                }

                // Proveedor
                if (tvSupplier != null) {
                    String supplierName = item.getSupplierName() != null ?
                            item.getSupplierName() : "Sin proveedor";
                    tvSupplier.setText(supplierName);
                    Log.d(TAG, "bind: Proveedor asignado: " + supplierName);
                } else {
                    Log.e(TAG, "bind: tvSupplier es null");
                }

                // Precio total
                if (tvTotalPrice != null) {
                    String totalPrice = currencyFormat.format(item.getTotalPrice());
                    tvTotalPrice.setText(totalPrice);
                    Log.d(TAG, "bind: Precio total asignado: " + totalPrice);
                } else {
                    Log.e(TAG, "bind: tvTotalPrice es null");
                }

                Log.d(TAG, "bind: Completado exitosamente para: " + item.getDescription());

            } catch (Exception e) {
                Log.e(TAG, "bind: Error durante bind", e);
            }
        }
    }
}