package com.example.popayan_noc.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.popayan_noc.R;
import com.example.popayan_noc.adapter.EventCardAdapter;
import com.example.popayan_noc.model.Events;
import com.example.popayan_noc.service.EventsApi;
import com.example.popayan_noc.util.AuthUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EventsListFragment extends Fragment implements EventCardAdapter.OnEventClickListener {

    private static final String TAG = "EventsListFragment";
    private static final String ARG_PLACE_ID = "place_id";
    private static final String ARG_PLACE_NAME = "place_name";

    private int placeId;
    private String placeName;

    private TextView tvEventsTitle;
    private RecyclerView rvEventsList;
    private EventCardAdapter eventCardAdapter;
    private List<Events> eventList;
    private ProgressBar progressBarEvents;
    private TextView tvNoEventsMessage;

    private String authToken;

    public EventsListFragment() {
        // Constructor público vacío requerido
    }

    public static EventsListFragment newInstance(int placeId, String placeName) {
        EventsListFragment fragment = new EventsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLACE_ID, placeId);
        args.putString(ARG_PLACE_NAME, placeName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getInt(ARG_PLACE_ID);
            placeName = getArguments().getString(ARG_PLACE_NAME);
            Log.d(TAG, "Fragment created for place: " + placeName + " (ID: " + placeId + ")");
        } else {
            Log.e(TAG, "EventsListFragment started without place ID or name arguments.");
        }
        authToken = AuthUtils.getToken(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_list, container, false);

        tvEventsTitle = view.findViewById(R.id.tvEventsTitle);
        rvEventsList = view.findViewById(R.id.rvEventsList);
        progressBarEvents = view.findViewById(R.id.progressBarEvents);
        tvNoEventsMessage = view.findViewById(R.id.tvNoEventsMessage);

        if (placeName != null && !placeName.isEmpty()) {
            tvEventsTitle.setText("Eventos en " + capitalize(placeName));
        } else {
            tvEventsTitle.setText("Eventos");
        }

        eventList = new ArrayList<>();
        rvEventsList.setLayoutManager(new LinearLayoutManager(getContext()));
        eventCardAdapter = new EventCardAdapter(getContext(), eventList, this);
        rvEventsList.setAdapter(eventCardAdapter);

        loadEventsByPlace(placeId);

        return view;
    }

    private void loadEventsByPlace(int placeId) {
        if (getContext() == null || authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "Contexto nulo o token no válido. No se pueden cargar eventos.");
            Toast.makeText(getContext(), "Error: Token de autorización no disponible.", Toast.LENGTH_LONG).show();
            progressBarEvents.setVisibility(View.GONE);
            tvNoEventsMessage.setVisibility(View.VISIBLE);
            return;
        }

        progressBarEvents.setVisibility(View.VISIBLE);
        rvEventsList.setVisibility(View.GONE);
        tvNoEventsMessage.setVisibility(View.GONE);

        Log.d(TAG, "Cargando eventos para lugar ID: " + placeId);

        EventsApi.getEventosByLugar(getContext(), authToken, placeId,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBarEvents.setVisibility(View.GONE);
                        Log.d(TAG, "Respuesta de eventos: " + response.toString());
                        List<Events> fetchedEvents = new ArrayList<>();
                        try {
                            JSONArray datosArray = response.getJSONArray("datos");

                            if (datosArray.length() > 0) {
                                for (int i = 0; i < datosArray.length(); i++) {
                                    JSONObject eventObject = datosArray.getJSONObject(i);

                                    int id = eventObject.getInt("id");
                                    String nombre = eventObject.getString("nombre");
                                    int capacidad = eventObject.getInt("capacidad");
                                    String precio = eventObject.getString("precio");
                                    String descripcion = eventObject.getString("descripcion");
                                    String fechaHora = eventObject.getString("fecha_hora");
                                    boolean estado = eventObject.getBoolean("estado");
                                    int usuarioid = eventObject.getInt("usuarioid");

                                    List<String> portadaUrls = new ArrayList<>(); // Usaremos esta lista directamente
                                    if (eventObject.has("portada") && !eventObject.isNull("portada")) {
                                        if (eventObject.get("portada") instanceof JSONArray) {
                                            JSONArray portadaJsonArray = eventObject.getJSONArray("portada");
                                            for (int j = 0; j < portadaJsonArray.length(); j++) {
                                                String url = portadaJsonArray.getString(j);
                                                if (url != null && !url.isEmpty()) {
                                                    portadaUrls.add(url); // Añadimos cada URL del array JSON
                                                }
                                            }
                                        } else if (eventObject.get("portada") instanceof String) {
                                            // Si por alguna razón la API envía un solo string directamente (y no un array JSON)
                                            String singleUrl = eventObject.getString("portada");
                                            if (singleUrl != null && !singleUrl.isEmpty()) {
                                                portadaUrls.add(singleUrl);
                                            }
                                        }
                                    }
                                    // La lista 'portadaUrls' ya está lista para pasarse al constructor de Events

                                    JSONObject lugarObj = eventObject.getJSONObject("lugar");
                                    int lugarIdAnidado = lugarObj.getInt("id");
                                    String lugarNombreAnidado = lugarObj.getString("nombre");
                                    Events.LugarSimple lugarSimple = new Events.LugarSimple(lugarIdAnidado, lugarNombreAnidado);

                                    fetchedEvents.add(new Events(id, nombre, capacidad, precio, descripcion, fechaHora, estado, usuarioid, portadaUrls, lugarSimple)); // Pasamos portadaUrls directamente
                                }
                                eventCardAdapter.setEvents(fetchedEvents);
                                rvEventsList.setVisibility(View.VISIBLE);
                                tvNoEventsMessage.setVisibility(View.GONE);
                                Log.d(TAG, "Eventos cargados: " + fetchedEvents.size());
                            } else {
                                Toast.makeText(getContext(), "No hay eventos para este lugar.", Toast.LENGTH_SHORT).show();
                                eventCardAdapter.setEvents(new ArrayList<>());
                                rvEventsList.setVisibility(View.GONE);
                                tvNoEventsMessage.setVisibility(View.VISIBLE);
                                Log.d(TAG, "No se encontraron eventos para el lugar: " + placeName);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error al parsear JSON de eventos: " + e.getMessage(), e);
                            Toast.makeText(getContext(), "Error al procesar datos de eventos.", Toast.LENGTH_SHORT).show();
                            eventCardAdapter.setEvents(new ArrayList<>());
                            rvEventsList.setVisibility(View.GONE);
                            tvNoEventsMessage.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBarEvents.setVisibility(View.GONE);
                        rvEventsList.setVisibility(View.GONE);
                        tvNoEventsMessage.setVisibility(View.VISIBLE);

                        String errorMessage = "Error al cargar eventos.";
                        if (error.networkResponse != null) {
                            errorMessage = "Error de red: " + error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                Log.e(TAG, "Cuerpo de error de la API (eventos): " + responseBody);
                                JSONObject jsonError = new JSONObject(responseBody);
                                if (jsonError.has("message")) {
                                    errorMessage += " - " + jsonError.getString("message");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear el cuerpo del error (eventos): " + e.getMessage());
                            }
                        } else if (error.getMessage() != null) {
                            errorMessage = "Error: " + error.getMessage();
                        }
                        Log.e(TAG, "Error al cargar eventos: " + errorMessage, error);
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        eventCardAdapter.setEvents(new ArrayList<>());
                    }
                });
    }

    @Override
    public void onEventClick(Events event, int position) {
        Log.d(TAG, "Evento clicado: " + event.getNombre() + " en posición: " + position);
        Toast.makeText(getContext(), "Has hecho clic en el evento: " + event.getNombre(), Toast.LENGTH_SHORT).show();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}