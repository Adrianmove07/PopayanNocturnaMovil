// EventsApi.java
package com.example.popayan_noc.service;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EventsApi {
    private static final String BASE_URL = "https://popnocturna.vercel.app/api";

    /**
     * Obtiene los eventos de un lugar específico.
     * @param context El contexto de la aplicación.
     * @param token El token de autorización.
     * @param lugarId El ID del lugar para el que se buscan eventos.
     * @param listener Listener para la respuesta exitosa.
     * @param errorListener Listener para errores.
     */
    public static void getEventosByLugares(Context context, String token, int lugarId,
                                           Response.Listener<JSONObject> listener,
                                           Response.ErrorListener errorListener) {
        String url = BASE_URL + "/lugares/" + lugarId + "/eventos";
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }
}