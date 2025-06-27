package com.example.popayan_noc.fragment;

import android.Manifest; // Importar para permisos de ubicación
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager; // Importar para PackageManager
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager; // Import for FragmentManager
import androidx.fragment.app.FragmentTransaction; // Import for FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.popayan_noc.R;
import com.example.popayan_noc.activity.LoginActivity;
import com.example.popayan_noc.adapter.EventAdapter;
import com.example.popayan_noc.adapter.PlaceAdapter;
import com.example.popayan_noc.util.AuthUtils;
import com.google.android.material.tabs.TabLayout;

import com.example.popayan_noc.model.Lugar;
import com.example.popayan_noc.model.Evento;
import com.example.popayan_noc.network.ApiService;
import com.example.popayan_noc.network.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TabLayout tabLayoutMainSelection;
    private LinearLayout llEventsSection;
    private LinearLayout llPlacesSection;

    private RecyclerView rvFeaturedPlaces;
    private PlaceAdapter placeAdapter;

    private RecyclerView rvEvents;
    private EventAdapter eventAdapter;

    private List<Lugar> lugaresList = new ArrayList<>();
    private List<Evento> eventList = new ArrayList<>();

    private TextView tvLugaresCount;
    private TextView tvEventosCount;
    private TextView tvRating;

    private TextView tvNoLugares;
    private TextView tvNoEvents;

    private RequestQueue queue;
    private static final String BASE_URL = "https://popnocturna.vercel.app/api";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        if (queue == null) {
            queue = Volley.newRequestQueue(requireContext());
        }

        tabLayoutMainSelection = view.findViewById(R.id.tabLayout_main_selection);
        llEventsSection = view.findViewById(R.id.llEventsSection);
        llPlacesSection = view.findViewById(R.id.llPlacesSection);

        rvFeaturedPlaces = view.findViewById(R.id.rvFeaturedPlaces);
        rvEvents = view.findViewById(R.id.rvEvents);

        tvLugaresCount = view.findViewById(R.id.tvLugaresCount);
        tvEventosCount = view.findViewById(R.id.tvEventosCount);
        tvRating = view.findViewById(R.id.tvRating);

        tvNoLugares = view.findViewById(R.id.tvNoLugares);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);

        rvFeaturedPlaces.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        tabLayoutMainSelection.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) { // Pestaña "Eventos" seleccionada
                    llEventsSection.setVisibility(View.VISIBLE);
                    llPlacesSection.setVisibility(View.GONE);
                    cargarEventos();
                } else if (tab.getPosition() == 1) { // Pestaña "Lugares" seleccionada
                    llEventsSection.setVisibility(View.GONE);
                    llPlacesSection.setVisibility(View.VISIBLE);
                    cargarLugares();
                } else if (tab.getPosition() == 2) { // Pestaña "Planea con nosotros" seleccionada
                    // Navigate to TodayDoFragment
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new TodayDoFragment()) // Assuming R.id.fragment_container is where your fragments are displayed
                                .addToBackStack(null) // Optional: Allows going back to HomeFragment
                                .commit();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { /* No-op */ }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { /* No-op */ }
        });

        // Set initial tab selection
        tabLayoutMainSelection.getTabAt(0).select(); // Select "Eventos" initially
        llEventsSection.setVisibility(View.VISIBLE);
        llPlacesSection.setVisibility(View.GONE);

        cargarEventos();
        cargarLugares();

        tvRating.setText("4.9");

        /*View fabMap = view.findViewById(R.id.fabMap);
        if (fabMap != null) {
            fabMap.setOnClickListener(v -> mostrarDialogoMapa());
        }*/
        return view;
    }

    private void cargarLugares() {
        if (getContext() == null) {
            Log.e("HomeFragment", "Contexto nulo en cargarLugares.");
            return;
        }

        String token = AuthUtils.getToken(getContext());

        if (token == null || token.isEmpty()) {
            rvFeaturedPlaces.setVisibility(View.GONE);
            tvNoLugares.setVisibility(View.VISIBLE);
            Log.e("HomeFragment", "Token de usuario no disponible. No se puede cargar lugares.");
            tvLugaresCount.setText("0");
            Toast.makeText(getContext(), "Token no disponible, no se cargan lugares", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Lugar>> call = apiService.listarLugares("Bearer " + token);

        call.enqueue(new Callback<List<Lugar>>() {
            @Override
            public void onResponse(@NonNull Call<List<Lugar>> call, @NonNull Response<List<Lugar>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    lugaresList = response.body();

                    if (!lugaresList.isEmpty()) {
                        placeAdapter = new PlaceAdapter(getContext(), lugaresList);
                        rvFeaturedPlaces.setAdapter(placeAdapter);
                        rvFeaturedPlaces.setVisibility(View.VISIBLE);
                        tvNoLugares.setVisibility(View.GONE);
                        tvLugaresCount.setText(String.valueOf(lugaresList.size()));
                    } else {
                        Log.w("HomeFragment", "No hay lugares disponibles.");
                        rvFeaturedPlaces.setVisibility(View.GONE);
                        tvNoLugares.setVisibility(View.VISIBLE);
                        tvLugaresCount.setText("0");
                        Toast.makeText(getContext(), "No hay lugares destacados disponibles.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.w("HomeFragment", "Respuesta no exitosa o cuerpo nulo para lugares: " + response.code());
                    rvFeaturedPlaces.setVisibility(View.GONE);
                    tvNoLugares.setVisibility(View.VISIBLE);
                    tvLugaresCount.setText("0");
                    Toast.makeText(getContext(), "Error en la respuesta del servidor al cargar lugares.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Lugar>> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;

                Log.e("HomeFragment", "Error cargando lugares: " + t.getMessage(), t);
                rvFeaturedPlaces.setVisibility(View.GONE);
                tvNoLugares.setVisibility(View.VISIBLE);
                tvLugaresCount.setText("0");
                Toast.makeText(getContext(), "Error de red al cargar lugares: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarEventos() {
        if (getContext() == null) {
            Log.e("HomeFragment", "Contexto nulo en cargarEventos.");
            return;
        }

        String url = BASE_URL + "/eventos";
        String token = AuthUtils.getToken(getContext());

        if (token == null || token.isEmpty()) {
            rvEvents.setVisibility(View.GONE);
            tvNoEvents.setVisibility(View.VISIBLE);
            Log.e("HomeFragment", "Token de usuario no disponible. No se puede cargar eventos.");
            tvEventosCount.setText("0");
            Toast.makeText(getContext(), "Token no disponible, no se cargan eventos", Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    if (!isAdded() || getContext() == null) return;
                    try {
                        JSONArray eventosArray = response.optJSONArray("datos");
                        eventList.clear();

                        if (eventosArray != null && eventosArray.length() > 0) {
                            for (int i = 0; i < eventosArray.length(); i++) {
                                JSONObject eventJson = eventosArray.getJSONObject(i);
                                // Crear un objeto Evento a partir del JSONObject
                                Evento evento = new Evento();
                                evento.setId(eventJson.optInt("id"));
                                evento.setNombre(eventJson.optString("nombre"));
                                evento.setDescripcion(eventJson.optString("descripcion"));
                                evento.setFechaHora(eventJson.optString("fecha_hora"));
                                evento.setCapacidad(eventJson.optInt("capacidad"));
                                evento.setPrecio(eventJson.optString("precio"));
                                // Manejar la lista de portadas si existe
                                JSONArray portadaArray = eventJson.optJSONArray("portada");
                                if (portadaArray != null && portadaArray.length() > 0) {
                                    List<String> portadas = new ArrayList<>();
                                    for (int j = 0; j < portadaArray.length(); j++) {
                                        portadas.add(portadaArray.optString(j));
                                    }
                                    evento.setPortada(portadas);
                                }
                                // Si tu JSON incluye datos de Lugar anidados:
                                JSONObject lugarJson = eventJson.optJSONObject("lugar");
                                if (lugarJson != null) {
                                    Lugar lugar = new Lugar();
                                    lugar.setId(lugarJson.optInt("id"));
                                    lugar.setNombre(lugarJson.optString("nombre"));

                                    // Asumiendo que Lugar también tiene un campo 'imageUrl' o similar
                                    // si es un String o una lista de Strings
                                    JSONArray lugarPortadaArray = lugarJson.optJSONArray("portada");
                                    if (lugarPortadaArray != null && lugarPortadaArray.length() > 0) {
                                        List<String> lugarPortadas = new ArrayList<>();
                                        for(int k=0; k<lugarPortadaArray.length(); k++){
                                            lugarPortadas.add(lugarPortadaArray.optString(k));
                                        }

                                    }
                                    evento.setLugar(lugar);
                                }
                                evento.setEstado(eventJson.optString("estado")); // Estado del evento

                                eventList.add(evento);
                            }

                            eventAdapter = new EventAdapter(getContext(), eventList); // Pasa la lista de objetos Evento
                            rvEvents.setAdapter(eventAdapter);
                            rvEvents.setVisibility(View.VISIBLE);
                            tvNoEvents.setVisibility(View.GONE);
                            tvEventosCount.setText(String.valueOf(eventList.size()));
                        } else {
                            Log.w("HomeFragment", "No hay eventos disponibles en la respuesta.");
                            rvEvents.setVisibility(View.GONE);
                            tvNoEvents.setVisibility(View.VISIBLE);
                            tvEventosCount.setText("0");
                        }
                    } catch (JSONException e) {
                        Log.e("HomeFragment", "Error parseando JSON de eventos: " + e.getMessage(), e);
                        rvEvents.setVisibility(View.GONE);
                        tvNoEvents.setVisibility(View.VISIBLE);
                        tvEventosCount.setText("0");
                        Toast.makeText(getContext(), "Error al procesar eventos.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    if (!isAdded() || getContext() == null) return;
                    Log.e("HomeFragment", "Error cargando eventos: " + error.toString(), error);
                    rvEvents.setVisibility(View.GONE);
                    tvNoEvents.setVisibility(View.VISIBLE);
                    tvEventosCount.setText("0");
                    Toast.makeText(getContext(), "Error de red al cargar eventos.", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() throws com.android.volley.AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + Objects.requireNonNull(token));
                return headers;
            }
        };
        queue.add(request);
    }

    private void actualizarMarcadoresMapa(org.osmdroid.views.MapView mapView, org.osmdroid.api.IMapController mapController) {
        mapView.getOverlays().clear();
        for (Lugar lugar : lugaresList) {
            // Your existing logic for adding markers to the map
        }
        mapView.invalidate();
    }

    private void mostrarDialogoMapa() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_map, null);
        org.osmdroid.views.MapView dialogMapView = dialogView.findViewById(R.id.osmMapViewDialog);
        dialogMapView.setMultiTouchControls(true);
        org.osmdroid.api.IMapController dialogMapController = dialogMapView.getController();

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            Toast.makeText(getContext(), "Permisos de ubicación necesarios para el mapa.", Toast.LENGTH_LONG).show();
            return;
        }

        dialogMapController.setZoom(15.0);
        dialogMapController.setCenter(new GeoPoint(2.4419, -76.6063));
        actualizarMarcadoresMapa(dialogMapView, dialogMapController);

        builder.setView(dialogView);
        builder.setNegativeButton("Cerrar", (d, w) -> d.dismiss());
        builder.show();
    }
}