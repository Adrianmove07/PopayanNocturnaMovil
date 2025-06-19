package com.example.popayan_noc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.popayan_noc.Place;
import com.example.popayan_noc.R;

import java.util.List;

public class FavoritePlaceAdapter extends RecyclerView.Adapter<FavoritePlaceAdapter.FavoriteViewHolder> {
    private final List<Place> favoritePlaces;
    private final Context context;

    public FavoritePlaceAdapter(Context context, List<Place> favoritePlaces) {
        this.context = context;
        this.favoritePlaces = favoritePlaces;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_place, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Place place = favoritePlaces.get(position);
        holder.tvPlaceName.setText(place.nombre);
        holder.tvPlaceZone.setText(place.ubicacion);
        // TODO: Cargar imagen real si existe
        holder.ivPlacePhoto.setImageResource(R.drawable.ic_placeholder);
        holder.ivFavorite.setImageResource(R.drawable.ic_favorite);
    }

    @Override
    public int getItemCount() {
        return favoritePlaces.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlacePhoto, ivFavorite;
        TextView tvPlaceName, tvPlaceZone;
        CardView cardView;
        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlacePhoto = itemView.findViewById(R.id.ivPlacePhoto);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceZone = itemView.findViewById(R.id.tvPlaceZone);
            cardView = (CardView) itemView;
        }
    }
}
