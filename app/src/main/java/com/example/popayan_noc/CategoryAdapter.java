package com.example.popayan_noc;

import java.util.ArrayList;
import android.graphics.Color;
import android.view.Gravity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private Context context;
    private List<Category> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
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
        android.util.Log.d("CategoryAdapter", "onBindViewHolder called for position: " + position);
        if (categoryList != null && position < categoryList.size()) {
            android.util.Log.d("CategoryAdapter", "Category name: " + categoryList.get(position).tipo);
        }
        Category category = categoryList.get(position);
        holder.tvCategoryName.setText(category.tipo);
        // Cargar imagen        // Lógica para asignar íconos bonitos y mostrar siempre el nombre
        // Normalizar el nombre de la categoría para mapear correctamente
        String tipo = (category.tipo != null) ? category.tipo.trim().toLowerCase().replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace("ü", "u") : "";
        int iconRes;
        switch (tipo) {
            case "comida":
            case "food":
                iconRes = R.drawable.ic_category_food;
                break;
            case "bebida":
            case "drink":
                iconRes = R.drawable.ic_category_drink;
                break;
            case "entretenimiento":
            case "entertainment":
                iconRes = R.drawable.ic_category_entertainment;
                break;
            case "cultura":
            case "culture":
                iconRes = R.drawable.ic_category_culture;
                break;
            case "deporte":
            case "sport":
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
        holder.imgCategoryIcon.setImageResource(iconRes);
        // Siempre mostrar el nombre de la categoría, incluso si está vacío
        String nombreCategoria = (category.tipo != null && !category.tipo.trim().isEmpty()) ? capitalize(category.tipo) : "Categoría";
        holder.tvCategoryName.setText(nombreCategoria);
        holder.tvCategoryName.setVisibility(View.VISIBLE);
        holder.tvCategoryName.setTextColor(Color.parseColor("#F6F6F6"));
        holder.tvCategoryName.setGravity(Gravity.CENTER);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category, holder.getAdapterPosition());
            }
        });
    }

    // Capitaliza la primera letra y pone el resto en minúscula
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // Agrega este método público para actualizar las categorías
    public void setCategories(List<Category> categories) {
    android.util.Log.d("CategoryAdapter", "setCategories called. Incoming size: " + (categories == null ? 0 : categories.size()));
        this.categoryList.clear();
        // Si el backend no manda categorías, crea una lista local por defecto
        if (categories == null || categories.isEmpty()) {
            this.categoryList.addAll(getDefaultCategories());
        } else {
            this.categoryList.addAll(categories);
        }
        notifyDataSetChanged();
    }

    // Categorías bonitas por defecto con nombre y mapeo a íconos
    private List<Category> getDefaultCategories() {
        List<Category> defaults = new ArrayList<>();
        defaults.add(new Category(0, "Comida", "", "", true));
        defaults.add(new Category(0, "Bebida", "", "", true));
        defaults.add(new Category(0, "Entretenimiento", "", "", true));
        defaults.add(new Category(0, "Cultura", "", "", true));
        defaults.add(new Category(0, "Deporte", "", "", true));
        defaults.add(new Category(0, "Naturaleza", "", "", true));
        defaults.add(new Category(0, "Música", "", "", true));
        return defaults;
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
