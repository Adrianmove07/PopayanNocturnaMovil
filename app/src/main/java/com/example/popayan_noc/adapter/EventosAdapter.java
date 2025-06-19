package com.example.popayan_noc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.popayan_noc.util.AuthUtils;
import com.example.popayan_noc.service.EventApi;
import com.example.popayan_noc.R;
import com.example.popayan_noc.model.Evento;
import com.example.popayan_noc.model.Lugar;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.List;

public class EventosAdapter extends RecyclerView.Adapter<EventosAdapter.EventoViewHolder> {
    private Context context;
    private List<Evento> eventos;
    private HashMap<Integer, JSONArray> commentsCache = new HashMap<>();

    public EventosAdapter(Context context, List<Evento> eventos) {
        this.context = context;
        this.eventos = eventos;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_evento_detalle, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = eventos.get(position);
        
        // Cargar imagen de la portada
        if (!evento.getPortada().isEmpty()) {
            String urlImagen = evento.getPortada().get(0);
            Glide.with(context)
                .load(urlImagen)
                .placeholder(R.drawable.placeholder_img)
                .into(holder.ivPortada);
        }

        // Mostrar datos del evento
        holder.tvNombre.setText(evento.getNombre());
        holder.tvDescripcion.setText(evento.getDescripcion());
        holder.tvFechaHora.setText(evento.getFechaHora());
        holder.tvCapacidad.setText("Capacidad: " + evento.getCapacidad());
        holder.tvPrecio.setText("Precio: " + evento.getPrecio());

        // Mostrar datos del lugar
        Lugar lugar = evento.getLugar();
        if (lugar != null) {
            holder.tvLugarNombre.setText(lugar.getNombre());
            holder.tvLugarUbicacion.setText(lugar.getUbicacion());
        } else {
            holder.tvLugarNombre.setText("Lugar no disponible");
            holder.tvLugarUbicacion.setText("Ubicación no disponible");
        }

        // --- Cargar comentarios existentes ---
        int eventId = evento.getId();
        JSONArray cached = commentsCache.get(eventId);
        if (cached != null) {
            holder.rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
            holder.rvComments.setAdapter(new CommentAdapter(context, cached));
            holder.rvComments.setVisibility(View.VISIBLE);
        } else {
            new Thread(() -> {
                try {
                    URL url = new URL("https://popnocturna.vercel.app/api/comentario?eventoid=" + eventId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    InputStream is = conn.getInputStream();
                    Scanner s = new Scanner(is).useDelimiter("\\A");
                    String response = s.hasNext() ? s.next() : "";
                    is.close();
                    conn.disconnect();
                    JSONArray comentarios = new JSONArray(response);
                    commentsCache.put(eventId, comentarios);
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> {
                        holder.rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
                        holder.rvComments.setAdapter(new CommentAdapter(context, comentarios));
                        holder.rvComments.setVisibility(View.VISIBLE);
                    });
                } catch (Exception e) {
                    // No mostrar nada si falla
                }
            }).start();
        }

        // --- Botón Calificar ---
        holder.btnCalificar.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null);
            RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
            dialogView.findViewById(R.id.btnCancelRating).setOnClickListener(bv -> dialog.dismiss());
            dialogView.findViewById(R.id.btnSendRating).setOnClickListener(bv -> {
                int puntuacion = (int) ratingBar.getRating();
                if (puntuacion < 1 || puntuacion > 5) {
                    android.widget.Toast.makeText(context, "Selecciona una puntuación de 1 a 5", android.widget.Toast.LENGTH_SHORT).show();
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
                            android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                            mainHandler.post(() -> android.widget.Toast.makeText(context, "No hay sesión activa. Inicia sesión para calificar.", android.widget.Toast.LENGTH_LONG).show());
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
                        Scanner s = new Scanner(is).useDelimiter("\\A");
                        String response = s.hasNext() ? s.next() : "";
                        is.close();
                        conn.disconnect();
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> {
                            if (responseCode == 201) {
                                android.widget.Toast.makeText(context, "¡Calificación enviada!", android.widget.Toast.LENGTH_SHORT).show();
                                holder.tvRating.setVisibility(View.VISIBLE);
                                holder.tvRating.setText("Calificación: " + puntuacion + " estrellas");
                            } else if (response.contains("Ya has calificado")) {
                                android.widget.Toast.makeText(context, "Ya has calificado este evento", android.widget.Toast.LENGTH_LONG).show();
                            } else if (response.contains("propietario")) {
                                android.widget.Toast.makeText(context, "Los propietarios no pueden calificar eventos", android.widget.Toast.LENGTH_LONG).show();
                            } else {
                                android.widget.Toast.makeText(context, "Error: " + response, android.widget.Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> android.widget.Toast.makeText(context, "Error al enviar calificación", android.widget.Toast.LENGTH_LONG).show());
                    }
                }).start();
            });
            dialog.show();
        });

        // --- Botón Comentar ---
        holder.btnComentar.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null);
            EditText etComment = dialogView.findViewById(R.id.etComment);
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
            dialogView.findViewById(R.id.btnCancel).setOnClickListener(bv -> dialog.dismiss());
            dialogView.findViewById(R.id.btnSend).setOnClickListener(bv -> {
                String comentario = etComment.getText().toString().trim();
                if (comentario.isEmpty()) {
                    android.widget.Toast.makeText(context, "Escribe un comentario", android.widget.Toast.LENGTH_SHORT).show();
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
                            android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                            mainHandler.post(() -> android.widget.Toast.makeText(context, "No hay sesión activa. Inicia sesión para comentar.", android.widget.Toast.LENGTH_LONG).show());
                            return;
                        }
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                        conn.setDoOutput(true);
                        JSONObject body = new JSONObject();
                        body.put("eventoid", eventId);
                        body.put("comentario", comentario);
                        OutputStream os = conn.getOutputStream();
                        os.write(body.toString().getBytes("UTF-8"));
                        os.close();
                        int responseCode = conn.getResponseCode();
                        InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                        Scanner s = new Scanner(is).useDelimiter("\\A");
                        String response = s.hasNext() ? s.next() : "";
                        is.close();
                        conn.disconnect();
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> {
                            if (responseCode == 201) {
                                android.widget.Toast.makeText(context, "¡Comentario enviado!", android.widget.Toast.LENGTH_SHORT).show();
                                // Actualizar comentarios
                                commentsCache.remove(eventId);
                                holder.rvComments.setVisibility(View.GONE);
                                // Cargar comentarios nuevamente
                                new Thread(() -> {
                                    try {
                                        URL urlComments = new URL("https://popnocturna.vercel.app/api/comentario?eventoid=" + eventId);
                                        HttpURLConnection connComments = (HttpURLConnection) urlComments.openConnection();
                                        connComments.setRequestMethod("GET");
                                        connComments.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                                        InputStream isComments = connComments.getInputStream();
                                        Scanner sComments = new Scanner(isComments).useDelimiter("\\A");
                                        String responseComments = sComments.hasNext() ? sComments.next() : "";
                                        isComments.close();
                                        connComments.disconnect();
                                        JSONArray comentarios = new JSONArray(responseComments);
                                        commentsCache.put(eventId, comentarios);
                                        android.os.Handler mainHandlerComments = new android.os.Handler(context.getMainLooper());
                                        mainHandlerComments.post(() -> {
                                            holder.rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
                                            holder.rvComments.setAdapter(new CommentAdapter(context, comentarios));
                                            holder.rvComments.setVisibility(View.VISIBLE);
                                        });
                                    } catch (Exception e) {
                                        // No mostrar nada si falla
                                    }
                                }).start();
                            } else if (response.contains("propietario")) {
                                android.widget.Toast.makeText(context, "Los propietarios no pueden comentar eventos", android.widget.Toast.LENGTH_LONG).show();
                            } else {
                                android.widget.Toast.makeText(context, "Error: " + response, android.widget.Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> android.widget.Toast.makeText(context, "Error al enviar comentario", android.widget.Toast.LENGTH_LONG).show());
                    }
                }).start();
            });
            dialog.show();
        });

        // --- Botón Reservar ---
        boolean eventoInactivo = false;
        String estadoEvento = evento.getEstado();
        if (!estadoEvento.equalsIgnoreCase("activo")) {
            eventoInactivo = true;
        }
        // Comparar fecha del evento con la fecha actual
        String fechaEventoStr = evento.getFechaHora();
        boolean eventoPasado = false;
        if (!fechaEventoStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date fechaEvento = sdf.parse(fechaEventoStr.replace("Z", ""));
                if (fechaEvento != null && fechaEvento.before(new Date())) {
                    eventoPasado = true;
                }
            } catch (Exception e) { /* Si hay error, no ocultar por fecha */ }
        }
        if (eventoInactivo || eventoPasado) {
            holder.btnReservar.setVisibility(View.GONE);
        } else {
            holder.btnReservar.setVisibility(View.VISIBLE);
            holder.btnReservar.setEnabled(true);
            holder.btnReservar.setText("Reservar");
            holder.btnReservar.setOnClickListener(v -> {
                String token = AuthUtils.getToken(context);
                if (token == null || token.isEmpty()) {
                    Toast.makeText(context, "Por favor, inicia sesión primero", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Mostrar diálogo para seleccionar cantidad de entradas
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Reservar entrada para " + evento.getNombre());

                // Crear vista personalizada
                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reservation, null);
                builder.setView(dialogView);

                // Referencias a elementos del diálogo
                NumberPicker npEntradas = dialogView.findViewById(R.id.npEntradas);

                // Configurar NumberPicker
                npEntradas.setMinValue(1);
                npEntradas.setMaxValue(10);
                npEntradas.setValue(1);

                // Referencias a botones
                Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmar);
                Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

                // Crear diálogo
                AlertDialog dialog = builder.create();

                // Configurar botones
                btnConfirmar.setOnClickListener(view -> {
                    int cantidadEntradas = npEntradas.getValue();
                    EventApi.createReservation(context, token, evento.getId(), evento.getFechaHora(), cantidadEntradas,
                        response -> {
                            try {
                                String mensaje = response.getString("mensaje");
                                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
                                
                                // Actualizar capacidad en la UI
                                int capacidadActual = response.getInt("capacidadActual");
                                evento.setCapacidad(capacidadActual);
                                holder.tvCapacidad.setText("Capacidad: " + capacidadActual);
                                
                                // Actualizar lista completa
                                eventos.set(position, evento);
                                notifyItemChanged(position);
                                
                                // Cerrar diálogo
                                dialog.dismiss();
                            } catch (JSONException e) {
                                Toast.makeText(context, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            try {
                                String errorMessage = "Error al crear la reserva";
                                if (error.networkResponse != null) {
                                    String errorBody = new String(error.networkResponse.data);
                                    try {
                                        JSONObject errorJson = new JSONObject(errorBody);
                                        errorMessage = errorJson.getString("mensaje");
                                    } catch (Exception e) {
                                        // Si no se puede parsear el error
                                    }
                                }
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(context, "Error desconocido", Toast.LENGTH_SHORT).show();
                            }
                        });
                });
                
                btnCancelar.setOnClickListener(view -> dialog.dismiss());
                
                // Mostrar diálogo
                dialog.show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return eventos == null ? 0 : eventos.size();
    }

    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivPortada;
        public TextView tvNombre;
        public TextView tvDescripcion;
        public TextView tvFechaHora;
        public TextView tvCapacidad;
        public TextView tvPrecio;
        public TextView tvLugarNombre;
        public TextView tvLugarUbicacion;
        public TextView tvRating;
        public Button btnCalificar;
        public Button btnComentar;
        public Button btnReservar;
        public RecyclerView rvComments;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.ivPortada);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvCapacidad = itemView.findViewById(R.id.tvCapacidad);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvLugarNombre = itemView.findViewById(R.id.tvLugarNombre);
            tvLugarUbicacion = itemView.findViewById(R.id.tvLugarUbicacion);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnCalificar = itemView.findViewById(R.id.btnCalificar);
            btnComentar = itemView.findViewById(R.id.btnComentar);
            btnReservar = itemView.findViewById(R.id.btnReservar);
            rvComments = itemView.findViewById(R.id.rvComments);
        }
    }
}
