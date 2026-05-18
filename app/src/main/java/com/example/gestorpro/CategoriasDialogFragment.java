package com.example.gestorpro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorpro.database.Categoria;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriasDialogFragment extends DialogFragment implements CategoriasAdapter.OnCategoriaClickListener {

    private static final String TAG = "CategoriasDialog";
    private TextInputEditText etNuevaCategoria;
    private Button btnAddCategoria, btnClose;
    private RecyclerView rvCategorias;
    private CategoriasAdapter adapter;
    private FirebaseFirestore db;
    private CollectionReference categoriasRef;
    private List<Categoria> categoriasList = new ArrayList<>();
    private ListenerRegistration registration;
    
    // Variable para saber si estamos editando
    private Categoria categoriaEnEdicion = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_categorias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        
        etNuevaCategoria = v.findViewById(R.id.etNuevaCategoria);
        btnAddCategoria = v.findViewById(R.id.btnAddCategoria);
        btnClose = v.findViewById(R.id.btnClose);
        rvCategorias = v.findViewById(R.id.rvCategorias);

        db = FirebaseFirestore.getInstance();
        categoriasRef = db.collection("categorias");

        rvCategorias.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoriasAdapter(new ArrayList<>(), this);
        rvCategorias.setAdapter(adapter);

        // Configuramos un único listener para el botón
        btnAddCategoria.setOnClickListener(view -> procesarBotonPrincipal());
        
        btnClose.setOnClickListener(view -> dismiss());

        escucharCategorias();
        resetearEstado();
    }

    private void escucharCategorias() {
        if (registration != null) registration.remove();
        
        registration = categoriasRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error en Firestore", error);
                return;
            }
            if (value != null) {
                categoriasList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Categoria cat = doc.toObject(Categoria.class);
                    cat.setId_categoria(doc.getId());
                    categoriasList.add(cat);
                }
                adapter.setCategorias(new ArrayList<>(categoriasList));
            }
        });
    }

    private void procesarBotonPrincipal() {
        String nombre = etNuevaCategoria.getText().toString().trim();
        
        if (nombre.isEmpty()) {
            etNuevaCategoria.setError("Escribe un nombre");
            Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoriaEnEdicion == null) {
            agregarCategoria(nombre);
        } else {
            actualizarCategoria(nombre);
        }
    }

    private void agregarCategoria(String nombre) {
        btnAddCategoria.setEnabled(false);
        
        DocumentReference newDoc = categoriasRef.document();
        String id = newDoc.getId();

        Map<String, Object> data = new HashMap<>();
        data.put("id_categoria", id);
        data.put("nombre_categoria", nombre);
        data.put("descripcion", "");
        data.put("url_icono", "");

        newDoc.set(data).addOnSuccessListener(aVoid -> {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Añadida: " + nombre, Toast.LENGTH_SHORT).show();
                resetearEstado();
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                btnAddCategoria.setEnabled(true);
                Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarCategoria(String nombre) {
        btnAddCategoria.setEnabled(false);
        
        categoriasRef.document(categoriaEnEdicion.getId_categoria())
                .update("nombre_categoria", nombre)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Actualizada", Toast.LENGTH_SHORT).show();
                        resetearEstado();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        btnAddCategoria.setEnabled(true);
                        Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetearEstado() {
        categoriaEnEdicion = null;
        etNuevaCategoria.setText("");
        etNuevaCategoria.setError(null);
        etNuevaCategoria.clearFocus();
        btnAddCategoria.setText("Añadir");
        btnAddCategoria.setEnabled(true);
    }

    @Override
    public void onDeleteClick(Categoria categoria) {
        categoriasRef.document(categoria.getId_categoria()).delete()
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) Toast.makeText(requireContext(), "Eliminada", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditClick(Categoria categoria) {
        categoriaEnEdicion = categoria;
        etNuevaCategoria.setText(categoria.getNombre_categoria());
        etNuevaCategoria.requestFocus();
        btnAddCategoria.setText("Actualizar");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (registration != null) registration.remove();
    }
}
