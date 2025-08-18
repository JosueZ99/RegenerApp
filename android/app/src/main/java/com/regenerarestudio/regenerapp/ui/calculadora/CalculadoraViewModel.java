package com.regenerarestudio.regenerapp.ui.calculadora;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CalculadoraViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public CalculadoraViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Calculadoras especializadas para construcción e iluminación");
    }

    public LiveData<String> getText() {
        return mText;
    }
}