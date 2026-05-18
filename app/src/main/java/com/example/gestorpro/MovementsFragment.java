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

import com.example.gestorpro.database.Movimiento;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MovementsFragment extends Fragment {

    private RecyclerView recyclerView;
    private MovementAdapter adapter;
    private List<Movimiento> movementList = new ArrayList<>();
    private String userUid;
    private FirebaseFirestore db;

    public static MovementsFragment newInstance(String userUid) {
        MovementsFragment fragment = new MovementsFragment();
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
        adapter = new MovementAdapter(movementList);
        recyclerView.setAdapter(adapter);

        View searchView = view.findViewById(R.id.searchView);
        if (searchView != null) searchView.setVisibility(View.GONE);

        loadMovements();
        return view;
    }

    private void loadMovements() {
        // Sincronización: Se eliminó el filtro por id_usuario para que todos vean el historial global
        db.collection("movimientos")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        movementList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Movimiento mov = doc.toObject(Movimiento.class);
                            mov.setId_movimiento(doc.getId());
                            movementList.add(mov);
                        }
                        adapter.setMovementList(movementList);
                    }
                });
    }
}
