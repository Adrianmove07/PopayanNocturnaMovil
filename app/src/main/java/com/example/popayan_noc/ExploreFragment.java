package com.example.popayan_noc;

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
import com.facebook.shimmer.ShimmerFrameLayout; // Shimmer loader
import com.example.popayan_noc.model.Lugar;
import com.example.popayan_noc.model.Categoria; // Assuming this is the model for Lugar's category
import com.example.popayan_noc.network.ApiService;
import com.example.popayan_noc.network.RetrofitClient;
import java.util.HashSet;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import android.widget.Toast; // Added for error messages

import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Intent;

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
    // private int selectedCategoryIndex = 0; // No longer explicitly used for filtering logic trigger
    // Volley queue and BASE_URL removed as RetrofitClient handles this.
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

        // Setup suggestions list
        suggestionAdapter = new SuggestionAdapter(new ArrayList<>(allSuggestions), suggestion -> {
            etSearch.setText(suggestion);
            etSearch.setSelection(suggestion.length());
            hideSuggestions();
            hideKeyboard();
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(suggestionAdapter);

        // Setup horizontal categories carousel
        categoryAdapter = new CategoryAdapter(getContext(), new ArrayList<>(), (category, position) -> {
            RecyclerView.ViewHolder viewHolder = rvCategories.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                animateCategoryClick(viewHolder);
            }
            android.widget.Toast.makeText(getContext(), "Categoría: " + category.tipo, android.widget.Toast.LENGTH_SHORT).show();
            filterPlacesByCategory(category.tipo);
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);
        // Siempre carga categorías locales si la lista está vacía
        categoryAdapter.setCategories(null); // fuerza categorías locales por defecto

        // Adapter de lugares (now expects List<Lugar>)
        placeAdapter = new PlaceAdapter(getContext(), filteredPlaces);
        rvPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlaces.setAdapter(placeAdapter);

        // Carga categorías y lugares desde el backend usando Retrofit
        fetchPlacesAndCategories();
        

        // Touch on search bar container shows search
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
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSuggestions(s.toString());
                if (s.length() == 0) hideSuggestions();
                else showSuggestions();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Hide search on outside touch
        view.setOnTouchListener((v, event) -> {
            if (etSearch.hasFocus()) {
                etSearch.clearFocus();
                hideKeyboard();
            }
            return false;
        });

        // Acción para la tarjeta de Eventos Próximos
        CardView cardEventosProximos = view.findViewById(R.id.cardEventosProximos);
        cardEventosProximos.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EventosProximosActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void filterPlacesByCategory(String tipoCategoria) {
        filteredPlaces.clear();
        if (tipoCategoria == null || tipoCategoria.isEmpty()) { // Show all if category is null/empty
            if (allPlaces != null) filteredPlaces.addAll(allPlaces);
        } else {
            if (allPlaces != null) {
                for (Lugar p : allPlaces) {
                    if (p.getCategoria() != null && tipoCategoria.equals(p.getCategoria().getTipo())) {
                        filteredPlaces.add(p);
                    }
                }
            }
        }
        if (placeAdapter != null) placeAdapter.notifyDataSetChanged();
        if (filteredPlaces.isEmpty() && tipoCategoria != null && !tipoCategoria.isEmpty()) {
             Toast.makeText(getContext(), "No hay lugares en la categoría: " + tipoCategoria, Toast.LENGTH_SHORT).show();
        }
    }



    // private int findCategoryIndexByTipo(String tipo) { ... } // Method removed as it's no longer directly used for core filtering logic.

    private void fetchPlacesAndCategories() {
        if (shimmerFrameLayout != null) showShimmer(true);
        String token = AuthUtils.getToken(getContext());

        if (token == null || token.isEmpty()) {
            Log.e("ExploreFragment", "Token de usuario no disponible.");
            if (getContext() != null) Toast.makeText(getContext(), "Error de autenticación. Intente iniciar sesión de nuevo.", Toast.LENGTH_LONG).show();
            if (shimmerFrameLayout != null) showShimmer(false);
            // Consider navigating to login screen
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Lugar>> call = apiService.listarLugares("Bearer " + token);

        call.enqueue(new Callback<List<Lugar>>() {
            @Override
            public void onResponse(Call<List<Lugar>> call, Response<List<Lugar>> response) {
                if (shimmerFrameLayout != null) showShimmer(false);
                if (!isAdded() || getContext() == null) return; // Fragment not attached or context is null

                if (response.isSuccessful() && response.body() != null) {
                    allPlaces.clear();
                    allPlaces.addAll(response.body());

                    Set<String> uniqueCategoryTipos = new HashSet<>();
                    List<com.example.popayan_noc.Category> categoriesForAdapter = new ArrayList<>(); // Use the fully qualified name or ensure Category is imported
                    for (Lugar lugar : allPlaces) {
                        if (lugar.getCategoria() != null && lugar.getCategoria().getTipo() != null) {
                            if (uniqueCategoryTipos.add(lugar.getCategoria().getTipo())) {
                                // Assuming com.example.popayan_noc.Category is the correct class for CategoryAdapter
                                categoriesForAdapter.add(new com.example.popayan_noc.Category(0, lugar.getCategoria().getTipo(), "", "", false));
                            }
                        }
                    }
                    if (categoryAdapter != null) {
                        categoryAdapter.setCategories(categoriesForAdapter);
                    }

                    filteredPlaces.clear();
                    if (!categoriesForAdapter.isEmpty() && !allPlaces.isEmpty()) {
                        filterPlacesByCategory(categoriesForAdapter.get(0).tipo); // Filter by the first category
                    } else {
                        filteredPlaces.addAll(allPlaces); // Show all if no categories or no specific filter
                    }
                    if (placeAdapter != null) placeAdapter.notifyDataSetChanged();

                    if (allPlaces.isEmpty()) {
                        Toast.makeText(getContext(), "No hay lugares disponibles.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ExploreFragment", "Error al cargar lugares: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Error al cargar lugares. Código: " + response.code(), Toast.LENGTH_LONG).show();
                    if (allPlaces != null) allPlaces.clear();
                    if (filteredPlaces != null) filteredPlaces.clear();
                    if (placeAdapter != null) placeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Lugar>> call, Throwable t) {
                if (shimmerFrameLayout != null) showShimmer(false);
                if (!isAdded() || getContext() == null) return; // Fragment not attached or context is null

                Log.e("ExploreFragment", "Fallo en la conexión al cargar lugares: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                if (allPlaces != null) allPlaces.clear();
                if (filteredPlaces != null) filteredPlaces.clear();
                if (placeAdapter != null) placeAdapter.notifyDataSetChanged();
            }
        });
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

    private void animatePlacesFadeIn() {
        if (rvPlaces != null) {
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(400);
            rvPlaces.startAnimation(fadeIn);
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

    // Lugares dummy para fallback visual
    private List<Place> getDummyPlaces() {
        List<Place> places = new ArrayList<>();
        places.add(new Place(1, 1, 1, "Restaurante El Sabor", "Comida típica de Popayán", "Centro", true, "", true));
        places.add(new Place(2, 2, 1, "Bar La Noche", "Tragos y música en vivo", "Zona Rosa", true, "", true));
        places.add(new Place(3, 3, 1, "Cine Popayán", "Estrenos y clásicos", "Centro", true, "", true));
        places.add(new Place(4, 4, 1, "Museo de Historia", "Cultura y arte local", "Centro", true, "", true));
        places.add(new Place(5, 6, 1, "Parque Natural", "Naturaleza y senderismo", "Sur", true, "", true));
        return places;
    }

    private void showSearch() {
        etSearch.setVisibility(View.VISIBLE);
        etSearch.requestFocus();
        showKeyboard();
        animateSearchBarExpand(true);
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
        // Color highlight
        int colorFrom = cardSearchBar.getCardBackgroundColor().getDefaultColor();
        int colorTo = expand ? 0xFFFFFFFF : 0xFFF5F5F5;
        ValueAnimator colorAnim = ValueAnimator.ofArgb(colorFrom, colorTo);
        colorAnim.setDuration(250);
        colorAnim.addUpdateListener(anim -> cardSearchBar.setCardBackgroundColor((int) anim.getAnimatedValue()));
        colorAnim.start();
        isSearchExpanded = expand;
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
        for (String s : allSuggestions) if (s.toLowerCase().contains(query.toLowerCase())) filtered.add(s);
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

