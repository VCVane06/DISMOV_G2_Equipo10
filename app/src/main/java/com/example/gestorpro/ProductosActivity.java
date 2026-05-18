package com.example.gestorpro;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorpro.database.Categoria;
import com.example.gestorpro.database.Producto;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProductosActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private String userUid, userName;
    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private FirebaseFirestore db;
    private CollectionReference productosRef, categoriasRef;
    private EditText etSearch;
    private Spinner spinnerFilter;
    private TextView tvNumProducts, tvTotalItems, tvTotalValue;
    private ExtendedFloatingActionButton btnRegisterProduct;
    private ImageView btnBack, btnManageCategories;
    private View btnScan;
    private ChipGroup chipGroupCategories;
    
    private List<Producto> allProductsList = new ArrayList<>();
    private List<Categoria> categoriasList = new ArrayList<>();
    private String selectedCategoryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        userUid = getIntent().getStringExtra("USER_UID");
        userName = getIntent().getStringExtra("USER_NAME");
        
        if (userUid == null) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        productosRef = db.collection("productos");
        categoriasRef = db.collection("categorias");

        initViews();
        setupFilters();
        setupRealtimeListeners();
    }

    private void initViews() {
        rvProducts = findViewById(R.id.rvProducts);
        etSearch = findViewById(R.id.etSearchProduct);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        tvNumProducts = findViewById(R.id.tvNumProducts);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        btnRegisterProduct = findViewById(R.id.btnRegisterProduct);
        btnBack = findViewById(R.id.btnBack);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        btnScan = findViewById(R.id.btnScan);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(new ArrayList<>(), this);
        rvProducts.setAdapter(adapter);

        btnRegisterProduct.setOnClickListener(v -> showAddProductDialog());
        btnBack.setOnClickListener(v -> finish());
        btnManageCategories.setOnClickListener(v -> {
            CategoriasDialogFragment dialog = new CategoriasDialogFragment();
            dialog.show(getSupportFragmentManager(), "gestionar_categorias");
        });

        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra("USER_UID", userUid);
            intent.putExtra("USER_NAME", userName);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltrosYBusqueda();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        String[] filters = {"Todos", "Menor stock", "Mayor precio", "Menor precio", "Nombre A-Z"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltrosYBusqueda();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRealtimeListeners() {
        // Listener de Productos (Sincronizado para todos los perfiles)
        productosRef.addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        allProductsList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Producto producto = doc.toObject(Producto.class);
                            producto.setId(doc.getId());
                            allProductsList.add(producto);
                        }
                        aplicarFiltrosYBusqueda();
                    }
                });

        // Listener de Categorías para los Chips
        categoriasRef.addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                categoriasList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Categoria cat = doc.toObject(Categoria.class);
                    cat.setId_categoria(doc.getId());
                    categoriasList.add(cat);
                }
                updateCategoryChips();
            }
        });
    }

    private void updateCategoryChips() {
        chipGroupCategories.removeAllViews();
        
        // Chip para "Todos"
        Chip allChip = new Chip(this);
        allChip.setText("Todas");
        allChip.setCheckable(true);
        allChip.setChecked(selectedCategoryId == null);
        allChip.setOnClickListener(v -> {
            selectedCategoryId = null;
            aplicarFiltrosYBusqueda();
        });
        chipGroupCategories.addView(allChip);

        for (Categoria cat : categoriasList) {
            Chip chip = new Chip(this);
            chip.setText(cat.getNombre_categoria());
            chip.setCheckable(true);
            chip.setChecked(cat.getId_categoria().equals(selectedCategoryId));
            chip.setOnClickListener(v -> {
                selectedCategoryId = cat.getId_categoria();
                aplicarFiltrosYBusqueda();
            });
            chipGroupCategories.addView(chip);
        }
    }

    private void aplicarFiltrosYBusqueda() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        String filterType = spinnerFilter.getSelectedItem().toString();
        List<Producto> filteredList = new ArrayList<>();
        
        for (Producto p : allProductsList) {
            boolean matchesSearch = p.getNombre().toLowerCase().contains(query) || 
                                   (p.getCodigo_barras() != null && p.getCodigo_barras().contains(query));
            
            boolean matchesCategory = (selectedCategoryId == null) || 
                                     (p.getId_categoria() != null && p.getId_categoria().equals(selectedCategoryId));

            if (matchesSearch && matchesCategory) {
                filteredList.add(p);
            }
        }

        switch (filterType) {
            case "Menor stock":
                Collections.sort(filteredList, (p1, p2) -> Long.compare(p1.getStock_actual(), p2.getStock_actual()));
                break;
            case "Mayor precio":
                Collections.sort(filteredList, (p1, p2) -> Double.compare(p2.getPrecio(), p1.getPrecio()));
                break;
            case "Menor precio":
                Collections.sort(filteredList, (p1, p2) -> Double.compare(p1.getPrecio(), p2.getPrecio()));
                break;
            case "Nombre A-Z":
                Collections.sort(filteredList, (p1, p2) -> p1.getNombre().compareToIgnoreCase(p2.getNombre()));
                break;
        }
        actualizarInterfaz(filteredList);
    }

    private void actualizarInterfaz(List<Producto> products) {
        adapter.setProductList(products);
        tvNumProducts.setText("Productos: " + products.size());
        long totalQty = 0; double totalVal = 0;
        for (Producto p : products) {
            totalQty += p.getStock_actual();
            totalVal += (p.getStock_actual() * p.getPrecio());
        }
        tvTotalItems.setText("Unidades totales: " + totalQty);
        tvTotalValue.setText(String.format(Locale.getDefault(), "Valor total: $%.2f", totalVal));
    }

    private void showAddProductDialog() {
        ProductoDialogFragment dialog = ProductoDialogFragment.newInstance(userUid, userName, null);
        dialog.show(getSupportFragmentManager(), "registrar_producto");
    }

    @Override
    public void onProductClick(Producto producto) {
        ProductoDialogFragment dialog = ProductoDialogFragment.newInstance(userUid, userName, producto);
        dialog.show(getSupportFragmentManager(), "editar_producto");
    }
}
