package com.sigev.modelo;

import java.io.Serializable;

/**
 * Usuario del sistema SIGEV (tabla usuario).
 * claveHash guarda la contrasena cifrada, nunca la contrasena real.
 * Es Serializable porque se guarda en la sesion de Tomcat.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String correo;
    private String claveHash;
    private String rol;

    public Usuario() {
    }

    public Usuario(String nombre, String correo, String claveHash, String rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.claveHash = claveHash;
        this.rol = rol;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public void setClaveHash(String claveHash) {
        this.claveHash = claveHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
