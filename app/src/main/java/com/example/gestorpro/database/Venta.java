package com.example.gestorpro.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ventas")
public class Venta {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long fecha;
    private double total; // Podría calcularse, pero lo almacenamos para agilizar tickets

    // Constructor
    public Venta(long fecha, double total) {
        this.fecha = fecha;
        this.total = total;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}