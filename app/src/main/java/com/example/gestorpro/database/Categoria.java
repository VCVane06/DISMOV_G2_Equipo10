package com.example.gestorpro.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class Categoria {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private String descripcion;

    // Constructor
    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters (obligatorios para Room)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
