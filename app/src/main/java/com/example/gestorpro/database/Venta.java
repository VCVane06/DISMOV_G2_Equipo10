package com.example.gestorpro.database;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;
import java.util.List;

public class Venta implements Serializable {
    @DocumentId
    private String id_venta;
    private Timestamp fecha;
    private double total;
    private List<VentaItemSnapshot> lista_productos;
    private String id_vendedor;
    private String nombre_vendedor;
    private String metodo_pago;

    public Venta() {
    }

    public Venta(String id_vendedor, String nombre_vendedor, double total, List<VentaItemSnapshot> lista_productos) {
        this.id_vendedor = id_vendedor;
        this.nombre_vendedor = nombre_vendedor;
        this.total = total;
        this.lista_productos = lista_productos;
        this.metodo_pago = "Efectivo";
        this.fecha = Timestamp.now(); // Sincronización instantánea
    }

    public String getId_venta() { return id_venta; }
    public void setId_venta(String id_venta) { this.id_venta = id_venta; }
    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public List<VentaItemSnapshot> getLista_productos() { return lista_productos; }
    public void setLista_productos(List<VentaItemSnapshot> lista_productos) { this.lista_productos = lista_productos; }
    public String getId_vendedor() { return id_vendedor; }
    public void setId_vendedor(String id_vendedor) { this.id_vendedor = id_vendedor; }
    public String getNombre_vendedor() { return nombre_vendedor; }
    public void setNombre_vendedor(String nombre_vendedor) { this.nombre_vendedor = nombre_vendedor; }
    public String getMetodo_pago() { return metodo_pago; }
    public void setMetodo_pago(String metodo_pago) { this.metodo_pago = metodo_pago; }

    public static class VentaItemSnapshot implements Serializable {
        private String producto_id;
        private String codigo_barras;
        private String nombre_producto;
        private long cantidad;
        private double precio_unitario;
        private double subtotal;
        private String id_categoria;
        private String nombre_categoria;
        private String sub_categoria;
        private String presentacion;
        private String marca;
        private String sabor;

        public VentaItemSnapshot() {}

        public VentaItemSnapshot(Producto p, long cantidad) {
            this.producto_id = p.getId();
            this.codigo_barras = p.getCodigo_barras();
            this.nombre_producto = p.getNombre();
            this.cantidad = cantidad;
            this.precio_unitario = p.getPrecio();
            this.subtotal = cantidad * p.getPrecio();
            this.id_categoria = p.getId_categoria();
            this.sub_categoria = p.getSub_categoria();
            this.presentacion = p.getPresentacion();
            this.marca = p.getMarca();
            this.sabor = p.getSabor();
        }

        // Getters y Setters
        public String getProducto_id() { return producto_id; }
        public void setProducto_id(String producto_id) { this.producto_id = producto_id; }
        public String getCodigo_barras() { return codigo_barras; }
        public void setCodigo_barras(String codigo_barras) { this.codigo_barras = codigo_barras; }
        public String getNombre_producto() { return nombre_producto; }
        public void setNombre_producto(String nombre_producto) { this.nombre_producto = nombre_producto; }
        public long getCantidad() { return cantidad; }
        public void setCantidad(long cantidad) { this.cantidad = cantidad; }
        public double getPrecio_unitario() { return precio_unitario; }
        public void setPrecio_unitario(double precio_unitario) { this.precio_unitario = precio_unitario; }
        public double getSubtotal() { return subtotal; }
        public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
        public String getId_categoria() { return id_categoria; }
        public void setId_categoria(String id_categoria) { this.id_categoria = id_categoria; }
        public String getNombre_categoria() { return nombre_categoria; }
        public void setNombre_categoria(String nombre_categoria) { this.nombre_categoria = nombre_categoria; }
        public String getSub_categoria() { return sub_categoria; }
        public void setSub_categoria(String sub_categoria) { this.sub_categoria = sub_categoria; }
        public String getPresentacion() { return presentacion; }
        public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
        public String getMarca() { return marca; }
        public void setMarca(String marca) { this.marca = marca; }
        public String getSabor() { return sabor; }
        public void setSabor(String sabor) { this.sabor = sabor; }
    }
}
