package com.example.popayan_noc.model;

public class Categoria {
    private int id;
    private String tipo;
    private String descripcion;
    private String imagen;
    private boolean estado;

    public Categoria() {}

    public Categoria(int id, String tipo, String descripcion, String imagen, boolean estado) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public String getImagen() { return imagen; }
    public boolean isEstado() { return estado; }

    public void setId(int id) { this.id = id; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public void setEstado(boolean estado) { this.estado = estado; }
}
