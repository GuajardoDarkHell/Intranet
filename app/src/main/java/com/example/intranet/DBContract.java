package com.example.intranet;

import android.provider.BaseColumns;

public class DBContract {
    //Tabla de la BD??? Preguntarle a santi
    public static  final String DATABASE_NAME = "intmov.db";
    public static final String TABLE_USUARIO = "usuario";
    public static final String TABLE_LOGIN = "login";
    //Columnas
    public static class Usuario implements BaseColumns{
        public static String ID = "usr_id";
        public static String USU_USUARIO ="usr_nick";
        public static String USU_PASSWORD ="usr_nombre";
        public static String USU_UUID ="usr_app";
        public static String NOMBRE ="usr_apm";
        public static String APELLIDOS ="cat_rsc_database";
        public static String UUID ="uuid";
        public static String FECHALOG ="fechalog";
    }

    public static class Login implements BaseColumns{
        public static String _ID = "_id";
        public static String USUARIO = "usuario";
        public static String PASSWORD = "password";
        public static String SUBDOMINIO = "subdominio";
        public static String DOMINIO = "dominio";

    }
}
