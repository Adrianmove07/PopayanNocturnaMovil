package com.example.popayan_noc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnDoLogin;
    private TextView tvGoRegister, tvForgotPassword;
    private ProgressDialog progressDialog;
    private static final String LOGIN_URL = "https://popnocturna.vercel.app/api/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnDoLogin = findViewById(R.id.btnDoLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Iniciando sesión...");

        btnDoLogin.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.93f).scaleY(0.93f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });

        btnDoLogin.setOnClickListener(v -> attemptLogin());

        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        // Animación de entrada para logo y campos al iniciar sesión
        final ImageView logo = findViewById(R.id.logoImageView);
        final TextView title = findViewById(R.id.loginTitle);
        logo.setAlpha(0f);
        title.setAlpha(0f);
        etEmail.setAlpha(0f);
        etPassword.setAlpha(0f);
        btnDoLogin.setAlpha(0f);
        tvGoRegister.setAlpha(0f);
        tvForgotPassword.setAlpha(0f);

        
        logo.animate().alpha(1f).setDuration(700).withEndAction(() -> {
            title.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
            title.animate().alpha(1f).setDuration(500).withEndAction(() -> {
                etEmail.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
                etEmail.animate().alpha(1f).setDuration(400);
                etPassword.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
                etPassword.animate().alpha(1f).setDuration(400);
                btnDoLogin.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
                btnDoLogin.animate().alpha(1f).setDuration(350);
                tvGoRegister.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
                tvGoRegister.animate().alpha(1f).setDuration(300);
                tvForgotPassword.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
                tvForgotPassword.animate().alpha(1f).setDuration(300);
            });
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        JSONObject body = new JSONObject();
        try {
            body.put("correo", email);
            body.put("contrasena", password);
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(this, "Error interno", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, LOGIN_URL, body,
                response -> {
                    progressDialog.dismiss();
                    handleLoginSuccess(response);
                },
                error -> {
                    progressDialog.dismiss();
                    String msg = "Error de conexión";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorMsg = new String(error.networkResponse.data);
                            JSONObject errorObj = new JSONObject(errorMsg);
                            msg = errorObj.optString("mensaje", msg);
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                });
        queue.add(request);
    }

    private void handleLoginSuccess(JSONObject response) {
        // Espera: { token, usuario: { id, correo, rol } }
        try {
            String token = response.getString("token");
            JSONObject usuario = response.getJSONObject("usuario");
            // Guarda el token y el usuario
            SharedPreferences prefs = getSharedPreferences("popnoc_prefs", Context.MODE_PRIVATE);
            prefs.edit()
                    .putString("token", token)
                    .putString("usuario", usuario.toString())
                    .apply();
            // Navega al apartado de usuario (UserHomeActivity)
            Intent intent = new Intent(this, UserHomeActivity.class);
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            Toast.makeText(this, "Respuesta inesperada del servidor", Toast.LENGTH_LONG).show();
        }
    }
}
