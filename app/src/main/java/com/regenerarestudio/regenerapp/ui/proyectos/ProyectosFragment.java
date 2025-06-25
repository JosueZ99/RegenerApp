package com.regenerarestudio.regenerapp.ui.proyectos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.regenerarestudio.regenerapp.MainActivity;
import com.regenerarestudio.regenerapp.databinding.FragmentProyectosBinding;
import com.regenerarestudio.regenerapp.model.Project;

public class ProyectosFragment extends Fragment implements ProjectsAdapter.OnProjectClickListener {

    private FragmentProyectosBinding binding;
    private ProyectosViewModel proyectosViewModel;
    private ProjectsAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        proyectosViewModel = new ViewModelProvider(this).get(ProyectosViewModel.class);

        binding = FragmentProyectosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        observeViewModel();

        return root;
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.rvProjects;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProjectsAdapter(proyectosViewModel.getProjects().getValue(), this);
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        proyectosViewModel.getProjects().observe(getViewLifecycleOwner(), projects -> {
            if (adapter != null) {
                adapter.updateProjects(projects);
            }
        });
    }

    @Override
    public void onProjectClick(Project project) {
        // Seleccionar el proyecto en el ViewModel
        proyectosViewModel.selectProject(project);

        // Notificar al MainActivity sobre la selección
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.setProjectSelected(true, project.getName());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}