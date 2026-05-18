package com.example.gestorpro;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gestorpro.database.Movimiento;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MovementAdapter extends RecyclerView.Adapter<MovementAdapter.MovementViewHolder> {

    private List<Movimiento> movementList;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public MovementAdapter(List<Movimiento> movementList) {
        this.movementList = movementList;
    }

    public void setMovementList(List<Movimiento> movementList) {
        this.movementList = movementList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movement, parent, false);
        return new MovementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovementViewHolder holder, int position) {
        Movimiento mov = movementList.get(position);
        holder.tvProductName.setText(mov.getNombreProducto());
        
        String tipo = mov.getTipo() != null ? mov.getTipo().toUpperCase() : "";
        holder.tvType.setText(tipo);
        
        // Colores modernos y fondo según tipo
        if ("ENTRADA".equals(tipo)) {
            holder.tvType.setTextColor(Color.parseColor("#059669")); // Verde oscuro
            holder.tvType.setBackgroundColor(Color.parseColor("#ECFDF5"));
        } else if ("SALIDA".equals(tipo)) {
            holder.tvType.setTextColor(Color.parseColor("#DC2626")); // Rojo oscuro
            holder.tvType.setBackgroundColor(Color.parseColor("#FEF2F2"));
        } else {
            holder.tvType.setTextColor(Color.parseColor("#D97706")); // Naranja oscuro
            holder.tvType.setBackgroundColor(Color.parseColor("#FFFBEB"));
        }

        holder.tvDetails.setText("Cantidad: " + mov.getCantidad() + " unidades");
        
        // Sincronización: Mostrar nombre del usuario que realizó la acción
        String responsable = mov.getNombre_usuario() != null ? mov.getNombre_usuario() : "Usuario";
        holder.tvUserName.setText("Realizado por: " + responsable);
        
        if (mov.getTimestamp() != null) {
            holder.tvDate.setText(sdf.format(mov.getTimestamp().toDate()));
        } else {
            holder.tvDate.setText("---");
        }
    }

    @Override
    public int getItemCount() {
        return movementList.size();
    }

    static class MovementViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvType, tvDetails, tvDate, tvUserName;

        public MovementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvMovementProductName);
            tvType = itemView.findViewById(R.id.tvMovementType);
            tvDetails = itemView.findViewById(R.id.tvMovementDetails);
            tvDate = itemView.findViewById(R.id.tvMovementDate);
            tvUserName = itemView.findViewById(R.id.tvMovementUserName);
        }
    }
}
