package com.example.popayan_noc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.popayan_noc.Place;
import com.example.popayan_noc.R;
import com.example.popayan_noc.adapter.FavoritePlaceAdapter;

import java.util.List;
import java.util.ArrayList;

public class FavoritesFragment extends Fragment {
    private RecyclerView rvFavorites;
    private TextView tvEmpty;
    private FavoritePlaceAdapter adapter;
    private List<Place> favoritePlaces = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        tvEmpty = view.findViewById(R.id.tvEmptyFavorites);

        // Dummy data para ejemplo
        favoritePlaces.add(new Place(1, 1, 1, "Restaurante El Sabor", "Comida típica de Popayán", "Centro", true, "", true));
        favoritePlaces.add(new Place(2, 2, 1, "Bar La Noche", "Tragos y música en vivo", "Zona Rosa", true, "", true));

        adapter = new FavoritePlaceAdapter(getContext(), favoritePlaces);
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(adapter);
        updateEmptyState();
        return view;
    }

    private void updateEmptyState() {
        if (favoritePlaces.isEmpty()) {
            rvFavorites.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvFavorites.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
