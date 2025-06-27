package com.example.popayan_noc.service;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
public class NotificacionApi {

    private static final String BASE_URL = "https://popnocturna.vercel.app/api";

    public static void getNotificaciones(Context context, String token, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/notificacion";
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray notificaciones = response.getJSONArray("notificaciones");
                        listener.onResponse(notificaciones);
                    } catch (Exception e) {
                        Log.e("NotificacionesApi", "Error al parsear JSON: " + e.getMessage());
                        errorListener.onErrorResponse(new VolleyError("Error al parsear notificaciones"));
                    }
                },
                error -> {
                    Log.e("NotificacionesApi", "Error en la solicitud:", error);
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

    public static void eliminarNotificacion(Context context, String token, int id, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/notificacion/" + id;

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    listener.onResponse(response);
                },
                error -> {
                    Log.e("NotificacionesApi", "Error al eliminar notificación:", error);
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
