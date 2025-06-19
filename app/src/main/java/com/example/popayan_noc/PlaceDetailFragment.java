package com.example.popayan_noc;

import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.AuthFailureError;
import com.bumptech.glide.Glide;
import com.example.popayan_noc.model.Evento;
import com.example.popayan_noc.model.Lugar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PlaceDetailFragment extends Fragment {

    private Button btnVerEventos;
    private RecyclerView rvEvents;
    private Button btnSeeReviews;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_detail, container, false);
        ImageView ivPlaceImage = view.findViewById(R.id.ivPlaceImage);
        TextView tvPlaceName = view.findViewById(R.id.tvPlaceName);
        TextView tvPlaceDescription = view.findViewById(R.id.tvPlaceDescription);
        TextView tvPlaceLocation = view.findViewById(R.id.tvPlaceLocation);
        btnVerEventos = view.findViewById(R.id.btnVerEventos);
        rvEvents = view.findViewById(R.id.rvEvents);
        rvEvents.setVisibility(View.GONE); // Oculto por defecto hasta cargar
        btnSeeReviews = view.findViewById(R.id.btnSeeReviews);

        Bundle args = getArguments();
        if (args != null) {
            tvPlaceName.setText(args.getString("lugarNombre", "-"));
            tvPlaceDescription.setText(args.getString("lugarDescripcion", "Sin descripción"));
            tvPlaceLocation.setText(args.getString("lugarUbicacion", "Sin ubicación"));
            String imagen = args.getString("lugarImagen", "");
            if (!imagen.isEmpty()) {
                Glide.with(this).load(imagen).placeholder(R.drawable.placeholder_img).into(ivPlaceImage);
            } else {
                ivPlaceImage.setImageResource(R.drawable.placeholder_img);
            }
            
            // Configurar el botón para ver todos los eventos
            int lugarId = args.getInt("lugarId", 1);
            btnVerEventos.setOnClickListener(v -> {
                String token = AuthUtils.getToken(requireContext());
                
                // Log para depuración
                if (token == null || token.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        btnVerEventos.setEnabled(true);
                        btnVerEventos.setText("Ver Eventos");
                        Toast.makeText(requireContext(), "No hay sesión activa", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // Mostrar indicador de carga
                btnVerEventos.setEnabled(false);
                btnVerEventos.setText("Cargando...");
                
                // Usar EventApi.getAllEventos para obtener todos los eventos
                EventApi.getAllEventos(requireContext(), token, response -> {
                    try {
                        ArrayList<Evento> eventosFiltrados = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject eventoJson = response.getJSONObject(i);
                                int eventoLugarId = eventoJson.getInt("lugarid");
                                if (eventoLugarId == lugarId) {
                                    Evento evento = new Evento();
                                    evento.setId(eventoJson.getInt("id"));
                                    evento.setNombre(eventoJson.getString("nombre"));
                                    evento.setCapacidad(eventoJson.getInt("capacidad"));
                                    evento.setPrecio(eventoJson.getString("precio"));
                                    evento.setDescripcion(eventoJson.getString("descripcion"));
                                    evento.setFechaHora(eventoJson.getString("fecha_hora"));
                                    
                                    // Obtener el lugar asociado al evento
                                    try {
                                        JSONObject lugarJson = eventoJson.getJSONObject("lugar");
                                        if (lugarJson != null) {
                                            Lugar lugar = new Lugar();
                                            lugar.setId(lugarJson.getInt("lugarid"));
                                            lugar.setNombre(lugarJson.getString("nombre"));
                                            lugar.setUbicacion(lugarJson.getString("ubicacion"));
                                            evento.setLugar(lugar);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        // Si hay un error al obtener el lugar, seguimos con el evento sin lugar
                                    }
                                    
                                    // Obtener la primera imagen de portada
                                    JSONArray portadaArray = eventoJson.getJSONArray("portada");
                                    if (portadaArray.length() > 0) {
                                        List<String> portadaList = new ArrayList<>();
                                        portadaList.add(portadaArray.getString(0));
                                        evento.setPortada(portadaList);
                                    }
                                    
                                    eventosFiltrados.add(evento);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                continue; // Saltar este evento si hay un error
                            }
                        }
                        
                        if (!eventosFiltrados.isEmpty()) {
                            Fragment fragment = new EventosLugarFragment();
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("eventos", eventosFiltrados);
                            bundle.putString("lugarNombre", args.getString("lugarNombre", ""));
                            fragment.setArguments(bundle);
                            requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.main, fragment)
                                .addToBackStack(null)
                                .commit();
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                btnVerEventos.setEnabled(true);
                                btnVerEventos.setText("Ver Eventos");
                                Toast.makeText(requireContext(), "No hay eventos disponibles para este lugar", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() -> {
                            btnVerEventos.setEnabled(true);
                            btnVerEventos.setText("Ver Eventos");
                            Toast.makeText(requireContext(), "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                        });
                    }
                }, error -> {
                    requireActivity().runOnUiThread(() -> {
                        btnVerEventos.setEnabled(true);
                        btnVerEventos.setText("Ver Eventos");
                        
                        String errorMessage = "Error desconocido";
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            switch (statusCode) {
                                case 401:
                                    errorMessage = "Sesión expirada. Por favor, inicia sesión nuevamente.";
                                    break;
                                case 404:
                                    errorMessage = "No se encontraron eventos para este lugar.";
                                    break;
                                case 500:
                                    errorMessage = "Error en el servidor. Por favor, intenta de nuevo.";
                                    break;
                                default:
                                    errorMessage = "Error de conexión: " + statusCode;
                            }
                        } else {
                            if (error instanceof com.android.volley.NoConnectionError) {
                                errorMessage = "No se pudo establecer conexión con el servidor.";
                            } else if (error instanceof com.android.volley.TimeoutError) {
                                errorMessage = "Tiempo de espera agotado. Por favor, verifica tu conexión a internet.";
                            } else if (error instanceof com.android.volley.AuthFailureError) {
                                errorMessage = "Error de autenticación. Por favor, inicia sesión nuevamente.";
                            } else {
                                errorMessage = "Error de conexión. Por favor, intenta de nuevo.";
                            }
                        }
                        
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        btnSeeReviews.setOnClickListener(v -> {
            if (args == null) {
                Toast.makeText(requireContext(), "Error: No se recibieron datos del lugar", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Fragment fragment = new EventReviewsFragment();
            Bundle reviewsArgs = new Bundle();
            reviewsArgs.putInt("lugarId", args.getInt("lugarId"));
            reviewsArgs.putString("lugarNombre", args.getString("lugarNombre"));
            fragment.setArguments(reviewsArgs);
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit();
        });

        return view;
    }
}
