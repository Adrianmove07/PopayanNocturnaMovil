package com.example.popayan_noc.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.popayan_noc.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private final JSONArray commentList;
    private final Context context;

    public CommentAdapter(Context context, JSONArray commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        JSONObject comment = commentList.optJSONObject(position);
        if (comment != null) {
            try {
                // ---------- Usuario ----------
                String nombreUsuario = "Anónimo";
                if (comment.has("usuario") && !comment.isNull("usuario")) {
                    JSONObject usuario = comment.getJSONObject("usuario");
                    if (usuario.has("nombre")) {
                        nombreUsuario = usuario.getString("nombre");
                    }
                }
                holder.tvCommentUser.setText(nombreUsuario);
                Log.d("COMMENT_DEBUG", "Nombre usuario: " + nombreUsuario);

                // ---------- Contenido ----------
                String contenido = comment.optString("contenido", "");
                holder.tvCommentContent.setText(contenido);

                // ---------- Fecha ----------
                String fechaCompleta = comment.optString("fecha_hora", "");
                String fechaFormateada = "";
                if (!fechaCompleta.isEmpty()) {
                    try {
                        // Parsear fecha UTC (ISO 8601)
                        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                        formatoEntrada.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date fechaDate = formatoEntrada.parse(fechaCompleta);

                        // Formato de salida local
                        SimpleDateFormat formatoSalida = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        fechaFormateada = formatoSalida.format(fechaDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                holder.tvCommentDate.setText(fechaFormateada);
                Log.d("COMMENT_DEBUG", "Fecha formateada: " + fechaFormateada);

                // ---------- Estado ----------
                boolean estado = comment.optBoolean("estado", true);
                int aprobacion = comment.optInt("aprobacion", 2);
                if (!estado || aprobacion != 2) {
                    holder.tvCommentStatus.setVisibility(View.VISIBLE);
                    holder.tvCommentStatus.setText("Pendiente de aprobación");
                } else {
                    holder.tvCommentStatus.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentList.length();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser, tvCommentContent, tvCommentDate, tvCommentStatus;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            tvCommentStatus = itemView.findViewById(R.id.tvCommentStatus);
        }
    }
}
