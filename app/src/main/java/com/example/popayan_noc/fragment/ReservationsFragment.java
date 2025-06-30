package com.example.popayan_noc.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.example.popayan_noc.R;
import com.example.popayan_noc.adapter.ReservationsAdapter;
import com.example.popayan_noc.model.Reserva;
import com.example.popayan_noc.model.ReservaResponse;
import com.example.popayan_noc.service.ReservationApi;
import com.example.popayan_noc.util.AuthUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReservationsFragment extends Fragment {

    private static final String TAG = "ReservationsFragment";
    private String authToken;

    private RecyclerView recyclerViewReservations;
    private ProgressBar progressBar;
    private ReservationsAdapter reservationsAdapter;
    private List<Reserva> pendingReservationsList;
    private TextView tvEmptyMessage;


    public ReservationsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservations, container, false);

        recyclerViewReservations = view.findViewById(R.id.recyclerViewReservations);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        recyclerViewReservations.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingReservationsList = new ArrayList<>();
        reservationsAdapter = new ReservationsAdapter(pendingReservationsList);
        recyclerViewReservations.setAdapter(reservationsAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authToken = AuthUtils.getToken(getContext());
        if (authToken == null) {
            Toast.makeText(getContext(), "No se encontró token de autenticación. Inicie sesión nuevamente.", Toast.LENGTH_LONG).show();
            // You might want to redirect to a login screen or handle this case
            progressBar.setVisibility(View.GONE);
            recyclerViewReservations.setVisibility(View.GONE);
            return;
        }

        fetchAllUserReservationsAndFilter();
    }

    private void fetchAllUserReservationsAndFilter() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewReservations.setVisibility(View.GONE);
        tvEmptyMessage.setVisibility(View.GONE); // Ocultar mensaje al iniciar

        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Contexto es nulo, no se puede realizar la llamada a la API.");
            progressBar.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.VISIBLE); // Mostrar mensaje si no hay contexto
            return;
        }

        ReservationApi.getAllUserReservations(context, authToken,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar.setVisibility(View.GONE);

                        Log.d(TAG, "Respuesta de la API para todas las reservas: " + response.toString());

                        Gson gson = new GsonBuilder().create();
                        ReservaResponse reservaResponse = gson.fromJson(response.toString(), ReservaResponse.class);

                        if (reservaResponse != null && reservaResponse.isOk()) {
                            List<Reserva> allReservations = reservaResponse.getReservas();
                            pendingReservationsList.clear();

                            if (allReservations != null) {
                                for (Reserva reserva : allReservations) {
                                    if ("Pendiente".equalsIgnoreCase(reserva.getAprobacion())) {
                                        pendingReservationsList.add(reserva);
                                    }
                                }
                            }

                            if (pendingReservationsList.isEmpty()) {
                                // No hay reservas pendientes
                                recyclerViewReservations.setVisibility(View.GONE);
                                tvEmptyMessage.setVisibility(View.VISIBLE);
                            } else {
                                // Hay reservas pendientes
                                recyclerViewReservations.setVisibility(View.VISIBLE);
                                tvEmptyMessage.setVisibility(View.GONE);
                                reservationsAdapter.notifyDataSetChanged();
                            }
                        } else {
                            // Error en la respuesta
                            recyclerViewReservations.setVisibility(View.GONE);
                            tvEmptyMessage.setVisibility(View.VISIBLE);
                            tvEmptyMessage.setText("Error al obtener reservas");
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        recyclerViewReservations.setVisibility(View.GONE);
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        tvEmptyMessage.setText("Error al cargar reservas");

                        Log.e(TAG, "Error de la API al obtener todas las reservas: " + error.toString());
                    }
                });
    }
}