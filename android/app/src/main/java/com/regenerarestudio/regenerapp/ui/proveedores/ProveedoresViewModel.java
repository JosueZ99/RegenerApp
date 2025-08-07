package com.regenerarestudio.regenerapp.ui.proveedores;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProveedoresViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ProveedoresViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Gestión de Proveedores\n\nProveedores registrados:\n\n• Ferretería El Constructor\n• Materiales Andinos\n• Distribuidora Central\n• Cemento Chimborazo\n\nEsta sección está siempre disponible");
    }

    public LiveData<String> getText() {
        return mText;
    }
}