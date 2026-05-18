package com.example.gestorpro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorpro.database.Venta;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Venta.VentaItemSnapshot> cartItems;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onQuantityChanged();
        void onItemRemoved(int position);
    }

    public CartAdapter(List<Venta.VentaItemSnapshot> cartItems, OnCartChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Venta.VentaItemSnapshot item = cartItems.get(position);
        holder.bind(item, position, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQty, tvSubtotal;
        ImageView btnAdd, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQty = itemView.findViewById(R.id.tvCartItemQty);
            tvSubtotal = itemView.findViewById(R.id.tvCartItemSubtotal);
            btnAdd = itemView.findViewById(R.id.btnAddQty);
            btnRemove = itemView.findViewById(R.id.btnRemoveQty);
        }

        public void bind(Venta.VentaItemSnapshot item, int position, OnCartChangeListener listener) {
            tvName.setText(item.getNombre_producto());
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f c/u", item.getPrecio_unitario()));
            tvQty.setText(String.valueOf(item.getCantidad()));
            tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", item.getCantidad() * item.getPrecio_unitario()));

            btnAdd.setOnClickListener(v -> {
                item.setCantidad(item.getCantidad() + 1);
                item.setSubtotal(item.getCantidad() * item.getPrecio_unitario());
                notifyItemChanged(position);
                listener.onQuantityChanged();
            });

            btnRemove.setOnClickListener(v -> {
                if (item.getCantidad() > 1) {
                    item.setCantidad(item.getCantidad() - 1);
                    item.setSubtotal(item.getCantidad() * item.getPrecio_unitario());
                    notifyItemChanged(position);
                    listener.onQuantityChanged();
                } else {
                    cartItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartItems.size());
                    listener.onItemRemoved(position);
                }
            });
        }
    }
}
