package com.example.gestorpro.database;


import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "usuarios")
public class Usuario {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "nombre_usuario")
    private String nombreUsuario;

    @ColumnInfo(name = "contrasena_hash")
    private String contrasenaHash;

    @ColumnInfo(name = "foto_perfil")
    private String fotoPerfil;

    @ColumnInfo(name = "fecha_creacion")
    private long fechaCreacion; // timestamp

    // Constructor
    public Usuario(String email, String nombreUsuario, String contrasenaHash) {
        this.email = email;
        this.nombreUsuario = nombreUsuario;
        this.contrasenaHash = contrasenaHash;
        this.fechaCreacion = System.currentTimeMillis();
    }

    // Getters y setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
