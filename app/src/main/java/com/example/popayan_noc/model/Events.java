// Events.java
package com.example.popayan_noc.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Events { // Clase Events (plural)

    private int id;
    private String nombre;
    private int capacidad;
    private String precio;
    private String descripcion;
    private String fechaHora;
    private boolean estado;
    private int usuarioid;
    private List<String> portada; // Una lista de URLs de imágenes
    private LugarSimple lugar; // Clase anidada para la información simplificada del lugar

    public Events(int id, String nombre, int capacidad, String precio, String descripcion,
                  String fechaHora, boolean estado, int usuarioid, List<String> portada, LugarSimple lugar) {
        this.id = id;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.precio = precio;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.usuarioid = usuarioid;
        this.portada = portada != null ? portada : new ArrayList<>(); // Asegurarse de que no sea nulo
        this.lugar = lugar;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getCapacidad() { return capacidad; }
    public String getPrecio() { return precio; }
    public String getDescripcion() { return descripcion; }
    public String getFechaHora() { return fechaHora; }
    public boolean isEstado() { return estado; }
    public int getUsuarioid() { return usuarioid; }
    public List<String> getPortada() { return portada; } // Retorna la lista de URLs
    public LugarSimple getLugar() { return lugar; }

    // SETTERS (si son necesarios)
    // ...

    // Clase anidada para el lugar simplificado
    public static class LugarSimple {
        private int id;
        private String nombre;

        public LugarSimple(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
    }

    // Método para parsear el string de portada a una lista de Strings
    // Este método es útil si la API devuelve una cadena como "url1,url2"
    // Sin embargo, si la API devuelve un JSONArray de URLs, este método no es estrictamente necesario,
    // pero lo he mantenido por si lo usas en otros contextos.
    public static List<String> parsePortadaString(String portadaString) {
        List<String> urls = new ArrayList<>();
        if (portadaString != null && !portadaString.isEmpty()) {
            // Asume que la cadena podría contener múltiples URLs separadas por coma si ese fuera el caso en otro lado
            // Para tu JSON actual, solo contendrá una URL directamente
            String[] parts = portadaString.split(",");
            for (String part : parts) {
                String trimmedPart = part.trim();
                if (!trimmedPart.isEmpty()) {
                    urls.add(trimmedPart);
                }
            }
        }
        return urls;
    }
}