package com.regenerarestudio.regenerapp.ui.proveedores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.regenerarestudio.regenerapp.R;

import java.util.List;

/**
 * Adapter para la lista de Proveedores
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/proveedores/SuppliersAdapter.java
 */
public class SuppliersAdapter extends RecyclerView.Adapter<SuppliersAdapter.SupplierViewHolder> {

    private List<ProveedoresFragment.Supplier> suppliers;
    private OnSupplierClickListener onSupplierClickListener;
    private OnCallClickListener onCallClickListener;
    private OnWhatsAppClickListener onWhatsAppClickListener;
    private OnViewCatalogClickListener onViewCatalogClickListener;

    public interface OnSupplierClickListener {
        void onSupplierClick(ProveedoresFragment.Supplier supplier);
    }

    public interface OnCallClickListener {
        void onCallClick(ProveedoresFragment.Supplier supplier);
    }

    public interface OnWhatsAppClickListener {
        void onWhatsAppClick(ProveedoresFragment.Supplier supplier);
    }

    public interface OnViewCatalogClickListener {
        void onViewCatalogClick(ProveedoresFragment.Supplier supplier);
    }

    public SuppliersAdapter(List<ProveedoresFragment.Supplier> suppliers,
                            OnSupplierClickListener onSupplierClickListener,
                            OnCallClickListener onCallClickListener,
                            OnWhatsAppClickListener onWhatsAppClickListener,
                            OnViewCatalogClickListener onViewCatalogClickListener) {
        this.suppliers = suppliers;
        this.onSupplierClickListener = onSupplierClickListener;
        this.onCallClickListener = onCallClickListener;
        this.onWhatsAppClickListener = onWhatsAppClickListener;
        this.onViewCatalogClickListener = onViewCatalogClickListener;
    }

    @NonNull
    @Override
    public SupplierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_supplier, parent, false);
        return new SupplierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupplierViewHolder holder, int position) {
        ProveedoresFragment.Supplier supplier = suppliers.get(position);
        holder.bind(supplier);
    }

    @Override
    public int getItemCount() {
        return suppliers.size();
    }

    public void updateSuppliers(List<ProveedoresFragment.Supplier> newSuppliers) {
        this.suppliers = newSuppliers;
        notifyDataSetChanged();
    }

    class SupplierViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivSupplierLogo;
        private View indicatorStatus;
        private TextView tvSupplierName;
        private TextView tvSpecialties;
        private TextView tvLocation;
        private TextView tvRating;
        private TextView tvReviewsCount;
        private TextView tvDeliveryTime;
        private TextView tvDiscountAverage;
        private TextView tvWarranty;
        private MaterialButton btnCallSupplier;
        private MaterialButton btnWhatsappSupplier;
        private MaterialButton btnViewCatalog;

        public SupplierViewHolder(@NonNull View itemView) {
            super(itemView);

            ivSupplierLogo = itemView.findViewById(R.id.iv_supplier_logo);
            indicatorStatus = itemView.findViewById(R.id.indicator_status);
            tvSupplierName = itemView.findViewById(R.id.tv_supplier_name);
            tvSpecialties = itemView.findViewById(R.id.tv_specialties);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvReviewsCount = itemView.findViewById(R.id.tv_reviews_count);
            tvDeliveryTime = itemView.findViewById(R.id.tv_delivery_time);
            tvDiscountAverage = itemView.findViewById(R.id.tv_discount_average);
            tvWarranty = itemView.findViewById(R.id.tv_warranty);
            btnCallSupplier = itemView.findViewById(R.id.btn_call_supplier);
            btnWhatsappSupplier = itemView.findViewById(R.id.btn_whatsapp_supplier);
            btnViewCatalog = itemView.findViewById(R.id.btn_view_catalog);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (onSupplierClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onSupplierClickListener.onSupplierClick(suppliers.get(position));
                    }
                }
            });

            btnCallSupplier.setOnClickListener(v -> {
                if (onCallClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onCallClickListener.onCallClick(suppliers.get(position));
                    }
                }
            });

            btnWhatsappSupplier.setOnClickListener(v -> {
                if (onWhatsAppClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onWhatsAppClickListener.onWhatsAppClick(suppliers.get(position));
                    }
                }
            });

            btnViewCatalog.setOnClickListener(v -> {
                if (onViewCatalogClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onViewCatalogClickListener.onViewCatalogClick(suppliers.get(position));
                    }
                }
            });
        }

        public void bind(ProveedoresFragment.Supplier supplier) {
            // Información básica
            tvSupplierName.setText(supplier.getName());
            tvSpecialties.setText(supplier.getSpecialties());
            tvLocation.setText(supplier.getLocation());

            // Rating y reseñas
            tvRating.setText(String.valueOf(supplier.getRating()));
            tvReviewsCount.setText(supplier.getReviewsCount() + " reseñas");

            // Información comercial
            tvDeliveryTime.setText(supplier.getDeliveryTime());
            tvDiscountAverage.setText(supplier.getDiscountRange());
            tvWarranty.setText(supplier.getWarranty());

            // Estado online/offline
            configureOnlineStatus(supplier.isOnline());

            // Logo según categoría
            configureSupplierLogo(supplier.getCategory());
        }

        private void configureOnlineStatus(boolean isOnline) {
            if (isOnline) {
                indicatorStatus.setBackgroundTintList(
                        itemView.getContext().getColorStateList(R.color.success)
                );
            } else {
                indicatorStatus.setBackgroundTintList(
                        itemView.getContext().getColorStateList(R.color.gray_400)
                );
            }
        }

        private void configureSupplierLogo(String category) {
            int iconRes;
            int backgroundColorRes;

            switch (category.toLowerCase()) {
                case "construccion":
                    iconRes = R.drawable.ic_build_24;
                    backgroundColorRes = R.color.status_design;
                    break;
                case "iluminacion":
                    iconRes = R.drawable.ic_lightbulb_24;
                    backgroundColorRes = R.color.warning;
                    break;
                case "electrico":
                    iconRes = R.drawable.ic_electrical_services_24;
                    backgroundColorRes = R.color.success;
                    break;
                case "acabados":
                    iconRes = R.drawable.ic_palette_24;
                    backgroundColorRes = R.color.status_installation;
                    break;
                default:
                    iconRes = R.drawable.ic_business_24;
                    backgroundColorRes = R.color.primary_color;
                    break;
            }

            ivSupplierLogo.setImageResource(iconRes);
            ivSupplierLogo.setBackgroundTintList(
                    itemView.getContext().getColorStateList(backgroundColorRes)
            );
        }
    }
}