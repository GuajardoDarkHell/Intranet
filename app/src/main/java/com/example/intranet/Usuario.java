package com.example.intranet;

public class Usuario {
    String Usu_usuario,Usu_password,Usu_uuid,Nombre,Apellidos;


    public Usuario(String usu_usuario, String usu_password, String usu_uuid, String nombre, String apellidos) {
        Usu_usuario = usu_usuario;
        Usu_password = usu_password;
        Usu_uuid = usu_uuid;
        Nombre = nombre;
        Apellidos = apellidos;

    }

    public String getUsu_usuario() {
        return Usu_usuario;
    }

    public void setUsu_usuario(String usu_usuario){ Usu_usuario = usu_usuario; }

    public String getUsu_password() {
        return Usu_password;
    }

    public void setUsu_password(String usu_password) {
        Usu_password = usu_password;
    }

    public String getUsu_uuid() {
        return Usu_uuid;
    }

    public void setUsu_uuid(String usu_uuid) {
        Usu_uuid = usu_uuid;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getApellidos() {
        return Apellidos;
    }

    public void setApellidos(String apellidos) {
        Apellidos = apellidos;
    }
}