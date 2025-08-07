package com.regenerarestudio.regenerapp.ui.exportacion;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExportacionViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ExportacionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Centro de Exportación\n\n📄 Documentos Disponibles\n\nTipos de exportación:\n\n📋 Listas de Materiales\n• Formato PDF profesional\n• Archivo Excel editable\n• CSV para otros sistemas\n\n💰 Presupuestos\n• Presupuesto para cliente\n• Análisis interno detallado\n• Comparativas de escenarios\n\n📊 Reportes de Proyecto\n• Estado de avance\n• Control financiero\n• Cronograma actualizado\n\n💾 Backup de Datos\n• Exportación completa\n• Respaldo por proyecto\n• Sincronización futura");
    }

    public LiveData<String> getText() {
        return mText;
    }
}