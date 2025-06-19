package com.example.popayan_noc.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.popayan_noc.R;
import com.example.popayan_noc.adapter.EventAdapter;
import com.example.popayan_noc.model.Evento;

import java.util.List;

public class EventosProximosActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventos_proximos);

        RecyclerView rv = findViewById(R.id.rvEventosProximos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        // Animación de entrada (removida porque causaba crash)

        // Datos de ejemplo (mock) para eventos
        org.json.JSONArray eventos = new org.json.JSONArray();
        try {
            eventos.put(new org.json.JSONObject()
                    .put("nombre", "Festival de Música Nocturna")
                    .put("fecha", "10 Junio 2025, 8:00 pm")
                    .put("descripcion", "Disfruta de una noche llena de música, luces y food trucks en el parque principal."));
            eventos.put(new org.json.JSONObject()
                    .put("nombre", "Feria Gastronómica Popayán")
                    .put("fecha", "15 Junio 2025, 12:00 pm")
                    .put("descripcion", "Sabores únicos, chefs invitados y experiencias culinarias para toda la familia."));
            eventos.put(new org.json.JSONObject()
                    .put("nombre", "Caminata Nocturna Histórica")
                    .put("fecha", "18 Junio 2025, 7:30 pm")
                    .put("descripcion", "Recorrido guiado por los lugares más emblemáticos del centro histórico de Popayán."));
            eventos.put(new org.json.JSONObject()
                    .put("nombre", "Cine al Parque")
                    .put("fecha", "22 Junio 2025, 7:00 pm")
                    .put("descripcion", "Proyección gratuita de películas familiares bajo las estrellas."));
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        EventAdapter adapter = new EventAdapter((Context) this, (List<Evento>) eventos);
        rv.setAdapter(adapter);

        // Botón volver
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
    }
}
