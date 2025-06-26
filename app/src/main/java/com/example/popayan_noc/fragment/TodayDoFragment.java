package com.example.popayan_noc.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popayan_noc.R;
import com.example.popayan_noc.adapter.CategoryAdapter;
import com.example.popayan_noc.model.Categoria;
import com.example.popayan_noc.service.CategoryApi;
import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.example.popayan_noc.util.AuthUtils; // <-- ¡Importa tu clase AuthUtils!

public class TodayDoFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "TodayDoFragment";
    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Categoria> categoryList;
    private TextView tvTitleTodayDo;

    private String authToken;

    public TodayDoFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today_do, container, false);


        authToken = AuthUtils.getToken(getContext());

        if (authToken == null) {
            Log.e(TAG, "Auth token is null. User might not be logged in.");
            Toast.makeText(getContext(), "Sesión no iniciada. Por favor, inicia sesión.", Toast.LENGTH_LONG).show();

        }


        recyclerView = view.findViewById(R.id.rv_categories_today_do);
        categoryList = new ArrayList<>();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, this);
        recyclerView.setAdapter(categoryAdapter);

        loadCategoriesFromApi();

        return view;
    }

    private void loadCategoriesFromApi() {
        // La validación del token ahora es más efectiva ya que se obtiene de AuthUtils
        if (getContext() == null || authToken == null || authToken.isEmpty()) { // Ya no comparamos con "TU_TOKEN_DE_AUTORIZACION_AQUI"
            Log.e(TAG, "Contexto nulo o token no disponible/válido. No se pudo cargar categorías.");
            Toast.makeText(getContext(), "Error: Token de autorización no disponible.", Toast.LENGTH_LONG).show();
            categoryAdapter.setCategories(null);
            return;
        }

        Log.d(TAG, "Intentando cargar categorías desde la API...");

        CategoryApi.getCategorias(getContext(), authToken,
                new com.android.volley.Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "Respuesta de la API (categorías): " + response.toString());
                        List<Categoria> fetchedCategories = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject categoryObject = response.getJSONObject(i);
                                int id = categoryObject.getInt("id");
                                String tipo = categoryObject.getString("tipo");
                                String descripcion = categoryObject.getString("descripcion");
                                String imagen = categoryObject.getString("imagen");
                                boolean estado = categoryObject.getBoolean("estado");

                                fetchedCategories.add(new Categoria(id, tipo, descripcion, imagen, estado));
                            }
                            categoryAdapter.setCategories(fetchedCategories);
                            Log.d(TAG, "Categorías cargadas correctamente: " + fetchedCategories.size());
                        } catch (JSONException e) {
                            Log.e(TAG, "Error al parsear JSON de categorías: " + e.getMessage());
                            Toast.makeText(getContext(), "Error al procesar datos de categorías.", Toast.LENGTH_SHORT).show();
                            categoryAdapter.setCategories(null);
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Error desconocido al cargar categorías.";
                        if (error.networkResponse != null) {
                            errorMessage = "Error de red: " + error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                Log.e(TAG, "Error Response Body (categorías): " + responseBody);
                                JSONObject jsonError = new JSONObject(responseBody);
                                if (jsonError.has("message")) {
                                    errorMessage += " - " + jsonError.getString("message");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error response (categorías): " + e.getMessage());
                            }
                        } else if (error.getMessage() != null) {
                            errorMessage = "Error: " + error.getMessage();
                        } else {
                            errorMessage = "Error de conexión (network response is null).";
                        }
                        Log.e(TAG, "Error al cargar categorías: " + errorMessage, error);
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        categoryAdapter.setCategories(null);
                    }
                });
    }

    @Override
    public void onCategoryClick(Categoria category, int position) {
        Log.d(TAG, "Categoría clicada: " + category.getTipo() + " (ID: " + category.getId() + ")");
        Toast.makeText(getContext(), "Cargando lugares para: " + category.getTipo(), Toast.LENGTH_SHORT).show();

        PlacesListFragment placesFragment = PlacesListFragment.newInstance(category.getId(), category.getTipo());

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, placesFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Log.e(TAG, "No se pudo obtener la actividad para reemplazar el fragmento.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}