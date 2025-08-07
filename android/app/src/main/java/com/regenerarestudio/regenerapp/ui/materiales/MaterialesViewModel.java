package com.regenerarestudio.regenerapp.ui.materiales;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MaterialesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MaterialesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Base de Datos de Materiales\n\n📦 Catálogo Completo\n\nCategorías disponibles:\n\n🏗️ Estructurales\n• Cemento, acero, madera\n• Bloques y ladrillos\n\n🎨 Acabados\n• Pinturas y revestimientos\n• Pisos y cerámicas\n\n💡 Iluminación\n• Luminarias arquitectónicas\n• Sistemas de control\n\n⚡ Instalaciones\n• Tuberías y cables\n• Accesorios eléctricos");
    }

    public LiveData<String> getText() {
        return mText;
    }
}