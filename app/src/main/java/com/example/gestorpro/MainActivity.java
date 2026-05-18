package com.example.gestorpro;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.gestorpro.database.Producto;
import com.example.gestorpro.database.Venta;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private String userUid, userName, userEmail, userRole;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private NavigationView navigationView;
    private ListenerRegistration userListener;

    // UI Views
    private TextView tvWelcome;
    private TextView tvStatTodaySales, tvStatLowStock, tvStatTotalProducts;
    private View cardStatSales, cardStatStock, cardStatProducts;
    private View sectionSalesChart, sectionInventoryStatus, sectionBestSellers;
    private TextView tvProdName1, tvProdName2, tvProdName3;
    private ProgressBar pbStock1, pbStock2, pbStock3;
    private LinearLayout containerBestSellers;
    private BarChart barChartVentas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userUid = getIntent().getStringExtra("USER_UID");
        if (userUid == null && mAuth.getCurrentUser() != null) {
            userUid = mAuth.getCurrentUser().getUid();
        }

        initViews();

        if (userUid != null) {
            inicializarInterfaz();
            escucharDatosUsuario();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageView ivMenu = findViewById(R.id.ivMenu);
        tvWelcome = findViewById(R.id.tvWelcome);

        tvStatTodaySales = findViewById(R.id.tvStatTodaySales);
        tvStatLowStock = findViewById(R.id.tvStatLowStock);
        tvStatTotalProducts = findViewById(R.id.tvStatTotalProducts);
        cardStatSales = findViewById(R.id.cardStatSales);
        cardStatStock = findViewById(R.id.cardStatStock);
        cardStatProducts = findViewById(R.id.cardStatProducts);

        sectionSalesChart = findViewById(R.id.sectionSalesChart);
        sectionInventoryStatus = findViewById(R.id.sectionInventoryStatus);
        sectionBestSellers = findViewById(R.id.sectionBestSellers);

        tvProdName1 = findViewById(R.id.tvReportProdName1);
        tvProdName2 = findViewById(R.id.tvReportProdName2);
        tvProdName3 = findViewById(R.id.tvReportProdName3);
        pbStock1 = findViewById(R.id.pbReportStock1);
        pbStock2 = findViewById(R.id.pbReportStock2);
        pbStock3 = findViewById(R.id.pbReportStock3);
        
        containerBestSellers = findViewById(R.id.containerBestSellers);
        barChartVentas = findViewById(R.id.barChartVentas);

        ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        findViewById(R.id.btnVerMasStock).setOnClickListener(v -> abrirActividad(ProductosActivity.class));
    }

    private void escucharDatosUsuario() {
        if (userListener != null) userListener.remove();
        userListener = db.collection("usuarios").document(userUid)
                .addSnapshotListener((doc, e) -> {
                    if (e != null || doc == null || !doc.exists()) return;
                    userName = doc.getString("nombre");
                    userEmail = doc.getString("correo");
                    userRole = doc.getString("rol");
                    String fotoUrl = doc.getString("foto_url");

                    tvWelcome.setText("Hola, " + userName);
                    View header = navigationView.getHeaderView(0);
                    TextView tvHeaderName = header.findViewById(R.id.tvUserName);
                    TextView tvHeaderEmail = header.findViewById(R.id.tvUserEmail);
                    if (tvHeaderName != null) tvHeaderName.setText(userName);
                    if (tvHeaderEmail != null) tvHeaderEmail.setText(userEmail);
                    
                    ImageView ivProfile = header.findViewById(R.id.ivProfile);
                    if (ivProfile != null && fotoUrl != null && !fotoUrl.isEmpty()) {
                        Glide.with(this).load(fotoUrl).circleCrop().into(ivProfile);
                    }
                    
                    ajustarMenuPorRol();
                });
    }

    private void inicializarInterfaz() {
        setupBarChart();
        escucharVentasHoy();
        escucharDatosReporte();
        escucharDatosGraficaSemanales();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_productos) abrirActividad(ProductosActivity.class);
            else if (id == R.id.nav_escanear) abrirActividad(ScanActivity.class);
            else if (id == R.id.nav_venta) abrirActividad(PosActivity.class);
            else if (id == R.id.nav_historial) abrirActividad(HistoryActivity.class);
            else if (id == R.id.nav_usuarios) abrirActividad(UserManagementActivity.class);
            else if (id == R.id.nav_config) abrirActividad(SettingsActivity.class);
            
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void abrirActividad(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("USER_UID", userUid);
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("USER_ROLE", userRole);
        startActivity(intent);
    }

    private void ajustarMenuPorRol() {
        Menu menu = navigationView.getMenu();
        if (userRole != null) {
            menu.findItem(R.id.nav_usuarios).setVisible("Administrador".equals(userRole));
        }
    }

    private void escucharVentasHoy() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        db.collection("ventas")
                .whereGreaterThanOrEqualTo("fecha", cal.getTime())
                .addSnapshotListener(MetadataChanges.INCLUDE, (value, error) -> {
                    if (error != null) return;
                    double totalHoy = 0;
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Object t = doc.get("total");
                            if (t instanceof Number) totalHoy += ((Number) t).doubleValue();
                        }
                    }
                    tvStatTodaySales.setText(String.format(Locale.getDefault(), "$%.0f", totalHoy));
                });
    }

    private void setupBarChart() {
        barChartVentas.getDescription().setEnabled(false);
        barChartVentas.getLegend().setEnabled(false);
        XAxis xAxis = barChartVentas.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        barChartVentas.getAxisRight().setEnabled(false);
        barChartVentas.getAxisLeft().setDrawGridLines(false);
    }

    private void escucharDatosReporte() {
        db.collection("productos")
                .addSnapshotListener(MetadataChanges.INCLUDE, (value, error) -> {
                    if (error != null || value == null) return;
                    List<Producto> productos = new ArrayList<>();
                    int lowStockCount = 0;
                    for (QueryDocumentSnapshot doc : value) {
                        Producto p = doc.toObject(Producto.class);
                        p.setId(doc.getId());
                        productos.add(p);
                        if (p.getStock_actual() <= p.getStock_minimo()) lowStockCount++;
                    }
                    tvStatTotalProducts.setText(String.valueOf(productos.size()));
                    tvStatLowStock.setText(String.valueOf(lowStockCount));
                    actualizarGraficasStock(productos);
                    actualizarMasVendido(productos);
                });
    }

    private void escucharDatosGraficaSemanales() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        db.collection("ventas")
                .whereGreaterThanOrEqualTo("fecha", cal.getTime())
                .addSnapshotListener(MetadataChanges.INCLUDE, (value, error) -> {
                    if (error != null || value == null) return;
                    Map<String, Double> revenueByDay = new HashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    
                    for (int i = 6; i >= 0; i--) {
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.DAY_OF_YEAR, -i);
                        revenueByDay.put(sdf.format(c.getTime()), 0.0);
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        Date f = doc.getDate("fecha");
                        Object t = doc.get("total");
                        if (f != null && t instanceof Number) {
                            String dStr = sdf.format(f);
                            if (revenueByDay.containsKey(dStr)) {
                                double current = revenueByDay.get(dStr);
                                revenueByDay.put(dStr, current + ((Number) t).doubleValue());
                            }
                        }
                    }
                    actualizarBarChart(revenueByDay);
                });
    }

    private void actualizarBarChart(Map<String, Double> revenueByDay) {
        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -(6 - i));
            String label = sdf.format(c.getTime());
            labels.add(label);
            entries.add(new BarEntry(i, revenueByDay.getOrDefault(label, 0.0).floatValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Ventas");
        dataSet.setColor(Color.parseColor("#0F172A")); // Azul Marino Oscuro
        dataSet.setValueTextColor(Color.parseColor("#0F172A"));
        dataSet.setValueTextSize(10f);
        
        barChartVentas.setData(new BarData(dataSet));
        barChartVentas.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartVentas.animateY(1000);
        barChartVentas.invalidate();
    }

    private void actualizarGraficasStock(List<Producto> productos) {
        List<Producto> sorted = new ArrayList<>(productos);
        Collections.sort(sorted, (p1, p2) -> Long.compare(p1.getStock_actual(), p2.getStock_actual()));

        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            Producto p = sorted.get(i);
            int progress = (p.getStock_minimo() > 0) ? (int)(p.getStock_actual() * 100 / (p.getStock_minimo() * 2)) : 100;
            if (progress > 100) progress = 100;
            if (i == 0) { tvProdName1.setText(p.getNombre()); pbStock1.setProgress(progress); }
            else if (i == 1) { tvProdName2.setText(p.getNombre()); pbStock2.setProgress(progress); }
            else if (i == 2) { tvProdName3.setText(p.getNombre()); pbStock3.setProgress(progress); }
        }
    }

    private void actualizarMasVendido(List<Producto> productos) {
        containerBestSellers.removeAllViews();
        List<Producto> sorted = new ArrayList<>(productos);
        Collections.sort(sorted, (p1, p2) -> Long.compare(p2.getVentasTotales(), p1.getVentasTotales()));
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            Producto p = sorted.get(i);
            if (p.getVentasTotales() <= 0) continue;
            View view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null);
            TextView text = view.findViewById(android.R.id.text1);
            text.setText(p.getNombre() + " (" + p.getVentasTotales() + " ventas)");
            text.setTextColor(Color.parseColor("#1E293B"));
            containerBestSellers.addView(view);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) userListener.remove();
    }
}
