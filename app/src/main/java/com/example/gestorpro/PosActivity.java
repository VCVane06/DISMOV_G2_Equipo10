package com.example.gestorpro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorpro.database.Producto;
import com.example.gestorpro.database.Venta;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PosActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private static final int SCAN_REQUEST_CODE = 456;
    private EditText etSearch;
    private View btnScan;
    private RecyclerView rvCart;
    private TextView tvTotal;
    private MaterialButton btnCompleteSale;
    private ImageView btnBack;

    private CartAdapter adapter;
    private List<Venta.VentaItemSnapshot> cartItems = new ArrayList<>();
    private FirebaseFirestore db;
    private String userUid, userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);

        userUid = getIntent().getStringExtra("USER_UID");
        userName = getIntent().getStringExtra("USER_NAME");
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearchPos);
        btnScan = findViewById(R.id.btnScanPos);
        rvCart = findViewById(R.id.rvCart);
        tvTotal = findViewById(R.id.tvTotalPos);
        btnCompleteSale = findViewById(R.id.btnCompleteSale);
        btnBack = findViewById(R.id.btnBack);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cartItems, this);
        rvCart.setAdapter(adapter);
    }

    private void setupListeners() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                buscarProducto(query);
                etSearch.setText("");
            }
            return true;
        });

        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra(ScanActivity.EXTRA_RETURN_RESULT, true);
            intent.putExtra("USER_UID", userUid);
            intent.putExtra("USER_NAME", userName);
            startActivityForResult(intent, SCAN_REQUEST_CODE);
        });

        btnCompleteSale.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Añade productos al carrito primero", Toast.LENGTH_SHORT).show();
            } else {
                confirmarVenta();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String code = data.getStringExtra(ScanActivity.EXTRA_SCANNED_CODE);
            if (code != null) buscarProducto(code);
        }
    }

    private void buscarProducto(String query) {
        db.collection("productos")
                .whereEqualTo("codigo_barras", query)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Producto p = queryDocumentSnapshots.getDocuments().get(0).toObject(Producto.class);
                        p.setId(queryDocumentSnapshots.getDocuments().get(0).getId());
                        agregarAlCarrito(p);
                    } else {
                        db.collection("productos")
                                .whereEqualTo("nombre", query)
                                .get()
                                .addOnSuccessListener(snapshots -> {
                                    if (!snapshots.isEmpty()) {
                                        Producto p = snapshots.getDocuments().get(0).toObject(Producto.class);
                                        p.setId(snapshots.getDocuments().get(0).getId());
                                        agregarAlCarrito(p);
                                    } else {
                                        preguntarRegistro(query);
                                    }
                                });
                    }
                });
    }

    private void preguntarRegistro(String code) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Producto no encontrado")
                .setMessage("El código '" + code + "' no está registrado. ¿Deseas darlo de alta?")
                .setPositiveButton("Registrar", (dialog, which) -> {
                    ProductoDialogFragment dialogFragment = ProductoDialogFragment.newInstance(userUid, userName, null, code);
                    dialogFragment.show(getSupportFragmentManager(), "registrar_desde_pos");
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void agregarAlCarrito(Producto producto) {
        if (producto.getStock_actual() <= 0) {
            Toast.makeText(this, "Sin stock: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
            return;
        }

        for (Venta.VentaItemSnapshot item : cartItems) {
            if (item.getProducto_id().equals(producto.getId())) {
                if (item.getCantidad() < producto.getStock_actual()) {
                    item.setCantidad(item.getCantidad() + 1);
                    item.setSubtotal(item.getCantidad() * item.getPrecio_unitario());
                    adapter.notifyDataSetChanged();
                    onQuantityChanged();
                }
                return;
            }
        }

        cartItems.add(new Venta.VentaItemSnapshot(producto, 1));
        adapter.notifyDataSetChanged();
        onQuantityChanged();
    }

    @Override
    public void onQuantityChanged() {
        double total = 0;
        for (Venta.VentaItemSnapshot item : cartItems) {
            total += item.getSubtotal();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
    }

    @Override
    public void onItemRemoved(int position) {
        onQuantityChanged();
    }

    private void confirmarVenta() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar Pago")
                .setMessage("Total: " + tvTotal.getText().toString() + "\n¿Finalizar venta?")
                .setPositiveButton("Confirmar", (dialog, which) -> finalizarVenta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void finalizarVenta() {
        double totalVenta = 0;
        for (Venta.VentaItemSnapshot item : cartItems) {
            totalVenta += item.getSubtotal();
        }

        WriteBatch batch = db.batch();
        DocumentReference ventaRef = db.collection("ventas").document();
        
        Venta nuevaVenta = new Venta(userUid, userName, totalVenta, new ArrayList<>(cartItems));
        batch.set(ventaRef, nuevaVenta);

        for (Venta.VentaItemSnapshot item : cartItems) {
            DocumentReference prodRef = db.collection("productos").document(item.getProducto_id());
            batch.update(prodRef, "stock_actual", FieldValue.increment(-item.getCantidad()));
            batch.update(prodRef, "ventasTotales", FieldValue.increment(item.getCantidad()));
            
            DocumentReference movRef = db.collection("movimientos").document();
            Map<String, Object> mov = new HashMap<>();
            mov.put("id_producto", item.getProducto_id());
            mov.put("nombreProducto", item.getNombre_producto());
            mov.put("tipo", "salida");
            mov.put("cantidad", item.getCantidad());
            mov.put("id_usuario", userUid);
            mov.put("nombre_usuario", userName);
            mov.put("timestamp", Timestamp.now()); // Sincronización instantánea para el Dashboard
            mov.put("precio", item.getPrecio_unitario());
            mov.put("descripcion", "Venta POS");
            batch.set(movRef, mov);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Venta realizada", Toast.LENGTH_SHORT).show();
            cartItems.clear();
            adapter.notifyDataSetChanged();
            onQuantityChanged();
        });
    }
}
