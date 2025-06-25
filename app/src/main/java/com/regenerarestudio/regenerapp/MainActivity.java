package com.regenerarestudio.regenerapp;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.regenerarestudio.regenerapp.databinding.ActivityMainBinding;
import com.regenerarestudio.regenerapp.ui.proyectos.ProyectosFragment;
import com.regenerarestudio.regenerapp.ui.calculadora.CalculadoraFragment;
import com.regenerarestudio.regenerapp.ui.presupuestos.PresupuestosFragment;
import com.regenerarestudio.regenerapp.ui.dashboard.DashboardFragment;
import com.regenerarestudio.regenerapp.ui.proveedores.ProveedoresFragment;
import com.regenerarestudio.regenerapp.ui.home.HomeFragment;
import com.regenerarestudio.regenerapp.ui.gallery.GalleryFragment;
import com.regenerarestudio.regenerapp.ui.slideshow.SlideshowFragment;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private BottomNavigationView bottomNavigation;
    private boolean isProjectSelected = false; // Estado del proyecto
    private String currentProjectName = ""; // Nombre del proyecto actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // Configurar FAB
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Función rápida - Próximamente", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });

        // Configurar Navigation Drawer
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_materiales, R.id.nav_exportacion, R.id.nav_configuracion,
                R.id.nav_ayuda, R.id.nav_acerca)
                .setOpenableLayout(drawer)
                .build();

        // Configurar Bottom Navigation
        bottomNavigation = binding.appBarMain.bottomNavigation;
        setupBottomNavigation();

        // Configurar Navigation Drawer listener
        setupNavigationDrawer(navigationView);

        // Cargar fragmento inicial (Proyectos)
        loadFragment(new ProyectosFragment());
        bottomNavigation.setSelectedItemId(R.id.navigation_proyectos);

        // Inicializar estado de navegación
        updateNavigationState();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_proyectos) {
                selectedFragment = new ProyectosFragment();
            } else if (itemId == R.id.navigation_calculadora) {
                if (!isProjectSelected) {
                    showProjectRequiredMessage();
                    return false;
                }
                selectedFragment = new CalculadoraFragment();
            } else if (itemId == R.id.navigation_presupuestos) {
                if (!isProjectSelected) {
                    showProjectRequiredMessage();
                    return false;
                }
                selectedFragment = new PresupuestosFragment();
            } else if (itemId == R.id.navigation_dashboard) {
                if (!isProjectSelected) {
                    showProjectRequiredMessage();
                    return false;
                }
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.navigation_proveedores) {
                selectedFragment = new ProveedoresFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void setupNavigationDrawer(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_materiales) {
                selectedFragment = new GalleryFragment(); // Placeholder
                Toast.makeText(this, "Base de Datos de Materiales", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_exportacion) {
                selectedFragment = new SlideshowFragment(); // Placeholder
                Toast.makeText(this, "Exportación", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_configuracion) {
                selectedFragment = new HomeFragment(); // Placeholder
                Toast.makeText(this, "Configuración", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_ayuda) {
                Toast.makeText(this, "Ayuda", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_acerca) {
                Toast.makeText(this, "Acerca de RegenerApp v1.0", Toast.LENGTH_SHORT).show();
                return true;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            binding.drawerLayout.closeDrawers();
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment_content_main, fragment);
        transaction.commit();
    }

    private void showProjectRequiredMessage() {
        Toast.makeText(this, getString(R.string.msg_seleccione_proyecto), Toast.LENGTH_LONG).show();
        // Cambiar automáticamente a la sección de proyectos
        bottomNavigation.setSelectedItemId(R.id.navigation_proyectos);
    }

    private void updateNavigationState() {
        Menu menu = bottomNavigation.getMenu();

        // Habilitar/deshabilitar elementos según el estado del proyecto
        menu.findItem(R.id.navigation_calculadora).setEnabled(isProjectSelected);
        menu.findItem(R.id.navigation_presupuestos).setEnabled(isProjectSelected);
        menu.findItem(R.id.navigation_dashboard).setEnabled(isProjectSelected);

        // Proyectos y Proveedores siempre habilitados
        menu.findItem(R.id.navigation_proyectos).setEnabled(true);
        menu.findItem(R.id.navigation_proveedores).setEnabled(true);
    }

    // Método público para que los fragmentos puedan cambiar el estado del proyecto
    public void setProjectSelected(boolean selected, String projectName) {
        this.isProjectSelected = selected;
        this.currentProjectName = projectName;
        updateNavigationState();

        if (selected) {
            Toast.makeText(this, "Proyecto seleccionado: " + projectName, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isProjectSelected() {
        return isProjectSelected;
    }

    public String getCurrentProjectName() {
        return currentProjectName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.nav_host_fragment_content_main), mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}