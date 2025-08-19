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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.regenerarestudio.regenerapp.MainActivity;
import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.databinding.FragmentCalculadoraBinding;
import com.regenerarestudio.regenerapp.data.models.CalculationResponse;
import com.regenerarestudio.regenerapp.ui.calculadora.adapters.CalculationDetailsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment de Calculadoras Especializadas - Versión simplificada y funcional
 */
public class CalculadoraFragment extends Fragment {

    private FragmentCalculadoraBinding binding;
    private CalculadoraViewModel calculadoraViewModel;

    // UI Components
    private AutoCompleteTextView categoryDropdown;
    private AutoCompleteTextView typeDropdown;
    private LinearLayout formContainer;
    private MaterialCardView cardForm;
    private MaterialCardView cardResult;
    private MaterialCardView cardDetails;
    private LinearLayout actionButtonsLayout;
    private RecyclerView rvCalculationDetails;

    // Data - Usamos un enfoque híbrido: estático para evitar problemas + dinámico para el futuro
    private Map<String, String[]> calculatorTypes;
    private Map<String, Map<String, FieldConfig>> formConfigurations;
    private String selectedCategory = "";
    private String selectedType = "";
    private String selectedTypeCode = "";
    private Map<String, Object> formFields;
    private CalculationDetailsAdapter detailsAdapter;
    private CalculationResponse currentCalculation;

    // Project info
    private long projectId;
    private String projectName;

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
        getProjectInfo();
        setupCalculatorTypes();
        setupDropdowns();
        setupButtons();
        setupRecyclerView();
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
        rvCalculationDetails = binding.rvCalculationDetails;
        formFields = new HashMap<>();
    }

    private void getProjectInfo() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            projectId = mainActivity.getSelectedProjectId();
            projectName = mainActivity.getSelectedProjectName();
        }
    }

    private void setupCalculatorTypes() {
        calculatorTypes = new HashMap<>();
        formConfigurations = new HashMap<>();

        // Categoría Construcción
        calculatorTypes.put("Construcción", new String[]{
                "Pintura", "Gypsum", "Empaste"
        });

        // Categoría Iluminación
        calculatorTypes.put("Iluminación", new String[]{
                "Perfiles", "Cintas LED", "Cables"
        });

        // Configuraciones de formularios con opciones predefinidas
        setupFormConfigurations();
    }

    private void setupFormConfigurations() {
        // Pintura
        Map<String, FieldConfig> paintConfig = new HashMap<>();
        paintConfig.put("area_to_paint", new FieldConfig("decimal", "Área a Pintar (m²)", true));
        paintConfig.put("number_of_coats", new FieldConfig("integer", "Número de Capas", true));
        paintConfig.put("paint_type", new FieldConfig("choice", "Tipo de Pintura", true,
                new String[]{"Látex", "Esmalte"}));
        paintConfig.put("coverage_per_liter", new FieldConfig("decimal", "Rendimiento (m²/L)", true));
        formConfigurations.put("Pintura", paintConfig);

        // Gypsum
        Map<String, FieldConfig> gypsumConfig = new HashMap<>();
        gypsumConfig.put("area_to_cover", new FieldConfig("decimal", "Área a Cubrir (m²)", true));
        gypsumConfig.put("thickness", new FieldConfig("decimal", "Espesor (mm)", true));
        gypsumConfig.put("gypsum_type", new FieldConfig("choice", "Tipo de Gypsum", true,
                new String[]{"Standard", "Húmedo"}));
        formConfigurations.put("Gypsum", gypsumConfig);

        // Empaste
        Map<String, FieldConfig> empasteConfig = new HashMap<>();
        empasteConfig.put("area_to_cover", new FieldConfig("decimal", "Área a Empastar (m²)", true));
        empasteConfig.put("empaste_type", new FieldConfig("choice", "Tipo de Empaste", true,
                new String[]{"Interior", "Exterior", "Sellador"}));
        empasteConfig.put("number_of_coats", new FieldConfig("integer", "Número de Capas", true));
        empasteConfig.put("coverage_per_kg", new FieldConfig("decimal", "Rendimiento (m²/kg)", false));
        formConfigurations.put("Empaste", empasteConfig);

        // Cintas LED
        Map<String, FieldConfig> ledConfig = new HashMap<>();
        ledConfig.put("total_length", new FieldConfig("decimal", "Longitud Total (m)", true));
        ledConfig.put("power_per_meter", new FieldConfig("choice", "Potencia por Metro (W/m)", true,
                new String[]{"4.8", "9.6", "14.4"}));
        ledConfig.put("voltage", new FieldConfig("choice", "Voltaje", true,
                new String[]{"12V", "24V"}));
        ledConfig.put("strip_type", new FieldConfig("choice", "Tipo de Cinta", true,
                new String[]{"SMD5050", "SMD2835"}));
        formConfigurations.put("Cintas LED", ledConfig);

        // Perfiles
        Map<String, FieldConfig> profilesConfig = new HashMap<>();
        profilesConfig.put("total_length", new FieldConfig("decimal", "Longitud Total (m)", true));
        profilesConfig.put("profile_type", new FieldConfig("choice", "Tipo de Perfil", true,
                new String[]{"Superficie", "Empotrado", "Suspendido", "Esquina"}));
        profilesConfig.put("profile_size", new FieldConfig("choice", "Tamaño", true,
                new String[]{"16mm", "20mm", "25mm", "30mm"}));
        profilesConfig.put("finish_type", new FieldConfig("choice", "Acabado", true,
                new String[]{"Anodizado", "Blanco", "Negro", "Plateado"}));
        profilesConfig.put("accessories_needed", new FieldConfig("choice", "Accesorios", false,
                new String[]{"Básicos", "Completos"}));
        formConfigurations.put("Perfiles", profilesConfig);

        // Cables
        Map<String, FieldConfig> cablesConfig = new HashMap<>();
        cablesConfig.put("total_length", new FieldConfig("decimal", "Longitud Total (m)", true));
        cablesConfig.put("wire_gauge", new FieldConfig("choice", "Calibre", true,
                new String[]{"12 AWG", "14 AWG"}));
        cablesConfig.put("cable_type", new FieldConfig("choice", "Tipo de Cable", true,
                new String[]{"THHN", "Flexible"}));
        cablesConfig.put("installation_type", new FieldConfig("choice", "Tipo de Instalación", true,
                new String[]{"Conduit", "Directo"}));
        formConfigurations.put("Cables", cablesConfig);
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
            selectedTypeCode = getTypeCode(selectedType);
            createDynamicForm();
        });
    }

    private String getTypeCode(String typeName) {
        // Mapear nombres de UI a códigos del backend
        switch (typeName) {
            case "Pintura": return "paint";
            case "Gypsum": return "gypsum";
            case "Empaste": return "empaste";
            case "Cintas LED": return "led_strip";
            case "Perfiles": return "profiles";
            case "Cables": return "cable";
            default: return typeName.toLowerCase();
        }
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
            typeDropdown.setEnabled(true);
            binding.layoutTypeDropdown.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        detailsAdapter = new CalculationDetailsAdapter();
        rvCalculationDetails.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCalculationDetails.setAdapter(detailsAdapter);
    }

    private void createDynamicForm() {
        clearDynamicForm();

        if (selectedType.isEmpty()) return;

        // Mostrar formulario
        cardForm.setVisibility(View.VISIBLE);
        actionButtonsLayout.setVisibility(View.VISIBLE);

        // Actualizar título del formulario
        binding.tvFormTitle.setText("Calculadora de " + selectedType);

        // Crear campos según la configuración
        Map<String, FieldConfig> config = formConfigurations.get(selectedType);
        if (config != null) {
            for (Map.Entry<String, FieldConfig> entry : config.entrySet()) {
                String fieldKey = entry.getKey();
                FieldConfig fieldConfig = entry.getValue();

                if (fieldConfig.type.equals("choice") && fieldConfig.choices != null) {
                    addDropdownField(fieldKey, fieldConfig);
                } else {
                    addTextInputField(fieldKey, fieldConfig);
                }
            }
        }
    }

    private void addDropdownField(String key, FieldConfig config) {
        // Crear TextInputLayout con estilo de dropdown
        TextInputLayout textInputLayout = new TextInputLayout(requireContext(), null,
                com.google.android.material.R.attr.textInputStyle);
        textInputLayout.setHint(config.label);
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setEndIconMode(TextInputLayout.END_ICON_DROPDOWN_MENU);

        // Configurar parámetros de layout
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.bottomMargin = dpToPx(16);
        textInputLayout.setLayoutParams(layoutParams);

        // Crear AutoCompleteTextView configurado como dropdown
        AutoCompleteTextView dropdown = new AutoCompleteTextView(requireContext());
        dropdown.setHint(config.label);
        dropdown.setInputType(android.text.InputType.TYPE_NULL); // No editable
        dropdown.setKeyListener(null); // Prevenir edición
        dropdown.setFocusable(false); // No focuseable para evitar teclado
        dropdown.setClickable(true); // Pero clickeable para dropdown

        // Configurar adapter con opciones
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                config.choices
        );
        dropdown.setAdapter(adapter);
        dropdown.setThreshold(1);

        // Hacer que se comporte como un verdadero dropdown
        dropdown.setOnClickListener(v -> dropdown.showDropDown());
        dropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                dropdown.showDropDown();
            }
        });

        // Agregar al layout
        textInputLayout.addView(dropdown);
        formContainer.addView(textInputLayout);

        // Guardar referencia
        formFields.put(key, dropdown);
    }

    private void addTextInputField(String key, FieldConfig config) {
        // Crear TextInputLayout
        TextInputLayout textInputLayout = new TextInputLayout(requireContext());
        textInputLayout.setHint(config.label);
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        // Configurar parámetros de layout
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.bottomMargin = dpToPx(16);
        textInputLayout.setLayoutParams(layoutParams);

        // Crear TextInputEditText
        TextInputEditText editText = new TextInputEditText(requireContext());
        editText.setHint(config.label);

        // Configurar tipo de entrada
        switch (config.type) {
            case "decimal":
                editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case "integer":
                editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                break;
            case "text":
            default:
                editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                break;
        }

        // Agregar al layout
        textInputLayout.addView(editText);
        formContainer.addView(textInputLayout);

        // Guardar referencia
        formFields.put(key, editText);
    }

    private void setupButtons() {
        binding.btnCalculate.setOnClickListener(v -> performCalculation());
        binding.btnClear.setOnClickListener(v -> clearForm());
        binding.btnAddToBudget.setOnClickListener(v -> showAddToBudgetDialog());
    }

    private void performCalculation() {
        if (!validateForm()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recopilar datos del formulario
        Map<String, Object> formData = new HashMap<>();

        for (Map.Entry<String, Object> entry : formFields.entrySet()) {
            String key = entry.getKey();
            Object fieldComponent = entry.getValue();
            String value = "";

            if (fieldComponent instanceof TextInputEditText) {
                value = ((TextInputEditText) fieldComponent).getText().toString().trim();
            } else if (fieldComponent instanceof AutoCompleteTextView) {
                value = ((AutoCompleteTextView) fieldComponent).getText().toString().trim();
            }

            if (!value.isEmpty()) {
                // Convertir según el tipo esperado
                Map<String, FieldConfig> config = formConfigurations.get(selectedType);
                if (config != null && config.containsKey(key)) {
                    FieldConfig fieldConfig = config.get(key);
                    switch (fieldConfig.type) {
                        case "decimal":
                            try {
                                formData.put(key, Double.parseDouble(value));
                            } catch (NumberFormatException e) {
                                formData.put(key, value);
                            }
                            break;
                        case "integer":
                            try {
                                formData.put(key, Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                                formData.put(key, value);
                            }
                            break;
                        default:
                            formData.put(key, value);
                            break;
                    }
                } else {
                    formData.put(key, value);
                }
            }
        }

        // Realizar cálculo con el backend
        calculadoraViewModel.performCalculation(selectedTypeCode, formData, projectId);
    }

    private boolean validateForm() {
        boolean isValid = true;
        Map<String, FieldConfig> config = formConfigurations.get(selectedType);

        for (Map.Entry<String, Object> entry : formFields.entrySet()) {
            String key = entry.getKey();
            Object fieldComponent = entry.getValue();
            String value = "";

            if (fieldComponent instanceof TextInputEditText) {
                TextInputEditText editText = (TextInputEditText) fieldComponent;
                value = editText.getText().toString().trim();

                // Validar campos obligatorios
                if (config != null && config.containsKey(key)) {
                    FieldConfig fieldConfig = config.get(key);
                    if (fieldConfig.required && value.isEmpty()) {
                        editText.setError("Campo obligatorio");
                        isValid = false;
                    } else {
                        editText.setError(null);
                    }
                }
            } else if (fieldComponent instanceof AutoCompleteTextView) {
                AutoCompleteTextView dropdown = (AutoCompleteTextView) fieldComponent;
                value = dropdown.getText().toString().trim();

                // Validar dropdowns obligatorios
                if (config != null && config.containsKey(key)) {
                    FieldConfig fieldConfig = config.get(key);
                    if (fieldConfig.required && value.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Por favor selecciona una opción para: " + fieldConfig.label,
                                Toast.LENGTH_SHORT).show();
                        isValid = false;
                    }
                }
            }
        }

        return isValid;
    }

    private void displayResult(CalculationResponse result) {
        currentCalculation = result;

        // Mostrar resultado principal
        binding.tvResultQuantity.setText(result.getFormattedQuantity());
        binding.tvResultCost.setText(result.getFormattedCost());

        // Mostrar cards de resultado
        cardResult.setVisibility(View.VISIBLE);
        cardDetails.setVisibility(View.VISIBLE);

        // Poblar detalles específicos
        populateDetails(result);
    }

    private void populateDetails(CalculationResponse result) {
        List<CalculationDetailsAdapter.DetailItem> details = new ArrayList<>();

        // Agregar detalles específicos
        if (result.getSpecificDetails() != null) {
            for (Map.Entry<String, Object> entry : result.getSpecificDetails().entrySet()) {
                String label = formatDetailLabel(entry.getKey());
                String value = formatDetailValue(entry.getKey(), entry.getValue());
                details.add(new CalculationDetailsAdapter.DetailItem(label, value));
            }
        }

        // Agregar resultados detallados
        if (result.getDetailedResults() != null) {
            for (Map.Entry<String, Object> entry : result.getDetailedResults().entrySet()) {
                String label = formatDetailLabel(entry.getKey());
                String value = formatDetailValue(entry.getKey(), entry.getValue());
                details.add(new CalculationDetailsAdapter.DetailItem(label, value));
            }
        }

        detailsAdapter.updateDetails(details);
    }

    private String formatDetailLabel(String key) {
        return key.replace("_", " ")
                .substring(0, 1).toUpperCase() +
                key.replace("_", " ").substring(1);
    }

    private String formatDetailValue(String key, Object value) {
        if (value == null) return "N/A";

        if (value instanceof Number) {
            double numValue = ((Number) value).doubleValue();

            if (key.contains("area")) {
                return String.format("%.2f m²", numValue);
            } else if (key.contains("length") || key.contains("meters")) {
                return String.format("%.2f m", numValue);
            } else if (key.contains("cost") || key.contains("price")) {
                return String.format("$%.2f", numValue);
            } else if (key.contains("percentage") || key.contains("factor")) {
                return String.format("%.1f%%", numValue * 100);
            } else {
                return String.format("%.2f", numValue);
            }
        }

        return value.toString();
    }

    private void showAddToBudgetDialog() {
        if (currentCalculation == null) {
            Toast.makeText(requireContext(), "No hay cálculo para agregar", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Añadir al Presupuesto")
                .setMessage("¿Deseas agregar este cálculo al presupuesto inicial del proyecto?\n\n" +
                        "Cantidad: " + currentCalculation.getFormattedQuantity() + "\n" +
                        "Costo estimado: " + currentCalculation.getFormattedCost())
                .setPositiveButton("Añadir", (dialog, which) -> {
                    calculadoraViewModel.addCalculationToBudget(
                            currentCalculation.getCalculationId(),
                            null, null, "Agregado desde calculadora"
                    );
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void observeViewModel() {
        // Observar resultado del cálculo
        calculadoraViewModel.getCalculationResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                displayResult(result);
            }
        });

        // Observar errores
        calculadoraViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        // Observar estado de carga
        calculadoraViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.btnCalculate.setEnabled(!isLoading);
                binding.btnClear.setEnabled(!isLoading);

                if (isLoading) {
                    binding.btnCalculate.setText("Calculando...");
                } else {
                    binding.btnCalculate.setText("Calcular");
                }
            }
        });

        // Observar si fue agregado al presupuesto
        calculadoraViewModel.getIsAddedToBudget().observe(getViewLifecycleOwner(), isAdded -> {
            if (isAdded != null && isAdded) {
                Toast.makeText(requireContext(), "¡Agregado al presupuesto exitosamente!", Toast.LENGTH_SHORT).show();
                binding.btnAddToBudget.setEnabled(false);
                binding.btnAddToBudget.setText("Ya agregado");
            }
        });
    }

    private void clearForm() {
        for (Object fieldComponent : formFields.values()) {
            if (fieldComponent instanceof TextInputEditText) {
                TextInputEditText editText = (TextInputEditText) fieldComponent;
                editText.setText("");
                editText.setError(null);
            } else if (fieldComponent instanceof AutoCompleteTextView) {
                AutoCompleteTextView dropdown = (AutoCompleteTextView) fieldComponent;
                dropdown.setText("", false);
            }
        }
        hideFormAndResults();
        calculadoraViewModel.clearResults();
    }

    private void clearDynamicForm() {
        // Remover campos dinámicos (mantener solo el título)
        int childCount = formContainer.getChildCount();
        for (int i = childCount - 1; i >= 1; i--) {
            formContainer.removeViewAt(i);
        }
        formFields.clear();
    }

    private void hideFormAndResults() {
        cardForm.setVisibility(View.GONE);
        cardResult.setVisibility(View.GONE);
        cardDetails.setVisibility(View.GONE);
        actionButtonsLayout.setVisibility(View.GONE);
        detailsAdapter.clearDetails();
        currentCalculation = null;
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

    // Clase auxiliar para configuración de campos
    private static class FieldConfig {
        String type;
        String label;
        boolean required;
        String[] choices;

        FieldConfig(String type, String label, boolean required) {
            this.type = type;
            this.label = label;
            this.required = required;
        }

        FieldConfig(String type, String label, boolean required, String[] choices) {
            this.type = type;
            this.label = label;
            this.required = required;
            this.choices = choices;
        }
    }
}