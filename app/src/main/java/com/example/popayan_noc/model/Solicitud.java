package com.example.popayan_noc.model;

public class Solicitud {
    private int id;
    private int usuario_id;
    private String descripcion;
    private String estado;
    private String created_at;
    private String updated_at;

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuario_id() { return usuario_id; }
    public void setUsuario_id(int usuario_id) { this.usuario_id = usuario_id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
}
