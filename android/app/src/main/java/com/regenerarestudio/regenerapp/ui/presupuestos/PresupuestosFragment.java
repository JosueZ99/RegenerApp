package com.regenerarestudio.regenerapp.ui.presupuestos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import com.regenerarestudio.regenerapp.MainActivity;
import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.databinding.FragmentPresupuestosBinding;

// IMPORTS CORREGIDOS Y AGREGADOS
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.regenerarestudio.regenerapp.data.models.BudgetItemCreateUpdateRequest;
import com.regenerarestudio.regenerapp.data.models.Supplier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.text.NumberFormat;

/**
 * Fragment del Sistema de Presupuestos con tablas interactivas
 * VERSIÓN CORREGIDA CON CACHÉ - Solucionado timing de ViewPager2
 */
public class PresupuestosFragment extends Fragment {

    private static final String TAG = "PresupuestosFragment";

    private FragmentPresupuestosBinding binding;
    private PresupuestosViewModel presupuestosViewModel;

    // Información del proyecto
    private long projectId;
    private String projectName;

    // Adapter para ViewPager
    private BudgetPagerAdapter pagerAdapter;

    // Referencias a fragments (solución al problema de timing)
    private BudgetInitialTableFragment budgetInitialFragment;
    private ExpensesRealTableFragment expensesRealFragment;

    // CACHE para datos cuando los fragments no están listos (NUEVA SOLUCIÓN)
    private List<Map<String, Object>> cachedBudgetData;
    private List<Map<String, Object>> cachedExpensesData;

    // Formatters
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPresupuestosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "===== onViewCreated INICIANDO =====");

        // Inicializar ViewModel
        presupuestosViewModel = new ViewModelProvider(this).get(PresupuestosViewModel.class);
        Log.d(TAG, "onViewCreated - ViewModel inicializado: " + (presupuestosViewModel != null));

        // Obtener datos del proyecto desde MainActivity
        getProjectDataFromActivity();
        Log.d(TAG, "onViewCreated - Proyecto obtenido: ID=" + projectId + ", Nombre=" + projectName);

        // Configurar componentes de la UI
        Log.d(TAG, "onViewCreated - Configurando ViewPager...");
        setupViewPager();

        Log.d(TAG, "onViewCreated - Configurando FAB buttons...");
        setupFabButtons();

        Log.d(TAG, "onViewCreated - Configurando observers...");
        observeViewModel();

        // Cargar datos iniciales
        Log.d(TAG, "onViewCreated - Iniciando carga de datos...");
        loadAllBudgetData();

        Log.d(TAG, "onViewCreated - PresupuestosFragment configurado correctamente");
        Log.d(TAG, "===== onViewCreated FINALIZADO =====");
    }

    // ==========================================
    // CONFIGURACIÓN DE LA UI
    // ==========================================

    /**
     * Obtener datos del proyecto desde MainActivity
     */
    private void getProjectDataFromActivity() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();

            projectId = activity.getSelectedProjectId();
            projectName = activity.getSelectedProjectName();

            Log.d(TAG, "Datos del proyecto obtenidos - ID: " + projectId + ", Nombre: " + projectName);

            if (projectId <= 0) {
                Log.e(TAG, "Error: No hay proyecto seleccionado");
                showError("Error: No hay proyecto seleccionado");
                return;
            }
        } else {
            Log.e(TAG, "Error: Activity no es instanceof MainActivity");
            showError("Error: No se pudo obtener datos del proyecto");
        }
    }

    /**
     * Configurar ViewPager2 y TabLayout para las dos tablas
     */
    private void setupViewPager() {
        Log.d(TAG, "Configurando ViewPager y TabLayout");

        // Crear adapter
        pagerAdapter = new BudgetPagerAdapter(this);
        binding.viewPagerBudget.setAdapter(pagerAdapter);

        // Conectar TabLayout con ViewPager2
        new TabLayoutMediator(binding.tabLayoutBudget, binding.viewPagerBudget,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(getString(R.string.presupuesto_inicial));
                            break;
                        case 1:
                            tab.setText(getString(R.string.gastos_reales));
                            break;
                    }
                }).attach();

        // Listener para cambios de tab
        binding.tabLayoutBudget.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                updateFabVisibility(tab.getPosition());
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                // No necesario
            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                // No necesario
            }
        });

        // Configurar visibilidad inicial de FABs
        updateFabVisibility(0);
    }

    /**
     * Configurar Floating Action Buttons
     */
    private void setupFabButtons() {
        Log.d(TAG, "Configurando FAB buttons");

        // FAB Agregar Item
        binding.fabAddItem.setOnClickListener(v -> {
            int currentTab = binding.viewPagerBudget.getCurrentItem();
            boolean isExpense = currentTab == 1; // Tab 1 = Gastos Reales
            showManualEntryForm(isExpense);
        });

        // FAB Copiar a Gastos Reales
        binding.fabCopyToExpenses.setOnClickListener(v -> showCopyConfirmationDialog());

        // Mostrar FABs
        binding.layoutFabActions.setVisibility(View.VISIBLE);
    }

    /**
     * Actualizar visibilidad de FABs según el tab seleccionado
     */
    private void updateFabVisibility(int tabPosition) {
        if (tabPosition == 0) {
            // Presupuesto Inicial: mostrar FAB copiar
            binding.fabCopyToExpenses.setVisibility(View.VISIBLE);
            binding.fabAddItem.setImageResource(R.drawable.ic_add_24);
        } else {
            // Gastos Reales: ocultar FAB copiar
            binding.fabCopyToExpenses.setVisibility(View.GONE);
            binding.fabAddItem.setImageResource(R.drawable.ic_add_24);
        }
    }

    /**
     * Cargar todos los datos de presupuestos del backend
     */
    private void loadAllBudgetData() {
        Log.d(TAG, "===== loadAllBudgetData INICIANDO =====");

        if (projectId <= 0) {
            Log.e(TAG, "loadAllBudgetData - ERROR: ID de proyecto inválido: " + projectId);
            showError("Error: ID de proyecto inválido");
            return;
        }

        Log.d(TAG, "loadAllBudgetData - Validación passed, llamando ViewModel...");
        Log.d(TAG, "loadAllBudgetData - ProjectId: " + projectId);
        Log.d(TAG, "loadAllBudgetData - ViewModel: " + (presupuestosViewModel != null ? "OK" : "NULL"));

        presupuestosViewModel.loadAllBudgetData(projectId);

        Log.d(TAG, "loadAllBudgetData - Llamada al ViewModel completada");
        Log.d(TAG, "===== loadAllBudgetData FINALIZADO =====");
    }

    /**
     * Observar todos los LiveData del ViewModel - VERSIÓN CORREGIDA CON CACHÉ
     */
    private void observeViewModel() {
        Log.d(TAG, "Configurando observadores del ViewModel");

        // Observar presupuesto inicial - VERSIÓN CON CACHÉ
        presupuestosViewModel.getBudgetInitial().observe(getViewLifecycleOwner(), budgetItems -> {
            Log.d(TAG, "Observer: Presupuesto inicial recibido - Items: " +
                    (budgetItems != null ? budgetItems.size() : 0));

            // Guardar en caché
            cachedBudgetData = budgetItems;

            // Intentar enviar inmediatamente
            if (budgetInitialFragment != null) {
                Log.d(TAG, "Observer: Enviando datos a BudgetInitialTableFragment (referencia directa)");
                budgetInitialFragment.updateBudgetData(budgetItems);
            } else {
                Log.d(TAG, "Observer: budgetInitialFragment es null, datos guardados en caché");
            }
        });

        // Observar gastos reales - VERSIÓN CON CACHÉ
        presupuestosViewModel.getExpensesReal().observe(getViewLifecycleOwner(), expenses -> {
            Log.d(TAG, "Observer: Gastos reales recibidos - Items: " +
                    (expenses != null ? expenses.size() : 0));

            if (expenses != null && !expenses.isEmpty()) {
                Log.d(TAG, "Observer: Primer gasto real = " + expenses.get(0).toString());
            }

            // Guardar en caché
            cachedExpensesData = expenses;

            // Intentar enviar inmediatamente
            if (expensesRealFragment != null) {
                Log.d(TAG, "Observer: Enviando " + (expenses != null ? expenses.size() : 0) +
                        " gastos a ExpensesRealTableFragment (referencia directa)");
                expensesRealFragment.updateExpensesData(expenses);
            } else {
                Log.d(TAG, "Observer: expensesRealFragment es null, datos guardados en caché");
            }
        });

        // Observar estados de carga
        presupuestosViewModel.getIsLoadingBudget().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "Observer: Estado de carga presupuesto - " + isLoading);
            updateLoadingState(isLoading);
        });

        presupuestosViewModel.getIsLoadingExpenses().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "Observer: Estado de carga gastos - " + isLoading);
            updateLoadingState(isLoading);
        });

        // Observar errores
        presupuestosViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Observer: Error recibido - " + error);
                showError(error);
            } else {
                Log.d(TAG, "Observer: No hay errores");
            }
        });
    }

    // ==========================================
    // MÉTODOS NUEVOS PARA CACHÉ (SOLUCIÓN AL TIMING)
    // ==========================================

    /**
     * Enviar datos cacheados al fragment de presupuesto inicial
     */
    private void sendCachedDataToBudgetFragment() {
        if (budgetInitialFragment != null && cachedBudgetData != null) {
            // Usar un Handler para asegurar que el fragment esté completamente inicializado
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (budgetInitialFragment != null && cachedBudgetData != null) {
                    Log.d(TAG, "Enviando datos cacheados a BudgetInitialTableFragment: " +
                            cachedBudgetData.size() + " items");
                    budgetInitialFragment.updateBudgetData(cachedBudgetData);
                }
            }, 100);
        }
    }

    /**
     * Enviar datos cacheados al fragment de gastos reales
     */
    private void sendCachedDataToExpensesFragment() {
        if (expensesRealFragment != null && cachedExpensesData != null) {
            // Usar un Handler para asegurar que el fragment esté completamente inicializado
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (expensesRealFragment != null && cachedExpensesData != null) {
                    Log.d(TAG, "Enviando datos cacheados a ExpensesRealTableFragment: " +
                            cachedExpensesData.size() + " items");
                    expensesRealFragment.updateExpensesData(cachedExpensesData);
                }
            }, 100);
        }
    }

    // ==========================================
    // MÉTODOS PÚBLICOS (EXPONER FUNCIONALIDAD)
    // ==========================================

    /**
     * Exponer ViewModel para fragments hijos
     */
    public PresupuestosViewModel getPresupuestosViewModel() {
        return presupuestosViewModel;
    }

    /**
     * Método público para mostrar formulario (llamado desde BudgetInitialTableFragment)
     */
    public void showManualEntryForm(boolean isExpense, BudgetItemCreateUpdateRequest editItem) {
        Log.d(TAG, "showManualEntryForm PUBLIC - isExpense: " + isExpense + ", editItem: " + (editItem != null ? "Editar" : "Nuevo"));
        showManualEntryFormInternal(isExpense, editItem);
    }

    // ==========================================
    // IMPLEMENTACIÓN DEL FORMULARIO MANUAL
    // ==========================================

    /**
     * Mostrar formulario manual para añadir/editar items de presupuesto
     * @param isExpense true si es para gastos reales, false para presupuesto inicial
     */
    private void showManualEntryForm(boolean isExpense) {
        showManualEntryFormInternal(isExpense, null);
    }

    /**
     * Mostrar formulario manual para añadir/editar items de presupuesto (SOBRECARGADO)
     * @param isExpense true si es para gastos reales, false para presupuesto inicial
     * @param editItem item a editar (null si es para crear nuevo)
     */
    private void showManualEntryFormInternal(boolean isExpense, BudgetItemCreateUpdateRequest editItem) {
        Log.d(TAG, "showManualEntryFormInternal - isExpense: " + isExpense + ", editItem: " + (editItem != null ? "Editar" : "Nuevo"));

        // Crear layout del dialog
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_budget_manual_entry, null);

        // Referencias a los widgets del formulario
        TextInputEditText etDescription = dialogView.findViewById(R.id.et_description);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.et_quantity);
        TextInputEditText etUnitPrice = dialogView.findViewById(R.id.et_unit_price);
        TextInputEditText etSpaces = dialogView.findViewById(R.id.et_spaces);
        AutoCompleteTextView spnCategory = dialogView.findViewById(R.id.spn_category);
        AutoCompleteTextView spnSupplier = dialogView.findViewById(R.id.spn_supplier);
        AutoCompleteTextView spnUnit = dialogView.findViewById(R.id.spn_unit);

        // Campos específicos para gastos reales
        TextInputEditText etDiscount = dialogView.findViewById(R.id.et_discount);
        TextInputEditText etPurchaseDate = dialogView.findViewById(R.id.et_purchase_date);
        LinearLayout layoutExpenseFields = dialogView.findViewById(R.id.layout_expense_fields);

        // Mostrar/ocultar campos específicos para gastos
        layoutExpenseFields.setVisibility(isExpense ? View.VISIBLE : View.GONE);

        // Configurar dropdowns
        setupCategoryDropdown(spnCategory);
        setupSupplierDropdown(spnSupplier);
        setupUnitDropdown(spnUnit);

        // Configurar date picker para gastos reales
        if (isExpense) {
            setupDatePicker(etPurchaseDate);
        }

        // Si es edición, precargar datos
        if (editItem != null) {
            etDescription.setText(editItem.getDescription());
            etQuantity.setText(String.valueOf(editItem.getQuantity()));
            etUnitPrice.setText(String.valueOf(editItem.getUnitPrice()));
            etSpaces.setText(editItem.getSpaces());
            spnCategory.setText(editItem.getCategory(), false);
            spnUnit.setText(editItem.getUnit(), false);

            // Buscar y establecer proveedor
            if (editItem.getSupplierId() != null) {
                loadSupplierById(editItem.getSupplierId(), supplier -> {
                    if (supplier != null) {
                        spnSupplier.setText(supplier.getName(), false);
                    }
                });
            }

            // Campos específicos para gastos
            if (isExpense && editItem.getDiscount() != null) {
                etDiscount.setText(String.valueOf(editItem.getDiscount()));
            }
            if (isExpense && editItem.getPurchaseDate() != null) {
                etPurchaseDate.setText(editItem.getPurchaseDate());
            }
        }

        // Crear dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle(editItem != null ?
                        (isExpense ? "Editar Gasto Real" : "Editar Item Presupuesto") :
                        (isExpense ? "Añadir Gasto Real" : "Añadir Item Presupuesto"))
                .setView(dialogView)
                .setPositiveButton(editItem != null ? "Actualizar" : "Añadir", null)
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Configurar botón positivo manualmente para validar
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                if (validateAndSubmitForm(isExpense, editItem, etDescription, etQuantity,
                        etUnitPrice, etSpaces, spnCategory, spnSupplier, spnUnit,
                        etDiscount, etPurchaseDate, dialog)) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    // ==========================================
    // CONFIGURACIÓN DE DROPDOWNS
    // ==========================================

    /**
     * Configurar dropdown de categorías
     */
    private void setupCategoryDropdown(AutoCompleteTextView dropdown) {
        String[] categories = {"Construcción", "Iluminación", "Eléctrico", "Acabados", "Otros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, categories);
        dropdown.setAdapter(adapter);
    }

    /**
     * Configurar dropdown de proveedores (carga desde ViewModel)
     */
    private void setupSupplierDropdown(AutoCompleteTextView dropdown) {
        // Cargar proveedores desde el ViewModel
        loadAllSuppliers(suppliers -> {
            if (suppliers != null && !suppliers.isEmpty()) {
                String[] supplierNames = suppliers.stream()
                        .map(Supplier::getName)
                        .toArray(String[]::new);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_dropdown_item_1line, supplierNames);
                dropdown.setAdapter(adapter);
            }
        });
    }

    /**
     * Configurar dropdown de unidades
     */
    private void setupUnitDropdown(AutoCompleteTextView dropdown) {
        String[] units = {"m²", "m", "litros", "galones", "kg", "unidad", "caja", "rollo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, units);
        dropdown.setAdapter(adapter);
    }

    /**
     * Configurar date picker para fecha de compra
     */
    private void setupDatePicker(TextInputEditText etDate) {
        etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Seleccionar fecha de compra")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                etDate.setText(sdf.format(new Date(selection)));
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });
    }

    // ==========================================
    // MÉTODOS HELPER PARA CONVERSIÓN
    // ==========================================

    /**
     * Convertir BudgetItemCreateUpdateRequest a Map para el ViewModel
     */
    private Map<String, Object> budgetRequestToMap(BudgetItemCreateUpdateRequest request) {
        Map<String, Object> map = new HashMap<>();

        if (request.getId() != null) {
            map.put("id", request.getId());
        }
        map.put("project", request.getProjectId());
        map.put("description", request.getDescription());
        map.put("quantity", request.getQuantity());
        map.put("unit_price", request.getUnitPrice());
        map.put("unit", request.getUnit());
        map.put("category", request.getCategory());
        map.put("spaces", request.getSpaces());
        map.put("supplier", request.getSupplierId());

        // Campos específicos para gastos reales
        if (request.getDiscount() != null) {
            map.put("discount", request.getDiscount());
        }
        if (request.getPurchaseDate() != null) {
            map.put("purchase_date", request.getPurchaseDate());
        }

        return map;
    }

    // ==========================================
    // VALIDACIÓN Y ENVÍO - VERSIÓN CORREGIDA
    // ==========================================

    /**
     * Validar formulario y enviar datos - VERSIÓN CORREGIDA
     */
    private boolean validateAndSubmitForm(boolean isExpense, BudgetItemCreateUpdateRequest editItem,
                                          TextInputEditText etDescription, TextInputEditText etQuantity,
                                          TextInputEditText etUnitPrice, TextInputEditText etSpaces,
                                          AutoCompleteTextView spnCategory, AutoCompleteTextView spnSupplier,
                                          AutoCompleteTextView spnUnit, TextInputEditText etDiscount,
                                          TextInputEditText etPurchaseDate, AlertDialog dialog) {

        // Validar campos obligatorios
        if (TextUtils.isEmpty(etDescription.getText())) {
            etDescription.setError("Descripción es obligatoria");
            return false;
        }

        if (TextUtils.isEmpty(etQuantity.getText())) {
            etQuantity.setError("Cantidad es obligatoria");
            return false;
        }

        if (TextUtils.isEmpty(etUnitPrice.getText())) {
            etUnitPrice.setError("Precio unitario es obligatorio");
            return false;
        }

        if (TextUtils.isEmpty(spnCategory.getText())) {
            spnCategory.setError("Categoría es obligatoria");
            return false;
        }

        if (TextUtils.isEmpty(spnSupplier.getText())) {
            spnSupplier.setError("Proveedor es obligatorio");
            return false;
        }

        try {
            // Obtener valores
            String description = etDescription.getText().toString().trim();
            double quantity = Double.parseDouble(etQuantity.getText().toString());
            double unitPrice = Double.parseDouble(etUnitPrice.getText().toString());
            String spaces = etSpaces.getText().toString().trim();
            String category = spnCategory.getText().toString();
            String supplierName = spnSupplier.getText().toString();
            String unit = spnUnit.getText().toString();

            // Validar valores numéricos
            if (quantity <= 0) {
                etQuantity.setError("Cantidad debe ser mayor a 0");
                return false;
            }

            if (unitPrice <= 0) {
                etUnitPrice.setError("Precio debe ser mayor a 0");
                return false;
            }

            // Campos específicos para gastos
            Double discount = null;
            String purchaseDate = null;

            if (isExpense) {
                if (!TextUtils.isEmpty(etDiscount.getText())) {
                    discount = Double.parseDouble(etDiscount.getText().toString());
                    if (discount < 0 || discount > 100) {
                        etDiscount.setError("Descuento debe estar entre 0 y 100%");
                        return false;
                    }
                }

                purchaseDate = etPurchaseDate.getText().toString().trim();
                if (TextUtils.isEmpty(purchaseDate)) {
                    etPurchaseDate.setError("Fecha de compra es obligatoria");
                    return false;
                }
            }

            // Crear objeto de request
            BudgetItemCreateUpdateRequest request = new BudgetItemCreateUpdateRequest();
            request.setProjectId(projectId);
            request.setDescription(description);
            request.setQuantity(quantity);
            request.setUnitPrice(unitPrice);
            request.setSpaces(spaces);
            request.setCategory(category);
            request.setUnit(unit);
            request.setDiscount(discount);
            request.setPurchaseDate(purchaseDate);

            // Por ahora usar proveedor fijo (TODO: implementar búsqueda real)
            request.setSupplierId(1L); // Proveedor por defecto

            // Convertir a Map para el ViewModel
            Map<String, Object> requestMap = budgetRequestToMap(request);

            // Enviar request usando los métodos correctos del ViewModel
            if (editItem != null) {
                // Actualizar item existente
                if (isExpense) {
                    // TODO: Implementar updateExpenseReal cuando esté disponible
                    presupuestosViewModel.addExpenseReal(requestMap);
                    showSuccessMessage("Gasto actualizado exitosamente");
                } else {
                    presupuestosViewModel.updateBudgetItem(editItem.getId(), requestMap);
                    showSuccessMessage("Item actualizado exitosamente");
                }
            } else {
                // Crear nuevo item
                if (isExpense) {
                    presupuestosViewModel.addExpenseReal(requestMap);
                    showSuccessMessage("Gasto añadido exitosamente");
                } else {
                    presupuestosViewModel.addItemToBudget(requestMap);
                    showSuccessMessage("Item añadido exitosamente");
                }
            }

            return true;

        } catch (NumberFormatException e) {
            showErrorMessage("Error: Revise los valores numéricos ingresados");
            return false;
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES PARA PROVEEDORES
    // ==========================================

    /**
     * Interface para callbacks de proveedor
     */
    public interface SupplierCallback {
        void onSupplierLoaded(Supplier supplier);
    }

    /**
     * Interface para callbacks de lista de proveedores
     */
    public interface SuppliersListCallback {
        void onSuppliersLoaded(List<Supplier> suppliers);
    }

    /**
     * Cargar todos los proveedores
     */
    private void loadAllSuppliers(SuppliersListCallback callback) {
        // TODO: Implementar llamada al ViewModel para obtener proveedores
        Log.d(TAG, "loadAllSuppliers: Implementar llamada al ViewModel");
        // Por ahora callback con lista vacía para evitar errores
        callback.onSuppliersLoaded(new ArrayList<>());
    }

    /**
     * Cargar proveedor por ID
     */
    private void loadSupplierById(Long supplierId, SupplierCallback callback) {
        // TODO: Implementar llamada al ViewModel para obtener proveedor por ID
        Log.d(TAG, "loadSupplierById: Implementar llamada al ViewModel para ID " + supplierId);
        // Por ahora callback con null para evitar errores
        callback.onSupplierLoaded(null);
    }

    /**
     * Cargar proveedor por nombre
     */
    private void loadSupplierByName(String supplierName, SupplierCallback callback) {
        // TODO: Implementar llamada al ViewModel para obtener proveedor por nombre
        Log.d(TAG, "loadSupplierByName: Implementar llamada al ViewModel para nombre " + supplierName);
        // Por ahora callback con null para evitar errores
        callback.onSupplierLoaded(null);
    }

    // ==========================================
    // MÉTODOS DE UI Y MANEJO DE EVENTOS
    // ==========================================

    /**
     * Actualizar estado de carga visual
     */
    private void updateLoadingState(boolean isLoading) {
        // Aquí puedes agregar indicadores de carga si es necesario
        Log.d(TAG, "Estado de carga actualizado: " + isLoading);
    }

    /**
     * Mostrar error al usuario
     */
    private void showError(String error) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, "Error mostrado al usuario: " + error);
    }

    /**
     * Mostrar confirmación para copiar presupuesto a gastos
     */
    private void showCopyConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Copiar Presupuesto")
                .setMessage("¿Desea copiar todos los items del presupuesto inicial a gastos reales?")
                .setPositiveButton(getString(R.string.btn_copiar_gastos), (dialog, which) -> {
                    copyBudgetToExpenses();
                })
                .setNegativeButton(getString(R.string.btn_cancelar), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void copyBudgetToExpenses() {
        // TODO: Implementar lógica para copiar presupuesto a gastos usando API
        Toast.makeText(requireContext(), "Función de copiado - Próximamente", Toast.LENGTH_SHORT).show();

        // Cambiar a la tab de gastos reales
        binding.viewPagerBudget.setCurrentItem(1, true);

        // Recargar datos
        refreshBudgetData();
    }

    // ==========================================
    // MÉTODOS AUXILIARES DE UI
    // ==========================================

    /**
     * Mostrar mensaje de éxito
     */
    private void showSuccessMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Mostrar mensaje de error
     */
    private void showErrorMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    // ==========================================
    // MÉTODOS PÚBLICOS
    // ==========================================

    /**
     * Método público para refrescar datos desde otras partes de la app
     */
    public void refreshBudgetData() {
        Log.d(TAG, "Refrescando datos de presupuestos");
        if (projectId > 0) {
            presupuestosViewModel.refreshAllData();
        } else {
            Log.w(TAG, "No se puede refrescar: no hay proyecto seleccionado");
        }
    }

    /**
     * Método para agregar item desde otras partes (ej: calculadora)
     */
    public void addItemFromCalculation(Map<String, Object> budgetItem) {
        Log.d(TAG, "Agregando item desde calculadora");
        presupuestosViewModel.addItemToBudget(budgetItem);
    }

    // ==========================================
    // MÉTODOS PÚBLICOS ADICIONALES PARA CRUD DE GASTOS REALES
    // ==========================================

    /**
     * Obtener ID del proyecto actual (para fragments hijos)
     * NUEVO - Necesario para ExpensesRealTableFragment
     */
    public long getProjectId() {
        return projectId;
    }

    // ==========================================
    // CICLO DE VIDA
    // ==========================================

    @Override
    public void onResume() {
        super.onResume();
        // Refrescar datos cuando el fragment vuelve a ser visible
        refreshBudgetData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Limpiar datos del ViewModel cuando se destruye la vista
        if (presupuestosViewModel != null) {
            presupuestosViewModel.clearData();
        }

        // Limpiar referencias y caché
        budgetInitialFragment = null;
        expensesRealFragment = null;
        cachedBudgetData = null;          // NUEVO - Limpiar caché
        cachedExpensesData = null;        // NUEVO - Limpiar caché

        binding = null;
        Log.d(TAG, "Vista destruida y datos limpiados");
    }

    // ==========================================
    // ADAPTER PARA VIEWPAGER - VERSIÓN CORREGIDA CON CACHÉ
    // ==========================================

    /**
     * Adapter para ViewPager2 que maneja las dos tablas de presupuesto
     * VERSIÓN CORREGIDA: Con notificación de creación de fragments + envío de caché
     */
    private class BudgetPagerAdapter extends FragmentStateAdapter {

        public BudgetPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Log.d(TAG, "BudgetPagerAdapter - createFragment position: " + position);

            switch (position) {
                case 0:
                    // Crear y guardar referencia al fragment de presupuesto inicial
                    budgetInitialFragment = new BudgetInitialTableFragment();
                    Log.d(TAG, "BudgetPagerAdapter - BudgetInitialTableFragment creado");

                    // Enviar datos desde caché si están disponibles
                    sendCachedDataToBudgetFragment();

                    return budgetInitialFragment;
                case 1:
                    // Crear y guardar referencia al fragment de gastos reales
                    expensesRealFragment = new ExpensesRealTableFragment();
                    Log.d(TAG, "BudgetPagerAdapter - ExpensesRealTableFragment creado");

                    // Enviar datos desde caché si están disponibles
                    sendCachedDataToExpensesFragment();

                    return expensesRealFragment;
                default:
                    Log.w(TAG, "BudgetPagerAdapter - Posición inválida: " + position);
                    budgetInitialFragment = new BudgetInitialTableFragment();
                    sendCachedDataToBudgetFragment();
                    return budgetInitialFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Presupuesto Inicial y Gastos Reales
        }
    }
}