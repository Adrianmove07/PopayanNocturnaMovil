package com.example.popayan_noc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.popayan_noc.model.Evento;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class EventosLugarFragment extends Fragment {
    private RecyclerView rvEventos;
    private TextView tvTitulo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eventos_lugar, container, false);
        rvEventos = view.findViewById(R.id.rvEventos);
        tvTitulo = view.findViewById(R.id.tvTitulo);

        Bundle args = getArguments();
        if (args != null) {
            String lugarNombre = args.getString("lugarNombre", "");
            List<Evento> eventos = new ArrayList<>();
            if (args.containsKey("eventos")) {
                eventos = args.getParcelableArrayList("eventos");
            }

            if (eventos != null && !eventos.isEmpty()) {
                tvTitulo.setText("Eventos de " + lugarNombre);
                rvEventos.setLayoutManager(new LinearLayoutManager(getContext()));
                rvEventos.setAdapter(new EventosAdapter(getContext(), eventos));
            } else {
                tvTitulo.setText("No hay eventos disponibles");
            }
        }

        return view;
    }
}
