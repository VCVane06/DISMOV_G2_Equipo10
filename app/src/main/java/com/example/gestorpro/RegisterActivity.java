package com.example.gestorpro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestorpro.database.AppDatabase;
import com.example.gestorpro.database.Usuario;
import com.example.gestorpro.database.UsuarioDao;

import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLoginLink;
    private UsuarioDao usuarioDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // Inicializar base de datos
        AppDatabase db = AppDatabase.getInstance(this);
        usuarioDao = db.usuarioDao();

        // Acción del botón continuar (registro)
        btnRegister.setOnClickListener(v -> {
            String nombre = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbTerms.isChecked()) {
                Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
                return;
            }

            // Realizar el registro en un hilo secundario
            Executors.newSingleThreadExecutor().execute(() -> {
                // Verificar si el correo ya existe
                Usuario existingUser = usuarioDao.getByEmail(email);
                
                if (existingUser != null) {
                    runOnUiThread(() -> Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show());
                } else {
                    // Crear y guardar el nuevo usuario
                    Usuario nuevoUsuario = new Usuario(email, nombre, password);
                    usuarioDao.insert(nuevoUsuario);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();
                        // Volver al login
                        finish();
                    });
                }
            });
        });

        // Volver al login
        tvLoginLink.setOnClickListener(v -> finish());
    }
}
