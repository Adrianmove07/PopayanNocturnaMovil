// src/main/java/com/example/popayan_noc/adapter/ReservationsAdapter.java
package com.example.popayan_noc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.popayan_noc.R; // Asegúrate de que esta línea apunte a tu clase R
import com.example.popayan_noc.model.Reserva;
import java.util.List;

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ReservationViewHolder> {

    private List<Reserva> reservationList; // Lista de reservas a mostrar

    public ReservationsAdapter(List<Reserva> reservationList) {
        this.reservationList = reservationList;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout de un solo elemento de reserva
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        // Obtiene la reserva en la posición actual
        Reserva reserva = reservationList.get(position);

        // Asigna los datos de la reserva a los TextViews del layout del elemento
        holder.tvEventName.setText("Nombre del Evento: " + reserva.getEvento().getNombre());
        holder.tvReservationNumber.setText("Reserva #: " + reserva.getNumero_reserva());

        // Para la fecha, puedes mostrar solo la parte de la fecha, no la hora completa
        String[] dateTime = reserva.getEvento().getFechaHora().split("T");
        holder.tvEventDate.setText("Fecha: " + dateTime[0]);

        holder.tvLocation.setText("Ubicación: " + reserva.getEvento().getLugar().getNombre() + ", " + reserva.getEvento().getLugar().getUbicacion());
        holder.tvTickets.setText("Cantidad de Entradas: " + reserva.getCantidad_entradas());
        holder.tvStatus.setText("Estado: " + reserva.getAprobacion());

        // Puedes añadir lógica aquí para cambiar el color del estado si lo deseas
        if ("Pendiente".equalsIgnoreCase(reserva.getAprobacion())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
        } else if ("Aprobado".equalsIgnoreCase(reserva.getAprobacion())) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        // Retorna el número total de elementos en la lista
        return reservationList.size();
    }

    // Clase interna ViewHolder para contener las vistas de cada elemento
    public static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvReservationNumber, tvEventDate, tvLocation, tvTickets, tvStatus;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicializa los TextViews a partir del layout del elemento
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvReservationNumber = itemView.findViewById(R.id.tvReservationNumber);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTickets = itemView.findViewById(R.id.tvTickets);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}