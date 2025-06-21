package com.example.popayan_noc.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.popayan_noc.R;
import com.example.popayan_noc.fragment.ExploreFragment;
import com.example.popayan_noc.fragment.FavoritesFragment;
import com.example.popayan_noc.fragment.HomeFragment;
import com.example.popayan_noc.fragment.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Configuración de la Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // --- Configuración del Navigation Drawer ---
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // --- Configuración de la Bottom Navigation View ---
        bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setVisibility(View.INVISIBLE);
        bottomNav.post(() -> {
            bottomNav.setVisibility(View.VISIBLE);
            bottomNav.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_nav));
        });

        // Cargar el fragmento inicial solo si es la primera vez que se crea la actividad
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()) // <--- ¡CAMBIO IMPORTANTE AQUÍ!
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Listener para la Bottom Navigation View
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_explore) {
                selectedFragment = new ExploreFragment();
            } else if (id == R.id.nav_favorites) {
                selectedFragment = new FavoritesFragment();
            } else if (id == R.id.nav_user) {
                selectedFragment = new UserFragment();
            } else {
                selectedFragment = new HomeFragment();
            }

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                    )
                    .replace(R.id.fragment_container, selectedFragment) // <--- ¡CAMBIO IMPORTANTE AQUÍ!
                    .commit();

            navigationView.setCheckedItem(id);

            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment selectedFragment = null;

        if (id == R.id.nav_home) {
            selectedFragment = new HomeFragment();
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else if (id == R.id.nav_explore) {
            selectedFragment = new ExploreFragment();
            bottomNav.setSelectedItemId(R.id.nav_explore);
        } else if (id == R.id.nav_favorites) {
            selectedFragment = new FavoritesFragment();
            bottomNav.setSelectedItemId(R.id.nav_favorites);
        } else if (id == R.id.nav_user) {
            selectedFragment = new UserFragment();
            bottomNav.setSelectedItemId(R.id.nav_user);
        }
        else if (id == R.id.nav_gallery) {
            Toast.makeText(this, "Navegando a Galería", Toast.LENGTH_SHORT).show();
            // selectedFragment = new GalleryFragment();
        } else if (id == R.id.nav_slideshow) {
            Toast.makeText(this, "Navegando a Diapositivas", Toast.LENGTH_SHORT).show();
            // selectedFragment = new SlideshowFragment();
        } else if (id == R.id.nav_share) {
            Toast.makeText(this, "Compartir app (simulado)", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_send) {
            Toast.makeText(this, "Enviar feedback (simulado)", Toast.LENGTH_SHORT).show();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                    )
                    .replace(R.id.fragment_container, selectedFragment) // <--- ¡CAMBIO IMPORTANTE AQUÍ!
                    .commit();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}