package com.example.popayan_noc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.view.MotionEvent;
import android.widget.RatingBar;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import com.example.popayan_noc.model.Lugar; // Import the new Lugar model
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.widget.Toast; // Asegurarse que Toast está importado
import java.util.ArrayList; // Para pasar lista de fotos

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {
    private Context context;
    private List<Lugar> placeList;

    public PlaceAdapter(Context context, List<Lugar> placeList) {
        this.context = context;
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Lugar place = placeList.get(position);
        holder.tvPlaceName.setText(place.getNombre());
        holder.tvPlaceLocation.setText(place.getUbicacion());

        // Campos que no están en el nuevo modelo Lugar o no se usan directamente aquí
        holder.tvPlaceDistance.setText(""); // Opcional: calcular si es posible
        holder.tvPlaceDates.setText("");    // Opcional: si se añade a Lugar
        holder.tvPlacePrice.setText("");    // Opcional: si se añade a Lugar

        // Manejo de categoría desde el objeto Lugar
        if (place.getCategoria() != null && place.getCategoria().getTipo() != null && !place.getCategoria().getTipo().isEmpty()) {
            holder.tvTag.setText(place.getCategoria().getTipo());
            holder.tvTag.setVisibility(View.VISIBLE);
        } else {
            holder.tvTag.setVisibility(View.GONE);
        }

        // Rating no está en el nuevo modelo Lugar, así que se ocultan los elementos de rating
        holder.ratingBar.setVisibility(View.GONE);
        holder.tvPlaceRating.setVisibility(View.GONE);

        // Cargar imagen con Glide usando el getter del modelo Lugar
        if (place.getImagen() != null && !place.getImagen().isEmpty()) {
            Glide.with(context).load(place.getImagen()).placeholder(R.drawable.placeholder_img).into(holder.imgPlace);
        } else {
            holder.imgPlace.setImageResource(R.drawable.placeholder_img); // Imagen por defecto
        }

        // --- INICIO: Lógica para nuevos elementos UI ---

        // Indicador de Estado
        // Asumimos que 'lugar.isEstado()' devuelve true si está activo/abierto, false si no.
        // Necesitarás definir los colores apropiados en tus resources o directamente.
        if (place.isEstado()) {
            holder.viewPlaceStatusIndicator.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde para activo
            holder.viewPlaceStatusIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.viewPlaceStatusIndicator.setBackgroundColor(Color.parseColor("#F44336")); // Rojo para inactivo/cerrado
            holder.viewPlaceStatusIndicator.setVisibility(View.VISIBLE); // O GONE si no quieres mostrarlo cuando está inactivo
        }
        // Si quieres que esté oculto si no hay info de estado, tendrías que añadir una lógica para ello.

        // Icono de Galería
        if (place.getFotosLugar() != null && !place.getFotosLugar().isEmpty()) {
            holder.ivGalleryIcon.setVisibility(View.VISIBLE);
            holder.ivGalleryIcon.setOnClickListener(v -> {
                // Abrir el dialog de galería tipo carrusel
                if (context instanceof androidx.fragment.app.FragmentActivity) {
                    androidx.fragment.app.FragmentManager fm = ((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager();
                    com.example.popayan_noc.ImageGalleryDialogFragment dialog = com.example.popayan_noc.ImageGalleryDialogFragment.newInstance(new java.util.ArrayList<>(place.getFotosLugar()));
                    dialog.show(fm, "image_gallery");
                }
            });
        } else {
            holder.ivGalleryIcon.setVisibility(View.GONE);
        }

        // Icono de Menú PDF
        if (place.getCartaPdf() != null && !place.getCartaPdf().isEmpty()) {
            holder.ivMenuIcon.setVisibility(View.VISIBLE);
            holder.ivMenuIcon.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(place.getCartaPdf()));
                if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(browserIntent);
                } else {
                    Toast.makeText(context, "No se encontró una app para abrir el PDF.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            holder.ivMenuIcon.setVisibility(View.GONE);
        }

        // Icono de Mapa
        if (place.getUbicacion() != null && !place.getUbicacion().isEmpty()) {
            holder.ivMapIcon.setVisibility(View.VISIBLE);
            holder.ivMapIcon.setOnClickListener(v -> {
                // Crear Uri para la búsqueda en el mapa. Formato: "geo:0,0?q=direccion"
                // O si tienes latitud/longitud: "geo:lat,lng?q=lat,lng(NombreLugar)"
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(place.getUbicacion() + ", " + place.getNombre()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps"); // Intenta abrir Google Maps directamente
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    // Si Google Maps no está, intenta con cualquier app de mapas
                    Intent genericMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(place.getUbicacion())));
                    if (genericMapIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(genericMapIntent);
                    } else {
                        Toast.makeText(context, "No se encontró una app de mapas.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            holder.ivMapIcon.setVisibility(View.GONE);
        }

        // --- FIN: Lógica para nuevos elementos UI ---

        // Listener para abrir PlaceDetailFragment con argumentos del nuevo modelo Lugar
        holder.cardView.setOnClickListener(v -> {
            androidx.fragment.app.Fragment fragment = new com.example.popayan_noc.PlaceDetailFragment();
            android.os.Bundle args = new android.os.Bundle();
            args.putInt("lugarId", place.getId());
            args.putString("lugarNombre", place.getNombre());
            args.putString("lugarDescripcion", place.getDescripcion() != null ? place.getDescripcion() : "");
            args.putString("lugarUbicacion", place.getUbicacion() != null ? place.getUbicacion() : "");
            args.putString("lugarImagen", place.getImagen() != null ? place.getImagen() : "");
            // Considerar pasar más datos si PlaceDetailFragment los puede manejar, ej. fotosLugar o categoría completa
            // args.putStringArrayList("lugarFotos", (java.util.ArrayList<String>) place.getFotosLugar());
            // if (place.getCategoria() != null) { args.putString("lugarCategoriaTipo", place.getCategoria().getTipo()); }
            args.putString("lugarEventos", "No hay eventos asociados"); // Mantener o adaptar si hay datos de eventos
            fragment.setArguments(args);

            if (context instanceof androidx.fragment.app.FragmentActivity) {
                ((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, fragment) // Asegúrate que R.id.main es el ID correcto del contenedor de fragmentos
                    .addToBackStack(null)
                    .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace, ivGalleryIcon, ivMenuIcon, ivMapIcon;
        TextView tvPlaceName, tvPlaceLocation, tvPlaceDistance, tvPlaceDates, tvPlacePrice, tvPlaceRating, tvTag;
        CardView cardView;
        RatingBar ratingBar;
        View viewPlaceStatusIndicator;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.imgPlace);
            cardView = (CardView) itemView;
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvPlaceRating = itemView.findViewById(R.id.tvPlaceRating);
            // Animación de escala al presionar
            cardView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        cardView.animate().scaleX(0.97f).scaleY(0.97f).setDuration(120).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        cardView.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                        break;
                }
                return false;
            });
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceLocation = itemView.findViewById(R.id.tvPlaceLocation);
            tvPlaceDistance = itemView.findViewById(R.id.tvPlaceDistance);
            tvPlaceDates = itemView.findViewById(R.id.tvPlaceDates);
            tvPlacePrice = itemView.findViewById(R.id.tvPlacePrice);
            tvPlaceRating = itemView.findViewById(R.id.tvPlaceRating);
            tvTag = itemView.findViewById(R.id.tvTag);
            // Nuevos elementos de UI
            viewPlaceStatusIndicator = itemView.findViewById(R.id.viewPlaceStatusIndicator);
            ivGalleryIcon = itemView.findViewById(R.id.ivGalleryIcon);
            ivMenuIcon = itemView.findViewById(R.id.ivMenuIcon);
            ivMapIcon = itemView.findViewById(R.id.ivMapIcon);
            cardView = (CardView) itemView;
        }
    }
}
