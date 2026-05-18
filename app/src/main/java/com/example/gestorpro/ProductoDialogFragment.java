package com.example.gestorpro;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gestorpro.database.Categoria;
import com.example.gestorpro.database.Producto;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductoDialogFragment extends DialogFragment {

    private static final int SCAN_REQUEST_CODE = 123;
    private Producto producto;
    private String usuarioId, userName, codigoInicial;
    private FirebaseFirestore db;

    // Layouts
    private LinearLayout layoutProductDetail, layoutProductForm, layoutStockUpdate;
    private View layoutAdminHeader;
    
    // Detail Views
    private TextView tvDetailCode, tvDetailName, tvDetailPrice, tvDetailStockPercent, tvDetailStockValues;
    private ProgressBar pbDetailStock;
    private BarChart detailChart;
    private ImageView btnDismiss;
    private Button btnOpenStockUpdate, btnOpenEditForm;
    
    // Form Views
    private TextInputLayout tilCodigoBarras;
    private TextInputEditText etCodigoBarras, etNombre, etPrecio, etCantidadActual, etCantidadMinima, etDescripcion;
    private Spinner spinnerCategoria;
    private TextView tvFormTitle;
    private Button btnSaveProduct;
    
    // Quick Stock Views
    private TextView tvCurrentStockLabel;
    private TextInputEditText etQuickStock;
    private Button btnConfirmStockUpdate, btnCancelStockUpdate;

    private List<Categoria> categoriasList = new ArrayList<>();

    public static ProductoDialogFragment newInstance(String usuarioId, String userName, Producto producto) {
        return newInstance(usuarioId, userName, producto, null);
    }

    public static ProductoDialogFragment newInstance(String usuarioId, String userName, Producto producto, String codigoInicial) {
        ProductoDialogFragment fragment = new ProductoDialogFragment();
        Bundle args = new Bundle();
        args.putString("usuarioId", usuarioId);
        args.putString("userName", userName);
        args.putString("codigoInicial", codigoInicial);
        if (producto != null) args.putSerializable("producto", producto);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        if (getArguments() != null) {
            usuarioId = getArguments().getString("usuarioId");
            userName = getArguments().getString("userName");
            producto = (Producto) getArguments().getSerializable("producto");
            codigoInicial = getArguments().getString("codigoInicial");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_product, container, false);
        initViews(view);
        cargarCategorias();
        setupLogic();
        return view;
    }

    private void initViews(View v) {
        layoutAdminHeader = v.findViewById(R.id.layoutAdminHeader);
        layoutProductDetail = v.findViewById(R.id.layoutProductDetail);
        layoutProductForm = v.findViewById(R.id.layoutProductForm);
        layoutStockUpdate = v.findViewById(R.id.layoutStockUpdate);

        // Header
        btnDismiss = v.findViewById(R.id.btnDismiss);

        // Detail
        tvDetailCode = v.findViewById(R.id.tvDetailCode);
        tvDetailName = v.findViewById(R.id.tvDetailName);
        tvDetailPrice = v.findViewById(R.id.tvDetailPrice);
        tvDetailStockPercent = v.findViewById(R.id.tvDetailStockPercent);
        tvDetailStockValues = v.findViewById(R.id.tvDetailStockValues);
        pbDetailStock = v.findViewById(R.id.pbDetailStock);
        detailChart = v.findViewById(R.id.detailChart);
        btnOpenStockUpdate = v.findViewById(R.id.btnOpenStockUpdate);
        btnOpenEditForm = v.findViewById(R.id.btnOpenEditForm);

        // Form
        tvFormTitle = v.findViewById(R.id.tvFormTitle);
        tilCodigoBarras = v.findViewById(R.id.tilCodigoBarras);
        etCodigoBarras = v.findViewById(R.id.etCodigoBarras);
        etNombre = v.findViewById(R.id.etNombre);
        etPrecio = v.findViewById(R.id.etPrecio);
        etCantidadActual = v.findViewById(R.id.etCantidadActual);
        etCantidadMinima = v.findViewById(R.id.etCantidadMinima);
        etDescripcion = v.findViewById(R.id.etDescripcion);
        spinnerCategoria = v.findViewById(R.id.spinnerCategoria);
        btnSaveProduct = v.findViewById(R.id.btnSaveProduct);

        // Quick Stock
        tvCurrentStockLabel = v.findViewById(R.id.tvCurrentStockLabel);
        etQuickStock = v.findViewById(R.id.etQuickStock);
        btnConfirmStockUpdate = v.findViewById(R.id.btnConfirmStockUpdate);
        btnCancelStockUpdate = v.findViewById(R.id.btnCancelStockUpdate);
    }

    private void setupLogic() {
        if (producto != null) {
            showDetails();
        } else {
            showForm("Nuevo Producto");
            if (codigoInicial != null) etCodigoBarras.setText(codigoInicial);
        }

        btnDismiss.setOnClickListener(v -> dismiss());
        btnOpenStockUpdate.setOnClickListener(v -> showStockUpdate());
        btnOpenEditForm.setOnClickListener(v -> showForm("Editar Producto"));
        btnSaveProduct.setOnClickListener(v -> guardarProducto());
        btnConfirmStockUpdate.setOnClickListener(v -> actualizarStockRapido());
        btnCancelStockUpdate.setOnClickListener(v -> showDetails());

        // Escáner de código de barras integrado en el formulario
        if (tilCodigoBarras != null) {
            tilCodigoBarras.setEndIconOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ScanActivity.class);
                intent.putExtra(ScanActivity.EXTRA_RETURN_RESULT, true);
                intent.putExtra("USER_UID", usuarioId);
                intent.putExtra("USER_NAME", userName);
                startActivityForResult(intent, SCAN_REQUEST_CODE);
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String code = data.getStringExtra(ScanActivity.EXTRA_SCANNED_CODE);
            if (code != null && etCodigoBarras != null) {
                etCodigoBarras.setText(code);
                Toast.makeText(getContext(), "Código escaneado: " + code, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDetails() {
        layoutAdminHeader.setVisibility(View.VISIBLE);
        layoutProductDetail.setVisibility(View.VISIBLE);
        layoutProductForm.setVisibility(View.GONE);
        layoutStockUpdate.setVisibility(View.GONE);

        tvDetailCode.setText(producto.getCodigo_barras());
        tvDetailName.setText(producto.getNombre());
        tvDetailPrice.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));
        
        long actual = producto.getStock_actual();
        long min = producto.getStock_minimo();
        
        int progress = (min > 0) ? (int)(actual * 100 / (min * 2)) : 100;
        if (progress > 100) progress = 100;
        
        pbDetailStock.setProgress(progress);
        tvDetailStockPercent.setText(((actual * 100) / (min > 0 ? min : 1)) + "%");
        tvDetailStockValues.setText(actual + "/" + min);
        
        setupMockChart();
    }

    private void showForm(String title) {
        layoutAdminHeader.setVisibility(View.VISIBLE);
        layoutProductDetail.setVisibility(View.GONE);
        layoutProductForm.setVisibility(View.VISIBLE);
        layoutStockUpdate.setVisibility(View.GONE);

        tvFormTitle.setText(title);
        if (producto != null) {
            etCodigoBarras.setText(producto.getCodigo_barras());
            etNombre.setText(producto.getNombre());
            etPrecio.setText(String.valueOf(producto.getPrecio()));
            etCantidadActual.setText(String.valueOf(producto.getStock_actual()));
            etCantidadMinima.setText(String.valueOf(producto.getStock_minimo()));
            etDescripcion.setText(producto.getDescripcion_corta());
        }
    }

    private void showStockUpdate() {
        layoutProductDetail.setVisibility(View.GONE);
        layoutStockUpdate.setVisibility(View.VISIBLE);
        tvCurrentStockLabel.setText("Stock actual: " + producto.getStock_actual());
        etQuickStock.setText(String.valueOf(producto.getStock_actual()));
    }

    private void setupMockChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 10f));
        entries.add(new BarEntry(1, 20f));
        entries.add(new BarEntry(2, 15f));
        entries.add(new BarEntry(3, 25f));
        entries.add(new BarEntry(4, 18f));

        BarDataSet set = new BarDataSet(entries, "Ventas");
        set.setColor(Color.parseColor("#2563EB"));
        set.setDrawValues(false);
        
        BarData data = new BarData(set);
        detailChart.setData(data);
        detailChart.getDescription().setEnabled(false);
        detailChart.getLegend().setEnabled(false);
        detailChart.getAxisRight().setEnabled(false);
        detailChart.getAxisLeft().setDrawGridLines(false);
        detailChart.getXAxis().setDrawGridLines(false);
        detailChart.getXAxis().setEnabled(false);
        detailChart.invalidate();
    }

    private void cargarCategorias() {
        db.collection("categorias").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                categoriasList.clear();
                List<String> nombresCategorias = new ArrayList<>();
                nombresCategorias.add("Sin categoría");
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Categoria cat = document.toObject(Categoria.class);
                    cat.setId_categoria(document.getId());
                    categoriasList.add(cat);
                    nombresCategorias.add(cat.getNombre_categoria());
                }
                if (isAdded() && getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombresCategorias);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategoria.setAdapter(adapter);
                    if (producto != null && producto.getId_categoria() != null) {
                        for (int i = 0; i < categoriasList.size(); i++) {
                            if (categoriasList.get(i).getId_categoria().equals(producto.getId_categoria())) {
                                spinnerCategoria.setSelection(i + 1);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void actualizarStockRapido() {
        String val = etQuickStock.getText().toString();
        if (val.isEmpty()) return;
        long nuevoStock = Long.parseLong(val);
        
        WriteBatch batch = db.batch();
        DocumentReference ref = db.collection("productos").document(producto.getId());
        batch.update(ref, "stock_actual", nuevoStock);
        
        long diff = nuevoStock - producto.getStock_actual();
        if (diff != 0) {
            registrarMovimiento(batch, producto.getId(), producto.getNombre(), diff > 0 ? "entrada" : "ajuste", Math.abs(diff), "Actualización rápida");
        }
        
        batch.commit().addOnSuccessListener(aVoid -> {
            producto.setStock_actual(nuevoStock);
            Toast.makeText(getContext(), "Stock actualizado", Toast.LENGTH_SHORT).show();
            showDetails();
        });
    }

    private void guardarProducto() {
        String codigo = etCodigoBarras.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String stockStr = etCantidadActual.getText().toString().trim();
        String minStr = etCantidadMinima.getText().toString().trim();

        if (codigo.isEmpty() || nombre.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(getContext(), "Faltan datos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio = Double.parseDouble(precioStr);
        long stock = Long.parseLong(stockStr);
        long min = minStr.isEmpty() ? 0 : Long.parseLong(minStr);

        WriteBatch batch = db.batch();
        DocumentReference ref;
        Map<String, Object> map = new HashMap<>();
        map.put("codigo_barras", codigo);
        map.put("nombre", nombre);
        map.put("precio", precio);
        map.put("stock_actual", stock);
        map.put("stock_minimo", min);
        map.put("descripcion_corta", etDescripcion.getText().toString().trim());

        int catPos = spinnerCategoria.getSelectedItemPosition();
        map.put("id_categoria", catPos > 0 ? categoriasList.get(catPos-1).getId_categoria() : null);

        if (producto == null) {
            ref = db.collection("productos").document();
            map.put("usuario_creador_id", usuarioId);
            map.put("timestamp_creacion", Timestamp.now());
            map.put("ventasTotales", 0);
            batch.set(ref, map);
            registrarMovimiento(batch, ref.getId(), nombre, "entrada", stock, "Carga inicial");
        } else {
            ref = db.collection("productos").document(producto.getId());
            batch.update(ref, map);
            long diff = stock - producto.getStock_actual();
            if (diff != 0) registrarMovimiento(batch, producto.getId(), nombre, diff > 0 ? "entrada" : "ajuste", Math.abs(diff), "Edición de producto");
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Producto guardado con éxito", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void registrarMovimiento(WriteBatch batch, String id, String name, String tipo, long cant, String desc) {
        DocumentReference mov = db.collection("movimientos").document();
        Map<String, Object> m = new HashMap<>();
        m.put("id_producto", id);
        m.put("nombreProducto", name);
        m.put("tipo", tipo);
        m.put("cantidad", cant);
        m.put("id_usuario", usuarioId);
        m.put("nombre_usuario", userName);
        m.put("timestamp", Timestamp.now());
        m.put("descripcion", desc);
        batch.set(mov, m);
    }
}
