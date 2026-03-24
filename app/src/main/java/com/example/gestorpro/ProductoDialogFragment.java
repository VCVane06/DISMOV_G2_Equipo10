package com.example.gestorpro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.gestorpro.database.AppDatabase;
import com.example.gestorpro.database.Categoria;
import com.example.gestorpro.database.Producto;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductoDialogFragment extends DialogFragment {

    private Producto producto;
    private int usuarioId;
    private TextInputEditText etCodigoBarras, etNombre, etCantidadActual, etCantidadMinima, etPrecio;
    private Spinner spinnerCategoria;
    private AppDatabase db;
    private ExecutorService executorService;
    private OnProductoGuardadoListener listener;
    private List<Categoria> categoriasList = new ArrayList<>();

    public interface OnProductoGuardadoListener {
        void onProductoGuardado(Producto producto);
    }

    public static ProductoDialogFragment newInstance(int usuarioId, Producto producto) {
        ProductoDialogFragment fragment = new ProductoDialogFragment();
        Bundle args = new Bundle();
        args.putInt("usuarioId", usuarioId);
        if (producto != null) {
            args.putSerializable("producto", producto);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnProductoGuardadoListener(OnProductoGuardadoListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usuarioId = getArguments().getInt("usuarioId");
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
        View view = inflater.inflate(R.layout.dialog_product, null);

        etCodigoBarras = view.findViewById(R.id.etCodigoBarras);
        etNombre = view.findViewById(R.id.etNombre);
        etCantidadActual = view.findViewById(R.id.etCantidadActual);
        etCantidadMinima = view.findViewById(R.id.etCantidadMinima);
        etPrecio = view.findViewById(R.id.etPrecio);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);

        if (producto != null) {
            etCodigoBarras.setText(producto.getCodigoBarras());
            etNombre.setText(producto.getNombre());
            etCantidadActual.setText(String.valueOf(producto.getCantidadActual()));
            etCantidadMinima.setText(String.valueOf(producto.getCantidadMinima()));
            etPrecio.setText(String.valueOf(producto.getPrecioUnitario()));
        }

        cargarCategorias();

        builder.setView(view)
                .setTitle(producto == null ? "Agregar Producto" : "Editar Producto")
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", (dialog, which) -> dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> guardarProducto());
        });

        return dialog;
    }

    private void cargarCategorias() {
        executorService.execute(() -> {
            categoriasList = db.categoriaDao().getAll();
            List<String> nombresCategorias = new ArrayList<>();
            nombresCategorias.add("Sin categoría");
            for (Categoria c : categoriasList) {
                nombresCategorias.add(c.getNombre());
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, nombresCategorias);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategoria.setAdapter(adapter);

                        if (producto != null && producto.getCategoriaId() != null) {
                            for (int i = 0; i < categoriasList.size(); i++) {
                                if (categoriasList.get(i).getId() == producto.getCategoriaId()) {
                                    spinnerCategoria.setSelection(i + 1);
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    private void guardarProducto() {
        String codigo = etCodigoBarras.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String cantActualStr = etCantidadActual.getText().toString().trim();
        String cantMinStr = etCantidadMinima.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (codigo.isEmpty()) {
            etCodigoBarras.setError("Obligatorio");
            return;
        }
        if (nombre.isEmpty()) {
            etNombre.setError("Obligatorio");
            return;
        }
        if (cantActualStr.isEmpty()) {
            etCantidadActual.setError("Obligatorio");
            return;
        }
        if (cantMinStr.isEmpty()) {
            etCantidadMinima.setError("Obligatorio");
            return;
        }
        if (precioStr.isEmpty()) {
            etPrecio.setError("Obligatorio");
            return;
        }

        int cantActual = Integer.parseInt(cantActualStr);
        int cantMin = Integer.parseInt(cantMinStr);
        double precio = Double.parseDouble(precioStr);
        Integer categoriaId = null;
        int selectedPos = spinnerCategoria.getSelectedItemPosition();
        if (selectedPos > 0) {
            categoriaId = categoriasList.get(selectedPos - 1).getId();
        }

        if (producto == null) {
            producto = new Producto(usuarioId, codigo, nombre, cantMin, cantActual, precio, categoriaId, System.currentTimeMillis());
        } else {
            producto.setUsuarioId(usuarioId);
            producto.setCodigoBarras(codigo);
            producto.setNombre(nombre);
            producto.setCantidadActual(cantActual);
            producto.setCantidadMinima(cantMin);
            producto.setPrecioUnitario(precio);
            producto.setCategoriaId(categoriaId);
            producto.setUltimaActualizacion(System.currentTimeMillis());
        }

        if (listener != null) {
            listener.onProductoGuardado(producto);
        }
        dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
