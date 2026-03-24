package com.example.gestorpro;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorpro.database.AppDatabase;
import com.example.gestorpro.database.Producto;
import com.example.gestorpro.database.ProductoDao;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ProductosActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private int usuarioId;
    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private ProductoDao productoDao;
    private EditText etSearch;
    private TextView tvNumProducts, tvTotalItems, tvTotalValue, btnRegisterProduct;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        usuarioId = getIntent().getIntExtra("USER_ID", -1);
        if (usuarioId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar vistas
        rvProducts = findViewById(R.id.rvProducts);
        etSearch = findViewById(R.id.etSearchProduct);
        tvNumProducts = findViewById(R.id.tvNumProducts);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        btnRegisterProduct = findViewById(R.id.btnRegisterProduct);
        btnBack = findViewById(R.id.btnBack);

        // Configurar RecyclerView
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(new ArrayList<>(), this);
        rvProducts.setAdapter(adapter);

        // DB
        AppDatabase db = AppDatabase.getInstance(this);
        productoDao = db.productoDao();

        // Cargar datos
        loadProducts("");

        // Búsqueda
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadProducts(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Registrar producto (Abrir diálogo por ahora)
        btnRegisterProduct.setOnClickListener(v -> showAddProductDialog());

        // Volver
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadProducts(String query) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Producto> products;
            if (query.isEmpty()) {
                products = productoDao.getAllByUser(usuarioId);
            } else {
                products = productoDao.search(query, usuarioId);
            }

            // Calcular estadísticas
            int numProd = products.size();
            int totalQty = 0;
            double totalVal = 0;
            for (Producto p : products) {
                totalQty += p.getCantidadActual();
                totalVal += (p.getCantidadActual() * p.getPrecioUnitario());
            }

            final int finalTotalQty = totalQty;
            final double finalTotalVal = totalVal;

            runOnUiThread(() -> {
                adapter.setProductList(products);
                tvNumProducts.setText("Numero de productos: " + numProd);
                tvTotalItems.setText("Cantidad total de productos: " + finalTotalQty);
                tvTotalValue.setText(String.format(Locale.getDefault(), "Valor total de todos los productos: $%.2f", finalTotalVal));
            });
        });
    }

    private void showAddProductDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_product, null);
        TextInputEditText etCodigo = view.findViewById(R.id.etCodigoBarras);
        TextInputEditText etNombre = view.findViewById(R.id.etNombre);
        TextInputEditText etActual = view.findViewById(R.id.etCantidadActual);
        TextInputEditText etMinima = view.findViewById(R.id.etCantidadMinima);
        TextInputEditText etPrecio = view.findViewById(R.id.etPrecio);

        new AlertDialog.Builder(this)
                .setTitle("Registrar Producto")
                .setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String cod = etCodigo.getText().toString();
                    String nom = etNombre.getText().toString();
                    int act = Integer.parseInt(etActual.getText().toString());
                    int min = Integer.parseInt(etMinima.getText().toString());
                    double pre = Double.parseDouble(etPrecio.getText().toString());

                    Executors.newSingleThreadExecutor().execute(() -> {
                        Producto p = new Producto(usuarioId, cod, nom, min, act, pre, null, System.currentTimeMillis());
                        productoDao.insert(p);
                        runOnUiThread(() -> {
                            loadProducts(etSearch.getText().toString());
                            Toast.makeText(this, "Producto guardado", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onProductClick(Producto producto) {
        Toast.makeText(this, "Click en: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
    }
}
