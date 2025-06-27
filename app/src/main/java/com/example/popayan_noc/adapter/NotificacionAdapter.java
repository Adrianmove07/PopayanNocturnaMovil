package com.example.popayan_noc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.popayan_noc.R;
import com.example.popayan_noc.model.Notificacion;
import com.example.popayan_noc.service.NotificacionApi;

import org.json.JSONObject;

import java.util.List;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.ViewHolder> {
    private List<Notificacion> lista;
    private Context context;
    private String token;

    public NotificacionAdapter(List<Notificacion> lista, Context context, String token) {
        this.lista = lista;
        this.context = context;
        this.token = token;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtCuerpo;
        ImageView imgNotification, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.tvNotificationTitle);
            txtCuerpo = itemView.findViewById(R.id.tvNotificationMessage);
            imgNotification = itemView.findViewById(R.id.imgNotification);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Notificacion noti, Context context) {
            txtTitulo.setText(noti.titulo);
            txtCuerpo.setText(noti.cuerpo);
            Glide.with(context)
                    .load(noti.imagen)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_report)
                    .into(imgNotification);
        }
    }

    @NonNull
    @Override
    public NotificacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificacionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionAdapter.ViewHolder holder, int position) {
        Notificacion noti = lista.get(position);
        holder.bind(noti, context);

        holder.btnDelete.setOnClickListener(v -> {
            NotificacionApi.eliminarNotificacion(context, token, noti.id,
                    (JSONObject response) -> {
                        Toast.makeText(context, "Notificación eliminada", Toast.LENGTH_SHORT).show();
                        lista.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, lista.size());
                    },
                    error -> {
                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
            );
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
