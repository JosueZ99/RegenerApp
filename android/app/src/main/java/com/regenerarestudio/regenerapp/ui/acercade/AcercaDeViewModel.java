package com.regenerarestudio.regenerapp.ui.acercade;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AcercaDeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AcercaDeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("RegenerApp v1.0\n\n🏗️ Sistema de Gestión de Proyectos Arquitectónicos\n\n👨‍🎓 Desarrollado por:\nJoab Josue Zambrano Zambrano\nEstudiante de Tecnología Superior en Desarrollo de Software\nPontificia Universidad Católica del Ecuador\n\n🏢 Cliente:\nRegenerar Estudio\nArq. Juan Fernando Terán Benalcázar\nEspecialistas en iluminación arquitectónica\n\n📧 Contacto:\njjzambranoz@puce.edu.ec\n+593 93 936 2398\n\n🎯 Propósito:\nOptimizar la gestión de proyectos arquitectónicos mediante la automatización de cálculos de materiales, comparación de proveedores y control de presupuestos.\n\n⚖️ Licencia:\nDesarrollado como proyecto académico\nTodos los derechos reservados\n\n🔧 Tecnologías:\n• Android nativo (Java)\n• SQLite para almacenamiento\n• Material Design 3\n• Arquitectura MVVM\n\n📅 Fecha de desarrollo:\nJunio - Agosto 2025\n\n🌟 Versión actual: 1.0.0\nCódigo de compilación: 1");
    }

    public LiveData<String> getText() {
        return mText;
    }
}