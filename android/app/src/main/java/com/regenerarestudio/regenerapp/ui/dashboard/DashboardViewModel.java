package com.regenerarestudio.regenerapp.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Dashboard del Proyecto\n\nSelecciona un proyecto primero.\n\nMétricas disponibles:\n• Progreso general\n• Estado financiero\n• Próximos hitos\n• Resumen de materiales");
    }

    public LiveData<String> getText() {
        return mText;
    }
}