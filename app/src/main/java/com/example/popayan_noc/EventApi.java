package com.example.popayan_noc;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class EventApi {
    private static final String BASE_URL = "https://popnocturna.vercel.app/api";

    // Obtener todos los eventos
    public static void getAllEventos(Context context, String token, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/eventos";
        RequestQueue queue = Volley.newRequestQueue(context);
        
        // Log para depuración
        Log.d("EventApi", "URL: " + url);
        Log.d("EventApi", "Token: " + token);
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                // Obtener el array de datos del JSON
                JSONArray datos = response.getJSONArray("datos");
                Log.d("EventApi", "Respuesta exitosa: " + datos.toString());
                listener.onResponse(datos);
            } catch (Exception e) {
                Log.e("EventApi", "Error al procesar respuesta: " + e.getMessage());
                errorListener.onErrorResponse(new VolleyError("Error al procesar la respuesta"));
            }
        }, error -> {
            Log.e("EventApi", "Error en la solicitud:", error);
            if (error.networkResponse != null) {
                Log.e("EventApi", "Código de estado: " + error.networkResponse.statusCode);
                Log.e("EventApi", "Mensaje de error: " + new String(error.networkResponse.data));
                Log.e("EventApi", "Tipo de error: " + error.getClass().getSimpleName());
                Log.e("EventApi", "Causa: " + error.getCause());
            } else {
                Log.e("EventApi", "Error sin respuesta de red");
                Log.e("EventApi", "Tipo de error: " + error.getClass().getSimpleName());
                Log.e("EventApi", "Causa: " + error.getCause());
            }
            
            // Intentar obtener más detalles del error
            if (error instanceof com.android.volley.NoConnectionError) {
                Log.e("EventApi", "Error: No se pudo establecer conexión");
            } else if (error instanceof com.android.volley.TimeoutError) {
                Log.e("EventApi", "Error: Tiempo de espera agotado");
            } else if (error instanceof com.android.volley.AuthFailureError) {
                Log.e("EventApi", "Error: Fallo en la autenticación");
            }
            
            errorListener.onErrorResponse(error);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        
        queue.add(request);
    }

    // Obtener eventos de un lugar
    public static void getEventosByLugar(Context context, String token, int lugarId, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/eventos";
        RequestQueue queue = Volley.newRequestQueue(context);
        
        Log.d("EventApi", "URL eventos: " + url);
        Log.d("EventApi", "Token: " + token);
        Log.d("EventApi", "Lugar ID: " + lugarId);
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    Log.d("EventApi", "Respuesta exitosa eventos: " + response.toString());
                    JSONArray datos = response.getJSONArray("datos");
                    JSONArray filteredEvents = new JSONArray();
                    
                    for (int i = 0; i < datos.length(); i++) {
                        JSONObject event = datos.getJSONObject(i);
                        int eventLugarId = event.getInt("lugarid");
                        if (eventLugarId == lugarId) {
                            filteredEvents.put(event);
                        }
                    }
                    
                    listener.onResponse(filteredEvents);
                } catch (Exception e) {
                    Log.e("EventApi", "Error procesando respuesta: " + e.getMessage());
                    errorListener.onErrorResponse(new VolleyError("Error procesando respuesta: " + e.getMessage()));
                }
            },
            error -> {
                String errorMessage = "Error desconocido";
                if (error.networkResponse != null) {
                    int statusCode = error.networkResponse.statusCode;
                    try {
                        String errorBody = new String(error.networkResponse.data, "UTF-8");
                        Log.e("EventApi", "Error de servidor: " + errorBody);
                        errorMessage = "Error del servidor: " + errorBody;
                    } catch (Exception e) {
                        Log.e("EventApi", "Error al leer el cuerpo del error: " + e.getMessage());
                    }
                } else {
                    Log.e("EventApi", "Error de red: " + error.getMessage());
                    errorMessage = "Error de red: " + error.getMessage();
                }
                errorListener.onErrorResponse(new VolleyError(errorMessage));
            }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        
        queue.add(request);
    }

    // Obtener comentarios de un evento
    public static void getEventComments(Context context, String token, int eventId, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/comentarios/evento/" + eventId;
        RequestQueue queue = Volley.newRequestQueue(context);
        
        Log.d("EventApi", "URL comentarios: " + url);
        Log.d("EventApi", "Token: " + token);
        Log.d("EventApi", "Evento ID: " + eventId);
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    Log.d("EventApi", "Respuesta exitosa comentarios: " + response.toString());
                    JSONArray comentarios = response.getJSONArray("comentarios");
                    listener.onResponse(comentarios);
                } catch (Exception e) {
                    Log.e("EventApi", "Error procesando respuesta: " + e.getMessage());
                    errorListener.onErrorResponse(new VolleyError("Error procesando respuesta: " + e.getMessage()));
                }
            },
            error -> {
                Log.e("EventApi", "Error al obtener comentarios:", error);
                errorListener.onErrorResponse(error);
            }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        
        queue.add(request);
    }

    // Obtener calificaciones de un evento
    public static void getEventRatings(Context context, String token, int eventId, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/calificaciones?eventoid=" + eventId;
        RequestQueue queue = Volley.newRequestQueue(context);
        
        Log.d("EventApi", "URL calificaciones: " + url);
        Log.d("EventApi", "Token: " + token);
        Log.d("EventApi", "Evento ID: " + eventId);
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    Log.d("EventApi", "Respuesta exitosa calificaciones: " + response.toString());
                    JSONObject datos = response.getJSONObject("datos");
                    JSONArray calificaciones = datos.getJSONArray("calificaciones");
                    listener.onResponse(calificaciones);
                } catch (Exception e) {
                    Log.e("EventApi", "Error procesando respuesta: " + e.getMessage());
                    errorListener.onErrorResponse(new VolleyError("Error procesando respuesta: " + e.getMessage()));
                }
            },
            error -> {
                Log.e("EventApi", "Error al obtener calificaciones:", error);
                errorListener.onErrorResponse(error);
            }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        
        queue.add(request);
    }

    // Crear reserva
    public static void createReservation(Context context, String token, int eventoid, String fecha_hora, int cantidad_entradas, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/reserva";
        RequestQueue queue = Volley.newRequestQueue(context);
        
        Log.d("EventApi", "URL reserva: " + url);
        Log.d("EventApi", "Token: " + token);
        Log.d("EventApi", "Evento ID: " + eventoid);
        Log.d("EventApi", "Fecha: " + fecha_hora);
        Log.d("EventApi", "Entradas: " + cantidad_entradas);
        
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("eventoid", eventoid);
            jsonBody.put("fecha_hora", fecha_hora);
            jsonBody.put("cantidad_entradas", cantidad_entradas);
        } catch (Exception e) {
            Log.e("EventApi", "Error creando JSON: " + e.getMessage());
            errorListener.onErrorResponse(new VolleyError("Error creando JSON: " + e.getMessage()));
            return;
        }
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
            response -> {
                try {
                    Log.d("EventApi", "Respuesta exitosa reserva: " + response.toString());
                    listener.onResponse(response);
                } catch (Exception e) {
                    Log.e("EventApi", "Error procesando respuesta: " + e.getMessage());
                    errorListener.onErrorResponse(new VolleyError("Error procesando respuesta: " + e.getMessage()));
                }
            },
            error -> {
                Log.e("EventApi", "Error al crear reserva:", error);
                errorListener.onErrorResponse(error);
            }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        
        queue.add(request);
    }
}
