package com.regenerarestudio.regenerapp.ui.proyectos;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.regenerarestudio.regenerapp.data.api.ApiClient;
import com.regenerarestudio.regenerapp.data.api.ApiService;
import com.regenerarestudio.regenerapp.data.models.Project;
import com.regenerarestudio.regenerapp.data.network.NetworkStateManager;
import com.regenerarestudio.regenerapp.data.responses.PaginatedResponse;
import com.regenerarestudio.regenerapp.data.responses.ProjectSelectionResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel actualizado para manejar proyectos usando APIs REST
 * Reemplaza los datos hardcoded con llamadas reales al backend Django
 */
public class ProyectosViewModel extends AndroidViewModel {

    private static final String TAG = "ProyectosViewModel";

    // API Service
    private final ApiService apiService;

    // LiveData para proyectos
    private final MutableLiveData<List<Project>> projectsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Project> selectedProjectLiveData = new MutableLiveData<>();

    // Estado de red
    private final NetworkStateManager.NetworkStateLiveData networkState = new NetworkStateManager.NetworkStateLiveData();

    // Datos locales
    private List<Project> allProjects = new ArrayList<>();
    private List<Project> filteredProjects = new ArrayList<>();

    // Filtros actuales
    private String currentSearchFilter = "";
    private String currentTypeFilter = "";
    private String currentPhaseFilter = "";

    public ProyectosViewModel(@NonNull Application application) {
        super(application);
        this.apiService = ApiClient.getApiService();

        // Configurar URL del servidor según el entorno
        configureServerUrl();

        // Cargar proyectos al inicializar
        loadProjects();
    }

    /**
     * Configurar URL del servidor automáticamente
     */
    private void configureServerUrl() {
        // Detectar si está corriendo en emulador o dispositivo físico
        boolean isEmulator = android.os.Build.FINGERPRINT.contains("generic");

        Log.d(TAG, "Configurando servidor...");
        Log.d(TAG, "Es emulador: " + isEmulator);
        Log.d(TAG, "Build fingerprint: " + android.os.Build.FINGERPRINT);

        // Resetear cliente antes de reconfigurar
        ApiClient.resetClient();

        // Configurar nueva URL
        ApiClient.configureForEnvironment(getApplication(), isEmulator);

        // Debug de configuración
        ApiClient.debugCurrentConfiguration();
    }

    /**
     * Obtener LiveData de proyectos
     */
    public LiveData<List<Project>> getProjects() {
        return projectsLiveData;
    }

    /**
     * Obtener LiveData del proyecto seleccionado
     */
    public LiveData<Project> getSelectedProject() {
        return selectedProjectLiveData;
    }

    /**
     * Obtener estado de red
     */
    public LiveData<NetworkStateManager.NetworkState> getNetworkState() {
        return networkState.getNetworkState();
    }

    /**
     * Cargar proyectos desde el backend
     */
    public void loadProjects() {
        // Verificar conectividad
        if (!NetworkStateManager.checkConnectivityAndNotify(getApplication(), networkState)) {
            Log.w(TAG, "Sin conexión a internet, usando datos locales si existen");
            // Usar datos locales si existen o datos de fallback
            if (allProjects.isEmpty()) {
                loadFallbackData();
            }
            return;
        }

        networkState.setLoading("Cargando proyectos...");

        Call<PaginatedResponse<Project>> call = apiService.getProjects(null, null, null, null, null, null);

        call.enqueue(new Callback<PaginatedResponse<Project>>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedResponse<Project>> call, @NonNull Response<PaginatedResponse<Project>> response) {
                NetworkStateManager.NetworkState state = NetworkStateManager.processResponse(response);

                if (state.isSuccess() && response.body() != null) {
                    PaginatedResponse<Project> paginatedResponse = response.body();
                    allProjects = paginatedResponse.getResults();
                    applyCurrentFilters();
                    networkState.setSuccess("Proyectos cargados correctamente (" + paginatedResponse.getCount() + " total)");
                    Log.d(TAG, "Proyectos cargados: " + allProjects.size() + "/" + paginatedResponse.getCount());
                } else {
                    networkState.setError("Error al cargar proyectos: " + state.getMessage());
                    Log.e(TAG, "Error en respuesta: " + state.getMessage());
                    // Usar datos de fallback en caso de error
                    loadFallbackData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedResponse<Project>> call, @NonNull Throwable t) {
                NetworkStateManager.NetworkState state = NetworkStateManager.processError(t);
                networkState.setError(state.getMessage());
                Log.e(TAG, "Error de red al cargar proyectos", t);

                // Usar datos de fallback en caso de fallo de red
                loadFallbackData();
            }
        });
    }

    /**
     * Cargar datos de fallback cuando no hay conexión o hay errores
     */
    private void loadFallbackData() {
        Log.i(TAG, "Cargando datos de fallback");
        List<Project> fallbackProjects = createFallbackProjects();
        allProjects = fallbackProjects;
        applyCurrentFilters();
        networkState.setSuccess("Usando datos locales");
    }

    /**
     * Crear proyectos de fallback (datos hardcoded como backup)
     */
    private List<Project> createFallbackProjects() {
        List<Project> projects = new ArrayList<>();

        // Convertir constructor simple a Project completo con más campos
        projects.add(new Project(1L, "Fallback", "Juan Pérez Morales",
                "Cumbayá, Los Almendros", "residential", "in_progress", "design",
                "2025-01-15", "2025-04-30", "Iluminación arquitectónica integral para casa de 280m²",
                "https://drive.google.com/drive/folders/casa-moderna", 12500.0, true));

        projects.add(new Project(2L, "Oficina Corporativa TechSolutions", "TechSolutions Ecuador S.A.",
                "Av. Naciones Unidas E2-50", "commercial", "in_progress", "purchase",
                "2025-02-01", "2025-05-15", "Sistema de iluminación LED para oficinas modernas",
                "https://drive.google.com/drive/folders/oficina-tech", 18750.0, false));

        projects.add(new Project(3L, "Restaurante Boutique Centro", "María González",
                "Centro Histórico, García Moreno", "commercial", "planning", "design",
                "2025-03-10", "2025-06-20", "Iluminación ambiental para restaurante de alta cocina",
                "https://drive.google.com/drive/folders/restaurante", 9800.0, false));

        projects.add(new Project(4L, "Villa Familiar Los Chillos", "Carlos Rodríguez",
                "Valle de los Chillos, Sangolquí", "residential", "completed", "completed",
                "2024-09-15", "2024-12-30", "Proyecto de iluminación exterior e interior completado",
                "https://drive.google.com/drive/folders/villa-chillos", 15600.0, false));

        projects.add(new Project(5L, "Centro Comercial Norte", "Inversiones ABC S.A.",
                "Norte de Quito, La Prensa", "commercial", "planning", "design",
                "2025-04-01", "2025-08-30", "Iluminación para centro comercial de 3 niveles",
                "https://drive.google.com/drive/folders/centro-comercial", 35000.0, false));

        return projects;
    }

    /**
     * Seleccionar un proyecto y notificar al backend
     */
    public void selectProject(Project project) {
        if (project == null) {
            Log.w(TAG, "Intento de seleccionar proyecto nulo");
            return;
        }

        // Verificar conectividad
        if (!NetworkStateManager.checkConnectivityAndNotify(getApplication(), networkState)) {
            // Sin conexión, seleccionar localmente
            selectProjectLocally(project);
            return;
        }

        networkState.setLoading("Seleccionando proyecto...");

        Call<ProjectSelectionResponse> call = apiService.selectProject(project.getId());

        call.enqueue(new Callback<ProjectSelectionResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProjectSelectionResponse> call,
                                   @NonNull Response<ProjectSelectionResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ProjectSelectionResponse selectionResponse = response.body();

                    if (selectionResponse.isSuccessful()) {
                        // Actualizar proyecto seleccionado localmente
                        selectProjectLocally(project);
                        networkState.setSuccess();
                        Log.d(TAG, "Proyecto seleccionado correctamente: " + project.getName());
                    } else {
                        networkState.setError("Error al seleccionar proyecto: " + selectionResponse.getMessage());
                    }
                } else {
                    networkState.setError("Error al seleccionar proyecto");
                    // Seleccionar localmente como fallback
                    selectProjectLocally(project);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProjectSelectionResponse> call, @NonNull Throwable t) {
                NetworkStateManager.NetworkState state = NetworkStateManager.processError(t);
                networkState.setError(state.getMessage());
                Log.e(TAG, "Error de red al seleccionar proyecto", t);

                // Seleccionar localmente como fallback
                selectProjectLocally(project);
            }
        });
    }

    /**
     * Seleccionar proyecto localmente (actualizar UI)
     */
    private void selectProjectLocally(Project selectedProject) {
        // Deseleccionar todos los proyectos
        for (Project project : allProjects) {
            project.setSelected(false);
        }

        // Seleccionar el proyecto clickeado
        selectedProject.setSelected(true);

        // Actualizar LiveData
        selectedProjectLiveData.setValue(selectedProject);
        applyCurrentFilters(); // Actualizar lista mostrada

        Log.d(TAG, "Proyecto seleccionado localmente: " + selectedProject.getName());
    }

    /**
     * Aplicar filtros de búsqueda
     */
    public void applyFilters(String search, String type, String phase) {
        currentSearchFilter = search != null ? search.toLowerCase() : "";
        currentTypeFilter = type != null ? type : "";
        currentPhaseFilter = phase != null ? phase : "";

        applyCurrentFilters();
    }

    /**
     * Aplicar filtros actuales a la lista
     */
    private void applyCurrentFilters() {
        filteredProjects.clear();

        for (Project project : allProjects) {
            boolean matchesSearch = currentSearchFilter.isEmpty() ||
                    project.getName().toLowerCase().contains(currentSearchFilter) ||
                    project.getClient().toLowerCase().contains(currentSearchFilter) ||
                    project.getLocation().toLowerCase().contains(currentSearchFilter);

            boolean matchesType = currentTypeFilter.isEmpty() ||
                    currentTypeFilter.equals(project.getProjectType());

            boolean matchesPhase = currentPhaseFilter.isEmpty() ||
                    currentPhaseFilter.equals(project.getCurrentPhase());

            if (matchesSearch && matchesType && matchesPhase) {
                filteredProjects.add(project);
            }
        }

        projectsLiveData.setValue(new ArrayList<>(filteredProjects));
        Log.d(TAG, "Filtros aplicados. Proyectos mostrados: " + filteredProjects.size() + "/" + allProjects.size());
    }

    /**
     * Refrescar datos del servidor
     */
    public void refresh() {
        Log.d(TAG, "Refrescando datos de proyectos");
        loadProjects();
    }

    /**
     * Obtener el proyecto actualmente seleccionado
     */
    public Project getCurrentSelectedProject() {
        return selectedProjectLiveData.getValue();
    }

    /**
     * Verificar si hay algún proyecto seleccionado
     */
    public boolean hasSelectedProject() {
        Project selected = getCurrentSelectedProject();
        return selected != null && selected.isSelected();
    }

    /**
     * Limpiar filtros
     */
    public void clearFilters() {
        applyFilters("", "", "");
    }

    /**
     * Obtener número total de proyectos
     */
    public int getTotalProjectsCount() {
        return allProjects.size();
    }

    /**
     * Obtener número de proyectos filtrados
     */
    public int getFilteredProjectsCount() {
        return filteredProjects.size();
    }
}