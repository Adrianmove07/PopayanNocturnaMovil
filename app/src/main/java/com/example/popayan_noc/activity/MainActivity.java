package com.example.popayan_noc.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log; // Importar para depuración

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
import androidx.fragment.app.FragmentManager; // Importar FragmentManager
import androidx.fragment.app.FragmentTransaction; // Importar FragmentTransaction

import com.example.popayan_noc.R;
import com.example.popayan_noc.fragment.ExploreFragment;
import com.example.popayan_noc.fragment.FavoritesFragment;
import com.example.popayan_noc.fragment.HomeFragment;
import com.example.popayan_noc.fragment.UserFragment;
// Asegúrate de importar cualquier otro fragmento que vayas a usar, por ejemplo:
// import com.example.popayan_noc.fragment.SettingsFragment; // Si lo usas en el drawer
// import com.example.popayan_noc.fragment.ProfileFragment; // Si lo usas en el drawer

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject; // Para AuthUtils.getUser

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout; // Renombrado para consistencia
    private BottomNavigationView bottomNavigationView; // Renombrado para consistencia
    private Toolbar toolbar; // Hacemos el toolbar una variable de instancia
    private NavigationView navigationView; // Hacemos el navigationView una variable de instancia

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

        // 1. Inicializar vistas
        toolbar = findViewById(R.id.toolbar); // Asignar a la variable de instancia
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout); // Asignar a la variable de instancia
        navigationView = findViewById(R.id.nav_view); // Asignar a la variable de instancia
        navigationView.setNavigationItemSelectedListener(this); // Listener para el menú lateral

        // Configurar el ActionBarDrawerToggle (icono de hamburguesa)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bottomNavigationView = findViewById(R.id.bottom_navigation); // Asignar a la variable de instancia

        // Animación de aparición de la Bottom Navigation View
        bottomNavigationView.setVisibility(View.INVISIBLE);
        bottomNavigationView.post(() -> {
            bottomNavigationView.setVisibility(View.VISIBLE);
            bottomNavigationView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_nav));
        });

        // 2. Cargar información del usuario en el encabezado del Drawer (¡solo una vez!)
        updateNavHeader();

        // 3. Listener para la Bottom Navigation View
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // Llama al método helper para manejar la navegación del fragmento
            return handleFragmentNavigation(id, R.id.bottom_navigation);
        });

        // 4. Cargar el fragmento inicial al iniciar la actividad
        if (savedInstanceState == null) {
            // Selecciona el ítem Home en la BottomNav y permite que su listener cargue el fragmento
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            // Si quieres que el item "Home" en el drawer también esté marcado:
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    // Método helper para manejar la carga y las animaciones de fragmentos
    private boolean handleFragmentNavigation(int itemId, int sourceNavId) {
        Fragment selectedFragment = null;
        String toolbarTitle = ""; // Título por defecto

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
            toolbarTitle = "Inicio";
        } else if (itemId == R.id.nav_explore) {
            selectedFragment = new ExploreFragment();
            toolbarTitle = "Explorar";
        } else if (itemId == R.id.nav_favorites) {
            selectedFragment = new FavoritesFragment();
            toolbarTitle = "Favoritos";
        } else if (itemId == R.id.nav_user) {
            selectedFragment = new UserFragment();
            toolbarTitle = "Mi Perfil";
        }
        // Puedes añadir más casos para ítems del Drawer que no estén en la BottomNav
        else if (itemId == R.id.nav_gallery) {
            Toast.makeText(this, "Navegando a Galería", Toast.LENGTH_SHORT).show();
            // Si nav_gallery debe cargar un fragmento, descomenta y crea el fragmento:
            // selectedFragment = new GalleryFragment();
            // toolbarTitle = "Galería";
        }
        // ... otros ítems del drawer

        if (selectedFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Aplicar animaciones
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );

            // Reemplazar el fragmento en el contenedor
            fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
            fragmentTransaction.addToBackStack(null); // Permite volver atrás

            fragmentTransaction.commit();

            // Actualizar el título del Toolbar
            setToolbarTitle(toolbarTitle);

            // Sincronizar selección entre BottomNav y NavigationView
            if (sourceNavId == R.id.bottom_navigation) {
                // Si viene de la BottomNav, asegúrate de que el Drawer también esté marcado
                navigationView.setCheckedItem(itemId);
            } else if (sourceNavId == R.id.nav_view) {
                // Si viene del Drawer, intenta seleccionar en la BottomNav si el ítem existe allí
                // Esto previene errores si el ítem del Drawer no tiene un ID en la BottomNav
                if (bottomNavigationView.getMenu().findItem(itemId) != null) {
                    bottomNavigationView.setSelectedItemId(itemId);
                } else {
                    // Si el item del drawer no está en bottomNav, podrías querer deseleccionar BottomNav
                    // O dejarlo como estaba si no hay una correspondencia clara
                    Log.w("MainActivity", "El ítem del Drawer (" + getResources().getResourceEntryName(itemId) + ") no tiene correspondencia en BottomNavigationView.");
                }
            }
            return true;
        }
        return false; // No se manejó la selección
    }

    // Método público para que los fragmentos puedan actualizar el título del Toolbar
    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    // Método para actualizar la información del usuario en el encabezado del Drawer
    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderName = headerView.findViewById(R.id.tvNavHeaderName);
        TextView tvHeaderLastName = headerView.findViewById(R.id.tvNavHeaderLastname);
        TextView tvHeaderEmail = headerView.findViewById(R.id.tvNavHeaderEmail);

        JSONObject usuario = com.example.popayan_noc.util.AuthUtils.getUser(this);
        if (usuario != null) {
            String nombre = usuario.optString("nombre", "Invitado");
            String apellido = usuario.optString("apellido", ""); // Puedes dejarlo vacío si no hay por defecto
            String correo = usuario.optString("correo", "correo@ejemplo.com");
            tvHeaderName.setText(nombre);
            tvHeaderLastName.setText(apellido);
            tvHeaderEmail.setText(correo);
            Log.d("MainActivity", "Header del Drawer actualizado para: " + nombre);
        } else {
            tvHeaderName.setText("Invitado");
            tvHeaderLastName.setText("");
            tvHeaderEmail.setText("Inicia Sesión");
            Log.d("MainActivity", "Header del Drawer configurado para Invitado.");
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Si hay fragmentos en la pila de retroceso, pop uno
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
                // Opcional: Actualizar el título del toolbar al volver atrás si es necesario
                // Esto podría requerir una lógica más compleja o que los fragmentos en el stack
                // actualicen su propio título en onResume.
            } else {
                super.onBackPressed(); // Si no hay fragmentos en el stack, ejecuta la acción por defecto (salir)
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Llama al método helper para manejar la navegación del fragmento
        boolean handled = handleFragmentNavigation(id, R.id.nav_view);

        // Cierra el drawer después de la selección
        drawerLayout.closeDrawer(GravityCompat.START);
        return handled;
    }
}