package com.example.gestorpro;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText etName, etEmail, etPassword;
    private Spinner spinnerRole;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLoginLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Configurar Spinner de Roles
        String[] roles = {"Administrador", "Almacen", "Ventas"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        // Acción del botón continuar (registro)
        btnRegister.setOnClickListener(v -> registrarUsuario());

        // Volver al login
        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {
        String nombre = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rol = spinnerRole.getSelectedItem().toString();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            guardarDatosUsuario(user.getUid(), nombre, email, rol);
                        }
                    } else {
                        Log.e(TAG, "Error en autenticación", task.getException());
                        Toast.makeText(RegisterActivity.this, "Error en autenticación: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void guardarDatosUsuario(String uid, String nombre, String email, String rol) {
        // 2. Guardar detalles adicionales en Firestore
        // Usamos los nombres de campos definidos en tu estructura de colecciones
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("uid", uid);
        usuario.put("nombre", nombre);
        usuario.put("correo", email); // Usamos "correo" para coincidir con tu estructura
        usuario.put("rol", rol);
        usuario.put("fecha_creacion", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("usuarios").document(uid)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                    finish(); // Regresar al Login
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar perfil en Firestore", e);
                    Toast.makeText(RegisterActivity.this, "Error al guardar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Opcional: podrías eliminar el usuario de Auth si falla Firestore para mantener consistencia
                });
    }
}
