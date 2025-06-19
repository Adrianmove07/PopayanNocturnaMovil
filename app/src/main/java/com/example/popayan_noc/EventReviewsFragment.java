package com.example.popayan_noc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.AuthFailureError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONArray;

public class EventReviewsFragment extends Fragment {
    private static final String BASE_URL = "https://popnocturna.vercel.app/api";
    private RecyclerView rvRatings, rvComments;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabWriteComment;
    private Button btnLoadMore;
    private int lugarId;
    private CommentsAdapter commentsAdapter;
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

        // Inicializar vistas
        rvRatings = view.findViewById(R.id.rvRatings);
        rvComments = view.findViewById(R.id.rvComments);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        fabWriteComment = view.findViewById(R.id.fabWriteComment);
        btnLoadMore = view.findViewById(R.id.btnLoadMore);

        // Inicializar y configurar RecyclerViews
        commentsAdapter = new CommentsAdapter();
        ratingsAdapter = new RatingsAdapter();
        
        rvRatings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRatings.setAdapter(ratingsAdapter);
        rvComments.setAdapter(commentsAdapter);

        // Configurar SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this::loadEventReviews);
        
        // Configurar botones
        fabWriteComment.setOnClickListener(v -> showWriteCommentDialog());
        btnLoadMore.setOnClickListener(v -> loadMoreComments());
        // Configurar FAB
        fabWriteComment.setOnClickListener(v -> {
            // Abrir diálogo para escribir comentario
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_comment, null);
            EditText etComment = dialogView.findViewById(R.id.etComment);
            new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()
                    .show();
        });

        // Cargar datos iniciales
        loadEventReviews();

        return view;
    }

    private void loadEventReviews() {
        // Obtener el token de autenticación
        String token = AuthUtils.getToken(getContext());
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "No hay sesión iniciada", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener eventos del lugar
        EventApi.getEventosByLugar(getContext(), token, lugarId, response -> {
            try {
                totalEvents = response.length();
                loadedEvents = 0;
                allComments.clear();
                allRatings.clear();

                // Obtener comentarios y calificaciones de cada evento
                for (int i = 0; i < response.length(); i++) {
                    JSONObject event = response.getJSONObject(i);
                    int eventId = event.getInt("id");
                    
                    // Obtener comentarios del evento
                    getEventComments(eventId, token);
                    // Obtener calificaciones del evento
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
                    allComments.add(response.getJSONObject(i));
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // No necesitamos hacer nada aquí ya que todo se inicializa en onCreateView
    }

    private void updateCommentsRecyclerView(JSONArray comments) {
        ArrayList<JSONObject> commentList = new ArrayList<>();
        for (int i = 0; i < comments.length(); i++) {
            try {
                commentList.add(comments.getJSONObject(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        commentsAdapter.setComments(commentList);
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
        // Aquí iría la lógica para cargar más comentarios
        Toast.makeText(getContext(), "Cargando más comentarios...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        rvRatings = null;
        rvComments = null;
        swipeRefresh = null;
        fabWriteComment = null;
        btnLoadMore = null;
    }
} // Fin de la clase EventReviewsFragment
