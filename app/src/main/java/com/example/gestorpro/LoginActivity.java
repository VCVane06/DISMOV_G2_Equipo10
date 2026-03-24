package com.example.gestorpro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestorpro.database.AppDatabase;
import com.example.gestorpro.database.Usuario;
import com.example.gestorpro.database.UsuarioDao;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvCreateAccount;
    private UsuarioDao usuarioDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);

        AppDatabase db = AppDatabase.getInstance(this);
        usuarioDao = db.usuarioDao();

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                Usuario usuario = usuarioDao.getByEmail(email);

                runOnUiThread(() -> {
                    if (usuario != null && usuario.getContrasenaHash().equals(password)) {
                        Toast.makeText(this, "¡Bienvenido, " + usuario.getNombreUsuario() + "!", Toast.LENGTH_SHORT).show();
                        
                        // Enviar datos del usuario a MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("USER_ID", usuario.getId());
                        intent.putExtra("USER_NAME", usuario.getNombreUsuario());
                        intent.putExtra("USER_EMAIL", usuario.getEmail());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
