package com.example.popayan_noc.activity;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.popayan_noc.R;
import com.example.popayan_noc.util.AuthUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000;

    // 1. Declarar el ActivityResultLauncher para manejar la solicitud de permiso
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permiso concedido. Puedes continuar con la lógica de tu splash.
                    Log.d("Permiso", "Permiso de notificación concedido");
                    // Opcional: Mostrar un mensaje al usuario
                    // Toast.makeText(this, "Permiso de notificación concedido", Toast.LENGTH_SHORT).show();
                } else {
                    // Permiso denegado. Informa al usuario que no recibirá notificaciones.
                    Log.d("Permiso", "Permiso de notificación denegado");
                    Toast.makeText(this, "Permiso de notificación denegado. No recibirás notificaciones.", Toast.LENGTH_LONG).show();
                }
                // Una vez que el usuario interactúe con el diálogo de permiso,
                // podemos iniciar la lógica de temporizador del splash.
                startSplashTimer();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_splash);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    AuthUtils.saveTokenDevice(this, token);
                    Log.d(TAG, "Token actual: " + token);
                });

        // 2. Llamar a la función para solicitar el permiso de notificación
        requestNotificationPermission();
    }

    // Método para iniciar el temporizador del splash y la navegación
    private void startSplashTimer() {
        String token = AuthUtils.getToken(this);
        // AuthUtils.getUser(this); // La variable user no se utiliza, se puede eliminar

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent nextActivity;
                if (token == null) { // Si no hay token guardado (usuario no logueado)
                    nextActivity = new Intent(SplashActivity.this, LoginActivity.class);
                } else { // Si hay un token guardado (usuario logueado)
                    nextActivity = new Intent(SplashActivity.this, MainActivity.class);
                }
                startActivity(nextActivity);
                finish(); // Finaliza SplashActivity para que el usuario no pueda volver a ella
            }
        }, SPLASH_TIME_OUT);
    }

    // 3. Método para solicitar el permiso de notificación
    private void requestNotificationPermission() {
        // Solo solicita el permiso en Android 13 (API 33) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // El permiso ya está concedido, podemos continuar con el temporizador del splash
                Log.d("Permiso", "Permiso de notificación ya concedido");
                startSplashTimer();
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // Opcional: Si el usuario ha denegado el permiso antes, puedes mostrar un diálogo
                // explicando por qué lo necesitas antes de volver a solicitarlo.
                // Por simplicidad, aquí lo solicitaremos directamente.
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Solicitar directamente el permiso
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // En versiones anteriores a Android 13, no se necesita solicitar este permiso.
            // Continuar directamente con el temporizador del splash.
            startSplashTimer();
        }
    }
}