package com.regenerarestudio.regenerapp.ui.calculadora;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CalculadoraViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CalculadoraViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Calculadora de Materiales\n\nSelecciona un proyecto primero para usar las calculadoras.\n\nTipos disponibles:\n• Muros\n• Techos\n• Pisos");
    }

    public LiveData<String> getText() {
        return mText;
    }
}