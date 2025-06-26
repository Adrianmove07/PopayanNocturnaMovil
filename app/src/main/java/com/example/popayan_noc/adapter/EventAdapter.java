package com.example.popayan_noc.adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.popayan_noc.R;
import com.example.popayan_noc.model.Evento; // ¡AHORA IMPORTAMOS Evento!
import com.example.popayan_noc.util.AuthUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List; // Usamos List
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Evento> eventList;
    private Context context;
    private SparseArray<JSONArray> commentsCache = new SparseArray<>();

    public EventAdapter(Context context, List<Evento> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Evento evento = eventList.get(position); // Obtener un objeto Evento


        String imageUrl = "";
        if (evento.getPortada() != null && !evento.getPortada().isEmpty()) {
            imageUrl = evento.getPortada().get(0);
        }

        if (!imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_img)
                    .error(R.drawable.placeholder_img)
                    .centerCrop()
                    .into(holder.imgEvent);
        } else {
            holder.imgEvent.setImageResource(R.drawable.placeholder_img);
        }

        holder.tvEventTitle.setText(evento.getNombre());

        String dateTime = "";
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdfIn.parse(evento.getFechaHora().replace("Z", ""));
            SimpleDateFormat sdfDateOut = new SimpleDateFormat("dd MMM", Locale.getDefault());
            SimpleDateFormat sdfTimeOut = new SimpleDateFormat("h:mm a", Locale.getDefault());
            dateTime = sdfDateOut.format(Objects.requireNonNull(date)) + " • " + sdfTimeOut.format(date);
        } catch (Exception e) {
            Log.e("EventAdapter", "Error al parsear fecha de Evento: " + e.getMessage());
        }
        holder.tvEventDateTime.setText(dateTime);

        if (evento.getLugar() != null) {

        } else {
            holder.tvEventLocation.setText("Ubicación no disponible");
        }


        final int eventId = evento.getId();
        if (eventId != -1) {
            JSONArray cachedComments = commentsCache.get(eventId);
            if (cachedComments != null) {
                holder.rvComments.setLayoutManager(new LinearLayoutManager(context));
                holder.rvComments.setAdapter(new CommentAdapter(context, cachedComments));
            } else {
                new Thread(() -> {
                    try {
                        URL url = new URL("https://popnocturna.vercel.app/api/comentario?eventoid=" + eventId);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        InputStream is = conn.getInputStream();
                        String response = new Scanner(is).useDelimiter("\\A").hasNext() ? new Scanner(is).next() : "";
                        is.close();
                        conn.disconnect();
                        JSONArray comentarios = new JSONArray(response);
                        commentsCache.put(eventId, comentarios);
                        new Handler(context.getMainLooper()).post(() -> {
                            holder.rvComments.setLayoutManager(new LinearLayoutManager(context));
                            holder.rvComments.setAdapter(new CommentAdapter(context, comentarios));
                        });
                    } catch (Exception e) {
                        Log.e("EventAdapter", "Error cargando comentarios para el evento " + eventId + ": " + e.getMessage());
                        new Handler(context.getMainLooper()).post(() -> {
                            Toast.makeText(context, "No se pudieron cargar los comentarios.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            }
        }


        holder.btnCalificar.setOnClickListener(v2 -> {
            if (eventId == -1) {
                Toast.makeText(context, "ID de evento no válido para calificar.", Toast.LENGTH_SHORT).show();
                return;
            }
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null);
            RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
            dialogView.findViewById(R.id.btnCancelRating).setOnClickListener(bv -> dialog.dismiss());
            dialogView.findViewById(R.id.btnSendRating).setOnClickListener(bv -> {
                int puntuacion = (int) ratingBar.getRating();
                if (puntuacion < 1 || puntuacion > 5) {
                    Toast.makeText(context, "Selecciona una puntuación de 1 a 5", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                new Thread(() -> {
                    try {
                        URL url = new URL("https://popnocturna.vercel.app/api/calificacion");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        String token = AuthUtils.getToken(context);
                        if (token == null || token.isEmpty()) {
                            new Handler(context.getMainLooper()).post(() -> Toast.makeText(context, "No hay sesión activa. Inicia sesión para calificar.", Toast.LENGTH_LONG).show());
                            return;
                        }
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                        conn.setDoOutput(true);
                        JSONObject body = new JSONObject();
                        body.put("eventoid", eventId);
                        body.put("puntuacion", puntuacion);
                        OutputStream os = conn.getOutputStream();
                        os.write(body.toString().getBytes("UTF-8"));
                        os.close();
                        int responseCode = conn.getResponseCode();
                        InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                        String response = new Scanner(is).useDelimiter("\\A").hasNext() ? new Scanner(is).next() : "";
                        is.close();
                        conn.disconnect();
                        new Handler(context.getMainLooper()).post(() -> {
                            if (responseCode == 201) {
                                Toast.makeText(context, "¡Calificación enviada!", Toast.LENGTH_SHORT).show();
                            } else if (response.contains("Ya has calificado")) {
                                Toast.makeText(context, "Ya has calificado este evento", Toast.LENGTH_LONG).show();
                            } else if (response.contains("propietario")) {
                                Toast.makeText(context, "Los propietarios no pueden calificar eventos", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Error: " + response, Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e("EventAdapter", "Error al enviar calificación: " + e.getMessage());
                        new Handler(context.getMainLooper()).post(() -> Toast.makeText(context, "Error al enviar calificación", Toast.LENGTH_LONG).show());
                    }
                }).start();
            });
            dialog.show();
        });

        // --- Botón Reservar ---
        boolean eventoInactivo = false;
        // Usa getEstado() del objeto Evento
        if (!evento.getEstado().equalsIgnoreCase("activo")) {
            eventoInactivo = true;
        }

        String fechaHoraEventoStr = evento.getFechaHora();
        boolean eventoPasado = false;
        if (fechaHoraEventoStr != null && !fechaHoraEventoStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date fechaEvento = sdf.parse(fechaHoraEventoStr.replace("Z", ""));
                if (fechaEvento != null && fechaEvento.before(new Date())) {
                    eventoPasado = true;
                }
            } catch (Exception e) {
                Log.e("EventAdapter", "Error al parsear fecha para verificar si el evento ha pasado: " + e.getMessage());
            }
        }

        if (eventoInactivo || eventoPasado) {
            holder.btnReservar.setVisibility(View.GONE);
        } else {
            holder.btnReservar.setVisibility(View.VISIBLE);
            holder.btnReservar.setEnabled(true);
            holder.btnReservar.setText("Reservar");
            holder.btnReservar.setOnClickListener(v -> {
                if (eventId == -1) {
                    Toast.makeText(context, "ID de evento no válido para reservar.", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(context)
                        .setTitle("Confirmar reserva")
                        .setMessage("¿Deseas reservar un cupo para este evento?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            new Thread(() -> {
                                try {
                                    URL url = new URL("https://popnocturna.vercel.app/api/reserva");
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    conn.setRequestMethod("POST");
                                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                                    String token = AuthUtils.getToken(context);
                                    if (token == null || token.isEmpty()) {
                                        new Handler(context.getMainLooper()).post(() -> Toast.makeText(context, "No hay sesión activa. Inicia sesión para reservar.", Toast.LENGTH_LONG).show());
                                        return;
                                    }
                                    conn.setRequestProperty("Authorization", "Bearer " + token);
                                    conn.setDoOutput(true);
                                    JSONObject body = new JSONObject();
                                    body.put("eventoid", eventId);
                                    body.put("fecha_hora", fechaHoraEventoStr); // Usa la fecha original del evento
                                    OutputStream os = conn.getOutputStream();
                                    os.write(body.toString().getBytes("UTF-8"));
                                    os.close();
                                    int responseCode = conn.getResponseCode();
                                    InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                                    String response = new Scanner(is).useDelimiter("\\A").hasNext() ? new Scanner(is).next() : "";
                                    is.close();
                                    conn.disconnect();
                                    new Handler(context.getMainLooper()).post(() -> {
                                        if (responseCode == 201) {
                                            Toast.makeText(context, "¡Reserva realizada con éxito!", Toast.LENGTH_SHORT).show();
                                            holder.btnReservar.setEnabled(false);
                                            holder.btnReservar.setText("Reservado");
                                        } else if (response.contains("ya tienes una reserva")) {
                                            Toast.makeText(context, "Ya tienes una reserva para este evento", Toast.LENGTH_LONG).show();
                                            holder.btnReservar.setEnabled(false);
                                            holder.btnReservar.setText("Reservado");
                                        } else if (response.contains("no existe") || response.contains("inactivo")) {
                                            Toast.makeText(context, "Este evento ya no está disponible para reservas.", Toast.LENGTH_LONG).show();
                                            holder.btnReservar.setEnabled(false);
                                            holder.btnReservar.setText("No disponible");
                                        } else {
                                            Toast.makeText(context, "No se pudo reservar: " + response, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.e("EventAdapter", "Error al reservar: " + e.getMessage());
                                    new Handler(context.getMainLooper()).post(() -> Toast.makeText(context, "Error al reservar", Toast.LENGTH_LONG).show());
                                }
                            }).start();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        // --- Diálogo de comentar mejorado ---
        holder.btnComentar.setOnClickListener(v -> {
            if (eventId == -1) {
                Toast.makeText(context, "ID de evento no válido para comentar.", Toast.LENGTH_SHORT).show();
                return;
            }
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null);
            EditText etComment = dialogView.findViewById(R.id.etComment);
            TextView tvCharCount = dialogView.findViewById(R.id.tvCharCount);
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
            dialogView.findViewById(R.id.btnCancel).setOnClickListener(bv -> dialog.dismiss());
            etComment.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    tvCharCount.setText(String.format(Locale.getDefault(), "%d/250", s.length()));
                }
                @Override public void afterTextChanged(Editable s) {}
            });
            dialogView.findViewById(R.id.btnSend).setOnClickListener(bv -> {
                String comentario = etComment.getText().toString().trim();
                if (comentario.isEmpty()) {
                    etComment.setError("El comentario no puede estar vacío");
                    return;
                }
                dialog.dismiss();
                new Thread(() -> {
                    try {
                        URL url = new URL("https://popnocturna.vercel.app/api/comentario");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        String token = AuthUtils.getToken(context);
                        if (token == null || token.isEmpty()) {
                            new Handler(context.getMainLooper()).post(() -> Toast.makeText(context, "No hay sesión activa. Inicia sesión para comentar.", Toast.LENGTH_LONG).show());
                            return;
                        }
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                        conn.setDoOutput(true);
                        JSONObject body = new JSONObject();
                        body.put("eventoid", eventId);
                        body.put("contenido", comentario);
                        OutputStream os = conn.getOutputStream();
                        os.write(body.toString().getBytes("UTF-8"));
                        os.close();
                        int responseCode = conn.getResponseCode();
                        InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                        String response = new Scanner(is).useDelimiter("\\A").hasNext() ? new Scanner(is).next() : "";
                        is.close();
                        conn.disconnect();
                        new Handler(context.getMainLooper()).post(() -> {
                            if (responseCode == 201) {
                                Toast.makeText(context, "Comentario enviado", Toast.LENGTH_SHORT).show();
                                commentsCache.remove(eventId);
                                onBindViewHolder(holder, position); // Vuelve a enlazar el ViewHolder para refrescar
                            } else {
                                Toast.makeText(context, "Error: " + response, Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e("EventAdapter", "Error al enviar comentario: " + e.getMessage());
                        new Handler(context.getMainLooper()).post(() -> Toast.makeText(context, "Error al enviar comentario", Toast.LENGTH_LONG).show());
                    }
                }).start();
            });
            dialog.show();
        });

        holder.itemView.setAnimation(android.view.animation.AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
    }

    @Override
    public int getItemCount() {
        return eventList.size(); // Usamos size() para List
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imgEvent;
        TextView tvEventTitle, tvEventDateTime, tvEventLocation;
        Button btnComentar;
        RecyclerView rvComments;
        Button btnCalificar;
        Button btnReservar;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imgEvent = itemView.findViewById(R.id.imgEvent);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);

            btnComentar = itemView.findViewById(R.id.btnComentar);
            rvComments = itemView.findViewById(R.id.rvComments);
            btnCalificar = itemView.findViewById(R.id.btnCalificar);
            btnReservar = itemView.findViewById(R.id.btnReservar);
        }
    }
}