package com.example.popayan_noc.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.popayan_noc.R;
import com.example.popayan_noc.fragment.ColaboradorDialogFragment;
import com.example.popayan_noc.fragment.ExploreFragment;
import com.example.popayan_noc.fragment.FavoritesFragment;
import com.example.popayan_noc.fragment.HomeFragment;
import com.example.popayan_noc.fragment.NotificationsFragment;
import com.example.popayan_noc.fragment.TodayDoFragment;
import com.example.popayan_noc.fragment.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private NavigationView navigationView;

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

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Configurar Toolbar como ActionBar

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Configurar el ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Animación de aparición de la Bottom Navigation View
        bottomNavigationView.setVisibility(View.INVISIBLE);
        bottomNavigationView.post(() -> {
            bottomNavigationView.setVisibility(View.VISIBLE);
            bottomNavigationView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_nav));
        });

        // Cargar información del usuario en el encabezado del Drawer
        updateNavHeader();

        // Listener para la Bottom Navigation View
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            return handleFragmentNavigation(id, R.id.bottom_navigation);
        });

        // Cargar el fragmento inicial
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_notifications) {
            // Abrir NotificationsFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_right, R.anim.slide_out_left
                    )
                    .replace(R.id.fragment_container, new NotificationsFragment())
                    .addToBackStack(null)
                    .commit();

            setToolbarTitle("Notificaciones");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean handleFragmentNavigation(int itemId, int sourceNavId) {
        Fragment selectedFragment = null;
        String toolbarTitle = "";

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
            toolbarTitle = "Inicio";
        } else if (itemId == R.id.nav_explore) {
            selectedFragment = new ExploreFragment();
            toolbarTitle = "Explorar";

        } else if (itemId == R.id.planea) {
            selectedFragment = new TodayDoFragment();
            toolbarTitle = "planear";

        } else if (itemId == R.id.nav_user) {
            selectedFragment = new UserFragment();
            toolbarTitle = "Mi Perfil";

        } else if (itemId == R.id.nav_gallery) {
            Toast.makeText(this, "Navegando a Galería", Toast.LENGTH_SHORT).show();
        }

        if (selectedFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );

            fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            setToolbarTitle(toolbarTitle);

            if (sourceNavId == R.id.bottom_navigation) {
                navigationView.setCheckedItem(itemId);
            } else if (sourceNavId == R.id.nav_view) {
                if (bottomNavigationView.getMenu().findItem(itemId) != null) {
                    bottomNavigationView.setSelectedItemId(itemId);
                } else {
                    Log.w("MainActivity", "Ítem del Drawer no encontrado en BottomNavigationView");
                }
            }
            return true;
        }
        return false;
    }

    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderName = headerView.findViewById(R.id.tvNavHeaderName);
        TextView tvHeaderLastName = headerView.findViewById(R.id.tvNavHeaderLastname);
        TextView tvHeaderEmail = headerView.findViewById(R.id.tvNavHeaderEmail);

        JSONObject usuario = com.example.popayan_noc.util.AuthUtils.getUser(this);
        if (usuario != null) {
            String nombre = usuario.optString("nombre", "Invitado");
            String apellido = usuario.optString("apellido", "");
            String correo = usuario.optString("correo", "correo@ejemplo.com");
            tvHeaderName.setText(nombre);
            tvHeaderLastName.setText(apellido);
            tvHeaderEmail.setText(correo);
        } else {
            tvHeaderName.setText("Invitado");
            tvHeaderLastName.setText("");
            tvHeaderEmail.setText("Inicia Sesión");
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            // Mostrar el modal
            ColaboradorDialogFragment dialog = new ColaboradorDialogFragment();
            dialog.show(getSupportFragmentManager(), "ColaboradorDialog");

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        // Para otros ítems del menú
        boolean handled = handleFragmentNavigation(id, R.id.nav_view);
        drawerLayout.closeDrawer(GravityCompat.START);
        return handled;
    }

}