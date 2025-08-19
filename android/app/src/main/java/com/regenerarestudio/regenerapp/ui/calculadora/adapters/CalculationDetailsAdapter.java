package com.regenerarestudio.regenerapp.ui.calculadora.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.regenerarestudio.regenerapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para mostrar detalles específicos del cálculo
 * Se usa en el RecyclerView dentro de cardDetails
 */
public class CalculationDetailsAdapter extends RecyclerView.Adapter<CalculationDetailsAdapter.DetailViewHolder> {

    private List<DetailItem> details;

    public CalculationDetailsAdapter() {
        this.details = new ArrayList<>();
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calculation_detail, parent, false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        DetailItem detail = details.get(position);
        holder.bind(detail);
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    public void updateDetails(List<DetailItem> newDetails) {
        this.details.clear();
        this.details.addAll(newDetails);
        notifyDataSetChanged();
    }

    public void clearDetails() {
        this.details.clear();
        notifyDataSetChanged();
    }

    // ViewHolder
    static class DetailViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLabel;
        private final TextView tvValue;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tv_detail_label);
            tvValue = itemView.findViewById(R.id.tv_detail_value);
        }

        public void bind(DetailItem detail) {
            tvLabel.setText(detail.getLabel());
            tvValue.setText(detail.getValue());
        }
    }

    // Clase para items de detalle
    public static class DetailItem {
        private final String label;
        private final String value;
        private final String type;

        public DetailItem(String label, String value) {
            this.label = label;
            this.value = value;
            this.type = "text";
        }

        public DetailItem(String label, String value, String type) {
            this.label = label;
            this.value = value;
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        // Factory methods para diferentes tipos de datos
        public static DetailItem createText(String label, String value) {
            return new DetailItem(label, value, "text");
        }

        public static DetailItem createNumber(String label, double value) {
            return new DetailItem(label, String.format(Locale.getDefault(), "%.2f", value), "number");
        }

        public static DetailItem createInteger(String label, int value) {
            return new DetailItem(label, String.valueOf(value), "integer");
        }

        public static DetailItem createCurrency(String label, double value) {
            return new DetailItem(label, String.format(Locale.getDefault(), "$%.2f", value), "currency");
        }

        public static DetailItem createPercentage(String label, double value) {
            return new DetailItem(label, String.format(Locale.getDefault(), "%.1f%%", value * 100), "percentage");
        }

        public static DetailItem createArea(String label, double value) {
            return new DetailItem(label, String.format(Locale.getDefault(), "%.2f m²", value), "area");
        }

        public static DetailItem createLength(String label, double value) {
            return new DetailItem(label, String.format(Locale.getDefault(), "%.2f m", value), "length");
        }

        public static DetailItem createVolume(String label, double value) {
            return new DetailItem(label, String.format(Locale.getDefault(), "%.2f L", value), "volume");
        }
    }
}