package com.example.gestorpro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gestorpro.database.Movimiento;
import com.example.gestorpro.database.Producto;
import com.example.gestorpro.database.Venta;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VentaDialogFragment extends DialogFragment {

    private Producto producto;
    private String usuarioId, userName;
    private TextInputEditText etCantidad;
    private TextView tvTotal, tvPrecioUnitario;
    private Button btnVender;
    private FirebaseFirestore db;
    private OnVentaRealizadaListener listener;

    public interface OnVentaRealizadaListener {
        void onVentaRealizada();
    }

    public static VentaDialogFragment newInstance(String usuarioId, String userName, Producto producto) {
        VentaDialogFragment fragment = new VentaDialogFragment();
        Bundle args = new Bundle();
        args.putString("usuarioId", usuarioId);
        args.putString("userName", userName);
        args.putSerializable("producto", producto);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnVentaRealizadaListener(OnVentaRealizadaListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usuarioId = getArguments().getString("usuarioId");
            userName = getArguments().getString("userName");
            producto = (Producto) getArguments().getSerializable("producto");
        }
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_venta, null);

        etCantidad = view.findViewById(R.id.etCantidadVenta);
        tvTotal = view.findViewById(R.id.tvTotalVenta);
        tvPrecioUnitario = view.findViewById(R.id.tvPrecioUnitario);
        btnVender = view.findViewById(R.id.btnProcesarVenta);

        TextView tvProductoNombre = view.findViewById(R.id.tvProductoNombre);
        tvProductoNombre.setText(producto.getNombre());
        tvPrecioUnitario.setText(String.format(Locale.getDefault(), "Precio unitario: $%.2f", producto.getPrecio()));

        etCantidad.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calcularTotal(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnVender.setOnClickListener(v -> realizarVenta());

        builder.setView(view)
                .setTitle("Realizar Venta")
                .setNegativeButton("Cancelar", (dialog, which) -> dismiss());

        return builder.create();
    }

    private void calcularTotal() {
        String cantidadStr = etCantidad.getText().toString();
        if (!cantidadStr.isEmpty()) {
            try {
                long cantidad = Long.parseLong(cantidadStr);
                double total = cantidad * producto.getPrecio();
                tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
            } catch (NumberFormatException e) {
                tvTotal.setText("Total: $0.00");
            }
        } else {
            tvTotal.setText("Total: $0.00");
        }
    }

    private void realizarVenta() {
        String cantidadStr = etCantidad.getText().toString().trim();
        if (cantidadStr.isEmpty()) {
            etCantidad.setError("Ingrese una cantidad");
            return;
        }

        long cantidad;
        try {
            cantidad = Long.parseLong(cantidadStr);
        } catch (NumberFormatException e) {
            etCantidad.setError("Cantidad no válida");
            return;
        }

        if (cantidad <= 0) {
            etCantidad.setError("Cantidad debe ser mayor a 0");
            return;
        }

        if (cantidad > producto.getStock_actual()) {
            etCantidad.setError("Stock insuficiente. Disponible: " + producto.getStock_actual());
            return;
        }

        WriteBatch batch = db.batch();

        // 1. Crear Venta
        DocumentReference ventaRef = db.collection("ventas").document();
        double totalVenta = cantidad * producto.getPrecio();

        Venta.VentaItemSnapshot item = new Venta.VentaItemSnapshot();
        item.setProducto_id(producto.getId());
        item.setCodigo_barras(producto.getCodigo_barras());
        item.setNombre_producto(producto.getNombre());
        item.setCantidad(cantidad);
        item.setPrecio_unitario(producto.getPrecio());
        item.setSubtotal(totalVenta);
        item.setId_categoria(producto.getId_categoria());
        item.setSub_categoria(producto.getSub_categoria());
        item.setPresentacion(producto.getPresentacion());
        item.setMarca(producto.getMarca());
        item.setSabor(producto.getSabor());

        List<Venta.VentaItemSnapshot> lista = new ArrayList<>();
        lista.add(item);

        Venta venta = new Venta(usuarioId, userName, totalVenta, lista);
        venta.setId_venta(ventaRef.getId());
        batch.set(ventaRef, venta);

        // 2. Registrar Movimiento
        DocumentReference movRef = db.collection("movimientos").document();
        Movimiento mov = new Movimiento(movRef.getId(), producto.getId(), producto.getNombre(), "salida", cantidad, producto.getPrecio(), usuarioId, userName, "Venta realizada");
        batch.set(movRef, mov);

        // 3. Actualizar Producto
        DocumentReference prodRef = db.collection("productos").document(producto.getId());
        batch.update(prodRef, "stock_actual", FieldValue.increment(-cantidad));
        batch.update(prodRef, "ventasTotales", FieldValue.increment(1));

        batch.commit().addOnSuccessListener(aVoid -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Venta exitosa", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onVentaRealizada();
                dismiss();
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
