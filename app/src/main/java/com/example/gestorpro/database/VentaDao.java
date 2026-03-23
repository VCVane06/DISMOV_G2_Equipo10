package com.example.gestorpro.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface VentaDao {
    @Insert
    long insert(Venta venta);

    @Update
    void update(Venta venta);

    @Delete
    void delete(Venta venta);

    @Query("SELECT * FROM ventas")
    List<Venta> getAll();

    @Query("SELECT * FROM ventas WHERE id = :id")
    Venta getById(int id);
}
