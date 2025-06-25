package com.regenerarestudio.regenerapp.ui.proyectos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.regenerarestudio.regenerapp.databinding.FragmentProyectosBinding;

public class ProyectosFragment extends Fragment {

    private FragmentProyectosBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProyectosViewModel proyectosViewModel =
                new ViewModelProvider(this).get(ProyectosViewModel.class);

        binding = FragmentProyectosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textProyectos;
        proyectosViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}