package com.example.popayan_noc.model;

public class Categoria {
    private String tipo;

    // Constructor, Getters y Setters
    public Categoria() {
    }

    public Categoria(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
