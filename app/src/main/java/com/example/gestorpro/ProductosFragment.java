package com.example.gestorpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gestorpro.database.AppDatabase;
import com.example.gestorpro.database.Producto;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductosFragment extends Fragment {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private ProductosAdapter adapter;
    private List<Producto> productosList = new ArrayList<>();
    private boolean mostrarSoloStockBajo;
    private AppDatabase db;
    private ExecutorService executorService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mostrarSoloStockBajo = getArguments().getBoolean("mostrarSoloStockBajo", false);
        }
        db = AppDatabase.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Corrected layout name from fragment_productos to fragment_products
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewProductos);
        searchView = view.findViewById(R.id.searchView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductosAdapter(productosList, new ProductosAdapter.OnProductoActionListener() {
            @Override
            public void onVender(Producto producto) {
                mostrarDialogoVenta(producto);
            }

            @Override
            public void onEditar(Producto producto) {
                if (getActivity() instanceof InventarioActivity) {
                    ((InventarioActivity) getActivity()).editarProducto(producto);
                }
            }

            @Override
            public void onEliminar(Producto producto) {
                if (getActivity() instanceof InventarioActivity) {
                    ((InventarioActivity) getActivity()).eliminarProducto(producto);
                }
            }
        });

        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filtrar(newText);
                return true;
            }
        });

        cargarProductos();

        return view;
    }

    private void cargarProductos() {
        executorService.execute(() -> {
            List<Producto> productos;
            if (mostrarSoloStockBajo) {
                productos = db.productoDao().getProductosStockBajo();
            } else {
                productos = db.productoDao().getAll();
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    productosList.clear();
                    productosList.addAll(productos);
                    adapter.actualizarLista(productosList);
                });
            }
        });
    }

    private void mostrarDialogoVenta(Producto producto) {
        VentaDialogFragment dialog = VentaDialogFragment.newInstance(producto);
        dialog.setOnVentaRealizadaListener(() -> {
            cargarProductos();
            if (getActivity() instanceof InventarioActivity) {
                ((InventarioActivity) getActivity()).actualizarTodosLosFragments();
            }
        });
        dialog.show(getParentFragmentManager(), "venta_dialog");
    }

    public void actualizarLista() {
        cargarProductos();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}