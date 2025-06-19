package com.example.popayan_noc.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.popayan_noc.R;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONException;
import org.json.JSONObject;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText etEmail, etCode, etPassword;
    private Button btnDoResetPassword;
    private ProgressDialog progressDialog;
    private static final String RESET_URL = "https://popnocturna.vercel.app/api/cambiar-contrasena-codigo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etEmail = findViewById(R.id.etResetEmail);
        etCode = findViewById(R.id.etResetCode);
        etPassword = findViewById(R.id.etResetPassword);
        btnDoResetPassword = findViewById(R.id.btnDoResetPassword);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Restableciendo contraseña...");

        // Si viene un email prellenado
        String email = getIntent().getStringExtra("email");
        if (email != null) etEmail.setText(email);

        btnDoResetPassword.setOnTouchListener((v, event) -> {
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

        btnDoResetPassword.setOnClickListener(v -> attemptResetPassword());
    }

    private void attemptResetPassword() {
        String email = etEmail.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean valid = true;
        if (email.isEmpty()) {
            etEmail.setError("Requerido");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo no válido");
            valid = false;
        } else {
            etEmail.setError(null);
        }
        if (code.isEmpty()) {
            etCode.setError("Requerido");
            valid = false;
        } else {
            etCode.setError(null);
        }
        if (password.isEmpty()) {
            etPassword.setError("Requerido");
            valid = false;
        } else if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$")) {
            etPassword.setError("8-20 caracteres, 1 mayúscula, 1 número y 1 símbolo");
            valid = false;
        } else {
            etPassword.setError(null);
        }
        if (!valid) return;

        progressDialog.show();
        JSONObject body = new JSONObject();
        try {
            body.put("correo", email);
            body.put("codigo", code);
            body.put("nuevaContrasena", password);
        } catch (JSONException e) {
            progressDialog.dismiss();
            Snackbar.make(btnDoResetPassword, "Error interno", Snackbar.LENGTH_LONG).show();
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, RESET_URL, body,
                response -> {
                    progressDialog.dismiss();
                    Snackbar.make(btnDoResetPassword, "¡Contraseña restablecida! Ya puedes iniciar sesión.", Snackbar.LENGTH_LONG).show();
                    btnDoResetPassword.postDelayed(() -> {
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }, 1500);
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
                    Snackbar.make(btnDoResetPassword, msg, Snackbar.LENGTH_LONG).show();
                });
        queue.add(request);
    }
}
