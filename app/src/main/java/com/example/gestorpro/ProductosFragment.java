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
    private int usuarioId; // Añadido para filtrar por usuario
    private AppDatabase db;
    private ExecutorService executorService;

    public static ProductosFragment newInstance(int usuarioId, boolean mostrarSoloStockBajo) {
        ProductosFragment fragment = new ProductosFragment();
        Bundle args = new Bundle();
        args.putInt("usuarioId", usuarioId);
        args.putBoolean("mostrarSoloStockBajo", mostrarSoloStockBajo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usuarioId = getArguments().getInt("usuarioId", -1);
            mostrarSoloStockBajo = getArguments().getBoolean("mostrarSoloStockBajo", false);
        }
        db = AppDatabase.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewProductos);
        searchView = view.findViewById(R.id.searchView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Actualizado para usar OnProductoClickListener
        adapter = new ProductosAdapter(productosList, producto -> {
            if (getActivity() instanceof InventarioActivity) {
                ((InventarioActivity) getActivity()).editarProducto(producto);
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
            // Corregido: Ahora pasamos usuarioId a las consultas
            if (mostrarSoloStockBajo) {
                productos = db.productoDao().getProductosStockBajo(usuarioId);
            } else {
                productos = db.productoDao().getAllByUser(usuarioId);
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
