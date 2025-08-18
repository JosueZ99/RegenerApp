package com.regenerarestudio.regenerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.regenerarestudio.regenerapp.databinding.ActivityProjectSelectionBinding;
import com.regenerarestudio.regenerapp.ui.proyectos.Project;
import com.regenerarestudio.regenerapp.ui.proyectos.ProjectAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity de selección de proyectos - Pantalla obligatoria antes de acceder a la app principal
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ProjectSelectionActivity.java
 */
public class ProjectSelectionActivity extends AppCompatActivity {

    private ActivityProjectSelectionBinding binding;
    private ProjectAdapter projectAdapter;
    private List<Project> projectList;
    private List<Project> filteredProjectList;

    private static final String PREFS_NAME = "RegenerAppPrefs";
    private static final String KEY_SELECTED_PROJECT_ID = "selected_project_id";
    private static final String KEY_SELECTED_PROJECT_NAME = "selected_project_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProjectSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        loadProjects();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Seleccionar Proyecto");
            getSupportActionBar().setSubtitle("RegenerApp - Regenerar Estudio");
        }
    }

    private void setupRecyclerView() {
        binding.rvProjects.setLayoutManager(new LinearLayoutManager(this));

        projectAdapter = new ProjectAdapter(new ArrayList<>(), this::onProjectSelected);
        binding.rvProjects.setAdapter(projectAdapter);
    }

    private void loadProjects() {
        // TODO: En el futuro conectar con API
        // Por ahora datos hardcoded
        projectList = createSampleProjects();
        filteredProjectList = new ArrayList<>(projectList);

        projectAdapter.updateProjects(filteredProjectList);

        // Mostrar mensaje si no hay proyectos
        if (projectList.isEmpty()) {
            Toast.makeText(this, "No hay proyectos disponibles", Toast.LENGTH_LONG).show();
        }
    }

    private List<Project> createSampleProjects() {
        List<Project> projects = new ArrayList<>();

        projects.add(new Project(
                1,
                "Casa Familiar Los Cerezos",
                "Familia Rodríguez Pérez",
                "Quito, Pichincha",
                "diseño",
                "2025-01-15",
                "2025-04-30",
                "Iluminación LED y arquitectónica para casa familiar de 3 plantas"
        ));

        projects.add(new Project(
                2,
                "Edificio Corporativo EcuaTech",
                "EcuaTech Solutions",
                "Guayaquil, Guayas",
                "compra",
                "2025-02-01",
                "2025-07-15",
                "Sistema completo de iluminación corporativa y espacios colaborativos"
        ));

        projects.add(new Project(
                3,
                "Restaurante Vista al Río",
                "Grupo Gastronómico Luna",
                "Cuenca, Azuay",
                "instalacion",
                "2024-11-20",
                "2025-02-28",
                "Ambiente cálido con iluminación decorativa y funcional"
        ));

        projects.add(new Project(
                4,
                "Hotel Boutique Andino",
                "Hoteles Patrimonio",
                "Otavalo, Imbabura",
                "diseño",
                "2025-03-01",
                "2025-09-30",
                "Iluminación temática inspirada en cultura andina"
        ));

        return projects;
    }

    private void onProjectSelected(Project project) {
        // Guardar proyecto seleccionado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_SELECTED_PROJECT_ID, project.getId());
        editor.putString(KEY_SELECTED_PROJECT_NAME, project.getName());
        editor.apply();

        // Mostrar confirmación
        Toast.makeText(this,
                "Proyecto seleccionado: " + project.getName(),
                Toast.LENGTH_SHORT).show();

        // Ir a MainActivity con proyecto seleccionado
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("project_id", project.getId());
        intent.putExtra("project_name", project.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_project_selection, menu);

        // Configurar SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView(); // Ahora funcionará correctamente

        searchView.setQueryHint("Buscar proyecto...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProjects(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProjects(newText);
                return true;
            }
        });

        return true;
    }

    private void filterProjects(String query) {
        filteredProjectList.clear();

        if (query.isEmpty()) {
            filteredProjectList.addAll(projectList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Project project : projectList) {
                if (project.getName().toLowerCase().contains(lowerCaseQuery) ||
                        project.getClient().toLowerCase().contains(lowerCaseQuery) ||
                        project.getLocation().toLowerCase().contains(lowerCaseQuery)) {
                    filteredProjectList.add(project);
                }
            }
        }

        projectAdapter.updateProjects(filteredProjectList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_refresh) {
            loadProjects();
            Toast.makeText(this, "Proyectos actualizados", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Prevenir regreso - esta es la pantalla obligatoria
        Toast.makeText(this, "Debe seleccionar un proyecto para continuar", Toast.LENGTH_LONG).show();
    }
}