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

    // Obtener todos los productos de un usuario específico
    @Query("SELECT * FROM productos WHERE usuario_id = :usuarioId")
    List<Producto> getAllByUser(int usuarioId);

    @Query("SELECT * FROM productos WHERE id = :id")
    Producto getById(int id);

    @Query("SELECT * FROM productos WHERE codigo_barras = :codigo AND usuario_id = :usuarioId")
    Producto getByCodigo(String codigo, int usuarioId);

    @Query("SELECT * FROM productos WHERE cantidad_actual <= cantidad_minima AND usuario_id = :usuarioId")
    List<Producto> getProductosStockBajo(int usuarioId);

    // Búsqueda por nombre o código para un usuario
    @Query("SELECT * FROM productos WHERE (nombre LIKE '%' || :query || '%' OR codigo_barras LIKE '%' || :query || '%') AND usuario_id = :usuarioId")
    List<Producto> search(String query, int usuarioId);
}
