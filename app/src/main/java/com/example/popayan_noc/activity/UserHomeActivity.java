package com.example.popayan_noc.activity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.popayan_noc.R;

public class UserHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        TextView tvWelcome = findViewById(R.id.tvWelcomeUser);
        tvWelcome.setText("¡Bienvenido a Popayán Nocturna!");

        // Nueva animación: después de 1.5s, pasa automáticamente a MainActivity
        tvWelcome.postDelayed(() -> {
            startActivity(new android.content.Intent(UserHomeActivity.this, MainActivity.class));
            finish();
        }, 1500);

        // Lógica para botón de cerrar sesión

       /* findViewById(R.id.btnLogout).setOnClickListener(v -> {
            // Si usas SharedPreferences para login, bórralos aquí
            android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            // Vuelve al Login y limpia el stack
            android.content.Intent intent = new android.content.Intent(UserHomeActivity.this, LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });*/
    }
}
