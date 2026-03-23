package com.example.gestorpro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gestorpro.database.Producto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder> {

    private List<Producto> productosOriginal = new ArrayList<>();
    private List<Producto> productosFiltrados = new ArrayList<>();
    private OnProductoActionListener listener;

    public interface OnProductoActionListener {
        void onVender(Producto producto);
        void onEditar(Producto producto);
        void onEliminar(Producto producto);
    }

    public ProductosAdapter(List<Producto> productos, OnProductoActionListener listener) {
        this.productosOriginal.addAll(productos);
        this.productosFiltrados.addAll(productos);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productosFiltrados.get(position);
        holder.bind(producto);
    }

    @Override
    public int getItemCount() {
        return productosFiltrados.size();
    }

    public void actualizarLista(List<Producto> nuevosProductos) {
        this.productosOriginal.clear();
        this.productosOriginal.addAll(nuevosProductos);
        this.productosFiltrados.clear();
        this.productosFiltrados.addAll(nuevosProductos);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        productosFiltrados.clear();
        if (texto.isEmpty()) {
            productosFiltrados.addAll(productosOriginal);
        } else {
            for (Producto producto : productosOriginal) {
                if (producto.getNombre().toLowerCase().contains(texto.toLowerCase()) ||
                        (producto.getCodigoBarras() != null &&
                                producto.getCodigoBarras().contains(texto))) {
                    productosFiltrados.add(producto);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ProductoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre, tvCodigo, tvCantidad, tvPrecio, tvAlerta;
        private Button btnVender, btnEditar, btnEliminar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreProducto);
            tvCodigo = itemView.findViewById(R.id.tvCodigoBarras);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvAlerta = itemView.findViewById(R.id.tvAlertaStock);
            btnVender = itemView.findViewById(R.id.btnVender);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }

        public void bind(Producto producto) {
            tvNombre.setText(producto.getNombre());
            if (producto.getCodigoBarras() != null && !producto.getCodigoBarras().isEmpty()) {
                tvCodigo.setText("Código: " + producto.getCodigoBarras());
                tvCodigo.setVisibility(View.VISIBLE);
            } else {
                tvCodigo.setVisibility(View.GONE);
            }

            tvCantidad.setText(String.format("Stock: %d", producto.getCantidadActual()));
            tvPrecio.setText(String.format(Locale.getDefault(), "Precio: $%.2f", producto.getPrecioUnitario()));

            // Verificar stock bajo
            if (producto.getCantidadActual() <= producto.getCantidadMinima()) {
                tvAlerta.setVisibility(View.VISIBLE);
                tvCantidad.setTextColor(0xFFFF0000);
            } else {
                tvAlerta.setVisibility(View.GONE);
                tvCantidad.setTextColor(0xFF000000);
            }

            btnVender.setOnClickListener(v -> listener.onVender(producto));
            btnEditar.setOnClickListener(v -> listener.onEditar(producto));
            btnEliminar.setOnClickListener(v -> listener.onEliminar(producto));
        }
    }
}