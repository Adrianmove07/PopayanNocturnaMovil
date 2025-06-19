package com.example.popayan_noc;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        // Animación de aparición para la barra de navegación
        bottomNav.setVisibility(View.INVISIBLE);
        bottomNav.post(() -> {
            bottomNav.setVisibility(View.VISIBLE);
            bottomNav.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_nav));
        });
        // Cargar fragmento inicial
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.main, new HomeFragment())
            .commit();

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
                    R.anim.slide_in_right, // enter
                    R.anim.slide_out_left, // exit
                    R.anim.slide_in_right, // popEnter
                    R.anim.slide_out_left  // popExit
                )
                .replace(R.id.main, selectedFragment)
                .commit();
            return true;
        });
    }
}