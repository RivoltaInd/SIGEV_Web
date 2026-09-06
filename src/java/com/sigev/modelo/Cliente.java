package com.sigev.modelo;

/**
 * Modelo de datos para un cliente del sistema SIGEV.
 * Sigue el mismo estandar de codificacion definido en AA1-EV02:
 * clase en PascalCase, atributos/metodos en camelCase.
 */
public class Cliente {

    private int id;
    private String nombre;
    private String documento;
    private String telefono;
    private String email;

    public Cliente() {
    }

    public Cliente(String nombre, String documento, String telefono, String email) {
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
        this.email = email;
    }

    public Cliente(int id, String nombre, String documento, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
        this.email = email;
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

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Cliente{id=" + id + ", nombre=" + nombre + ", documento=" + documento + "}";
    }
}
