package com.example.popayan_noc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.popayan_noc.R;
import com.example.popayan_noc.model.Events;

import java.util.List;

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

    public void setEvents(List<Events> events) {
        this.eventList = events;
        notifyDataSetChanged();
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

        holder.tvEventName.setText(event.getNombre());

        holder.tvEventPrice.setText(event.getPrecio());
        holder.tvEventPlace.setText(event.getLugar().getNombre());

        List<String> portadaUrls = event.getPortada();
        String imageUrl = null;

        if (portadaUrls != null && !portadaUrls.isEmpty()) {
            imageUrl = portadaUrls.get(0);
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.logo_popayan_nocturna)
                .error(R.drawable.logo_popayan_nocturna)
                .into(holder.imageViewPortada);

        holder.itemView.setOnClickListener(v -> listener.onEventClick(event, position));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvEventPrice, tvEventPlace;
        ImageView imageViewPortada;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventPrice = itemView.findViewById(R.id.tvEventPrice);
            tvEventPlace = itemView.findViewById(R.id.tvEventPlace);
            imageViewPortada = itemView.findViewById(R.id.imageViewPortada);
        }
    }

    // Método de ejemplo para formatear la fecha (ajusta según tu necesidad)
    private String formatDate(String dateTimeString) {

        return dateTimeString.split("T")[0];
    }
}