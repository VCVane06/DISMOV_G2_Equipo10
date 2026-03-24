package com.example.gestorpro.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Categoria.class, Producto.class, Movimiento.class, Usuario.class, Venta.class},
        version = 2, // Incrementado de 1 a 2
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CategoriaDao categoriaDao();
    public abstract ProductoDao productoDao();
    public abstract MovimientoDao movimientoDao();
    public abstract UsuarioDao usuarioDao();
    public abstract VentaDao ventaDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "gestorpro.db")
                            .fallbackToDestructiveMigration() // Añadido para evitar errores de migración en desarrollo
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
