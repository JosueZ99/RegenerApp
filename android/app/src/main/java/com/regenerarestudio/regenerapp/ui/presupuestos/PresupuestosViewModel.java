package com.regenerarestudio.regenerapp.ui.presupuestos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PresupuestosViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public PresupuestosViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Sistema dual de presupuestos: inicial vs gastos reales");
    }

    public LiveData<String> getText() {
        return mText;
    }
}