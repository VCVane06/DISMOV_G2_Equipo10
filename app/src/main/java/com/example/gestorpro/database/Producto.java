package com.example.gestorpro.database;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Producto implements Serializable {
    private String id;
    private String codigo_barras;
    private String nombre;
    private String descripcion_corta;
    private double precio;
    private long stock_actual;
    private long stock_minimo;
    private String id_categoria;
    private String sub_categoria;
    private String presentacion;
    private String marca;
    private String sabor;
    private String usuario_creador_id;
    private Timestamp timestamp_creacion;
    private String url_imagen;
    private long ventasTotales; // Cambiado a long para compatibilidad con Firestore increment

    public Producto() {
    }

    public Producto(String id, String usuario_creador_id, String codigo_barras, String nombre, double precio, long stock_actual, long stock_minimo, String id_categoria) {
        this.id = id;
        this.usuario_creador_id = usuario_creador_id;
        this.codigo_barras = codigo_barras;
        this.nombre = nombre;
        this.precio = precio;
        this.stock_actual = stock_actual;
        this.stock_minimo = stock_minimo;
        this.id_categoria = id_categoria;
        this.timestamp_creacion = Timestamp.now();
        this.ventasTotales = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigo_barras() { return codigo_barras; }
    public void setCodigo_barras(String codigo_barras) { this.codigo_barras = codigo_barras; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion_corta() { return descripcion_corta; }
    public void setDescripcion_corta(String descripcion_corta) { this.descripcion_corta = descripcion_corta; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public long getStock_actual() { return stock_actual; }
    public void setStock_actual(long stock_actual) { this.stock_actual = stock_actual; }

    public long getStock_minimo() { return stock_minimo; }
    public void setStock_minimo(long stock_minimo) { this.stock_minimo = stock_minimo; }

    public String getId_categoria() { return id_categoria; }
    public void setId_categoria(String id_categoria) { this.id_categoria = id_categoria; }

    public String getSub_categoria() { return sub_categoria; }
    public void setSub_categoria(String sub_categoria) { this.sub_categoria = sub_categoria; }

    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getSabor() { return sabor; }
    public void setSabor(String sabor) { this.sabor = sabor; }

    public String getUsuario_creador_id() { return usuario_creador_id; }
    public void setUsuario_creador_id(String usuario_creador_id) { this.usuario_creador_id = usuario_creador_id; }

    public Timestamp getTimestamp_creacion() { return timestamp_creacion; }
    public void setTimestamp_creacion(Timestamp timestamp_creacion) { this.timestamp_creacion = timestamp_creacion; }

    public String getUrl_imagen() { return url_imagen; }
    public void setUrl_imagen(String url_imagen) { this.url_imagen = url_imagen; }

    public long getVentasTotales() { return ventasTotales; }
    public void setVentasTotales(long ventasTotales) { this.ventasTotales = ventasTotales; }
}
