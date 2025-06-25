package com.regenerarestudio.regenerapp.ui.proyectos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.model.Project;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private List<Project> projects;
    private OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public ProjectsAdapter(List<Project> projects, OnProjectClickListener listener) {
        this.projects = projects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
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
        private TextView tvProjectName;
        private TextView tvProjectClient;
        private TextView tvProjectLocation;
        private TextView tvProjectStatus;
        private View itemView;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvProjectName = itemView.findViewById(R.id.tv_project_name);
            tvProjectClient = itemView.findViewById(R.id.tv_project_client);
            tvProjectLocation = itemView.findViewById(R.id.tv_project_location);
            tvProjectStatus = itemView.findViewById(R.id.tv_project_status);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProjectClick(projects.get(position));
                }
            });
        }

        public void bind(Project project) {
            tvProjectName.setText(project.getName());
            tvProjectClient.setText("Cliente: " + project.getClient());
            tvProjectLocation.setText("📍 " + project.getLocation());
            tvProjectStatus.setText(project.getStatus());

            // Resaltar proyecto seleccionado
            if (project.isSelected()) {
                itemView.setBackgroundResource(R.color.teal_200);
                tvProjectStatus.setText("✓ SELECCIONADO");
            } else {
                itemView.setBackgroundResource(android.R.color.transparent);
            }
        }
    }
}