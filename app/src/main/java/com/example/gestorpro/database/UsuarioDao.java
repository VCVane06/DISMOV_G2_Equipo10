package com.example.gestorpro.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface UsuarioDao {
    @Insert
    long insert(Usuario usuario);

    @Update
    void update(Usuario usuario);

    @Delete
    void delete(Usuario usuario);

    @Query("SELECT * FROM usuarios")
    List<Usuario> getAll();

    @Query("SELECT * FROM usuarios WHERE email = :email")
    Usuario getByEmail(String email);

    @Query("SELECT * FROM usuarios WHERE id = :id")
    Usuario getById(int id);
}
