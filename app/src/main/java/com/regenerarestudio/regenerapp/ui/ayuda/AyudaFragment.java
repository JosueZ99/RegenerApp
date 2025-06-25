package com.regenerarestudio.regenerapp.ui.ayuda;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.regenerarestudio.regenerapp.databinding.FragmentAyudaBinding;

public class AyudaFragment extends Fragment {

    private FragmentAyudaBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AyudaViewModel ayudaViewModel =
                new ViewModelProvider(this).get(AyudaViewModel.class);

        binding = FragmentAyudaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textAyuda;
        ayudaViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}