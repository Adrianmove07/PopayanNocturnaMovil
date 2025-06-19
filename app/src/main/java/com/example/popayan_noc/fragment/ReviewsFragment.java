package com.example.popayan_noc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import com.example.popayan_noc.R;
import com.example.popayan_noc.Review;
import com.example.popayan_noc.service.ReviewApi;
import com.example.popayan_noc.adapter.ReviewAdapter;
import com.example.popayan_noc.util.AuthUtils;
import com.facebook.shimmer.ShimmerFrameLayout;

public class ReviewsFragment extends Fragment {
    private RecyclerView rvReviews;
    private LinearLayout llEmptyReviews;
    private ShimmerFrameLayout shimmerLayout;
    private ReviewAdapter adapter;
    private List<Review> reviewList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);
        rvReviews = view.findViewById(R.id.rvReviews);
        llEmptyReviews = view.findViewById(R.id.llEmptyReviews);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        if (shimmerLayout != null) shimmerLayout.startShimmer();
        Button btnWriteReview = view.findViewById(R.id.btnWriteReview);

        // Loading y reviews reales
        reviewList.clear();
        adapter = new ReviewAdapter(getContext(), reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(adapter);
        llEmptyReviews.setVisibility(View.GONE);
        rvReviews.setVisibility(View.GONE);
        if (shimmerLayout != null) {
            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();
        }
        // Obtener lugarId y lugarNombre por argumentos
        int lugarId = getArguments() != null ? getArguments().getInt("lugarId", 1) : 1;
        String lugarNombre = getArguments() != null ? getArguments().getString("lugarNombre", "") : "";
        TextView tvTitle = view.findViewById(R.id.tvReviewsTitle);
        if (tvTitle != null && !lugarNombre.isEmpty()) {
            tvTitle.setText("Reseñas de " + lugarNombre);
        }
        int eventoId = lugarId; // Usar lugarId como id de evento/lugar
        String token = AuthUtils.getToken(requireContext());
        ReviewApi.getReviewsByEvento(requireContext(), token, eventoId, response -> {
            reviewList.clear();
            for (int i = 0; i < response.length(); i++) {
                org.json.JSONObject obj = response.optJSONObject(i);
                if (obj != null) {
                    String usuario = obj.optString("usuario", "-");
                    String comentario = obj.optString("comentario", "-");
                    int calificacion = obj.optInt("calificacion", 0);
                    reviewList.add(new Review(usuario, comentario, calificacion));
                }
            }
            adapter.notifyDataSetChanged();
            updateEmptyState();
        }, error -> {
            reviewList.clear();
            adapter.notifyDataSetChanged();
            updateEmptyState();
            if (shimmerLayout != null) {
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
            }
            llEmptyReviews.setVisibility(View.VISIBLE);
            ((TextView)llEmptyReviews.findViewById(R.id.tvEmptyReviews)).setText("Error al cargar reseñas");
        });

        btnWriteReview.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new WriteReviewFragment())
                .addToBackStack(null)
                .commit();
        });
        return view;
    }

    private void updateEmptyState() {
        if (reviewList.isEmpty()) {
            rvReviews.setVisibility(View.GONE);
            llEmptyReviews.setVisibility(View.VISIBLE);
        } else {
            rvReviews.setVisibility(View.VISIBLE);
            llEmptyReviews.setVisibility(View.GONE);
        }
    }
}
