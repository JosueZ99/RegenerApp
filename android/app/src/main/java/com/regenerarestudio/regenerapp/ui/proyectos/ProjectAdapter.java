package com.regenerarestudio.regenerapp.ui.proyectos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.data.models.Project;

import java.util.List;

/**
 * Adapter para la lista de Proyectos en la pantalla de selección
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/proyectos/ProjectAdapter.java
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projects;
    private OnProjectClickListener onProjectClickListener;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public ProjectAdapter(List<Project> projects, OnProjectClickListener onProjectClickListener) {
        this.projects = projects;
        this.onProjectClickListener = onProjectClickListener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_selection, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public void updateProjects(List<Project> newProjects) {
        this.projects = newProjects;
        notifyDataSetChanged();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private Chip chipStatus;
        private TextView tvProjectName;
        private TextView tvClient;
        private TextView tvLocation;
        private TextView tvDescription;
        private TextView tvStartDate;
        private TextView tvEndDate;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);

            chipStatus = itemView.findViewById(R.id.chip_status);
            tvProjectName = itemView.findViewById(R.id.tv_project_name);
            tvClient = itemView.findViewById(R.id.tv_client);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvStartDate = itemView.findViewById(R.id.tv_start_date);
            tvEndDate = itemView.findViewById(R.id.tv_end_date);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (onProjectClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onProjectClickListener.onProjectClick(projects.get(position));
                    }
                }
            });
        }

        public void bind(Project project) {
            // Información básica
            tvProjectName.setText(project.getName());
            tvClient.setText(project.getClient());
            tvLocation.setText(project.getLocation());
            tvDescription.setText(project.getDescription());

            // Fechas formateadas
            tvStartDate.setText(project.getFormattedStartDate());
            tvEndDate.setText(project.getFormattedEndDate());

            // Estado del proyecto
            configureStatusChip(project);
        }

        private void configureStatusChip(Project project) {
            // ✅ USAR getCurrentPhase() en lugar de getStatus()
            String phase = project.getCurrentPhase();
            String displayPhase = project.getDisplayPhase();

            if (phase == null || phase.isEmpty()) {
                chipStatus.setText("📋 Sin Definir");
                chipStatus.setChipBackgroundColorResource(R.color.gray_500);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));
                return;
            }

            // Configurar texto y color según la FASE (no el status)
            switch (phase.toLowerCase()) {
                case "design":
                    chipStatus.setText("🎨 " + displayPhase);  // "🎨 Diseño"
                    chipStatus.setChipBackgroundColorResource(R.color.status_design);
                    break;

                case "purchase":
                    chipStatus.setText("🛒 " + displayPhase);  // "🛒 Compra"
                    chipStatus.setChipBackgroundColorResource(R.color.status_purchase);
                    break;

                case "installation":
                    chipStatus.setText("🔧 " + displayPhase);  // "🔧 Instalación"
                    chipStatus.setChipBackgroundColorResource(R.color.status_installation);
                    break;

                case "completed":
                    chipStatus.setText("✅ " + displayPhase);  // "✅ Completado"
                    chipStatus.setChipBackgroundColorResource(R.color.status_completed);
                    break;

                default:
                    chipStatus.setText("📋 " + displayPhase);
                    chipStatus.setChipBackgroundColorResource(R.color.gray_500);
                    break;
            }

            // Color del texto siempre blanco para mejor legibilidad
            chipStatus.setTextColor(itemView.getContext().getColor(R.color.white));
        }
    }
}