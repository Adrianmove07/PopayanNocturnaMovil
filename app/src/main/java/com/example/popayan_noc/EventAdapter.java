package com.example.popayan_noc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private JSONArray eventList;
    private Context context;
    private android.util.SparseArray<JSONArray> commentsCache = new android.util.SparseArray<>();

    public EventAdapter(Context context, JSONArray eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        // Agrega el RecyclerView de comentarios dinámicamente
        RecyclerView rvComments = new RecyclerView(context);
        rvComments.setId(View.generateViewId());
        rvComments.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((ViewGroup) view).addView(rvComments);
        return new EventViewHolder(view, rvComments);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        JSONObject evento = eventList.optJSONObject(position);
        if (evento != null) {
            holder.tvEventName.setText(evento.optString("nombre", "Evento"));
            holder.tvEventDate.setText(evento.optString("fecha", ""));
            holder.tvEventDescription.setText(evento.optString("descripcion", ""));

            // --- Cargar comentarios existentes ---
            int eventId = evento.optInt("id", 1);
            JSONArray cached = commentsCache.get(eventId);
            if (cached != null) {
                holder.rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
                holder.rvComments.setAdapter(new CommentAdapter(context, cached));
            } else {
                new Thread(() -> {
                    try {
                        java.net.URL url = new java.net.URL("https://popnocturna.vercel.app/api/comentario?eventoid=" + eventId);
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        java.io.InputStream is = conn.getInputStream();
                        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                        String response = s.hasNext() ? s.next() : "";
                        is.close();
                        conn.disconnect();
                        JSONArray comentarios = new JSONArray(response);
                        commentsCache.put(eventId, comentarios);
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> {
                            holder.rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
                            holder.rvComments.setAdapter(new CommentAdapter(context, comentarios));
                        });
                    } catch (Exception e) {
                        // No mostrar nada si falla
                    }
                }).start();
            }

            // --- Botón Calificar ---
            holder.btnCalificar.setOnClickListener(v2 -> {
                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null);
                android.widget.RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
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
                            java.net.URL url = new java.net.URL("https://popnocturna.vercel.app/api/calificacion");
                            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            String token = com.example.popayan_noc.AuthUtils.getToken(context);
                            if (token == null || token.isEmpty()) {
                                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                                mainHandler.post(() -> android.widget.Toast.makeText(context, "No hay sesión activa. Inicia sesión para calificar.", android.widget.Toast.LENGTH_LONG).show());
                                return;
                            }
                            conn.setRequestProperty("Authorization", "Bearer " + token);
                            conn.setDoOutput(true);
                            org.json.JSONObject body = new org.json.JSONObject();
                            body.put("eventoid", eventId);
                            body.put("puntuacion", puntuacion);
                            java.io.OutputStream os = conn.getOutputStream();
                            os.write(body.toString().getBytes("UTF-8"));
                            os.close();
                            int responseCode = conn.getResponseCode();
                            java.io.InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                            String response = s.hasNext() ? s.next() : "";
                            is.close();
                            conn.disconnect();
                            android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                            mainHandler.post(() -> {
                                if (responseCode == 201) {
                                    android.widget.Toast.makeText(context, "¡Calificación enviada!", android.widget.Toast.LENGTH_SHORT).show();
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

            // --- Botón Reservar ---
            // --- Ocultar botón si evento inactivo o pasado ---
            boolean eventoInactivo = false;
            String estadoEvento = evento.optString("estado", "activo");
            if (!estadoEvento.equalsIgnoreCase("activo")) {
                eventoInactivo = true;
            }
            // Comparar fecha del evento con la fecha actual
            String fechaEventoStr = evento.optString("fecha", "");
            boolean eventoPasado = false;
            if (!fechaEventoStr.isEmpty()) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                    java.util.Date fechaEvento = sdf.parse(fechaEventoStr.replace("Z", ""));
                    if (fechaEvento != null && fechaEvento.before(new java.util.Date())) {
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
                    new android.app.AlertDialog.Builder(context)
                            .setTitle("Confirmar reserva")
                            .setMessage("¿Deseas reservar un cupo para este evento?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                new Thread(() -> {
                                    try {
                                        java.net.URL url = new java.net.URL("https://popnocturna.vercel.app/api/reserva");
                                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                                        conn.setRequestMethod("POST");
                                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                                        String token = com.example.popayan_noc.AuthUtils.getToken(context);
                                        if (token == null || token.isEmpty()) {
                                            android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                                            mainHandler.post(() -> android.widget.Toast.makeText(context, "No hay sesión activa. Inicia sesión para reservar.", android.widget.Toast.LENGTH_LONG).show());
                                            return;
                                        }
                                        conn.setRequestProperty("Authorization", "Bearer " + token);
                                        conn.setDoOutput(true);
                                        org.json.JSONObject body = new org.json.JSONObject();
                                        body.put("eventoid", eventId);
                                        body.put("fecha_hora", fechaEventoStr);
                                        java.io.OutputStream os = conn.getOutputStream();
                                        os.write(body.toString().getBytes("UTF-8"));
                                        os.close();
                                        int responseCode = conn.getResponseCode();
                                        java.io.InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                                        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                                        String response = s.hasNext() ? s.next() : "";
                                        is.close();
                                        conn.disconnect();
                                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                                        mainHandler.post(() -> {
                                            if (responseCode == 201) {
                                                android.widget.Toast.makeText(context, "¡Reserva realizada con éxito!", android.widget.Toast.LENGTH_SHORT).show();
                                                holder.btnReservar.setEnabled(false);
                                                holder.btnReservar.setText("Reservado");
                                            } else if (response.contains("ya tienes una reserva")) {
                                                android.widget.Toast.makeText(context, "Ya tienes una reserva para este evento", android.widget.Toast.LENGTH_LONG).show();
                                                holder.btnReservar.setEnabled(false);
                                                holder.btnReservar.setText("Reservado");
                                            } else if (response.contains("no existe") || response.contains("inactivo")) {
                                                android.widget.Toast.makeText(context, "Este evento ya no está disponible para reservas.", android.widget.Toast.LENGTH_LONG).show();
                                                holder.btnReservar.setEnabled(false);
                                                holder.btnReservar.setText("No disponible");
                                            } else {
                                                android.widget.Toast.makeText(context, "No se pudo reservar porque el evento fue eliminado o está inactivo.", android.widget.Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } catch (Exception e) {
                                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                                        mainHandler.post(() -> android.widget.Toast.makeText(context, "Error al reservar", android.widget.Toast.LENGTH_LONG).show());
                                    }
                                }).start();
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }

            // --- Diálogo de comentar mejorado ---
            holder.btnComentar.setOnClickListener(v -> {
                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null);
                android.widget.EditText etComment = dialogView.findViewById(R.id.etComment);
                android.widget.TextView tvCharCount = dialogView.findViewById(R.id.tvCharCount);
                android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();
                dialogView.findViewById(R.id.btnCancel).setOnClickListener(bv -> dialog.dismiss());
                etComment.addTextChangedListener(new android.text.TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                    @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                        tvCharCount.setText(s.length() + "/250");
                    }
                    @Override public void afterTextChanged(android.text.Editable s) {}
                });
                dialogView.findViewById(R.id.btnSend).setOnClickListener(bv -> {
                    String comentario = etComment.getText().toString().trim();
                    if (comentario.isEmpty()) {
                        etComment.setError("El comentario no puede estar vacío");
                        return;
                    }
                    dialog.dismiss();
                    // Llama al endpoint /comentario
                    new Thread(() -> {
                        try {
                            java.net.URL url = new java.net.URL("https://popnocturna.vercel.app/api/comentario");
                            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            String token = com.example.popayan_noc.AuthUtils.getToken(context);
                            if (token == null || token.isEmpty()) {
                                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                                mainHandler.post(() -> android.widget.Toast.makeText(context, "No hay sesión activa. Inicia sesión para comentar.", android.widget.Toast.LENGTH_LONG).show());
                                return;
                            }
                            conn.setRequestProperty("Authorization", "Bearer " + token);
                            conn.setDoOutput(true);
                            org.json.JSONObject body = new org.json.JSONObject();
                            body.put("eventoid", eventId);
                            body.put("contenido", comentario);
                            java.io.OutputStream os = conn.getOutputStream();
                            os.write(body.toString().getBytes("UTF-8"));
                            os.close();
                            int responseCode = conn.getResponseCode();
                            java.io.InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                            String response = s.hasNext() ? s.next() : "";
                            is.close();
                            conn.disconnect();
                            android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                            mainHandler.post(() -> {
                                if (responseCode == 201) {
                                    android.widget.Toast.makeText(context, "Comentario enviado", android.widget.Toast.LENGTH_SHORT).show();
                                    // Refresca comentarios
                                    commentsCache.remove(eventId);
                                    new Thread(() -> {
                                        try {
                                            java.net.URL url2 = new java.net.URL("https://popnocturna.vercel.app/api/comentario?eventoid=" + eventId);
                                            java.net.HttpURLConnection conn2 = (java.net.HttpURLConnection) url2.openConnection();
                                            conn2.setRequestMethod("GET");
                                            conn2.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                                            java.io.InputStream is2 = conn2.getInputStream();
                                            java.util.Scanner s2 = new java.util.Scanner(is2).useDelimiter("\\A");
                                            String response2 = s2.hasNext() ? s2.next() : "";
                                            is2.close();
                                            conn2.disconnect();
                                            JSONArray comentarios2 = new JSONArray(response2);
                                            commentsCache.put(eventId, comentarios2);
                                            mainHandler.post(() -> {
                                                holder.rvComments.setAdapter(new CommentAdapter(context, comentarios2));
                                            });
                                        } catch (Exception ignored) {}
                                    }).start();
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
        }
        // Animación estándar de Android para entrada
        holder.itemView.setAnimation(android.view.animation.AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
    }

    @Override
    public int getItemCount() {
        return eventList.length();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvEventDescription;
        android.widget.Button btnComentar;
        RecyclerView rvComments;
        android.widget.Button btnCalificar;
        android.widget.Button btnReservar;
        public EventViewHolder(@NonNull View itemView, RecyclerView rvComments) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
            btnComentar = itemView.findViewById(R.id.btnComentar);
            this.rvComments = rvComments;
            btnCalificar = itemView.findViewById(R.id.btnCalificar);
            btnReservar = itemView.findViewById(R.id.btnReservar);
        }
    }
}
