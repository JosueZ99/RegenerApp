package com.regenerarestudio.regenerapp.ui.calculadora;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.databinding.FragmentCalculadoraBinding;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment de Calculadoras Especializadas con formularios dinámicos
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/calculadora/CalculadoraFragment.java
 */
public class CalculadoraFragment extends Fragment {

    private FragmentCalculadoraBinding binding;
    private CalculadoraViewModel calculadoraViewModel;

    // Dropdowns
    private AutoCompleteTextView categoryDropdown;
    private AutoCompleteTextView typeDropdown;

    // Containers dinámicos
    private LinearLayout formContainer;
    private MaterialCardView cardForm;
    private MaterialCardView cardResult;
    private MaterialCardView cardDetails;
    private LinearLayout actionButtonsLayout;

    // Datos para dropdowns
    private Map<String, String[]> calculatorTypes;
    private String selectedCategory = "";
    private String selectedType = "";

    // Campos dinámicos del formulario
    private Map<String, TextInputEditText> formFields;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalculadoraBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calculadoraViewModel = new ViewModelProvider(this).get(CalculadoraViewModel.class);

        initializeViews();
        setupCalculatorTypes();
        setupDropdowns();
        setupButtons();
        observeViewModel();
    }

    private void initializeViews() {
        categoryDropdown = binding.dropdownCategory;
        typeDropdown = binding.dropdownType;
        formContainer = binding.containerCalculatorForm;
        cardForm = binding.cardCalculatorForm;
        cardResult = binding.cardResult;
        cardDetails = binding.cardDetails;
        actionButtonsLayout = binding.layoutActionButtons;
        formFields = new HashMap<>();
    }

    private void setupCalculatorTypes() {
        calculatorTypes = new HashMap<>();

        // Categoría Construcción
        calculatorTypes.put("Construcción", new String[]{
                "Pintura",
                "Gypsum",
                "Empaste"
        });

        // Categoría Iluminación
        calculatorTypes.put("Iluminación", new String[]{
                "Perfiles",
                "Cintas LED",
                "Cables"
        });
    }

    private void setupDropdowns() {
        // Configurar dropdown de categorías
        String[] categories = calculatorTypes.keySet().toArray(new String[0]);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        categoryDropdown.setAdapter(categoryAdapter);

        // Listener para categoría
        categoryDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categories[position];
            setupTypeDropdown();
            hideFormAndResults();
        });

        // Listener para tipo específico
        typeDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedType = (String) parent.getItemAtPosition(position);
            createDynamicForm();
        });
    }

    private void setupTypeDropdown() {
        if (selectedCategory.isEmpty()) return;

        String[] types = calculatorTypes.get(selectedCategory);
        if (types != null) {
            ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    types
            );
            typeDropdown.setAdapter(typeAdapter);
            typeDropdown.setText("");
            binding.layoutTypeDropdown.setVisibility(View.VISIBLE);
        }
    }

    private void createDynamicForm() {
        // Limpiar formulario anterior
        clearDynamicForm();

        // Mostrar card del formulario
        cardForm.setVisibility(View.VISIBLE);
        actionButtonsLayout.setVisibility(View.VISIBLE);

        // Actualizar título
        binding.tvFormTitle.setText("Calculadora de " + selectedType);

        // Crear campos según el tipo seleccionado
        switch (selectedType) {
            case "Pintura":
                createPaintForm();
                break;
            case "Gypsum":
                createGypsumForm();
                break;
            case "Empaste":
                createEmpasteForm();
                break;
            case "Perfiles":
                createProfileForm();
                break;
            case "Cintas LED":
                createLedStripForm();
                break;
            case "Cables":
                createCableForm();
                break;
        }
    }

    private void createPaintForm() {
        addFormField("area_total", "Área Total (m²)", "Ingresa el área total a pintar", "number");
        addFormField("num_manos", "Número de Manos", "Cantidad de capas de pintura", "number");
        addFormField("tipo_superficie", "Tipo de Superficie", "Interior/Exterior", "text");
        addFormField("rendimiento", "Rendimiento (m²/L)", "Metros cuadrados por litro", "number");
    }

    private void createGypsumForm() {
        addFormField("area_total", "Área Total (m²)", "Área total de gypsum", "number");
        addFormField("espesor", "Espesor (mm)", "Espesor del panel", "number");
        addFormField("tipo_panel", "Tipo de Panel", "Regular/Resistente a humedad", "text");
        addFormField("perdidas", "Pérdidas (%)", "Porcentaje de desperdicio", "number");
    }

    private void createEmpasteForm() {
        addFormField("area_total", "Área Total (m²)", "Área total a empastar", "number");
        addFormField("espesor_promedio", "Espesor Promedio (mm)", "Grosor promedio de empaste", "number");
        addFormField("tipo_empaste", "Tipo de Empaste", "Interior/Exterior", "text");
        addFormField("textura", "Textura", "Lisa/Rugosa", "text");
    }

    private void createProfileForm() {
        addFormField("longitud_total", "Longitud Total (m)", "Metros lineales de perfil", "number");
        addFormField("tipo_perfil", "Tipo de Perfil", "Empotrado/Superficial", "text");
        addFormField("ancho_perfil", "Ancho del Perfil (mm)", "Ancho en milímetros", "number");
        addFormField("acabado", "Acabado", "Anodizado/Natural", "text");
    }

    private void createLedStripForm() {
        addFormField("longitud_total", "Longitud Total (m)", "Metros de cinta LED", "number");
        addFormField("potencia_metro", "Potencia por Metro (W/m)", "Watts por metro", "number");
        addFormField("voltaje", "Voltaje (V)", "12V/24V", "number");
        addFormField("color_temperatura", "Temperatura Color (K)", "3000K/4000K/6000K", "number");
        addFormField("tipo_proteccion", "Tipo de Protección", "IP20/IP65/IP68", "text");
    }

    private void createCableForm() {
        addFormField("longitud_total", "Longitud Total (m)", "Metros de cable", "number");
        addFormField("calibre", "Calibre (AWG)", "Calibre del cable", "number");
        addFormField("numero_conductores", "Número de Conductores", "Cantidad de hilos", "number");
        addFormField("tipo_cable", "Tipo de Cable", "THHN/THW/THWN", "text");
        addFormField("voltaje_nominal", "Voltaje Nominal (V)", "Voltaje de operación", "number");
    }

    private void addFormField(String key, String label, String hint, String inputType) {
        // Crear TextInputLayout
        TextInputLayout textInputLayout = new TextInputLayout(requireContext());
        textInputLayout.setBoxStrokeColor(getResources().getColor(R.color.primary_color, null));
        textInputLayout.setHintTextColor(getResources().getColorStateList(R.color.primary_color, null));
        textInputLayout.setHint(label);
        textInputLayout.setHelperText(hint);
        textInputLayout.setStyle(com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);

        // Configurar parámetros de layout
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.bottomMargin = dpToPx(16);
        textInputLayout.setLayoutParams(layoutParams);

        // Crear TextInputEditText
        TextInputEditText editText = new TextInputEditText(requireContext());
        editText.setHint(hint);

        // Configurar tipo de entrada
        switch (inputType) {
            case "number":
                editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case "text":
            default:
                editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                break;
        }

        // Agregar EditText al Layout
        textInputLayout.addView(editText);

        // Agregar al contenedor
        formContainer.addView(textInputLayout);

        // Guardar referencia
        formFields.put(key, editText);
    }

    private void setupButtons() {
        binding.btnCalculate.setOnClickListener(v -> performCalculation());
        binding.btnClear.setOnClickListener(v -> clearForm());
        binding.btnAddToBudget.setOnClickListener(v -> addToBudget());
    }

    private void performCalculation() {
        if (!validateForm()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recopilar datos del formulario
        Map<String, String> formData = new HashMap<>();
        formData.put("category", selectedCategory);
        formData.put("type", selectedType);

        for (Map.Entry<String, TextInputEditText> entry : formFields.entrySet()) {
            String value = entry.getValue().getText().toString().trim();
            formData.put(entry.getKey(), value);
        }

        // Realizar cálculo (simulado por ahora)
        CalculationResult result = simulateCalculation(formData);

        // Mostrar resultado
        displayResult(result);
    }

    private boolean validateForm() {
        for (TextInputEditText field : formFields.values()) {
            if (field.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private CalculationResult simulateCalculation(Map<String, String> formData) {
        // Simulación de cálculo - en el futuro se conectará con el backend
        CalculationResult result = new CalculationResult();

        switch (selectedType) {
            case "Pintura":
                double area = Double.parseDouble(formData.get("area_total"));
                double manos = Double.parseDouble(formData.get("num_manos"));
                double rendimiento = Double.parseDouble(formData.get("rendimiento"));

                double litros = (area * manos) / rendimiento;
                result.quantity = String.format("%.2f Litros", litros);
                result.cost = String.format("$%.2f", litros * 15.50); // Precio estimado por litro
                break;

            case "Gypsum":
                double areaGypsum = Double.parseDouble(formData.get("area_total"));
                double panels = Math.ceil(areaGypsum / 2.88); // Panel estándar 1.2x2.4m
                result.quantity = String.format("%.0f Paneles", panels);
                result.cost = String.format("$%.2f", panels * 12.80); // Precio por panel
                break;

            default:
                result.quantity = "1.0 Unidad";
                result.cost = "$100.00";
                break;
        }

        return result;
    }

    private void displayResult(CalculationResult result) {
        binding.tvResultQuantity.setText(result.quantity);
        binding.tvResultCost.setText(result.cost);

        cardResult.setVisibility(View.VISIBLE);
        cardDetails.setVisibility(View.VISIBLE);
    }

    private void addToBudget() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Añadir al Presupuesto")
                .setMessage("¿Deseas agregar este cálculo al presupuesto inicial del proyecto?")
                .setPositiveButton("Añadir", (dialog, which) -> {
                    // TODO: Integrar con sistema de presupuestos
                    Toast.makeText(requireContext(), "Agregado al presupuesto exitosamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearForm() {
        for (TextInputEditText field : formFields.values()) {
            field.setText("");
        }
        hideFormAndResults();
    }

    private void clearDynamicForm() {
        // Remover campos dinámicos (mantener solo el título)
        int childCount = formContainer.getChildCount();
        for (int i = childCount - 1; i >= 1; i--) { // Mantener el primer hijo (título)
            formContainer.removeViewAt(i);
        }
        formFields.clear();
    }

    private void hideFormAndResults() {
        cardForm.setVisibility(View.GONE);
        cardResult.setVisibility(View.GONE);
        cardDetails.setVisibility(View.GONE);
        actionButtonsLayout.setVisibility(View.GONE);
    }

    private void observeViewModel() {
        // TODO: Implementar observadores cuando se conecte con el backend
        // calculadoraViewModel.getCalculationResult().observe(getViewLifecycleOwner(), result -> {
        //     displayResult(result);
        // });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Clase interna para resultados
    private static class CalculationResult {
        String quantity;
        String cost;
    }
}