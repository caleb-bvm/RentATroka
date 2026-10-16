package com.mycompany.rentatroka.modelo;

import java.io.Serializable;

/** JavaBean de la cuenta que permite ingresar al sistema. */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String nombreUsuario;
    private String claveHash;

    public Usuario() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public void setClaveHash(String claveHash) {
        this.claveHash = claveHash;
    }
}

