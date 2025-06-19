package com.example.popayan_noc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.popayan_noc.R;
import com.example.popayan_noc.activity.LoginActivity;
import com.example.popayan_noc.model.User;
import com.example.popayan_noc.util.AuthUtils;
import com.google.gson.Gson;

public class UserFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        String userJson = String.valueOf(AuthUtils.getUser(requireContext()));


        Gson gson = new Gson();
        User user = gson.fromJson(userJson, User.class);

        TextView nameText = view.findViewById(R.id.tvProfileName);
        TextView emailText = view.findViewById(R.id.tvProfileEmail);

        String nombreCompleto = user.nombre + " " + user.apellido;
        nameText.setText(nombreCompleto);
        emailText.setText(user.correo);

        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro de cerrar sesión?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        AuthUtils.deleteUser(requireContext());
                        AuthUtils.deleteToken(requireContext());
                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        return view;
    }


}
