package com.regenerarestudio.regenerapp.ui.ayuda;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AyudaViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AyudaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Centro de Ayuda\n\n❓ Preguntas Frecuentes\n\n🚀 Primeros Pasos\n• ¿Cómo crear mi primer proyecto?\n• ¿Cómo usar las calculadoras?\n• ¿Cómo generar un presupuesto?\n\n🧮 Calculadoras\n• ¿Qué tipos de cálculo están disponibles?\n• ¿Cómo interpretar los resultados?\n• ¿Cómo agregar materiales personalizados?\n\n💰 Presupuestos\n• ¿Cómo crear un presupuesto detallado?\n• ¿Cómo hacer seguimiento de gastos?\n• ¿Cómo exportar presupuestos?\n\n🏪 Proveedores\n• ¿Cómo agregar nuevos proveedores?\n• ¿Cómo comparar precios?\n• ¿Cómo actualizar información?\n\n📱 Contacto\nPara soporte técnico:\n📧 soporte@regenerarestudio.com\n📞 +593 93 936 2398\n\n🌐 Más recursos:\nVisita nuestro sitio web para tutoriales en video y documentación completa.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}