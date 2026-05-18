package com.example.gestorpro.database;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Movimiento implements Serializable {
    private String id_movimiento;
    private String id_producto;
    private String nombreProducto;
    private String tipo; // "entrada", "salida", "ajuste"
    private long cantidad;
    private double precio;
    private String id_usuario;
    private String nombre_usuario;
    private Timestamp timestamp; // Eliminado @ServerTimestamp para evitar nulls locales en el dashboard

    public Movimiento() {}

    public Movimiento(String id_movimiento, String id_producto, String nombreProducto, String tipo, long cantidad, double precio, String id_usuario, String nombre_usuario, String descripcion) {
        this.id_movimiento = id_movimiento;
        this.id_producto = id_producto;
        this.nombreProducto = nombreProducto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.precio = precio;
        this.id_usuario = id_usuario;
        this.nombre_usuario = nombre_usuario;
        this.descripcion = descripcion;
        this.timestamp = Timestamp.now(); // Asignación inmediata para sincronización en tiempo real
    }

    private String descripcion;

    public String getId_movimiento() { return id_movimiento; }
    public void setId_movimiento(String id_movimiento) { this.id_movimiento = id_movimiento; }

    public String getId_producto() { return id_producto; }
    public void setId_producto(String id_producto) { this.id_producto = id_producto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public long getCantidad() { return cantidad; }
    public void setCantidad(long cantidad) { this.cantidad = cantidad; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getId_usuario() { return id_usuario; }
    public void setId_usuario(String id_usuario) { this.id_usuario = id_usuario; }

    public String getNombre_usuario() { return nombre_usuario; }
    public void setNombre_usuario(String nombre_usuario) { this.nombre_usuario = nombre_usuario; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
