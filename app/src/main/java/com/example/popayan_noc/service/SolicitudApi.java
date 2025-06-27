package com.example.popayan_noc.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.popayan_noc.model.Solicitud;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolicitudApi {

    private static final String BASE_URL = "https://popnocturna.vercel.app/api/solicitud";

    public static void obtenerSolicitud(Context context, String token, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/mias";
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                listener,
                error -> {
                    Log.e("SolicitudApi", "Error al obtener solicitud:", error);
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

    public static void crearSolicitud(Context context, String token, JSONObject body, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL;
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                listener,
                error -> {
                    Log.e("SolicitudApi", "Error al crear solicitud:", error);
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

    public static void editarSolicitud(Context context, String token, int id, JSONObject body, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/" + id;
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body,
                listener,
                error -> {
                    Log.e("SolicitudApi", "Error al editar solicitud:", error);
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

    public static void eliminarSolicitud(Context context, String token, int id, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = BASE_URL + "/" + id;
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                listener,
                error -> {
                    Log.e("SolicitudApi", "Error al eliminar solicitud:", error);
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
