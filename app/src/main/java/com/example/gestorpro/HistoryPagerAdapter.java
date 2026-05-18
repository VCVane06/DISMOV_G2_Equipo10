package com.example.gestorpro;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HistoryPagerAdapter extends FragmentStateAdapter {

    private final String userUid;
    private final String userRole;

    public HistoryPagerAdapter(@NonNull FragmentActivity fragmentActivity, String userUid, String userRole) {
        super(fragmentActivity);
        this.userUid = userUid;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if ("Almacen".equals(userRole)) {
            return MovementsFragment.newInstance(userUid);
        } else if ("Ventas".equals(userRole)) {
            return SalesHistoryFragment.newInstance(userUid);
        } else {
            // Admin ve ambos
            if (position == 0) {
                return MovementsFragment.newInstance(userUid);
            } else {
                return SalesHistoryFragment.newInstance(userUid);
            }
        }
    }

    @Override
    public int getItemCount() {
        if ("Almacen".equals(userRole) || "Ventas".equals(userRole)) {
            return 1;
        }
        return 2; // Administrador ve 2 pestañas
    }
}
