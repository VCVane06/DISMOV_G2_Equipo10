package com.example.gestorpro;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.gestorpro.database.AppDatabase;
import com.example.gestorpro.database.Producto;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventarioActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabAgregar;
    private ProductosPagerAdapter pagerAdapter;
    private AppDatabase db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        // Inicializar base de datos
        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Inicializar vistas
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fabAgregar = findViewById(R.id.fabAgregar);

        // Configurar ViewPager con tabs
        pagerAdapter = new ProductosPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Todos");
                            break;
                        case 1:
                            tab.setText("Stock Bajo");
                            break;
                    }
                }
        ).attach();

        // Configurar FAB para agregar producto
        fabAgregar.setOnClickListener(v -> mostrarDialogoProducto(null));
    }

    private void mostrarDialogoProducto(Producto producto) {
        ProductoDialogFragment dialog = ProductoDialogFragment.newInstance(producto);
        dialog.setOnProductoGuardadoListener(productoGuardado -> {
            executorService.execute(() -> {
                if (productoGuardado.getId() == 0) {
                    // Nuevo producto
                    db.productoDao().insert(productoGuardado);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show();
                        pagerAdapter.actualizarDatos();
                    });
                } else {
                    // Actualizar producto
                    db.productoDao().update(productoGuardado);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                        pagerAdapter.actualizarDatos();
                    });
                }
            });
        });
        dialog.show(getSupportFragmentManager(), "producto_dialog");
    }

    public void editarProducto(Producto producto) {
        mostrarDialogoProducto(producto);
    }

    public void eliminarProducto(Producto producto) {
        executorService.execute(() -> {
            db.productoDao().delete(producto);
            runOnUiThread(() -> {
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                pagerAdapter.actualizarDatos();
            });
        });
    }

    public void actualizarTodosLosFragments() {
        if (pagerAdapter != null) {
            pagerAdapter.actualizarDatos();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}