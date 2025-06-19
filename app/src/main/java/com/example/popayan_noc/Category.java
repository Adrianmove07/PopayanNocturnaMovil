package com.example.popayan_noc;

public class Category {
    public int id;
    public String tipo;
    public String descripcion;
    public String imagen;
    public boolean estado;

    public Category(int id, String tipo, String descripcion, String imagen, boolean estado) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.estado = estado;
    }
}
