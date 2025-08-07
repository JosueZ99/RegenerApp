package com.regenerarestudio.regenerapp.ui.exportacion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.regenerarestudio.regenerapp.databinding.FragmentExportacionBinding;

public class ExportacionFragment extends Fragment {

    private FragmentExportacionBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExportacionViewModel exportacionViewModel =
                new ViewModelProvider(this).get(ExportacionViewModel.class);

        binding = FragmentExportacionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textExportacion;
        exportacionViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}