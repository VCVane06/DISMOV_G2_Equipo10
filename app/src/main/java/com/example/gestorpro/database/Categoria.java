package com.example.gestorpro.database;

import java.io.Serializable;

public class Categoria implements Serializable {
    private String id_categoria;
    private String nombre_categoria;
    private String descripcion;
    private String url_icono;

    public Categoria() {
    }

    public Categoria(String id_categoria, String nombre_categoria, String descripcion) {
        this.id_categoria = id_categoria;
        this.nombre_categoria = nombre_categoria;
        this.descripcion = descripcion;
    }

    public String getId_categoria() { return id_categoria; }
    public void setId_categoria(String id_categoria) { this.id_categoria = id_categoria; }

    public String getNombre_categoria() { return nombre_categoria; }
    public void setNombre_categoria(String nombre_categoria) { this.nombre_categoria = nombre_categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUrl_icono() { return url_icono; }
    public void setUrl_icono(String url_icono) { this.url_icono = url_icono; }

    @Override
    public String toString() {
        return nombre_categoria;
    }
}
