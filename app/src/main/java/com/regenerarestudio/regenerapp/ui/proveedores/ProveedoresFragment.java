package com.regenerarestudio.regenerapp.ui.proveedores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.regenerarestudio.regenerapp.databinding.FragmentProveedoresBinding;

public class ProveedoresFragment extends Fragment {

    private FragmentProveedoresBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProveedoresViewModel proveedoresViewModel =
                new ViewModelProvider(this).get(ProveedoresViewModel.class);

        binding = FragmentProveedoresBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textProveedores;
        proveedoresViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}