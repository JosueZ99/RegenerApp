package com.regenerarestudio.regenerapp.ui.materiales;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.regenerarestudio.regenerapp.databinding.FragmentMaterialesBinding;

public class MaterialesFragment extends Fragment {

    private FragmentMaterialesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MaterialesViewModel materialesViewModel =
                new ViewModelProvider(this).get(MaterialesViewModel.class);

        binding = FragmentMaterialesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textMateriales;
        materialesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}