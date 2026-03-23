package com.example.gestorpro.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.io.Serializable;

@Entity(tableName = "productos",
        foreignKeys = @ForeignKey(entity = Categoria.class,
                parentColumns = "id",
                childColumns = "categoria_id",
                onDelete = ForeignKey.SET_NULL))
public class Producto implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "codigo_barras")
    private String codigoBarras;

    private String nombre;

    @ColumnInfo(name = "cantidad_minima")
    private int cantidadMinima;

    @ColumnInfo(name = "cantidad_actual")
    private int cantidadActual;

    @ColumnInfo(name = "precio_unitario")
    private double precioUnitario;

    @ColumnInfo(name = "categoria_id")
    private Integer categoriaId; // Puede ser null

    @ColumnInfo(name = "ultima_actualizacion")
    private long ultimaActualizacion;

    // Constructor (sin id porque es auto-generado)
    public Producto(String codigoBarras, String nombre, int cantidadMinima,
                    int cantidadActual, double precioUnitario, Integer categoriaId, long ultimaActualizacion) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.cantidadMinima = cantidadMinima;
        this.cantidadActual = cantidadActual;
        this.precioUnitario = precioUnitario;
        this.categoriaId = categoriaId;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidadMinima() { return cantidadMinima; }
    public void setCantidadMinima(int cantidadMinima) { this.cantidadMinima = cantidadMinima; }

    public int getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(int cantidadActual) { this.cantidadActual = cantidadActual; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }

    public long getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(long ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
}
