package com.example.gestorpro.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CategoriaDao {
    @Insert
    long insert(Categoria categoria);

    @Update
    void update(Categoria categoria);

    @Delete
    void delete(Categoria categoria);

    @Query("SELECT * FROM categorias")
    List<Categoria> getAll();

    @Query("SELECT * FROM categorias WHERE id = :id")
    Categoria getById(int id);
}
