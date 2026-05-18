package com.example.gestorpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gestorpro.database.Venta;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SalesHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private SalesHistoryAdapter adapter;
    private List<Venta> salesList = new ArrayList<>();
    private String userUid;
    private FirebaseFirestore db;

    public static SalesHistoryFragment newInstance(String userUid) {
        SalesHistoryFragment fragment = new SalesHistoryFragment();
        Bundle args = new Bundle();
        args.putString("USER_UID", userUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userUid = getArguments().getString("USER_UID");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewProductos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SalesHistoryAdapter(salesList);
        recyclerView.setAdapter(adapter);

        View searchView = view.findViewById(R.id.searchView);
        if (searchView != null) searchView.setVisibility(View.GONE);

        loadSales();
        return view;
    }

    private void loadSales() {
        // Sincronización: Se eliminó el filtro por id_vendedor para que todos vean el historial global de ventas
        db.collection("ventas")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        salesList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Venta venta = doc.toObject(Venta.class);
                            venta.setId_venta(doc.getId());
                            salesList.add(venta);
                        }
                        adapter.setSalesList(salesList);
                    }
                });
    }
}
