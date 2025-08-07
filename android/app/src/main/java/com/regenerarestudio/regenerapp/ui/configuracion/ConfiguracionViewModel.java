package com.regenerarestudio.regenerapp.ui.configuracion;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConfiguracionViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ConfiguracionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Configuración de la App\n\n⚙️ Ajustes Generales\n\n🎨 Apariencia\n• Tema claro/oscuro\n• Tamaño de fuente\n• Idioma de la interfaz\n\n🔢 Cálculos\n• Factores de desperdicio\n• Unidades de medida\n• Precisión de decimales\n\n💰 Precios y Moneda\n• Moneda predeterminada\n• Formato de precios\n• Actualización automática\n\n📱 Notificaciones\n• Recordatorios de proyecto\n• Alertas de presupuesto\n• Actualizaciones de precios\n\n🔒 Seguridad\n• Autenticación biométrica\n• Backup automático\n• Privacidad de datos\n\n📍 Ubicación\n• Región de proveedores\n• Costos de transporte\n• Zona horaria");
    }

    public LiveData<String> getText() {
        return mText;
    }
}