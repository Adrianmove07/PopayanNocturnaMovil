package com.example.popayan_noc.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Lugar implements Parcelable {
    private int id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String imagen; // URL de la imagen principal
    //private String cartaPdf;


    @SerializedName("fotos_lugar")
    private List<String> fotosLugar;

    @SerializedName("categoriaid")
    private int categoriaId;

    @SerializedName("carta_pdf")
    private String cartaPdf;

    @SerializedName("estado")
    private boolean estado;

    private String createdAt;
    private String updatedAt;
    private Categoria categoria;

    // Constructor vacío (necesario para algunas librerías de deserialización)
    public Lugar() {
    }

    // Constructor para Parcelable
    protected Lugar(Parcel in) {
        id = in.readInt();
        nombre = in.readString();
        descripcion = in.readString();
        ubicacion = in.readString();
        imagen = in.readString();
        fotosLugar = in.createStringArrayList();
        categoriaId = in.readInt();
        estado = in.readByte() != 0;
        createdAt = in.readString();
        updatedAt = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nombre);
        dest.writeString(descripcion);
        dest.writeString(ubicacion);
        dest.writeString(imagen);
        dest.writeStringList(fotosLugar);
        dest.writeInt(categoriaId);
        dest.writeByte((byte) (estado ? 1 : 0));
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Lugar> CREATOR = new Creator<Lugar>() {
        @Override
        public Lugar createFromParcel(Parcel in) {
            return new Lugar(in);
        }

        @Override
        public Lugar[] newArray(int size) {
            return new Lugar[size];
        }
    };

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public List<String> getFotosLugar() {
        return fotosLugar;
    }

    public void setFotosLugar(List<String> fotosLugar) {
        this.fotosLugar = fotosLugar;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getCartaPdf() {
        return cartaPdf;
    }

    public void setCartaPdf(String cartaPdf) {
        this.cartaPdf = cartaPdf;
    }

    public boolean isEstado() { // Para booleanos, es común usar 'is' en el getter
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
