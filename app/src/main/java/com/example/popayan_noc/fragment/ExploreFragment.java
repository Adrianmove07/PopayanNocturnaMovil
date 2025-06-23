package com.example.popayan_noc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.animation.ValueAnimator;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;

import com.example.popayan_noc.Place;
import com.example.popayan_noc.R;
import com.example.popayan_noc.activity.EventosProximosActivity;
import com.example.popayan_noc.adapter.CategoryAdapter;
import com.example.popayan_noc.adapter.PlaceAdapter;
import com.example.popayan_noc.adapter.SuggestionAdapter;
import com.example.popayan_noc.util.AuthUtils;
import com.example.popayan_noc.model.Categoria;
import com.example.popayan_noc.model.Lugar;
import com.example.popayan_noc.network.ApiService;
import com.example.popayan_noc.network.RetrofitClient;
import com.example.popayan_noc.service.CategoryApi;
import com.facebook.shimmer.ShimmerFrameLayout;

import android.content.Intent;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager; // Ensure this is imported
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreFragment extends Fragment {
    private LinearLayout searchBarContainer;
    private EditText etSearch;
    private CardView cardSearchBar;
    private boolean isSearchExpanded = false;
    private RecyclerView rvSuggestions;
    private RecyclerView rvCategories;
    private SuggestionAdapter suggestionAdapter;
    private CategoryAdapter categoryAdapter;
    private RecyclerView rvPlaces;
    private PlaceAdapter placeAdapter;
    private List<Lugar> allPlaces = new ArrayList<>();
    private List<Lugar> filteredPlaces = new ArrayList<>();

    private List<String> allSuggestions = Arrays.asList(
            "Restaurantes", "Naturaleza", "Eventos hoy", "Cultura", "Bares", "Museos", "Miradores", "Cafés", "Fiesta", "Parques"
    );

    private ShimmerFrameLayout shimmerFrameLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        searchBarContainer = view.findViewById(R.id.search_bar_container);
        etSearch = view.findViewById(R.id.etSearch);
        cardSearchBar = view.findViewById(R.id.card_search_bar);
        rvSuggestions = view.findViewById(R.id.rvSuggestions);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvPlaces = view.findViewById(R.id.rvPlaces);
        shimmerFrameLayout = view.findViewById(R.id.shimmerLayout);

        // Setup suggestions
        suggestionAdapter = new SuggestionAdapter(new ArrayList<>(allSuggestions), suggestion -> {
            etSearch.setText(suggestion);
            etSearch.setSelection(suggestion.length());
            hideSuggestions();
            hideKeyboard();
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(suggestionAdapter);

        // Setup categories
        categoryAdapter = new CategoryAdapter(getContext(), new ArrayList<>(), (category, position) -> {
            RecyclerView.ViewHolder viewHolder = rvCategories.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) animateCategoryClick(viewHolder);
            filterPlacesByCategory(category.getTipo());
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // Setup places
        placeAdapter = new PlaceAdapter(getContext(), filteredPlaces);
        // --- THIS IS THE CHANGE ---
        rvPlaces.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // --- END OF CHANGE ---
        rvPlaces.setAdapter(placeAdapter);

        fetchPlacesAndCategories(); // carga los lugares
        fetchCategories();          // carga las categorías reales

        // Search bar
        searchBarContainer.setOnClickListener(v -> {
            showSearch();
            animateSearchBarExpand(true);
        });

        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showSuggestions();
                animateSearchBarExpand(true);
            } else {
                hideSuggestions();
                animateSearchBarExpand(false);
            }
        });

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSuggestions(s.toString());
                if (s.length() == 0) hideSuggestions(); else showSuggestions();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        view.setOnTouchListener((v, event) -> {
            if (etSearch.hasFocus()) {
                etSearch.clearFocus();
                hideKeyboard();
            }
            return false;
        });

        // Ir a eventos próximos
        CardView cardEventosProximos = view.findViewById(R.id.cardEventosProximos);
        cardEventosProximos.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EventosProximosActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void fetchPlacesAndCategories() {
        if (shimmerFrameLayout != null) showShimmer(true);
        String token = AuthUtils.getToken(getContext());

        if (token == null || token.isEmpty()) {
            Log.e("ExploreFragment", "Token de usuario no disponible.");
            Toast.makeText(getContext(), "Error de autenticación. Intente iniciar sesión de nuevo.", Toast.LENGTH_LONG).show();
            showShimmer(false);
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Lugar>> call = apiService.listarLugares("Bearer " + token);

        call.enqueue(new Callback<List<Lugar>>() {
            @Override
            public void onResponse(Call<List<Lugar>> call, Response<List<Lugar>> response) {
                showShimmer(false);
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    allPlaces.clear();
                    allPlaces.addAll(response.body());

                    filteredPlaces.clear();
                    filteredPlaces.addAll(allPlaces);
                    placeAdapter.notifyDataSetChanged();

                    if (allPlaces.isEmpty()) {
                        Toast.makeText(getContext(), "No hay lugares disponibles.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ExploreFragment", "Error al cargar lugares: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar lugares.", Toast.LENGTH_LONG).show();
                    allPlaces.clear();
                    filteredPlaces.clear();
                    placeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Lugar>> call, Throwable t) {
                showShimmer(false);
                Log.e("ExploreFragment", "Fallo al cargar lugares: " + t.getMessage());
                Toast.makeText(getContext(), "Fallo en la conexión.", Toast.LENGTH_LONG).show();
                allPlaces.clear();
                filteredPlaces.clear();
                placeAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchCategories() {
        String token = AuthUtils.getToken(getContext());
        if (token == null || token.isEmpty()) return;

        CategoryApi.getCategorias(getContext(), token, response -> {
            List<Categoria> categorias = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject obj = response.getJSONObject(i);
                    Categoria cat = new Categoria();
                    cat.setId(obj.getInt("id"));
                    cat.setTipo(obj.getString("tipo"));
                    cat.setDescripcion(obj.optString("descripcion", ""));
                    cat.setImagen(obj.optString("imagen", null));
                    cat.setEstado(obj.optBoolean("estado", true));
                    categorias.add(cat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            categoryAdapter.setCategories(categorias);

            // Autofiltrar por la primera categoría (opcional)
            if (!categorias.isEmpty()) {
                filterPlacesByCategory(categorias.get(0).getTipo());
            }

        }, error -> {
            Log.e("ExploreFragment", "Error al cargar categorías: " + error.getMessage());
        });
    }

    private void filterPlacesByCategory(String tipoCategoria) {
        filteredPlaces.clear();
        for (Lugar lugar : allPlaces) {
            if (lugar.getCategoria() != null && tipoCategoria.equalsIgnoreCase(lugar.getCategoria().getTipo())) {
                filteredPlaces.add(lugar);
            }
        }
        placeAdapter.notifyDataSetChanged();
        if (filteredPlaces.isEmpty()) {
            Toast.makeText(getContext(), "No hay lugares para la categoría: " + tipoCategoria, Toast.LENGTH_SHORT).show();
        }
    }

    private void showShimmer(boolean show) {
        if (shimmerFrameLayout == null) return;
        if (show) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            rvPlaces.setVisibility(View.GONE);
        } else {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            rvPlaces.setVisibility(View.VISIBLE);
        }
    }

    private void animateCategoryClick(RecyclerView.ViewHolder holder) {
        if (holder == null || holder.itemView == null) return;
        ScaleAnimation scale = new ScaleAnimation(
                1f, 1.08f, 1f, 1.08f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(120);
        scale.setRepeatCount(1);
        scale.setRepeatMode(ScaleAnimation.REVERSE);
        holder.itemView.startAnimation(scale);
    }

    private void showSearch() {
        etSearch.setVisibility(View.VISIBLE);
        etSearch.requestFocus();
        showKeyboard();
    }

    private void animateSearchBarExpand(boolean expand) {
        if (cardSearchBar == null) return;
        float start = cardSearchBar.getCardElevation();
        float end = expand ? 20f : 8f;
        int startHeight = cardSearchBar.getLayoutParams().height;
        int endHeight = expand ? dpToPx(62) : dpToPx(50);

        ValueAnimator elevationAnim = ValueAnimator.ofFloat(start, end);
        elevationAnim.setDuration(250);
        elevationAnim.addUpdateListener(animation ->
                cardSearchBar.setCardElevation((float) animation.getAnimatedValue()));
        elevationAnim.start();

        ValueAnimator heightAnim = ValueAnimator.ofInt(startHeight, endHeight);
        heightAnim.setDuration(250);
        heightAnim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = cardSearchBar.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            cardSearchBar.setLayoutParams(params);
        });
        heightAnim.start();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void showSuggestions() {
        rvSuggestions.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rvSuggestions.getLayoutParams();
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        rvSuggestions.setLayoutParams(params);
    }

    private void hideSuggestions() {
        rvSuggestions.setVisibility(View.GONE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rvSuggestions.getLayoutParams();
        params.height = 0;
        rvSuggestions.setLayoutParams(params);
    }

    private void filterSuggestions(String query) {
        List<String> filtered = new ArrayList<>();
        for (String s : allSuggestions) {
            if (s.toLowerCase().contains(query.toLowerCase())) filtered.add(s);
        }
        suggestionAdapter.updateSuggestions(filtered);
    }

    private void showKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }
}