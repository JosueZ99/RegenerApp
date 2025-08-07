package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.regenerarestudio.regenerapp.databinding.FragmentPresupuestosBinding;

public class PresupuestosFragment extends Fragment {

    private FragmentPresupuestosBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PresupuestosViewModel presupuestosViewModel =
                new ViewModelProvider(this).get(PresupuestosViewModel.class);

        binding = FragmentPresupuestosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textPresupuestos;
        presupuestosViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}