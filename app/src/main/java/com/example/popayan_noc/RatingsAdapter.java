package com.example.popayan_noc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.util.ArrayList;

public class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.RatingViewHolder> {
    private ArrayList<JSONObject> ratings;

    public RatingsAdapter() {
        ratings = new ArrayList<>();
    }

    public void setRatings(ArrayList<JSONObject> ratings) {
        this.ratings = ratings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        try {
            JSONObject rating = ratings.get(position);
            
            // Obtener el nombre del usuario
            JSONObject usuario = rating.getJSONObject("usuario");
            String userName = usuario.getString("nombre");
            holder.tvUsername.setText(userName);
            
            holder.ratingBar.setRating(rating.getInt("puntuacion"));
            
            // Obtener el nombre del lugar
            JSONObject evento = rating.getJSONObject("evento");
            String placeName = evento.getString("nombre");
            holder.tvDate.setText("En: " + placeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return ratings.size();
    }

    static class RatingViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvDate;
        RatingBar ratingBar;

        RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
