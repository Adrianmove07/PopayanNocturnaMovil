package com.example.popayan_noc.model;

import java.io.Serializable; // Para poder pasar el objeto entre fragments/activities
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Place implements Serializable { // Implementa Serializable para pasar el objeto
    private int id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String imagen; // URL de la imagen principal del lugar
    private int categoriaid;
    private Categoria categoria; // Puedes incluir el objeto Categoría si lo necesitas en el lugar
    private List<String> fotos_lugar; // Lista de URLs de fotos adicionales

    // Constructor
    public Place(int id, String nombre, String descripcion, String ubicacion, String imagen, int categoriaid, Categoria categoria, List<String> fotos_lugar) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.imagen = imagen;
        this.categoriaid = categoriaid;
        this.categoria = categoria;
        this.fotos_lugar = fotos_lugar;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getImagen() {
        return imagen;
    }

    public int getCategoriaid() {
        return categoriaid;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public List<String> getFotos_lugar() {
        return fotos_lugar;
    }

    // Opcional: Setters si planeas modificar los datos de los objetos Place
    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public void setCategoriaid(int categoriaid) {
        this.categoriaid = categoriaid;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public void setFotos_lugar(List<String> fotos_lugar) {
        this.fotos_lugar = fotos_lugar;
    }

    // Método estático para parsear las URLs de fotos si vienen como un solo string separado por comas
    public static List<String> parseFotosString(String fotosString) {
        if (fotosString == null || fotosString.isEmpty()) {
            return new ArrayList<>();
        }
        // Limpia espacios en blanco alrededor de las comas y luego divide
        return Arrays.asList(fotosString.split("\\s*,\\s*"));
    }
}