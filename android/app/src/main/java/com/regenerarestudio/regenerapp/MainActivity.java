package com.regenerarestudio.regenerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.regenerarestudio.regenerapp.databinding.ActivityMainBinding;
import com.regenerarestudio.regenerapp.ui.calculadora.CalculadoraFragment;
import com.regenerarestudio.regenerapp.ui.presupuestos.PresupuestosFragment;
import com.regenerarestudio.regenerapp.ui.dashboard.DashboardFragment;
import com.regenerarestudio.regenerapp.ui.proveedores.ProveedoresFragment;
import com.regenerarestudio.regenerapp.ui.materiales.MaterialesFragment;
import com.regenerarestudio.regenerapp.ui.exportacion.ExportacionFragment;
import com.regenerarestudio.regenerapp.ui.configuracion.ConfiguracionFragment;
import com.regenerarestudio.regenerapp.ui.ayuda.AyudaFragment;
import com.regenerarestudio.regenerapp.ui.acercade.AcercaDeFragment;

/**
 * MainActivity actualizada - Ahora solo maneja la app principal después de seleccionar proyecto
 * Path: android/app/src/main/java/com/regenerarestudio/regenerapp/MainActivity.java
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BottomNavigationView bottomNavigation;

    // Información del proyecto seleccionado
    private long selectedProjectId = -1;
    private String selectedProjectName = "";

    private static final String PREFS_NAME = "RegenerAppPrefs";
    private static final String KEY_SELECTED_PROJECT_ID = "selected_project_id";
    private static final String KEY_SELECTED_PROJECT_NAME = "selected_project_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si hay proyecto seleccionado
        if (!checkProjectSelection()) {
            // Si no hay proyecto, ir a pantalla de selección
            goToProjectSelection();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        updateToolbarTitle();

        setupDrawerLayout();
        setupBottomNavigation();
        setupNavigationDrawer();

        // Cargar Dashboard por defecto (ya no Proyectos)
        loadFragment(new DashboardFragment());
        bottomNavigation.setSelectedItemId(R.id.navigation_dashboard);
    }

    private boolean checkProjectSelection() {
        // Primero verificar si viene desde Intent (selección reciente)
        Intent intent = getIntent();
        if (intent.hasExtra("project_id") && intent.hasExtra("project_name")) {
            selectedProjectId = intent.getLongExtra("project_id", -1);
            selectedProjectName = intent.getStringExtra("project_name");

            // Guardar en preferencias para futuras sesiones
            saveProjectSelection();
            return true;
        }

        // Si no viene desde Intent, verificar SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedProjectId = prefs.getLong(KEY_SELECTED_PROJECT_ID, -1);
        selectedProjectName = prefs.getString(KEY_SELECTED_PROJECT_NAME, "");

        return selectedProjectId != -1 && !selectedProjectName.isEmpty();
    }

    private void saveProjectSelection() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_SELECTED_PROJECT_ID, selectedProjectId);
        editor.putString(KEY_SELECTED_PROJECT_NAME, selectedProjectName);
        editor.apply();
    }

    private void goToProjectSelection() {
        Intent intent = new Intent(this, ProjectSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateToolbarTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("RegenerApp");
            getSupportActionBar().setSubtitle(selectedProjectName);
        }
    }

    private void setupDrawerLayout() {
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Configurar toggle del drawer desde toolbar
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                this, drawer, binding.appBarMain.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupBottomNavigation() {
        bottomNavigation = binding.appBarMain.bottomNavigation;

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // Todas las opciones están disponibles ya que el proyecto está seleccionado
            if (itemId == R.id.navigation_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.navigation_calculadora) {
                selectedFragment = new CalculadoraFragment();
            } else if (itemId == R.id.navigation_presupuestos) {
                selectedFragment = new PresupuestosFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void setupNavigationDrawer() {
        NavigationView navigationView = binding.navView;

        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_proveedores) {
                selectedFragment = new ProveedoresFragment();
                Toast.makeText(this, "Gestión de Proveedores", Toast.LENGTH_SHORT).show();

            } else if (itemId == R.id.nav_materiales) {
                selectedFragment = new MaterialesFragment();
                Toast.makeText(this, "Base de Datos de Materiales", Toast.LENGTH_SHORT).show();

            } else if (itemId == R.id.nav_exportacion) {
                selectedFragment = new ExportacionFragment();
                Toast.makeText(this, "Centro de Exportación", Toast.LENGTH_SHORT).show();

            } else if (itemId == R.id.nav_cambiar_proyecto) {
                showChangeProjectDialog();
                binding.drawerLayout.closeDrawers();
                return true;

            } else if (itemId == R.id.nav_configuracion) {
                selectedFragment = new ConfiguracionFragment();
                Toast.makeText(this, "Configuración", Toast.LENGTH_SHORT).show();

            } else if (itemId == R.id.nav_ayuda) {
                selectedFragment = new AyudaFragment();
                Toast.makeText(this, "Centro de Ayuda", Toast.LENGTH_SHORT).show();

            } else if (itemId == R.id.nav_acerca) {
                selectedFragment = new AcercaDeFragment();
                Toast.makeText(this, "Acerca de RegenerApp", Toast.LENGTH_SHORT).show();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            binding.drawerLayout.closeDrawers();
            return true;
        });
    }

    private void showChangeProjectDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cambiar Proyecto")
                .setMessage(getString(R.string.msg_cambiar_proyecto))
                .setPositiveButton(getString(R.string.btn_cambiar), (dialog, which) -> {
                    // Limpiar proyecto seleccionado
                    clearProjectSelection();
                    // Ir a selección de proyectos
                    goToProjectSelection();
                })
                .setNegativeButton(getString(R.string.btn_cancelar), (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void clearProjectSelection() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_SELECTED_PROJECT_ID);
        editor.remove(KEY_SELECTED_PROJECT_NAME);
        editor.apply();
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment_content_main, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = binding.drawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // En lugar de cerrar la app, mostrar diálogo de confirmación
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Salir de RegenerApp")
                    .setMessage("¿Estás seguro de que quieres salir de la aplicación?")
                    .setPositiveButton("Salir", (dialog, which) -> {
                        super.onBackPressed();
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    // Métodos públicos para que los fragmentos obtengan información del proyecto
    public long getSelectedProjectId() {
        return selectedProjectId;
    }

    public String getSelectedProjectName() {
        return selectedProjectName;
    }
}