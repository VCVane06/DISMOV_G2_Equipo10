package com.example.gestorpro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etRole, etPhotoUrl;
    private MaterialButton btnSave, btnLogout, btnResetPassword;
    private ImageView btnBack, ivProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etSettingsName);
        etPhotoUrl = findViewById(R.id.etSettingsPhotoUrl);
        etEmail = findViewById(R.id.etSettingsEmail);
        etRole = findViewById(R.id.etSettingsRole);
        btnSave = findViewById(R.id.btnSaveSettings);
        btnLogout = findViewById(R.id.btnLogout);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);
        ivProfile = findViewById(R.id.ivProfilePicture);
    }

    private void loadUserData() {
        etEmail.setText(currentUser.getEmail());
        
        db.collection("usuarios").document(currentUser.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String nombre = document.getString("nombre");
                        String rol = document.getString("rol");
                        String foto = document.getString("foto_url");

                        etName.setText(nombre);
                        etRole.setText(rol);
                        etPhotoUrl.setText(foto);

                        if (foto != null && !foto.isEmpty()) {
                            Glide.with(this).load(foto).circleCrop().into(ivProfile);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveChanges());

        btnResetPassword.setOnClickListener(v -> sendResetEmail());

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void sendResetEmail() {
        String email = currentUser.getEmail();
        if (email != null) {
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Correo de recuperación enviado a: " + email, Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void saveChanges() {
        String newName = etName.getText().toString().trim();
        String newPhoto = etPhotoUrl.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            etName.setError("El nombre no puede estar vacío");
            return;
        }

        btnSave.setEnabled(false);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", newName);
        updates.put("foto_url", newPhoto);

        db.collection("usuarios").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    btnSave.setEnabled(true);
                    if (!newPhoto.isEmpty()) {
                        Glide.with(this).load(newPhoto).circleCrop().into(ivProfile);
                    }
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas salir?")
                .setPositiveButton("Salir", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
