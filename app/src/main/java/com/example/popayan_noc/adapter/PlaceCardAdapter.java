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
import com.example.popayan_noc.model.Place;

import java.util.ArrayList;
import java.util.List;

public class PlaceCardAdapter extends RecyclerView.Adapter<PlaceCardAdapter.PlaceViewHolder> {

    private Context context;
    private List<Place> placeList;
    private OnPlaceClickListener listener;

    // Interfaz para manejar clics en los ítems del lugar
    public interface OnPlaceClickListener {
        void onPlaceClick(Place place, int position); // Asegúrate de que esta sea la firma
    }

    public PlaceCardAdapter(Context context, List<Place> placeList, OnPlaceClickListener listener) {
        this.context = context;
        this.placeList = placeList != null ? placeList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_card, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);

        if (place.getImagen() != null && !place.getImagen().isEmpty()) {
            Glide.with(context)
                    .load(place.getImagen())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_category_placeholder)
                            .error(R.drawable.ic_category_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop())
                    .into(holder.imgPlaceMain);
        } else {
            holder.imgPlaceMain.setImageResource(R.drawable.ic_category_placeholder);
            Log.w("PlaceCardAdapter", "No image URL for place: " + place.getNombre() + ". Using default.");
        }

        holder.tvPlaceName.setText(place.getNombre() != null && !place.getNombre().isEmpty() ? place.getNombre() : "Lugar Desconocido");
        holder.tvPlaceDescription.setText(place.getDescripcion() != null && !place.getDescripcion().isEmpty() ? place.getDescripcion() : "Sin descripción.");
        holder.tvPlaceLocation.setText(place.getUbicacion() != null && !place.getUbicacion().isEmpty() ? place.getUbicacion() : "Ubicación no disponible.");

        // Configurar click listener para el ítem completo
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlaceClick(place, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public void setPlaces(List<Place> newPlaces) {
        this.placeList.clear();
        if (newPlaces != null) {
            this.placeList.addAll(newPlaces);
        }
        notifyDataSetChanged();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlaceMain;
        TextView tvPlaceName;
        TextView tvPlaceDescription;
        TextView tvPlaceLocation;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlaceMain = itemView.findViewById(R.id.imgPlaceMain);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceDescription = itemView.findViewById(R.id.tvPlaceDescription);
            tvPlaceLocation = itemView.findViewById(R.id.tvPlaceLocation);
        }
    }
}