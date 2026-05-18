package com.example.gestorpro;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HistoryActivity extends AppCompatActivity {

    private String userUid, userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        userUid = getIntent().getStringExtra("USER_UID");
        userRole = getIntent().getStringExtra("USER_ROLE");

        TabLayout tabLayout = findViewById(R.id.tabLayoutHistory);
        ViewPager2 viewPager = findViewById(R.id.viewPagerHistory);
        ImageView btnBack = findViewById(R.id.btnBack);

        HistoryPagerAdapter adapter = new HistoryPagerAdapter(this, userUid, userRole);
        viewPager.setAdapter(adapter);

        // Si el rol es Almacen o Ventas, ocultamos el TabLayout para que no cambien de pestaña
        if ("Almacen".equals(userRole) || "Ventas".equals(userRole)) {
            tabLayout.setVisibility(View.GONE);
            viewPager.setUserInputEnabled(false); // Deshabilitar swipe
        }

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if ("Almacen".equals(userRole)) {
                tab.setText("Movimientos de Inventario");
            } else if ("Ventas".equals(userRole)) {
                tab.setText("Historial de Ventas");
            } else {
                if (position == 0) {
                    tab.setText("Movimientos");
                } else {
                    tab.setText("Ventas");
                }
            }
        }).attach();

        btnBack.setOnClickListener(v -> finish());
    }
}
