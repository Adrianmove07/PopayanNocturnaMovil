// EventsListFragment.java
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
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.LinearLayout;

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

// Implement the new interface for the filter dialog
public class EventsListFragment extends Fragment implements EventCardAdapter.OnEventClickListener,
        PriceFilterDialogFragment.PriceFilterListener {

    private static final String TAG = "EventsListFragment";
    private static final String ARG_PLACE_ID = "place_id";
    private static final String ARG_PLACE_NAME = "place_name";

    private int placeId;
    private String placeName;

    private TextView tvEventsTitle;
    private RecyclerView rvEventsList;
    private EventCardAdapter eventCardAdapter;
    private List<Events> eventList; // This list will hold the CURRENTLY DISPLAYED events (filtered)
    private List<Events> allFetchedEvents; // This list will hold ALL events fetched from API (unfiltered)
    private ProgressBar progressBarEvents;
    private TextView tvNoEventsMessage;
    private Button btnFilterEvents; // Add a button for filtering

    private String authToken;
    private String currentPriceFilter = null; // Store the currently applied filter ("gratis", "economico", etc., or null for "todos")

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
        allFetchedEvents = new ArrayList<>(); // Initialize the unfiltered list
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_list, container, false);

        tvEventsTitle = view.findViewById(R.id.tvEventsTitle);
        rvEventsList = view.findViewById(R.id.rvEventsList);
        progressBarEvents = view.findViewById(R.id.progressBarEvents);
        tvNoEventsMessage = view.findViewById(R.id.tvNoEventsMessage);
        btnFilterEvents = view.findViewById(R.id.btnFilterEvents);

        if (placeName != null && !placeName.isEmpty()) {
            tvEventsTitle.setText("Eventos en " + capitalize(placeName));
        } else {
            tvEventsTitle.setText("Eventos");
        }

        eventList = new ArrayList<>(); // This will be the list passed to the adapter
        rvEventsList.setLayoutManager(new LinearLayoutManager(getContext()));
        eventCardAdapter = new EventCardAdapter(getContext(), eventList, this);
        rvEventsList.setAdapter(eventCardAdapter);

        // Set up the filter button click listener
        btnFilterEvents.setOnClickListener(v -> showPriceFilterDialog());

        // Initial load of all events
        loadAllEventsFromApi(placeId);

        return view;
    }

    private void showPriceFilterDialog() {
        PriceFilterDialogFragment dialogFragment = new PriceFilterDialogFragment();
        dialogFragment.setPriceFilterListener(this); // Set this fragment as the listener
        dialogFragment.show(getParentFragmentManager(), "PriceFilterDialog");
    }

    @Override
    public void onPriceFilterSelected(String priceRange) {
        // This method is called when a price range is selected in the dialog
        if ("todos".equals(priceRange)) {
            currentPriceFilter = null; // Clear filter if "todos" is selected
            Toast.makeText(getContext(), "Mostrando todos los eventos.", Toast.LENGTH_SHORT).show();
        } else {
            currentPriceFilter = priceRange;
            Toast.makeText(getContext(), "Filtrando por: " + priceRange, Toast.LENGTH_SHORT).show();
        }
        // Apply the filter to the already fetched events and update the UI
        applyCurrentFilter();
    }


    /**
     * Fetches all events for a given place from the API.
     * After fetching, it stores them in allFetchedEvents and then applies the current filter.
     */
    private void loadAllEventsFromApi(int placeId) {
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

        Log.d(TAG, "Cargando TODOS los eventos para lugar ID: " + placeId + " con token: " + authToken);

        // Call the API to get ALL events (no price filter here)
        EventsApi.getEventosByLugares(getContext(), authToken, placeId,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBarEvents.setVisibility(View.GONE);
                        Log.d(TAG, "Respuesta de eventos: " + response.toString());
                        allFetchedEvents.clear(); // Clear previous data
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

                                    List<String> portadaUrls = new ArrayList<>();
                                    if (eventObject.has("portada") && !eventObject.isNull("portada")) {
                                        Object portadaValue = eventObject.get("portada");
                                        if (portadaValue instanceof JSONArray) {
                                            JSONArray portadaJsonArray = (JSONArray) portadaValue;
                                            for (int j = 0; j < portadaJsonArray.length(); j++) {
                                                String url = portadaJsonArray.getString(j);
                                                if (url != null && !url.isEmpty()) {
                                                    portadaUrls.add(url);
                                                }
                                            }
                                        } else if (portadaValue instanceof String) {
                                            String singleUrl = (String) portadaValue;
                                            if (singleUrl != null && !singleUrl.isEmpty()) {
                                                portadaUrls.add(singleUrl);
                                            }
                                        }
                                    }

                                    JSONObject lugarObj = eventObject.getJSONObject("lugar");
                                    int lugarIdAnidado = lugarObj.getInt("id");
                                    String lugarNombreAnidado = lugarObj.getString("nombre");
                                    Events.LugarSimple lugarSimple = new Events.LugarSimple(lugarIdAnidado, lugarNombreAnidado);

                                    allFetchedEvents.add(new Events(id, nombre, capacidad, precio, descripcion, fechaHora, estado, usuarioid, portadaUrls, lugarSimple));
                                }
                                Log.d(TAG, "Total eventos obtenidos de la API (sin filtrar): " + allFetchedEvents.size());
                                // Now, apply the current filter to the fetched events
                                applyCurrentFilter();
                            } else {
                                Toast.makeText(getContext(), "No hay eventos para este lugar.", Toast.LENGTH_SHORT).show();
                                allFetchedEvents.clear(); // Ensure the unfiltered list is empty too
                                applyCurrentFilter(); // This will clear the adapter and show no events message
                                Log.d(TAG, "No se encontraron eventos para el lugar: " + placeName);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error al parsear JSON de eventos: " + e.getMessage(), e);
                            Toast.makeText(getContext(), "Error al procesar datos de eventos.", Toast.LENGTH_SHORT).show();
                            allFetchedEvents.clear();
                            applyCurrentFilter();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBarEvents.setVisibility(View.GONE);
                        rvEventsList.setVisibility(View.GONE);
                        tvNoEventsMessage.setVisibility(View.VISIBLE);
                        allFetchedEvents.clear(); // Clear data on error
                        applyCurrentFilter(); // Clear UI

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
                    }
                });
    }

    /**
     * Applies the current price filter to the allFetchedEvents list
     * and updates the RecyclerView.
     */
    private void applyCurrentFilter() {
        eventList.clear(); // Clear the list for the adapter

        if (allFetchedEvents.isEmpty()) {
            tvNoEventsMessage.setText("No se encontraron eventos disponibles.");
            tvNoEventsMessage.setVisibility(View.VISIBLE);
            rvEventsList.setVisibility(View.GONE);
            eventCardAdapter.notifyDataSetChanged();
            return;
        }

        if (currentPriceFilter == null || "todos".equalsIgnoreCase(currentPriceFilter)) {
            // If no filter or "todos" selected, show all fetched events
            eventList.addAll(allFetchedEvents);
        } else {
            // Apply the filter logic
            for (Events event : allFetchedEvents) {
                try {
                    // It's crucial that event.getPrecio() can be parsed to a number.
                    // If it contains "50.000", you need to clean it (e.g., remove dots)
                    // before parsing. Assuming simple string numbers.
                    float price = Float.parseFloat(event.getPrecio());

                    boolean matchesFilter = false;
                    switch (currentPriceFilter) {
                        case "gratis":
                            matchesFilter = (price == 0);
                            break;
                        case "economico":
                            matchesFilter = (price >= 1 && price <= 50000);
                            break;
                        case "medio":
                            matchesFilter = (price >= 50001 && price <= 150000);
                            break;
                        case "premium":
                            matchesFilter = (price >= 150001);
                            break;
                        default:
                            // Should not happen if dialog only offers valid options
                            Log.w(TAG, "Filtro de precio desconocido: " + currentPriceFilter);
                            break;
                    }

                    if (matchesFilter) {
                        eventList.add(event);
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing event price '" + event.getPrecio() + "': " + e.getMessage());
                    // Decide how to handle events with unparsable prices (e.g., skip them)
                }
            }
        }

        if (eventList.isEmpty()) {
            tvNoEventsMessage.setText("No se encontraron eventos con el filtro de precio seleccionado.");
            tvNoEventsMessage.setVisibility(View.VISIBLE);
            rvEventsList.setVisibility(View.GONE);
        } else {
            tvNoEventsMessage.setVisibility(View.GONE);
            rvEventsList.setVisibility(View.VISIBLE);
        }
        eventCardAdapter.notifyDataSetChanged();
        Log.d(TAG, "Eventos filtrados y mostrados: " + eventList.size());
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