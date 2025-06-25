package com.regenerarestudio.regenerapp.ui.presupuestos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PresupuestosViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PresupuestosViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Gestión de Presupuestos\n\nSelecciona un proyecto primero.\n\nFunciones disponibles:\n• Crear presupuesto\n• Seguimiento de gastos\n• Control financiero");
    }

    public LiveData<String> getText() {
        return mText;
    }
}