package com.example.popayan_noc.model;

public class Notificacion {
    public int id;
    public int remitente_id;
    public int receptor_id;
    public String titulo;
    public String cuerpo;
    public String imagen;
    public String tipo;
    public boolean leida;
    public String createdAt;
    public String updatedAt;
    public Remitente remitente;

    public static class Remitente {
        public int id;
        public String nombre;

        public Remitente(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }

    public Notificacion(int id, int remitente_id, int receptor_id, String titulo, String cuerpo,
                        String imagen, String tipo, boolean leida, String createdAt, String updatedAt,
                        Remitente remitente) {
        this.id = id;
        this.remitente_id = remitente_id;
        this.receptor_id = receptor_id;
        this.titulo = titulo;
        this.cuerpo = cuerpo;
        this.imagen = imagen;
        this.tipo = tipo;
        this.leida = leida;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.remitente = remitente;
    }
}
