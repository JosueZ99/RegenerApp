package com.regenerarestudio.regenerapp.ui.proveedores;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.regenerarestudio.regenerapp.R;
import com.regenerarestudio.regenerapp.databinding.FragmentProveedoresBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fragment de Gestión de Proveedores con búsqueda, filtros y acciones
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/ui/proveedores/ProveedoresFragment.java
 */
public class ProveedoresFragment extends Fragment {

    private FragmentProveedoresBinding binding;
    private ProveedoresViewModel proveedoresViewModel;

    // Adapter y datos
    private SuppliersAdapter adapter;
    private List<Supplier> suppliersList;
    private List<Supplier> filteredSuppliersList;

    // Estado de filtros
    private String currentSearchQuery = "";
    private String currentCategoryFilter = "todos";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProveedoresBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        proveedoresViewModel = new ViewModelProvider(this).get(ProveedoresViewModel.class);

        setupRecyclerView();
        setupSearchAndFilters();
        setupSorting();
        loadSuppliersData();
        observeViewModel();
    }

    private void setupRecyclerView() {
        suppliersList = new ArrayList<>();
        filteredSuppliersList = new ArrayList<>();

        adapter = new SuppliersAdapter(filteredSuppliersList,
                this::onSupplierClick,
                this::onCallSupplier,
                this::onWhatsAppSupplier,
                this::onViewCatalog
        );

        binding.rvSuppliers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSuppliers.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        // Configurar búsqueda en tiempo real
        binding.etSearchSuppliers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configurar filtros por categoría
        setupCategoryFilters();
    }

    private void setupCategoryFilters() {
        // Listener para chips de categoría
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chip_all_suppliers) {
                currentCategoryFilter = "todos";
            } else if (checkedId == R.id.chip_construction) {
                currentCategoryFilter = "construccion";
            } else if (checkedId == R.id.chip_lighting) {
                currentCategoryFilter = "iluminacion";
            } else if (checkedId == R.id.chip_electrical) {
                currentCategoryFilter = "electrico";
            } else if (checkedId == R.id.chip_finishes) {
                currentCategoryFilter = "acabados";
            }

            applyFilters();
        });
    }

    private void setupSorting() {
        binding.btnSortSuppliers.setOnClickListener(v -> showSortDialog());
    }

    private void showSortDialog() {
        String[] sortOptions = {
                "Mejor Rating",
                "Menor Distancia",
                "Nombre A-Z",
                "Más Reseñas",
                "Menor Tiempo de Entrega"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ordenar Proveedores")
                .setItems(sortOptions, (dialog, which) -> {
                    String selectedOption = sortOptions[which];
                    binding.btnSortSuppliers.setText(selectedOption);
                    applySorting(which);
                    dialog.dismiss();
                })
                .show();
    }

    private void applySorting(int sortType) {
        switch (sortType) {
            case 0: // Mejor Rating
                filteredSuppliersList.sort((s1, s2) -> Double.compare(s2.getRating(), s1.getRating()));
                break;
            case 1: // Menor Distancia
                filteredSuppliersList.sort((s1, s2) -> Double.compare(s1.getDistanceKm(), s2.getDistanceKm()));
                break;
            case 2: // Nombre A-Z
                filteredSuppliersList.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
                break;
            case 3: // Más Reseñas
                filteredSuppliersList.sort((s1, s2) -> Integer.compare(s2.getReviewsCount(), s1.getReviewsCount()));
                break;
            case 4: // Menor Tiempo de Entrega
                filteredSuppliersList.sort((s1, s2) -> s1.getDeliveryTime().compareToIgnoreCase(s2.getDeliveryTime()));
                break;
        }

        adapter.notifyDataSetChanged();
    }

    private void loadSuppliersData() {
        // Datos simulados - TODO: Obtener desde API
        suppliersList.clear();
        suppliersList.addAll(createSampleSuppliers());

        applyFilters();
        updateTotalCount();
    }

    private List<Supplier> createSampleSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();

        suppliers.add(new Supplier(
                1, "Pinturas Cóndor S.A.", "construccion",
                "Pinturas • Impermeabilizantes • Acabados",
                "Norte de Quito, 2.3 km", 4.5, 127,
                "24-48h", "5-8%", "12 meses", true,
                "0998765432", "https://wa.me/593998765432"
        ));

        suppliers.add(new Supplier(
                2, "LED Solutions Ecuador", "iluminacion",
                "Cintas LED • Perfiles • Controladores",
                "Centro de Quito, 1.8 km", 4.8, 89,
                "12-24h", "3-5%", "24 meses", true,
                "0987654321", "https://wa.me/593987654321"
        ));

        suppliers.add(new Supplier(
                3, "Gyplac Ecuador", "construccion",
                "Paneles Gypsum • Estructuras • Accesorios",
                "Sur de Quito, 4.2 km", 4.2, 156,
                "48-72h", "2-4%", "6 meses", false,
                "0976543210", "https://wa.me/593976543210"
        ));

        suppliers.add(new Supplier(
                4, "Perfiles Andinos", "iluminacion",
                "Perfiles Aluminio • Difusores • Tapas",
                "Valle de los Chillos, 8.5 km", 4.0, 73,
                "24-48h", "4-6%", "18 meses", true,
                "0965432109", "https://wa.me/593965432109"
        ));

        suppliers.add(new Supplier(
                5, "Cablesec", "electrico",
                "Cables • Conductores • Accesorios Eléctricos",
                "Norte de Quito, 3.1 km", 4.3, 94,
                "12-24h", "3-7%", "12 meses", true,
                "0954321098", "https://wa.me/593954321098"
        ));

        suppliers.add(new Supplier(
                6, "Materiales Quito", "construccion",
                "Empastes • Selladores • Herramientas",
                "Centro de Quito, 2.7 km", 4.1, 112,
                "24-48h", "4-8%", "6 meses", false,
                "0943210987", "https://wa.me/593943210987"
        ));

        return suppliers;
    }

    private void applyFilters() {
        filteredSuppliersList.clear();

        for (Supplier supplier : suppliersList) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    supplier.getName().toLowerCase().contains(currentSearchQuery) ||
                    supplier.getSpecialties().toLowerCase().contains(currentSearchQuery) ||
                    supplier.getLocation().toLowerCase().contains(currentSearchQuery);

            boolean matchesCategory = currentCategoryFilter.equals("todos") ||
                    supplier.getCategory().equals(currentCategoryFilter);

            if (matchesSearch && matchesCategory) {
                filteredSuppliersList.add(supplier);
            }
        }

        adapter.notifyDataSetChanged();
        updateTotalCount();
    }

    private void updateTotalCount() {
        binding.tvTotalSuppliers.setText(String.valueOf(filteredSuppliersList.size()));
    }

    // Callbacks del adapter
    private void onSupplierClick(Supplier supplier) {
        // TODO: Mostrar detalles completos del proveedor
        Toast.makeText(requireContext(), "Detalles de " + supplier.getName(), Toast.LENGTH_SHORT).show();
    }

    private void onCallSupplier(Supplier supplier) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + supplier.getPhoneNumber()));
            startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No se pudo realizar la llamada", Toast.LENGTH_SHORT).show();
        }
    }

    private void onWhatsAppSupplier(Supplier supplier) {
        try {
            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
            whatsappIntent.setData(Uri.parse(supplier.getWhatsappUrl()));
            startActivity(whatsappIntent);
        } catch (Exception e) {
            // Si no tiene WhatsApp, abrir navegador
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(supplier.getWhatsappUrl()));
                startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(requireContext(), "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onViewCatalog(Supplier supplier) {
        // TODO: Mostrar catálogo de productos del proveedor
        Toast.makeText(requireContext(), "Catálogo de " + supplier.getName() + " - Próximamente", Toast.LENGTH_SHORT).show();
    }

    private void observeViewModel() {
        // TODO: Implementar observadores cuando se conecte con el backend

        // Observar cambios en la lista de proveedores
        // proveedoresViewModel.getSuppliers().observe(getViewLifecycleOwner(), suppliers -> {
        //     updateSuppliersList(suppliers);
        // });

        // Observar errores
        // proveedoresViewModel.getError().observe(getViewLifecycleOwner(), error -> {
        //     if (error != null && !error.isEmpty()) {
        //         Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
        //     }
        // });
    }

    public void refreshSuppliers() {
        loadSuppliersData();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshSuppliers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Clase modelo para Proveedores
     */
    public static class Supplier {
        private long id;
        private String name;
        private String category;
        private String specialties;
        private String location;
        private double rating;
        private int reviewsCount;
        private String deliveryTime;
        private String discountRange;
        private String warranty;
        private boolean isOnline;
        private String phoneNumber;
        private String whatsappUrl;
        private double distanceKm;

        public Supplier(long id, String name, String category, String specialties,
                        String location, double rating, int reviewsCount, String deliveryTime,
                        String discountRange, String warranty, boolean isOnline,
                        String phoneNumber, String whatsappUrl) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.specialties = specialties;
            this.location = location;
            this.rating = rating;
            this.reviewsCount = reviewsCount;
            this.deliveryTime = deliveryTime;
            this.discountRange = discountRange;
            this.warranty = warranty;
            this.isOnline = isOnline;
            this.phoneNumber = phoneNumber;
            this.whatsappUrl = whatsappUrl;

            // Extraer distancia del location string
            try {
                String[] parts = location.split(", ");
                if (parts.length > 1) {
                    String distancePart = parts[1].replace(" km", "");
                    this.distanceKm = Double.parseDouble(distancePart);
                } else {
                    this.distanceKm = 0.0;
                }
            } catch (Exception e) {
                this.distanceKm = 0.0;
            }
        }

        // Getters
        public long getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getSpecialties() { return specialties; }
        public String getLocation() { return location; }
        public double getRating() { return rating; }
        public int getReviewsCount() { return reviewsCount; }
        public String getDeliveryTime() { return deliveryTime; }
        public String getDiscountRange() { return discountRange; }
        public String getWarranty() { return warranty; }
        public boolean isOnline() { return isOnline; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getWhatsappUrl() { return whatsappUrl; }
        public double getDistanceKm() { return distanceKm; }
    }
}