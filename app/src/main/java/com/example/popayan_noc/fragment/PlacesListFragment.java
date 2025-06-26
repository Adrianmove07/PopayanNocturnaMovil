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
import com.example.popayan_noc.adapter.PlaceCardAdapter;
import com.example.popayan_noc.model.Categoria;
import com.example.popayan_noc.model.Place;
import com.example.popayan_noc.service.PlaceApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.example.popayan_noc.util.AuthUtils; // <-- ¡Importa tu clase AuthUtils!

public class PlacesListFragment extends Fragment implements PlaceCardAdapter.OnPlaceClickListener {

    private static final String TAG = "PlacesListFragment";
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";

    private int categoryId;
    private String categoryName;

    private TextView tvCategoryPlacesTitle;
    private RecyclerView rvPlacesList;
    private PlaceCardAdapter placeCardAdapter;
    private List<Place> placeList;
    private ProgressBar progressBarPlaces;

    private String authToken;

    public PlacesListFragment() {
        // Required empty public constructor
    }

    public static PlacesListFragment newInstance(int categoryId, String categoryName) {
        PlacesListFragment fragment = new PlacesListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getInt(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
            Log.d(TAG, "Fragment created for category: " + categoryName + " (ID: " + categoryId + ")");
        } else {
            Log.e(TAG, "PlacesListFragment started without category ID or name arguments.");
        }
        // Obtener el token de autenticación
        authToken = AuthUtils.getToken(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places_list, container, false);

        // Opcional: Si el token es nulo, puedes mostrar un mensaje o redirigir al login
        if (authToken == null) {
            Log.e(TAG, "Auth token is null in PlacesListFragment. User might not be logged in.");
            Toast.makeText(getContext(), "Sesión no iniciada. No se pueden cargar lugares.", Toast.LENGTH_LONG).show();
            // Considera redirigir al usuario a la pantalla de login o mostrar un estado vacío
            // return view;
        }

        tvCategoryPlacesTitle = view.findViewById(R.id.tvCategoryPlacesTitle);
        rvPlacesList = view.findViewById(R.id.rvPlacesList);
        progressBarPlaces = view.findViewById(R.id.progressBarPlaces);

        if (categoryName != null && !categoryName.isEmpty()) {
            tvCategoryPlacesTitle.setText("Lugares en " + capitalize(categoryName));
        } else {
            tvCategoryPlacesTitle.setText("Lugares");
        }

        placeList = new ArrayList<>();
        rvPlacesList.setLayoutManager(new LinearLayoutManager(getContext()));
        placeCardAdapter = new PlaceCardAdapter(getContext(), placeList, this);
        rvPlacesList.setAdapter(placeCardAdapter);

        loadPlacesByCategory(categoryId);

        return view;
    }

    private void loadPlacesByCategory(int catId) {
        // La validación del token ahora es más efectiva
        if (getContext() == null || authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "Contexto nulo o token no válido. No se pueden cargar lugares.");
            Toast.makeText(getContext(), "Error: Token de autorización no disponible.", Toast.LENGTH_LONG).show();
            progressBarPlaces.setVisibility(View.GONE);
            return;
        }

        progressBarPlaces.setVisibility(View.VISIBLE);
        rvPlacesList.setVisibility(View.GONE);

        Log.d(TAG, "Cargando lugares para categoría ID: " + catId);

        PlaceApi.getLugaresByCategoria(getContext(), authToken, catId,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressBarPlaces.setVisibility(View.GONE);
                        rvPlacesList.setVisibility(View.VISIBLE);

                        Log.d(TAG, "Respuesta de lugares: " + response.toString());
                        List<Place> fetchedPlaces = new ArrayList<>();
                        try {
                            String mensaje = response.getString("mensaje");
                            int cantidad = response.getInt("cantidad");
                            String categoriaTipo = response.getString("categoria");
                            JSONArray datosArray = response.getJSONArray("datos");

                            if (cantidad > 0) {
                                for (int i = 0; i < datosArray.length(); i++) {
                                    JSONObject placeObject = datosArray.getJSONObject(i);

                                    int id = placeObject.getInt("id");
                                    String nombre = placeObject.getString("nombre");
                                    String descripcion = placeObject.getString("descripcion");
                                    String ubicacion = placeObject.getString("ubicacion");
                                    String imagen = placeObject.getString("imagen");
                                    int categoriaid = placeObject.getInt("categoriaid");

                                    String fotosLugarString = "";
                                    if (placeObject.has("fotos_lugar") && !placeObject.isNull("fotos_lugar")) {
                                        fotosLugarString = placeObject.getString("fotos_lugar");
                                    }
                                    List<String> fotos_lugar = Place.parseFotosString(fotosLugarString);

                                    JSONObject categoriaObj = placeObject.getJSONObject("categoria");
                                    String tipoCategoria = categoriaObj.getString("tipo");
                                    Categoria lugarCategoria = new Categoria(categoriaid, tipoCategoria, "", "", false);

                                    fetchedPlaces.add(new Place(id, nombre, descripcion, ubicacion, imagen, categoriaid, lugarCategoria, fotos_lugar));
                                }
                                placeCardAdapter.setPlaces(fetchedPlaces);
                                Log.d(TAG, "Lugares cargados: " + fetchedPlaces.size());
                            } else {
                                Toast.makeText(getContext(), "No hay lugares para esta categoría.", Toast.LENGTH_SHORT).show();
                                placeCardAdapter.setPlaces(new ArrayList<>());
                                Log.d(TAG, "No se encontraron lugares para la categoría: " + categoryName);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error al parsear JSON de lugares: " + e.getMessage());
                            Toast.makeText(getContext(), "Error al procesar datos de lugares.", Toast.LENGTH_SHORT).show();
                            placeCardAdapter.setPlaces(new ArrayList<>());
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBarPlaces.setVisibility(View.GONE);
                        rvPlacesList.setVisibility(View.GONE);

                        String errorMessage = "Error al cargar lugares.";
                        if (error.networkResponse != null) {
                            errorMessage = "Error de red: " + error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                Log.e(TAG, "Cuerpo de error de la API (lugares): " + responseBody);
                                JSONObject jsonError = new JSONObject(responseBody);
                                if (jsonError.has("message")) {
                                    errorMessage += " - " + jsonError.getString("message");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear el cuerpo del error (lugares): " + e.getMessage());
                            }
                        } else if (error.getMessage() != null) {
                            errorMessage = "Error: " + error.getMessage();
                        }
                        Log.e(TAG, "Error al cargar lugares: " + errorMessage, error);
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        placeCardAdapter.setPlaces(new ArrayList<>());
                    }
                });
    }

    @Override
    public void onPlaceClick(Place place, int position) {
        Log.d(TAG, "Lugar clicado: " + place.getNombre() + " en posición: " + position);
        Toast.makeText(getContext(), "Cargando eventos para: " + place.getNombre(), Toast.LENGTH_SHORT).show();

        // **** ESTA ES LA SECCIÓN CRUCIAL PARA NAVEGAR A EventsListFragment ****
        // 1. Crea una nueva instancia de EventsListFragment usando el método de fábrica.
        //    Le pasamos el ID y el nombre del lugar para que EventsListFragment sepa qué eventos cargar.
        EventsListFragment eventsFragment = EventsListFragment.newInstance(place.getId(), place.getNombre());

        // 2. Realiza la transacción de fragmentos.
        //    Esto reemplaza el fragmento actual (PlacesListFragment) con el nuevo (EventsListFragment).
        //    Asegúrate de que R.id.fragment_container sea el ID correcto de tu FrameLayout o FragmentContainerView en tu MainActivity.
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, eventsFragment) // Reemplaza el fragmento actual
                    .addToBackStack(null) // Permite al usuario volver a PlacesListFragment con el botón "Atrás"
                    .commit(); // Confirma y ejecuta la transacción
        } else {
            Log.e(TAG, "Error: getActivity() es nulo. No se pudo reemplazar el fragmento.");
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}