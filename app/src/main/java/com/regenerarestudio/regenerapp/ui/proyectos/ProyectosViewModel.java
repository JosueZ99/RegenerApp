package com.regenerarestudio.regenerapp.ui.proyectos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProyectosViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ProyectosViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Lista de Proyectos\n\n• Casa Moderna - Quito Norte\n• Oficina Corporativa - Cumbayá\n• Restaurante Boutique - Centro Histórico\n\nSelecciona un proyecto para continuar");
    }

    public LiveData<String> getText() {
        return mText;
    }
}