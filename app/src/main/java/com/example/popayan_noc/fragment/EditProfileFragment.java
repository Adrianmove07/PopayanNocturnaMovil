package com.example.popayan_noc.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.popayan_noc.service.ProfileApi;
import com.example.popayan_noc.R;
import com.example.popayan_noc.util.AuthUtils;

public class EditProfileFragment extends Fragment {
    private EditText etName, etEmail;
    private Button btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        etName = view.findViewById(R.id.etEditName);
        etEmail = view.findViewById(R.id.etEditEmail);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        // Cargar datos reales del usuario
        String token = AuthUtils.getToken(requireContext());
        ProfileApi.getProfile(requireContext(), token, response -> {
            etName.setText(response.optString("nombre", ""));
            etEmail.setText(response.optString("correo", ""));
        }, error -> {
            etName.setText("");
            etEmail.setText("");
            Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> {
            if (TextUtils.isEmpty(etName.getText()) || TextUtils.isEmpty(etEmail.getText())) {
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            // Guardar datos editados
            String nombre = etName.getText().toString();
            String correo = etEmail.getText().toString();
            org.json.JSONObject data = new org.json.JSONObject();
            try {
                data.put("nombre", nombre);
                data.put("correo", correo);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al preparar datos", Toast.LENGTH_SHORT).show();
                return;
            }
            btnSave.setEnabled(false);
            ProfileApi.updateProfile(requireContext(), token, data, response -> {
                Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                requireActivity().getSupportFragmentManager().popBackStack();
            }, error -> {
                Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
            });
        });
        return view;
    }
}
