package com.example.popayan_noc.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.TextView;

import com.example.popayan_noc.R;
import com.example.popayan_noc.service.ReviewApi;
import com.example.popayan_noc.util.AuthUtils;

public class WriteReviewFragment extends Fragment {
    private RatingBar rbRating;
    private EditText etReviewText;
    private Button btnSendReview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_review, container, false);
        rbRating = view.findViewById(R.id.rbRating);
        etReviewText = view.findViewById(R.id.etReviewText);
        btnSendReview = view.findViewById(R.id.btnSendReview);
        TextView tvWriteReviewTitle = view.findViewById(R.id.tvWriteReviewTitle);
        int lugarId = getArguments() != null ? getArguments().getInt("lugarId", 1) : 1;
        String lugarNombre = getArguments() != null ? getArguments().getString("lugarNombre", "") : "";
        if (tvWriteReviewTitle != null && !lugarNombre.isEmpty()) {
            tvWriteReviewTitle.setText("Reservar/Reseñar: " + lugarNombre);
        }

        btnSendReview.setOnClickListener(v -> {
            float rating = rbRating.getRating();
            String comment = etReviewText.getText().toString();
            if (rating == 0 || TextUtils.isEmpty(comment)) {
                Toast.makeText(getContext(), "Por favor califica y escribe tu reseña", Toast.LENGTH_SHORT).show();
                return;
            }
            // Enviar reseña real al backend
            int eventoId = lugarId; // Usar el lugarId recibido como id de evento/lugar
            String token = AuthUtils.getToken(requireContext());
            org.json.JSONObject data = new org.json.JSONObject();
            try {
                data.put("eventoid", eventoId);
                data.put("comentario", comment);
                data.put("calificacion", rating);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al preparar datos", Toast.LENGTH_SHORT).show();
                return;
            }
            btnSendReview.setEnabled(false);
            ReviewApi.postReview(requireContext(), token, data, response -> {
                Toast.makeText(getContext(), "¡Gracias por tu reseña!", Toast.LENGTH_SHORT).show();
                btnSendReview.setEnabled(true);
                requireActivity().getSupportFragmentManager().popBackStack();
            }, error -> {
                Toast.makeText(getContext(), "Error al enviar reseña", Toast.LENGTH_SHORT).show();
                btnSendReview.setEnabled(true);
            });
        });
        return view;
    }
}
