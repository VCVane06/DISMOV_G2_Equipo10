package com.example.gestorpro;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProductosPagerAdapter extends FragmentStateAdapter {

    private ProductosFragment todosFragment;
    private ProductosFragment stockBajoFragment;

    public ProductosPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            if (todosFragment == null) {
                todosFragment = new ProductosFragment();
                Bundle argsTodos = new Bundle();
                argsTodos.putBoolean("mostrarSoloStockBajo", false);
                todosFragment.setArguments(argsTodos);
            }
            return todosFragment;
        } else {
            if (stockBajoFragment == null) {
                stockBajoFragment = new ProductosFragment();
                Bundle argsStockBajo = new Bundle();
                argsStockBajo.putBoolean("mostrarSoloStockBajo", true);
                stockBajoFragment.setArguments(argsStockBajo);
            }
            return stockBajoFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void actualizarDatos() {
        if (todosFragment != null) {
            todosFragment.actualizarLista();
        }
        if (stockBajoFragment != null) {
            stockBajoFragment.actualizarLista();
        }
    }
}