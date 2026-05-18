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

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Producto> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Producto producto);
    }

    public ProductAdapter(List<Producto> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    public void setProductList(List<Producto> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Producto producto = productList.get(position);
        holder.bind(producto, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvPrice, tvStock;
        ImageView ivImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvCode = itemView.findViewById(R.id.tvProductCode);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStock = itemView.findViewById(R.id.tvProductStock);
            ivImage = itemView.findViewById(R.id.ivProductImage);
        }

        public void bind(final Producto producto, final OnProductClickListener listener) {
            tvName.setText(producto.getNombre());
            tvCode.setText("Código " + (producto.getCodigo_barras() != null ? producto.getCodigo_barras() : "S/N"));
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));
            tvStock.setText(String.format(Locale.getDefault(), "Stock: %d/%d", 
                    producto.getStock_actual(), producto.getStock_minimo()));

            // Siempre usar imagen local como se solicitó
            ivImage.setImageResource(R.drawable.logo_gestionpro);

            // Lógica de colores para el stock
            if (producto.getStock_actual() <= producto.getStock_minimo()) {
                tvStock.setTextColor(Color.RED); // Stock bajo
            } else {
                tvStock.setTextColor(Color.parseColor("#00BCD4")); // Stock normal
            }

            itemView.setOnClickListener(v -> listener.onProductClick(producto));
        }
    }
}
