package com.example.gestorpro;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorpro.database.Usuario;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private List<Usuario> userList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        db = FirebaseFirestore.getInstance();

        rvUsers = findViewById(R.id.rvUsers);
        ImageView btnBack = findViewById(R.id.btnBack);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList, this);
        rvUsers.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadUsers();
    }

    private void loadUsers() {
        db.collection("usuarios")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Usuario usuario = doc.toObject(Usuario.class);
                            usuario.setUid(doc.getId());
                            userList.add(usuario);
                        }
                        adapter.setUserList(userList);
                    }
                });
    }

    @Override
    public void onEditClick(Usuario usuario) {
        showEditRoleDialog(usuario);
    }

    @Override
    public void onDeleteClick(Usuario usuario) {
        showDeleteConfirmationDialog(usuario);
    }

    private void showDeleteConfirmationDialog(Usuario usuario) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Usuario")
                .setMessage("¿Estás seguro de que deseas eliminar a " + usuario.getNombre() + "?\n\nEsta acción eliminará su perfil de la base de datos.")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarUsuario(usuario.getUid()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarUsuario(String uid) {
        db.collection("usuarios").document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Usuario eliminado de la base de datos", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showEditRoleDialog(Usuario usuario) {
        String[] roles = {"Administrador", "Almacen", "Ventas"};
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Seleccionar el rol actual
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(usuario.getRol())) {
                spinner.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Editar Rol de " + usuario.getNombre())
                .setView(spinner)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoRol = spinner.getSelectedItem().toString();
                    actualizarRol(usuario.getUid(), nuevoRol);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarRol(String uid, String nuevoRol) {
        db.collection("usuarios").document(uid)
                .update("rol", nuevoRol)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Rol actualizado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
    }
}
