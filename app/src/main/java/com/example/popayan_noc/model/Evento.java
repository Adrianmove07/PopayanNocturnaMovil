package com.example.popayan_noc.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Evento implements Parcelable {
    @SerializedName("id")
    private int id;
    @SerializedName("nombre")
    private String nombre;
    @SerializedName("descripcion")
    private String descripcion;
    @SerializedName("fecha_hora")
    private String fechaHora;
    @SerializedName("capacidad")
    private int capacidad;
    @SerializedName("precio")
    private String precio;
    @SerializedName("portada")
    private List<String> portada;
    @SerializedName("lugar")
    private Lugar lugar;

    public Evento() {
        portada = new ArrayList<>();
    }

    protected Evento(Parcel in) {
        id = in.readInt();
        nombre = in.readString();
        descripcion = in.readString();
        fechaHora = in.readString();
        capacidad = in.readInt();
        precio = in.readString();
        portada = in.createStringArrayList();
        lugar = in.readParcelable(Lugar.class.getClassLoader());
    }

    public static final Creator<Evento> CREATOR = new Creator<Evento>() {
        @Override
        public Evento createFromParcel(Parcel in) {
            return new Evento(in);
        }

        @Override
        public Evento[] newArray(int size) {
            return new Evento[size];
        }
    };

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

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public List<String> getPortada() {
        return portada;
    }

    public void setPortada(List<String> portada) {
        this.portada = portada;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public void setLugar(Lugar lugar) {
        this.lugar = lugar;
    }

    @SerializedName("estado")
    private String estado = "activo"; // Estado por defecto

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nombre);
        dest.writeString(descripcion);
        dest.writeString(fechaHora);
        dest.writeInt(capacidad);
        dest.writeString(precio);
        dest.writeStringList(portada);
        dest.writeParcelable(lugar, flags);
        dest.writeString(estado);
    }
}
