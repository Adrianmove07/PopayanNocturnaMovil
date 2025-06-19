package com.example.popayan_noc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Button;
import android.widget.TextView;

public class OwnerPanelFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owner_panel, container, false);
        Button btnMyPlaces = view.findViewById(R.id.btnMyPlaces);
        Button btnOwnerStats = view.findViewById(R.id.btnOwnerStats);
        TextView tvMyPlacesTitle = view.findViewById(R.id.tvMyPlacesTitle);
        // TextView tvMyPlacesList = view.findViewById(R.id.tvMyPlacesList); // Corregir id si existe en layout
        com.facebook.shimmer.ShimmerFrameLayout shimmerOwnerPanel = view.findViewById(R.id.shimmerOwnerPanel);
        if (shimmerOwnerPanel != null) shimmerOwnerPanel.startShimmer();
        // tvMyPlacesList.setText("");
        tvMyPlacesTitle.setText("");
        btnMyPlaces.setOnClickListener(v -> {
            if (shimmerOwnerPanel != null) {
                shimmerOwnerPanel.setVisibility(View.VISIBLE);
                shimmerOwnerPanel.startShimmer();
            }
            // tvMyPlacesList.setVisibility(View.GONE);
            String token = AuthUtils.getToken(requireContext());
            OwnerApi.getOwnerPlaces(requireContext(), token, response -> {
                StringBuilder sb = new StringBuilder();
                if (response.length() == 0) {
                    sb.append("No tienes lugares registrados");
                } else {
                    for (int i = 0; i < response.length(); i++) {
                        org.json.JSONObject obj = response.optJSONObject(i);
                        if (obj != null) {
                            sb.append("- ").append(obj.optString("nombre", "(sin nombre)")).append("\n");
                        }
                    }
                }
                tvMyPlacesTitle.setText("Mis lugares:");
                // tvMyPlacesList.setText(sb.toString());
                if (shimmerOwnerPanel != null) {
                    shimmerOwnerPanel.stopShimmer();
                    shimmerOwnerPanel.setVisibility(View.GONE);
                }
                // tvMyPlacesList.setVisibility(View.VISIBLE);
            }, error -> {
                tvMyPlacesTitle.setText("");
                // tvMyPlacesList.setText("Error al cargar lugares");
                if (shimmerOwnerPanel != null) {
                    shimmerOwnerPanel.stopShimmer();
                    shimmerOwnerPanel.setVisibility(View.GONE);
                }
                // tvMyPlacesList.setVisibility(View.VISIBLE);
            });
        });
        // Cargar automáticamente al abrir
        btnMyPlaces.performClick();
        return view;
    }
}
