package com.example.popayan_noc.service; // O el paquete donde tengas tus servicios de API

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest; // Para respuestas de objetos JSON
import com.android.volley.toolbox.Volley;
import org.json.JSONObject; // Importa JSONObject
import java.util.HashMap;
import java.util.Map;

public class PlaceApi {
    private static final String BASE_URL = "https://popnocturna.vercel.app/api";

    /**
     * Obtiene los lugares de una categoría específica.
     * @param context El contexto de la aplicación.
     * @param token El token de autorización.
     * @param categoriaId El ID de la categoría a buscar.
     * @param listener Listener para la respuesta exitosa.
     * @param errorListener Listener para errores.
     */
    public static void getLugaresByCategoria(Context context, String token, int categoriaId,
                                             Response.Listener<JSONObject> listener, // Ahora esperamos un JSONObject
                                             Response.ErrorListener errorListener) {
        String url = BASE_URL + "/lugares/categoria/" + categoriaId;
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                // Si necesitas un Content-Type específico, agrégalo aquí
                // headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(request);
    }

    // Aquí podrías agregar otros métodos para Places, como obtener todos los lugares,
    // obtener detalles de un lugar por ID, etc.
}