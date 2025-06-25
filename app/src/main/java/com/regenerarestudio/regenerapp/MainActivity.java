package com.regenerarestudio.regenerapp;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.core.view.GravityCompat;
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

    private ActivityMainBinding binding;
    private BottomNavigationView bottomNavigation;
    private boolean isProjectSelected = false;
    private String currentProjectName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        // CONFIGURACIÓN LIMPIA - Sin ActionBarDrawerToggle
        // La esquina izquierda queda libre para navegación futura

        // Configurar Bottom Navigation
        bottomNavigation = binding.appBarMain.bottomNavigation;
        setupBottomNavigation();

        // Configurar Navigation Drawer listener
        setupNavigationDrawer(binding.navView);

        // Configurar FAB
        binding.appBarMain.fab.setOnClickListener(v ->
                Snackbar.make(v, "Función rápida - Próximamente", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show()
        );

        // Cargar fragmento inicial (Proyectos)
        loadFragment(new ProyectosFragment());
        bottomNavigation.setSelectedItemId(R.id.navigation_proyectos);

        // Inicializar estado de navegación
        updateNavigationState();
        updateToolbarTitle();
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
                selectedFragment = new GalleryFragment();
                Toast.makeText(this, "Base de Datos de Materiales", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_exportacion) {
                selectedFragment = new SlideshowFragment();
                Toast.makeText(this, "Exportación", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_configuracion) {
                selectedFragment = new HomeFragment();
                Toast.makeText(this, "Configuración", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_ayuda) {
                Toast.makeText(this, "Ayuda", Toast.LENGTH_SHORT).show();
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (itemId == R.id.nav_acerca) {
                Toast.makeText(this, "Acerca de RegenerApp v1.0", Toast.LENGTH_SHORT).show();
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START);
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
        bottomNavigation.setSelectedItemId(R.id.navigation_proyectos);
    }

    private void updateNavigationState() {
        Menu menu = bottomNavigation.getMenu();

        menu.findItem(R.id.navigation_calculadora).setEnabled(isProjectSelected);
        menu.findItem(R.id.navigation_presupuestos).setEnabled(isProjectSelected);
        menu.findItem(R.id.navigation_dashboard).setEnabled(isProjectSelected);

        menu.findItem(R.id.navigation_proyectos).setEnabled(true);
        menu.findItem(R.id.navigation_proveedores).setEnabled(true);
    }

    public void setProjectSelected(boolean selected, String projectName) {
        this.isProjectSelected = selected;
        this.currentProjectName = projectName;
        updateNavigationState();
        updateToolbarTitle();

        if (selected) {
            Toast.makeText(this, "Proyecto seleccionado: " + projectName, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateToolbarTitle() {
        if (getSupportActionBar() != null) {
            if (isProjectSelected) {
                getSupportActionBar().setTitle("📁 " + currentProjectName);
            } else {
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }
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
        // Inflar menú con ícono hamburguesa en esquina derecha
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Manejar clic en ícono hamburguesa (esquina derecha)
        if (item.getItemId() == R.id.action_drawer_menu) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}