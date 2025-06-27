package com.example.popayan_noc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.popayan_noc.R;
import com.example.popayan_noc.model.Categoria;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private Context context;
    private List<Categoria> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Categoria category, int position);
    }

    public CategoryAdapter(Context context, List<Categoria> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList != null ? categoryList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Categoria category = categoryList.get(position);

        String nombreCategoria = (category.getTipo() != null && !category.getTipo().trim().isEmpty())
                ? capitalize(category.getTipo()) : "Categoría";
        holder.tvCategoryName.setText(nombreCategoria);
        holder.tvCategoryName.setVisibility(View.VISIBLE);
        holder.tvCategoryName.setTextColor(Color.parseColor("#000000"));
        holder.tvCategoryName.setGravity(Gravity.CENTER);

        Log.d("CategoryAdapter", "Category: " + category.getTipo() + ", Image URL: " + category.getImagen());

        if (category.getImagen() != null && !category.getImagen().isEmpty()) {
            Glide.with(context)
                    .load(category.getImagen())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_category_placeholder)
                            .error(R.drawable.ic_category_culture)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop())
                    .into(holder.imgCategoryIcon);
        } else {
            setDefaultIcon(holder.imgCategoryIcon, category.getTipo());
            Log.w("CategoryAdapter", "No image URL for category: " + category.getTipo() + ". Using default icon.");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void setCategories(List<Categoria> categories) {
        this.categoryList.clear();
        if (categories == null || categories.isEmpty()) {
            this.categoryList.addAll(getDefaultCategories());
            Log.d("CategoryAdapter", "Setting default categories. Incoming list was null or empty.");
        } else {
            this.categoryList.addAll(categories);
            Log.d("CategoryAdapter", "Setting " + categories.size() + " categories from data source.");
        }
        notifyDataSetChanged();
    }

    private List<Categoria> getDefaultCategories() {
        List<Categoria> defaults = new ArrayList<>();
        defaults.add(new Categoria(0, "Comida", "Descripción comida por defecto", "", true));
        return defaults;
    }

    private void setDefaultIcon(ImageView imageView, String tipo) {
        String normalizedTipo = (tipo != null) ? tipo.trim().toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ü", "u") : "";

        int iconRes;
        switch (normalizedTipo) {
            case "comida":
            case "food":
            case "restaurante":
                iconRes = R.drawable.ic_category_food;
                break;
            case "bebida":
            case "drink":
            case "bar":
                iconRes = R.drawable.ic_category_drink;
                break;
            case "entretenimiento":
            case "entertainment":
            case "discoteca":
                iconRes = R.drawable.ic_category_entertainment;
                break;
            case "cultura":
            case "culture":
                iconRes = R.drawable.ic_category_culture;
                break;
            case "deporte":
            case "sport":
            case "canchas sinteticas":
                iconRes = R.drawable.ic_category_sport;
                break;
            case "naturaleza":
            case "nature":
                iconRes = R.drawable.ic_category_nature;
                break;
            case "musica":
            case "música":
            case "music":
                iconRes = R.drawable.ic_category_music;
                break;
            default:
                iconRes = R.drawable.ic_category_apps;
        }
        imageView.setImageResource(iconRes);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategoryIcon;
        TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategoryIcon = itemView.findViewById(R.id.imgCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
