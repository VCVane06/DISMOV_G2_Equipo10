package com.example.gestorpro.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ProductoDao {
    @Insert
    long insert(Producto producto);

    @Update
    void update(Producto producto);

    @Delete
    void delete(Producto producto);

    @Query("SELECT * FROM productos")
    List<Producto> getAll();

    @Query("SELECT * FROM productos WHERE id = :id")
    Producto getById(int id);

    @Query("SELECT * FROM productos WHERE codigo_barras = :codigo")
    Producto getByCodigo(String codigo);

    @Query("SELECT * FROM productos WHERE cantidad_actual <= cantidad_minima")
    List<Producto> getProductosStockBajo();
}
