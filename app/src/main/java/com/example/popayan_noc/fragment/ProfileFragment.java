package com.example.popayan_noc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.popayan_noc.R;
import com.example.popayan_noc.activity.LoginActivity;
import com.example.popayan_noc.util.AuthUtils;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnFavorites = view.findViewById(R.id.btnFavorites);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        // Botón condicional para propietarios
        Button btnOwnerPanel = null;
        boolean esPropietario = true; // TODO: Obtener rol real del usuario
        if (esPropietario) {
            btnOwnerPanel = new Button(getContext());
            btnOwnerPanel.setText("Panel de Propietario");
            ((ViewGroup) view).addView(btnOwnerPanel);
        }
        // Botón para reseñas
        Button btnReviews = new Button(getContext());
        btnReviews.setText("Ver Reseñas");
        ((ViewGroup) view).addView(btnReviews);
        // Botón para mapa
        Button btnMap = new Button(getContext());
        btnMap.setText("Explorar en Mapa");
        ((ViewGroup) view).addView(btnMap);

        // Mostrar datos reales del usuario desde SharedPreferences
        org.json.JSONObject usuario = AuthUtils.getUser(requireContext());
        if (usuario != null) {
            String nombre = usuario.optString("nombre", "-");
            String correo = usuario.optString("correo", "-");
            tvProfileName.setText(nombre);
            tvProfileEmail.setText(correo);
        } else {
            tvProfileName.setText("No disponible");
            tvProfileEmail.setText("");
        }

        // Listener para cerrar sesión
        btnLogout.setOnClickListener(v -> {
            AuthUtils.clearSession(requireContext());
            // Navegar a LoginActivity y limpiar el backstack
            android.content.Intent intent = new android.content.Intent(requireContext(), LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            // Navegar a edición de perfil
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new EditProfileFragment())
                .addToBackStack(null)
                .commit();
        });
        btnFavorites.setOnClickListener(v -> {
            // Navegar a favoritos
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new FavoritesFragment())
                .addToBackStack(null)
                .commit();
        });
        btnLogout.setOnClickListener(v -> {
            // Cerrar sesión: limpiar datos y volver a LoginActivity
            requireActivity().getSharedPreferences("user_data", 0).edit().clear().apply();
            android.content.Intent intent = new android.content.Intent(requireContext(), LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        if (btnOwnerPanel != null) {
            btnOwnerPanel.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new OwnerPanelFragment())
                    .addToBackStack(null)
                    .commit();
            });
        }
        if (btnOwnerPanel != null) {
            btnOwnerPanel.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, new OwnerPanelFragment())
                    .addToBackStack(null)
                    .commit();
            });
        }
        btnReviews.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new ReviewsFragment())
                .addToBackStack(null)
                .commit();
        });
        btnMap.setOnClickListener(v -> {
            // MapFragment ha sido eliminado. Puedes implementar aquí la navegación al HomeFragment (que contiene el mapa osmdroid), o mostrar un mensaje.
            // Ejemplo: navegar al HomeFragment (si es el que tiene el mapa)
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new HomeFragment())
                .addToBackStack(null)
                .commit();
            // O puedes mostrar un Toast:
            // Toast.makeText(getContext(), "La vista de mapa ha sido migrada. Encuentra el mapa en la pantalla principal", Toast.LENGTH_LONG).show();
        });
        return view;
    }
}
