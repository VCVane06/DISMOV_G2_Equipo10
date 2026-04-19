package com.example.gestorpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ImageView ivMenu = findViewById(R.id.ivMenu);

        // Obtener datos del Intent
        usuarioId = getIntent().getIntExtra("USER_ID", -1);
        String userName = getIntent().getStringExtra("USER_NAME");
        String userEmail = getIntent().getStringExtra("USER_EMAIL");

        // Configurar Header
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserName);
        TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmail);

        if (userName != null) tvUserName.setText(userName);
        if (userEmail != null) tvUserEmail.setText(userEmail);

        ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_reporte) {
                Toast.makeText(MainActivity.this, "Ya estás en Reporte", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_productos) {
                Intent intent = new Intent(MainActivity.this, ProductosActivity.class);
                intent.putExtra("USER_ID", usuarioId);
                startActivity(intent);
            } else if (id == R.id.nav_escanear) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                intent.putExtra("USER_ID", usuarioId);
                startActivity(intent);
            } else if (id == R.id.nav_venta) {
                Toast.makeText(MainActivity.this, "Abriendo Ventas...", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_config) {
                Toast.makeText(MainActivity.this, "Abriendo Configuración...", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
