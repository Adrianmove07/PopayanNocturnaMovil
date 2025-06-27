package com.example.popayan_noc.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.popayan_noc.R;
import com.example.popayan_noc.model.Events;
import com.example.popayan_noc.util.AuthUtils;
import com.google.android.material.button.MaterialButton;
import android.widget.NumberPicker;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.EventViewHolder> {

    private final Context context;
    private List<Events> eventList;
    private final OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Events event, int position);
    }

    public EventCardAdapter(Context context, List<Events> eventList, OnEventClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_events_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Events event = eventList.get(position);

        // Configuración básica de vistas
        holder.tvEventName.setText(event.getNombre());
        holder.tvEventPrice.setText(event.getPrecio());
        holder.tvEventPlace.setText(event.getLugar().getNombre());

        // Carga de imagen
        List<String> portadaUrls = event.getPortada();
        String imageUrl = (portadaUrls != null && !portadaUrls.isEmpty()) ? portadaUrls.get(0) : null;
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.logo_popayan_nocturna)
                .error(R.drawable.logo_popayan_nocturna)
                .into(holder.imageViewPortada);

        // Configuración del botón de reserva
        configureReserveButton(holder, event, position);

        // Click listener para el item completo
        holder.itemView.setOnClickListener(v -> listener.onEventClick(event, position));
    }

    private void configureReserveButton(EventViewHolder holder, Events event, int position) {
        boolean isEventAvailable = isEventAvailableForReservation(event);

        if (!isEventAvailable) {
            holder.btnReserve.setVisibility(View.GONE);
            return;
        }

        holder.btnReserve.setVisibility(View.VISIBLE);
        holder.btnReserve.setEnabled(true);
        holder.btnReserve.setText("Reservar");

        holder.btnReserve.setOnClickListener(v -> {
            if (event.getId() == 0) { // Asumiendo que 0 es un ID inválido
                Toast.makeText(context, "ID de evento no válido para reservar.", Toast.LENGTH_SHORT).show();
                return;
            }

            showReservationConfirmationDialog(holder, event);
        });
    }

    private boolean isEventAvailableForReservation(Events event) {
        // Verificar estado del evento (booleano en tu modelo)
        if (!event.isEstado()) {
            return false;
        }

        // Verificar fecha del evento
        return !isEventDatePassed(event.getFechaHora());
    }

    private boolean isEventDatePassed(String eventDateTime) {
        if (eventDateTime == null || eventDateTime.isEmpty()) {
            return true;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date eventDate = sdf.parse(eventDateTime.replace("Z", ""));
            return eventDate != null && eventDate.before(new Date());
        } catch (Exception e) {
            Log.e("EventAdapter", "Error al parsear fecha del evento", e);
            return true;
        }
    }

    private void showReservationConfirmationDialog(EventViewHolder holder, Events event) {
        // Crear un NumberPicker dinámico
        NumberPicker numberPicker = new NumberPicker(context);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);
        numberPicker.setWrapSelectorWheel(false);

        // Crear contenedor para el NumberPicker
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(numberPicker);

        new AlertDialog.Builder(context)
                .setTitle("Reservar entradas")
                .setMessage("Selecciona la cantidad de entradas:")
                .setView(layout)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    int cantidadEntradas = numberPicker.getValue();
                    processReservation(holder, event, cantidadEntradas);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void processReservation(EventViewHolder holder, Events event,  int cantidadEntradas) {
        new Thread(() -> {
            try {
                URL url = new URL("https://popnocturna.vercel.app/api/reserva");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                String token = AuthUtils.getToken(context);
                if (token == null || token.isEmpty()) {
                    showToastOnMainThread("No hay sesión activa. Inicia sesión para reservar.");
                    return;
                }
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("eventoid", event.getId());
                body.put("fecha_hora", event.getFechaHora());
                body.put("cantidad_entradas", cantidadEntradas);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("UTF-8"));
                }

                // Procesar la respuesta
                int responseCode = conn.getResponseCode();
                String response = getResponseString(conn, responseCode);
                conn.disconnect();

                handleReservationResponse(holder, event, responseCode, response);
            } catch (Exception e) {
                Log.e("EventAdapter", "Error al reservar", e);
                showToastOnMainThread("Error al conectar con el servidor");
            }
        }).start();
    }

    private String getResponseString(HttpURLConnection conn, int responseCode) throws Exception {
        try (InputStream is = (responseCode >= 200 && responseCode < 300) ?
                conn.getInputStream() : conn.getErrorStream();
             Scanner scanner = new Scanner(is).useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private void handleReservationResponse(EventViewHolder holder, Events event, int responseCode, String response) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (responseCode == 201) {
                Toast.makeText(context, "¡Reserva realizada con éxito!", Toast.LENGTH_SHORT).show();
                holder.btnReserve.setEnabled(false);
                holder.btnReserve.setText("Reservado");

            } else if (response.contains("ya tienes una reserva")) {
                Toast.makeText(context, "Ya tienes una reserva para este evento", Toast.LENGTH_LONG).show();
                holder.btnReserve.setEnabled(false);
                holder.btnReserve.setText("Reservado");
            } else if (response.contains("no existe") || response.contains("inactivo")) {
                Toast.makeText(context, "Este evento ya no está disponible para reservas.", Toast.LENGTH_LONG).show();
                holder.btnReserve.setEnabled(false);
                holder.btnReserve.setText("No disponible");
            } else {
                Toast.makeText(context, "No se pudo reservar, tienes resevas sin aprovar: ", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showToastOnMainThread(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvEventPrice, tvEventPlace;
        ImageView imageViewPortada;
        MaterialButton btnReserve;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventPrice = itemView.findViewById(R.id.tvEventPrice);
            tvEventPlace = itemView.findViewById(R.id.tvEventPlace);
            imageViewPortada = itemView.findViewById(R.id.imageViewPortada);
            btnReserve = itemView.findViewById(R.id.btnReserve);
        }
    }
}