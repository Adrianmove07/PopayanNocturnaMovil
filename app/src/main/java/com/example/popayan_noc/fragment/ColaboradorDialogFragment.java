package com.example.popayan_noc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.popayan_noc.R;
import com.example.popayan_noc.model.Solicitud;
import com.example.popayan_noc.service.SolicitudApi;
import com.example.popayan_noc.util.AuthUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ColaboradorDialogFragment extends DialogFragment {

    private ViewFlipper viewFlipper;
    private EditText etDescripcion;
    private TextView tvEstado, tvDescripcion;
    private int solicitudId = -1;
    private String token;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_colaborador_dialog, container, false);

        viewFlipper = view.findViewById(R.id.viewFlipper);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        tvEstado = view.findViewById(R.id.tvEstadoSolicitud);
        tvDescripcion = view.findViewById(R.id.tvDescripcionSolicitud);

        String token = AuthUtils.getToken(getContext());

        // Obtener solicitud
        SolicitudApi.obtenerSolicitud(getContext(), token, response -> {
            if (response.length() > 0) {
                try {
                    JSONObject solicitud = response.getJSONObject(0);
                    solicitudId = solicitud.getInt("id");
                    String estado = solicitud.getString("estado");
                    String descripcion = solicitud.getString("descripcion");

                    tvEstado.setText("Estado: " + estado);
                    tvDescripcion.setText(descripcion);
                    viewFlipper.setDisplayedChild(2);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "Error al procesar solicitud", Toast.LENGTH_SHORT).show();
                }
            } else {
                viewFlipper.setDisplayedChild(0);
            }
        }, error -> {
            Toast.makeText(getContext(), "Error al cargar solicitud", Toast.LENGTH_SHORT).show();
        });

        // Botón Continuar → mostrar formulario
        view.findViewById(R.id.btnContinuar).setOnClickListener(v -> viewFlipper.setDisplayedChild(1));

        // Botón Enviar/Editar
        view.findViewById(R.id.btnEnviarSolicitud).setOnClickListener(v -> {
            String descripcion = etDescripcion.getText().toString();
            if (descripcion.isEmpty()) {
                Toast.makeText(getContext(), "Por favor escribe una descripción", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject body = new JSONObject();
            try {
                body.put("descripcion", descripcion);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (solicitudId == -1) {
                // Crear nueva
                SolicitudApi.crearSolicitud(getContext(), token, body, response -> {
                    Toast.makeText(getContext(), "Solicitud enviada", Toast.LENGTH_SHORT).show();
                    dismiss();
                }, error -> {
                    Toast.makeText(getContext(), "Error al crear solicitud", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Editar existente
                SolicitudApi.editarSolicitud(getContext(), token, solicitudId, body, response -> {
                    Toast.makeText(getContext(), "Solicitud actualizada", Toast.LENGTH_SHORT).show();
                    dismiss();
                }, error -> {
                    Toast.makeText(getContext(), "Error al actualizar solicitud", Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Botón Editar → volver a formulario
        view.findViewById(R.id.btnEditarSolicitud).setOnClickListener(v -> viewFlipper.setDisplayedChild(1));

        // Botón Eliminar
        view.findViewById(R.id.btnEliminarSolicitud).setOnClickListener(v -> {
            SolicitudApi.eliminarSolicitud(getContext(), token, solicitudId, response -> {
                Toast.makeText(getContext(), "Solicitud eliminada", Toast.LENGTH_SHORT).show();
                dismiss();
            }, error -> {
                Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show();
            });
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

}
