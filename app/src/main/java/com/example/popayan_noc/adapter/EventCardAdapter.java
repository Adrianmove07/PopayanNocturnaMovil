package com.example.popayan_noc.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.popayan_noc.R;
import com.example.popayan_noc.model.Events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.EventViewHolder> {

    private Context context;
    private List<Events> eventList;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Events event, int position);
    }

    public EventCardAdapter(Context context, List<Events> eventList, OnEventClickListener listener) {
        this.context = context;
        this.eventList = eventList != null ? eventList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Events event = eventList.get(position);

        // --- Manejo robusto de la imagen de portada ---
        String imageUrl = null;
        if (event.getPortada() != null && !event.getPortada().isEmpty()) {
            imageUrl = event.getPortada().get(0); // Tomamos la primera URL
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_category_placeholder) // Placeholder visual
                            .error(R.drawable.ic_placeholder)     // Imagen en caso de error de carga (puedes crear esta imagen en drawable)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop())
                    .into(holder.imgEventCover);
        } else {
            // Si no hay URL válida, usa la imagen de placeholder
            holder.imgEventCover.setImageResource(R.drawable.ic_category_placeholder);
            Log.w("EventCardAdapter", "No valid image URL for event: " + event.getNombre() + ". Using default placeholder.");
        }

        // --- Establecer textos con validaciones y formateo ---
        holder.tvEventName.setText(event.getNombre() != null && !event.getNombre().isEmpty() ? event.getNombre() : "Evento Desconocido");
        // Asegúrate de tener tvEventDescription en tu item_event_card.xml si lo quieres mostrar
        // holder.tvEventDescription.setText(event.getDescripcion() != null && !event.getDescripcion().isEmpty() ? event.getDescripcion() : "Sin descripción.");

        // Formateo de fecha y hora
        holder.tvEventDate.setText(formatDateTime(event.getFechaHora()));

        // Formateo de precio
        holder.tvEventPrice.setText(formatPrice(event.getPrecio()));

        holder.tvEventLocation.setText(event.getLugar() != null && event.getLugar().getNombre() != null ? "Lugar: " + event.getLugar().getNombre() : "Lugar no especificado");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void setEvents(List<Events> newEvents) {
        this.eventList.clear();
        if (newEvents != null) {
            this.eventList.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imgEventCover;
        TextView tvEventName;
        TextView tvEventDate; // Cambiado a tvEventDate para mayor claridad
        TextView tvEventPrice;
        TextView tvEventLocation;
        // TextView tvEventDescription; // Si lo tienes en tu layout

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imgEventCover = itemView.findViewById(R.id.imgEventCover);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate); // Asegúrate de que este ID exista en item_event_card.xml
            tvEventPrice = itemView.findViewById(R.id.tvEventPrice);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            // tvEventDescription = itemView.findViewById(R.id.tvEventDescription); // Si lo tienes en tu layout
        }
    }

    // --- Métodos de Formateo ---

    private String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "Fecha/Hora no disponible";
        }
        try {
            // Formato de entrada de tu API (ISO 8601 con milisegundos y Z para UTC)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Importante para manejar la 'Z'

            Date date = inputFormat.parse(isoDateTime);

            // Formato de salida deseado (ej. "26 de junio de 2025, 08:00 PM")
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm a", new Locale("es", "ES"));
            // Puedes ajustar la zona horaria de salida si quieres mostrarla en la zona horaria local del usuario
            // outputFormat.setTimeZone(TimeZone.getDefault()); // Opcional, si quieres la hora local

            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("EventCardAdapter", "Error al parsear la fecha: " + isoDateTime + " - " + e.getMessage());
            return "Fecha/Hora inválida";
        }
    }

    private String formatPrice(String price) {
        if (price == null || price.isEmpty()) {
            return "Precio: N/A";
        }
        try {
            double priceValue = Double.parseDouble(price);
            // Formatea como moneda colombiana (COP) con separadores de miles y sin decimales
            // Locale.getDefault() usará la configuración regional del dispositivo, lo cual es buena práctica
            return String.format(Locale.getDefault(), "Precio: $%,.0f COP", priceValue);
        } catch (NumberFormatException e) {
            Log.e("EventCardAdapter", "Error al parsear el precio: " + price + " - " + e.getMessage());
            return "Precio: Inválido";
        }
    }
}