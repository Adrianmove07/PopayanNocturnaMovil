package com.example.popayan_noc.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.popayan_noc.R;
import com.example.popayan_noc.adapter.NotificacionAdapter;
import com.example.popayan_noc.model.Notificacion;
import com.example.popayan_noc.service.NotificacionApi;
import com.example.popayan_noc.util.AuthUtils;

import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    public NotificacionAdapter adapter;
    private ArrayList<Notificacion> lista;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lista = new ArrayList<>();

        String token = AuthUtils.getToken(requireContext());
        adapter = new NotificacionAdapter(lista, requireContext(), token);
        recyclerView.setAdapter(adapter);

        cargarNotificaciones(token);

        return view;
    }

    private void cargarNotificaciones(String token) {
        NotificacionApi.getNotificaciones(requireContext(), token,
                response -> {
                    lista.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject noti = response.getJSONObject(i);

                            JSONObject remitenteJson = noti.getJSONObject("remitente");
                            Notificacion.Remitente remitente = new Notificacion.Remitente(
                                    remitenteJson.getInt("id"),
                                    remitenteJson.getString("nombre")
                            );

                            Notificacion n = new Notificacion(
                                    noti.getInt("id"),
                                    noti.getInt("remitente_id"),
                                    noti.getInt("receptor_id"),
                                    noti.getString("titulo"),
                                    noti.getString("cuerpo"),
                                    noti.getString("imagen"),
                                    noti.getString("tipo"),
                                    noti.getBoolean("leida"),
                                    noti.getString("createdAt"),
                                    noti.getString("updatedAt"),
                                    remitente
                            );

                            lista.add(n);
                        } catch (Exception e) {
                            Log.e("NotificationsFragment", "Error parseando notificación", e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("NotificationsFragment", "Error obteniendo notificaciones", error);
                }
        );
    }
}
