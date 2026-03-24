package com.example.gestorpro;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private OnProductoClickListener listener;

    public interface OnProductoClickListener {
        void onProductClick(Producto producto);
    }

    public ProductosAdapter(List<Producto> productos, OnProductoClickListener listener) {
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
        holder.bind(producto, listener);
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

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvCode, tvPrice, tvStock;
        private ImageView ivImage;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvCode = itemView.findViewById(R.id.tvProductCode);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStock = itemView.findViewById(R.id.tvProductStock);
            ivImage = itemView.findViewById(R.id.ivProductImage);
        }

        public void bind(final Producto producto, final OnProductoClickListener listener) {
            tvName.setText(producto.getNombre());
            tvCode.setText("Código " + (producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "---"));
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecioUnitario()));
            tvStock.setText(String.format(Locale.getDefault(), "Stock: %d/%d", 
                    producto.getCantidadActual(), producto.getCantidadMinima()));

            // Lógica de colores para el stock
            if (producto.getCantidadActual() <= producto.getCantidadMinima()) {
                tvStock.setTextColor(Color.RED);
            } else {
                tvStock.setTextColor(Color.parseColor("#00BCD4"));
            }

            itemView.setOnClickListener(v -> listener.onProductClick(producto));
        }
    }
}
