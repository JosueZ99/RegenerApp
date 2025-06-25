package com.regenerarestudio.regenerapp.ui.proyectos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.regenerarestudio.regenerapp.model.Project;
import java.util.ArrayList;
import java.util.List;

public class ProyectosViewModel extends ViewModel {

    private final MutableLiveData<List<Project>> projectsLiveData;
    private List<Project> projects;

    public ProyectosViewModel() {
        projectsLiveData = new MutableLiveData<>();
        projects = createSampleProjects();
        projectsLiveData.setValue(projects);
    }

    private List<Project> createSampleProjects() {
        List<Project> sampleProjects = new ArrayList<>();

        sampleProjects.add(new Project(1, "Casa Moderna", "Juan Pérez", "Quito Norte", "Residencial", "EN PROGRESO"));
        sampleProjects.add(new Project(2, "Oficina Corporativa", "TechSolutions", "Cumbayá", "Comercial", "PLANIFICACIÓN"));
        sampleProjects.add(new Project(3, "Restaurante Boutique", "María González", "Centro Histórico", "Comercial", "EN PROGRESO"));
        sampleProjects.add(new Project(4, "Villa Familiar", "Carlos Rodríguez", "Los Chillos", "Residencial", "TERMINADO"));
        sampleProjects.add(new Project(5, "Centro Comercial", "Inversiones ABC", "Norte de Quito", "Comercial", "PRESUPUESTO"));

        return sampleProjects;
    }

    public LiveData<List<Project>> getProjects() {
        return projectsLiveData;
    }

    public void selectProject(Project selectedProject) {
        // Deseleccionar todos los proyectos
        for (Project project : projects) {
            project.setSelected(false);
        }

        // Seleccionar el proyecto clickeado
        selectedProject.setSelected(true);

        // Actualizar LiveData
        projectsLiveData.setValue(projects);
    }

    public Project getSelectedProject() {
        for (Project project : projects) {
            if (project.isSelected()) {
                return project;
            }
        }
        return null;
    }
}