package com.example.intranet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {
    public Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    public static final int VERSION = 2;
    public Database(@Nullable Context context){
        super(context,DBContract.DATABASE_NAME, null, VERSION);
    }
    //Crear Bases de Datos
    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsuario(db);
        createLogin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Base de datos, datos del Usuario
    public void createUsuario(SQLiteDatabase db)
    {

        db.execSQL("CREATE TABLE "+DBContract.TABLE_USUARIO+"("+DBContract.Usuario.ID+" INTEGER PRIMARY KEY,"+DBContract.Usuario.USU_USUARIO+" TEXT," +
                DBContract.Usuario.USU_PASSWORD+" TEXT,"+DBContract.Usuario.USU_UUID+" TEXT,"
                +DBContract.Usuario.NOMBRE+" TEXT,"+DBContract.Usuario.APELLIDOS+" TEXT,"+DBContract.Usuario.UUID+" TEXT,"
                +DBContract.Usuario.FECHALOG+" TEXT NOT NULL DEFAULT CURRENT_DATE);");
    }

    public void saveUsuario(int usr_id, String usr_nick, String usr_nombre, String usr_app, String usr_apm, String cat_rsc_database, SQLiteDatabase db)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.Usuario.ID,usr_id);
        contentValues.put(DBContract.Usuario.USU_USUARIO,usr_nick);
        contentValues.put(DBContract.Usuario.USU_PASSWORD,usr_nombre);
        contentValues.put(DBContract.Usuario.USU_UUID, usr_app);
        contentValues.put(DBContract.Usuario.NOMBRE, usr_apm);
        contentValues.put(DBContract.Usuario.APELLIDOS, cat_rsc_database);
        db.insert(DBContract.TABLE_USUARIO,null, contentValues);
    }

    public Cursor readUsuario(SQLiteDatabase db)
    {
        return db.query(DBContract.TABLE_USUARIO,null,null,null,null, null,null);
    }
    // Base de datos, datos del Loging
    public void createLogin(SQLiteDatabase db)
    {

        db.execSQL("CREATE TABLE "+DBContract.TABLE_LOGIN+"("+DBContract.Login._ID+" INTEGER PRIMARY KEY,"+DBContract.Login.USUARIO+" TEXT," +
                DBContract.Login.PASSWORD+" TEXT," +DBContract.Login.SUBDOMINIO+" TEXT," +DBContract.Login.DOMINIO+" TEXT);");
    }

    public void saveLogin(String usuario, String password, String subdominio, String dominio, SQLiteDatabase db)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.Login.USUARIO,usuario);
        contentValues.put(DBContract.Login.PASSWORD,password);
        contentValues.put(DBContract.Login.SUBDOMINIO,subdominio);
        contentValues.put(DBContract.Login.DOMINIO,dominio);
        db.insert(DBContract.TABLE_LOGIN,null, contentValues);
    }

    public Cursor readLogin(SQLiteDatabase db)
    {
        return db.query(DBContract.TABLE_LOGIN,null,null,null,null, null,null);
    }
}
