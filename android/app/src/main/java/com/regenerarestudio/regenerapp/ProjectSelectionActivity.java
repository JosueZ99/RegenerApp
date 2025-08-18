package com.regenerarestudio.regenerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.regenerarestudio.regenerapp.data.models.Project;
import com.regenerarestudio.regenerapp.data.network.NetworkStateManager;
import com.regenerarestudio.regenerapp.databinding.ActivityProjectSelectionBinding;
import com.regenerarestudio.regenerapp.ui.proyectos.ProjectAdapter;
import com.regenerarestudio.regenerapp.ui.proyectos.ProyectosViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity de selección de proyectos - Actualizado para usar APIs REST
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ProjectSelectionActivity.java
 */
public class ProjectSelectionActivity extends AppCompatActivity {

    private ActivityProjectSelectionBinding binding;
    private ProjectAdapter projectAdapter;
    private ProyectosViewModel proyectosViewModel;
    private LinearProgressIndicator progressIndicator;

    // Para filtros de búsqueda
    private String currentSearchQuery = "";

    private static final String PREFS_NAME = "RegenerAppPrefs";
    private static final String KEY_SELECTED_PROJECT_ID = "selected_project_id";
    private static final String KEY_SELECTED_PROJECT_NAME = "selected_project_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProjectSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupProgressIndicator();
        setupRecyclerView();
        setupViewModel();
        observeViewModel();

        // Cargar proyectos desde API
        loadProjects();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Seleccionar Proyecto");
            getSupportActionBar().setSubtitle("RegenerApp - Regenerar Estudio");
        }
    }

    private void setupProgressIndicator() {
        // Crear y configurar indicador de progreso
        progressIndicator = new LinearProgressIndicator(this);
        progressIndicator.setIndeterminate(true);
        progressIndicator.setVisibility(View.GONE);

        // Agregar al layout si hay un contenedor
        // Nota: Esto depende de tu layout, ajustar según sea necesario
    }

    private void setupRecyclerView() {
        binding.rvProjects.setLayoutManager(new LinearLayoutManager(this));
        projectAdapter = new ProjectAdapter(new ArrayList<>(), this::onProjectSelected);
        binding.rvProjects.setAdapter(projectAdapter);
    }

    private void setupViewModel() {
        proyectosViewModel = new ViewModelProvider(this).get(ProyectosViewModel.class);
    }

    private void observeViewModel() {
        // Observar lista de proyectos
        proyectosViewModel.getProjects().observe(this, projects -> {
            if (projects != null) {
                projectAdapter.updateProjects(projects);

                // Mostrar mensaje si no hay proyectos
                if (projects.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                }
            }
        });

        // Observar estado de red
        proyectosViewModel.getNetworkState().observe(this, networkState -> {
            if (networkState != null) {
                handleNetworkState(networkState);
            }
        });

        // Observar proyecto seleccionado
        proyectosViewModel.getSelectedProject().observe(this, selectedProject -> {
            if (selectedProject != null) {
                // Proyecto seleccionado exitosamente, ir a MainActivity
                navigateToMainActivity(selectedProject);
            }
        });
    }

    private void handleNetworkState(NetworkStateManager.NetworkState networkState) {
        switch (networkState.getState()) {
            case LOADING:
                showLoading(networkState.getMessage());
                break;

            case SUCCESS:
                hideLoading();
                if (networkState.getMessage() != null && !networkState.getMessage().isEmpty()) {
                    Toast.makeText(this, networkState.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;

            case ERROR:
                hideLoading();
                Toast.makeText(this, "Error: " + networkState.getMessage(), Toast.LENGTH_LONG).show();
                break;

            case NO_NETWORK:
                hideLoading();
                Toast.makeText(this, "Sin conexión a internet. Mostrando datos locales.", Toast.LENGTH_LONG).show();
                break;

            case IDLE:
                hideLoading();
                break;
        }
    }

    private void showLoading(String message) {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }

        // Opcional: Mostrar mensaje de carga
        if (message != null && !message.isEmpty()) {
            // Se puede agregar un TextView para mostrar el mensaje
        }
    }

    private void hideLoading() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        // Mostrar mensaje de "no hay proyectos"
        Toast.makeText(this, "No hay proyectos disponibles", Toast.LENGTH_LONG).show();

        // Opcional: Mostrar una vista de estado vacío
    }

    private void hideEmptyState() {
        // Ocultar vista de estado vacío si existe
    }

    private void loadProjects() {
        // Cargar proyectos usando el ViewModel (que ya usa APIs)
        proyectosViewModel.refresh();
    }

    private void onProjectSelected(Project project) {
        if (project == null) {
            Toast.makeText(this, "Error: Proyecto no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar confirmación
        Toast.makeText(this,
                "Seleccionando proyecto: " + project.getName(),
                Toast.LENGTH_SHORT).show();

        // Usar el ViewModel para seleccionar el proyecto (incluye llamada a API)
        proyectosViewModel.selectProject(project);
    }

    private void navigateToMainActivity(Project selectedProject) {
        // Guardar proyecto seleccionado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_SELECTED_PROJECT_ID, selectedProject.getId());
        editor.putString(KEY_SELECTED_PROJECT_NAME, selectedProject.getName());
        editor.apply();

        // Mostrar confirmación final
        Toast.makeText(this,
                "Proyecto seleccionado: " + selectedProject.getName(),
                Toast.LENGTH_SHORT).show();

        // Ir a MainActivity con proyecto seleccionado
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("project_id", selectedProject.getId());
        intent.putExtra("project_name", selectedProject.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_project_selection, menu);

        // Configurar SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Buscar proyecto...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters(newText);
                return true;
            }
        });

        return true;
    }

    private void applyFilters(String query) {
        currentSearchQuery = query;

        // Usar el ViewModel para aplicar filtros
        proyectosViewModel.applyFilters(query, "", "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_refresh) {
            // Refrescar usando el ViewModel
            loadProjects();
            Toast.makeText(this, "Actualizando proyectos...", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Prevenir regreso - esta es la pantalla obligatoria
        Toast.makeText(this, "Debe seleccionar un proyecto para continuar", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Aplicar filtros actuales al regresar a la actividad
        if (!currentSearchQuery.isEmpty()) {
            applyFilters(currentSearchQuery);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}