package com.example.popayan_noc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONException;
import org.json.JSONObject;

public class ValidateAccountActivity extends AppCompatActivity {
    private EditText etEmail, etCode;
    private Button btnValidate, btnResendCode;
    private ProgressDialog progressDialog;
    private static final String VALIDATE_URL = "https://popnocturna.vercel.app/api/validar-codigo";
    private static final String RESEND_URL = "https://popnocturna.vercel.app/api/reenviar-codigo";
    private int resendCooldown = 60;
    private android.os.CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_account);

        etEmail = findViewById(R.id.etValidationEmail);
        etCode = findViewById(R.id.etValidationCode);
        btnValidate = findViewById(R.id.btnValidateAccount);
        btnResendCode = findViewById(R.id.btnResendCode);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Validando cuenta...");

        // Si viene un email prellenado
        String email = getIntent().getStringExtra("email");
        if (email != null) etEmail.setText(email);

        btnValidate.setOnClickListener(v -> attemptValidate());

        btnResendCode.setOnClickListener(v -> resendCode());
        btnResendCode.setEnabled(true);
    }

    private void attemptValidate() {
        String email = etEmail.getText().toString().trim();
        String code = etCode.getText().toString().trim();
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
        if (!valid) return;

        progressDialog.show();
        JSONObject body = new JSONObject();
        try {
            body.put("correo", email);
            body.put("codigo", code);
        } catch (JSONException e) {
            progressDialog.dismiss();
            Snackbar.make(btnValidate, "Error interno", Snackbar.LENGTH_LONG).show();
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VALIDATE_URL, body,
                response -> {
                    progressDialog.dismiss();
                    String backendMsg = response.optString("mensaje", "Usuario validado correctamente");
                    Snackbar.make(btnValidate, backendMsg, Snackbar.LENGTH_LONG).show();
                    // Guardar usuario y token en preferencias
                    try {
                        String token = response.optString("token", null);
                        JSONObject usuario = response.optJSONObject("usuario");
                        if (token != null && usuario != null) {
                            android.content.SharedPreferences prefs = getSharedPreferences("popnoc_prefs", MODE_PRIVATE);
                            prefs.edit()
                                .putString("token", token)
                                .putString("usuario", usuario.toString())
                                .apply();
                        }
                    } catch (Exception ignored) {}
                    btnValidate.postDelayed(() -> {
                        // Navegar a la pantalla principal/logueada
                        Intent intent = new Intent(this, UserHomeActivity.class);
                        startActivity(intent);
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
                    Snackbar.make(btnValidate, msg, Snackbar.LENGTH_LONG).show();
                });
        queue.add(request);
    }

    private void resendCode() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo válido requerido");
            return;
        }
        btnResendCode.setEnabled(false);
        btnResendCode.setText("Enviando...");
        JSONObject body = new JSONObject();
        try {
            body.put("correo", email);
        } catch (JSONException e) {
            Snackbar.make(btnResendCode, "Error interno", Snackbar.LENGTH_LONG).show();
            btnResendCode.setEnabled(true);
            btnResendCode.setText("Reenviar código");
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, RESEND_URL, body,
            response -> {
                String backendMsg = response.optString("mensaje", "");
                Snackbar.make(btnResendCode, backendMsg, Snackbar.LENGTH_LONG).show();
                startResendCooldown();
            },
            error -> {
                String msg = "Error de conexión";
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    try {
                        String errorMsg = new String(error.networkResponse.data);
                        JSONObject errorObj = new JSONObject(errorMsg);
                        msg = errorObj.optString("mensaje", msg);
                    } catch (Exception ignored) {}
                }
                Snackbar.make(btnResendCode, msg, Snackbar.LENGTH_LONG).show();
                btnResendCode.setEnabled(true);
                btnResendCode.setText("Reenviar código");
            });
        queue.add(request);
    }

    private void startResendCooldown() {
        btnResendCode.setEnabled(false);
        resendTimer = new android.os.CountDownTimer(resendCooldown * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                btnResendCode.setText("Reenviar en " + (millisUntilFinished / 1000) + "s");
            }
            public void onFinish() {
                btnResendCode.setEnabled(true);
                btnResendCode.setText("Reenviar código");
            }
        }.start();
    }
}
