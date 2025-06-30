package com.example.popayan_noc.model;

public class Reserva {
    private int id;
    private String numero_reserva;
    private String fecha_hora;
    private int cantidad_entradas;
    private String aprobacion;
    private boolean estado;
    private Evento evento;
    private User usuario;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumero_reserva() {
        return numero_reserva;
    }

    public void setNumero_reserva(String numero_reserva) {
        this.numero_reserva = numero_reserva;
    }

    public String getFecha_hora() {
        return fecha_hora;
    }

    public void voidsetFecha_hora(String fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    public int getCantidad_entradas() {
        return cantidad_entradas;
    }

    public void setCantidad_entradas(int cantidad_entradas) {
        this.cantidad_entradas = cantidad_entradas;
    }

    public String getAprobacion() {
        return aprobacion;
    }

    public void setAprobacion(String aprobacion) {
        this.aprobacion = aprobacion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }
}