package com.example.gestorpro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gestorpro.database.Venta;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SalesHistoryAdapter extends RecyclerView.Adapter<SalesHistoryAdapter.SaleViewHolder> {

    private List<Venta> salesList;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public SalesHistoryAdapter(List<Venta> salesList) {
        this.salesList = salesList;
    }

    public void setSalesList(List<Venta> salesList) {
        this.salesList = salesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_history, parent, false);
        return new SaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        Venta venta = salesList.get(position);
        
        String id = venta.getId_venta();
        holder.tvSaleId.setText("ID: " + (id != null ? (id.length() > 8 ? id.substring(0, 8) : id) : "---"));
        holder.tvSaleTotal.setText(String.format(Locale.getDefault(), "$%.2f", venta.getTotal()));
        
        if (venta.getFecha() != null) {
            holder.tvSaleDate.setText(sdf.format(venta.getFecha().toDate()));
        } else {
            holder.tvSaleDate.setText("---");
        }
        
        int itemsCount = venta.getLista_productos() != null ? venta.getLista_productos().size() : 0;
        holder.tvSaleItemsSummary.setText(itemsCount + " productos vendidos");
        
        // Mostrar el nombre del vendedor que realizó la venta
        String vendedor = venta.getNombre_vendedor() != null ? venta.getNombre_vendedor() : "Vendedor";
        holder.tvSaleSellerName.setText("Vendedor: " + vendedor);
    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    static class SaleViewHolder extends RecyclerView.ViewHolder {
        TextView tvSaleId, tvSaleTotal, tvSaleDate, tvSaleItemsSummary, tvSaleSellerName;

        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSaleId = itemView.findViewById(R.id.tvSaleId);
            tvSaleTotal = itemView.findViewById(R.id.tvSaleTotal);
            tvSaleDate = itemView.findViewById(R.id.tvSaleDate);
            tvSaleItemsSummary = itemView.findViewById(R.id.tvSaleItemsSummary);
            tvSaleSellerName = itemView.findViewById(R.id.tvSaleSellerName);
        }
    }
}
