package com.regenerarestudio.regenerapp.ui.proveedores;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProveedoresViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public ProveedoresViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Gestión de proveedores locales con ratings y contacto");
    }

    public LiveData<String> getText() {
        return mText;
    }
}