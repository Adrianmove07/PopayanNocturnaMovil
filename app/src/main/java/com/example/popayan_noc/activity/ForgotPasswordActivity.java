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

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnSendRecovery;
    private ProgressDialog progressDialog;
    private static final String RECOVERY_URL = "https://popnocturna.vercel.app/api/enviar-codigo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etForgotEmail);
        btnSendRecovery = findViewById(R.id.btnSendRecovery);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Enviando código...");

        btnSendRecovery.setOnTouchListener((v, event) -> {
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

        btnSendRecovery.setOnClickListener(v -> attemptSendRecovery());
    }

    private void attemptSendRecovery() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Requerido");
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo no válido");
            return;
        } else {
            etEmail.setError(null);
        }

        progressDialog.show();
        JSONObject body = new JSONObject();
        try {
            body.put("correo", email);
        } catch (JSONException e) {
            progressDialog.dismiss();
            Snackbar.make(btnSendRecovery, "Error interno", Snackbar.LENGTH_LONG).show();
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, RECOVERY_URL, body,
                response -> {
                    progressDialog.dismiss();
                    Snackbar.make(btnSendRecovery, "Código enviado. Revisa tu correo.", Snackbar.LENGTH_LONG).show();
                    btnSendRecovery.postDelayed(() -> {
                        Intent intent = new Intent(this, ResetPasswordActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }, 1200);
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
                    Snackbar.make(btnSendRecovery, msg, Snackbar.LENGTH_LONG).show();
                });
        queue.add(request);
    }
}
