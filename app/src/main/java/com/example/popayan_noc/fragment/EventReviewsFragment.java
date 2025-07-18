package com.example.popayan_noc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.popayan_noc.R;
import com.example.popayan_noc.adapter.CommentAdapter; // <-- Usa tu adaptador correcto aquí
import com.example.popayan_noc.adapter.RatingsAdapter;
import com.example.popayan_noc.service.EventApi;
import com.example.popayan_noc.util.AuthUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class EventReviewsFragment extends Fragment {
    private static final String BASE_URL = "https://popnocturna.vercel.app/api";
    private RecyclerView rvRatings, rvComments;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabWriteComment;
    private Button btnLoadMore;
    private int lugarId;
    private CommentAdapter commentsAdapter; // ← Tipo correcto
    private RatingsAdapter ratingsAdapter;
    private ArrayList<JSONObject> allComments = new ArrayList<>();
    private ArrayList<JSONObject> allRatings = new ArrayList<>();
    private int totalEvents = 0;
    private int loadedEvents = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lugarId = getArguments().getInt("lugarId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_reviews, container, false);

        rvRatings = view.findViewById(R.id.rvRatings);
        rvComments = view.findViewById(R.id.rvComments);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        fabWriteComment = view.findViewById(R.id.fabWriteComment);
        btnLoadMore = view.findViewById(R.id.btnLoadMore);

        // Adaptadores
        commentsAdapter = new CommentAdapter(getContext(), new JSONArray()); // ← Instancia válida
        ratingsAdapter = new RatingsAdapter();

        rvRatings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRatings.setAdapter(ratingsAdapter);
        rvComments.setAdapter(commentsAdapter);

        swipeRefresh.setOnRefreshListener(this::loadEventReviews);

        fabWriteComment.setOnClickListener(v -> showWriteCommentDialog());
        btnLoadMore.setOnClickListener(v -> loadMoreComments());

        loadEventReviews();
        return view;
    }

    private void loadEventReviews() {
        String token = AuthUtils.getToken(getContext());
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "No hay sesión iniciada", Toast.LENGTH_SHORT).show();
            return;
        }

        EventApi.getEventosByLugar(getContext(), token, lugarId, response -> {
            try {
                totalEvents = response.length();
                loadedEvents = 0;
                allComments.clear();
                allRatings.clear();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject event = response.getJSONObject(i);
                    int eventId = event.getInt("id");
                    getEventComments(eventId, token);
                    getEventRatings(eventId, token);
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al cargar reseñas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(getContext(), "Error al cargar eventos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void getEventComments(int eventId, String token) {
        EventApi.getEventComments(getContext(), token, eventId, response -> {
            try {
                for (int i = 0; i < response.length(); i++) {
                    JSONObject comment = response.getJSONObject(i);
                    allComments.add(comment);
                    Log.d("COMMENT_DEBUG", "Comentario recibido: " + comment.toString());
                }
                loadedEvents++;
                if (loadedEvents >= totalEvents) {
                    updateCommentsRecyclerView(new JSONArray(allComments));
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al procesar comentarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(getContext(), "Error al cargar comentarios: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void getEventRatings(int eventId, String token) {
        EventApi.getEventRatings(getContext(), token, eventId, response -> {
            try {
                for (int i = 0; i < response.length(); i++) {
                    allRatings.add(response.getJSONObject(i));
                }
                loadedEvents++;
                if (loadedEvents >= totalEvents) {
                    updateRatingsRecyclerView(new JSONArray(allRatings));
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al procesar calificaciones: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(getContext(), "Error al cargar calificaciones: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateCommentsRecyclerView(JSONArray comments) {
        commentsAdapter = new CommentAdapter(getContext(), comments); // ← Nueva instancia con nuevos datos
        rvComments.setAdapter(commentsAdapter);
        swipeRefresh.setRefreshing(false);
    }

    private void updateRatingsRecyclerView(JSONArray ratings) {
        ArrayList<JSONObject> ratingList = new ArrayList<>();
        for (int i = 0; i < ratings.length(); i++) {
            try {
                ratingList.add(ratings.getJSONObject(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ratingsAdapter.setRatings(ratingList);
        swipeRefresh.setRefreshing(false);
    }

    private void showWriteCommentDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_comment, null);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        TextView tvCharCount = dialogView.findViewById(R.id.tvCharCount);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView)
                .setTitle("Escribe tu comentario")
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String comment = etComment.getText().toString().trim();
                    if (!comment.isEmpty()) {
                        // Aquí iría la lógica para enviar el comentario
                        Toast.makeText(getContext(), "Comentario enviado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "El comentario no puede estar vacío", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadMoreComments() {
        Toast.makeText(getContext(), "Cargando más comentarios...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rvRatings = null;
        rvComments = null;
        swipeRefresh = null;
        fabWriteComment = null;
        btnLoadMore = null;
    }
}
