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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.example.gestorpro.database.AppDatabase;
import com.example.gestorpro.database.Movimiento;
import com.example.gestorpro.database.Producto;
import com.example.gestorpro.database.Venta;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VentaDialogFragment extends DialogFragment {

    private Producto producto;
    private TextInputEditText etCantidad;
    private TextView tvTotal, tvPrecioUnitario;
    private Button btnVender;
    private AppDatabase db;
    private ExecutorService executorService;
    private OnVentaRealizadaListener listener;

    public interface OnVentaRealizadaListener {
        void onVentaRealizada();
    }

    public static VentaDialogFragment newInstance(Producto producto) {
        VentaDialogFragment fragment = new VentaDialogFragment();
        Bundle args = new Bundle();
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
            producto = (Producto) getArguments().getSerializable("producto");
        }
        db = AppDatabase.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
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
        tvPrecioUnitario.setText(String.format(Locale.getDefault(), "Precio unitario: $%.2f", producto.getPrecioUnitario()));

        etCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
                int cantidad = Integer.parseInt(cantidadStr);
                double total = cantidad * producto.getPrecioUnitario();
                tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
            } catch (NumberFormatException e) {
                tvTotal.setText("Total: $0.00");
            }
        } else {
            tvTotal.setText("Total: $0.00");
        }
    }

    private void realizarVenta() {
        String cantidadStr = etCantidad.getText().toString();
        if (cantidadStr.isEmpty()) {
            etCantidad.setError("Ingrese una cantidad");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
        } catch (NumberFormatException e) {
            etCantidad.setError("Cantidad no válida");
            return;
        }

        if (cantidad <= 0) {
            etCantidad.setError("Cantidad debe ser mayor a 0");
            return;
        }

        if (cantidad > producto.getCantidadActual()) {
            etCantidad.setError("Stock insuficiente. Disponible: " + producto.getCantidadActual());
            return;
        }

        executorService.execute(() -> {
            try {
                // Registrar venta
                double total = cantidad * producto.getPrecioUnitario();
                Venta venta = new Venta(System.currentTimeMillis(), total);
                long ventaId = db.ventaDao().insert(venta);

                // Registrar movimiento
                Movimiento movimiento = new Movimiento(
                        producto.getId(),
                        "SALIDA",
                        cantidad,
                        producto.getPrecioUnitario(),
                        System.currentTimeMillis(),
                        (int) ventaId,
                        "Venta realizada"
                );
                db.movimientoDao().insert(movimiento);

                // Actualizar stock del producto
                producto.setCantidadActual(producto.getCantidadActual() - cantidad);
                producto.setUltimaActualizacion(System.currentTimeMillis());
                db.productoDao().update(producto);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (listener != null) {
                            listener.onVentaRealizada();
                        }
                        dismiss();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
