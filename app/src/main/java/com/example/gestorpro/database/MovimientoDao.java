package com.example.gestorpro.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface MovimientoDao {
    @Insert
    long insert(Movimiento movimiento);

    @Update
    void update(Movimiento movimiento);

    @Delete
    void delete(Movimiento movimiento);

    @Query("SELECT * FROM movimientos")
    List<Movimiento> getAll();

    @Query("SELECT * FROM movimientos WHERE producto_id = :productoId")
    List<Movimiento> getByProducto(int productoId);
}
