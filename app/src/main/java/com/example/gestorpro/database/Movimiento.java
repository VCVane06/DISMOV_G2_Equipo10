package com.example.gestorpro.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "movimientos",
        foreignKeys = {
                @ForeignKey(entity = Producto.class,
                        parentColumns = "id",
                        childColumns = "producto_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Venta.class,
                        parentColumns = "id",
                        childColumns = "venta_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class Movimiento {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "producto_id")
    private int productoId;

    private String tipo; // "ENTRADA" o "SALIDA"

    private int cantidad;

    @ColumnInfo(name = "precio_unitario")
    private double precioUnitario;

    private long fecha;

    @ColumnInfo(name = "venta_id")
    private Integer ventaId; // Puede ser null si es una entrada de stock

    private String nota;

    // Constructor
    public Movimiento(int productoId, String tipo, int cantidad, double precioUnitario,
                      long fecha, Integer ventaId, String nota) {
        this.productoId = productoId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.fecha = fecha;
        this.ventaId = ventaId;
        this.nota = nota;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }

    public Integer getVentaId() { return ventaId; }
    public void setVentaId(Integer ventaId) { this.ventaId = ventaId; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
}
