package com.example.intranet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrincipalActivity extends AppCompatActivity {
    //variables
    public static TextView textView;
    ImageButton btnticket, btninvfis, btngas, btnsalir, btnsync;

    public static Database conn;
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-mm-dd");
    public static String fechaht;
    public static String usrid;
    public static String usudb;
    public static String subdo;
    public static String domine;
    public static String subd;
    private static final String fecho = "2023-09-19";


    //Permisos del dispositivo
    private static boolean tienePermisoGPS = false;


    private void verificarYPedirPermisosDeGPS() {

        // int estadoDePermiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        System.out.println( manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) +"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        // String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if ( manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            permisoDeGPSConcedido();
        } else {
            // Si no, entonces pedimos permisos. Ahora mira onRequestPermissionsResult

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PERMISOS_GPS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //System.out.println(android.provider.Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.LOCATION_MODE)+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+PackageManager.PERMISSION_GRANTED);
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CODIGO_PERMISOS_GPS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && provider.contains("gps")) {
                    permisoDeGPSConcedido();
                } else {
                    permisoDeGPSDenegado();
                }
                break;

        }
    }

    private void permisoDeGPSConcedido() {
        Toast.makeText(this, "Bienvenido a Unikasoft Mobile", Toast.LENGTH_SHORT).show();
        tienePermisoGPS = true;
    }

    private void permisoDeGPSDenegado() {
        conn = new Database(getApplicationContext(), "clientes.db", null, 2);
        final Database database = new Database(this);
        final SQLiteDatabase db = database.getWritableDatabase();
        @SuppressLint("Recycle") Cursor gps = db.rawQuery("select valor from configuracion where nombre='pvMovilCheck4GPS'",null);
        gps.moveToFirst();
        if(gps.getInt(0)==1) {
            Toast.makeText(this, "Debe tener el GPS encendido para usar la APP", Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
    }


    private static final int CODIGO_PERMISOS_GPS = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        verificarYPedirPermisosDeGPS();
        conn = new Database(PrincipalActivity.this);
        //desactivar boton retroceso de android
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();

            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        //Se enlazan las variables con los componenetes correspondientes
        btnticket = findViewById(R.id.btnticket);
        btninvfis = findViewById(R.id.btninvfis);
        btngas = findViewById(R.id.btngas);
        btnsalir = findViewById(R.id.btnsalir);
        btnsync = findViewById(R.id.btnsync);

        //recordar cargar los datos necesarios faltantes
        btnsync.setOnClickListener(v -> {
            ConnectivityManager connectivityManager = (ConnectivityManager) v.getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                // Si hay conexión a Internet en este momento
                btnsync.setVisibility(View.INVISIBLE);
                validarsync();
                validafecha();
                cargarsubdo();
                cargarsubd();
                uuid();
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                recreate();
            } else {
                // No hay conexión a Internet en este momento
                Toast.makeText(v.getContext().getApplicationContext(), "No hay conexión a Internet", Toast.LENGTH_LONG).show();
            }
        });
        btnsalir.setOnClickListener(v -> validasalir());

        //función asignada a cada boton

        btnticket.setOnClickListener(view -> {
            if (fecho.equals(fechaht)){
                validarTicket();
            }else {
                    Toast.makeText(getApplicationContext(), "Sincronize por favor", Toast.LENGTH_LONG).show();
                }
            if (fechaht == null){
                System.out.println("nulo");
            }
        });
        btninvfis.setOnClickListener(view -> {
            if (fecho.equals(fechaht)){
                validarInventario();
            }else {
                Toast.makeText(getApplicationContext(), "Sincronize por favor", Toast.LENGTH_LONG).show();
            }
        });
        btngas.setOnClickListener(view -> {

            if (fecho.equals(fechaht)){
                validarGasolina();
            }else {
                Toast.makeText(getApplicationContext(), "Sincronize por favor", Toast.LENGTH_LONG).show();
            }
        });
    }

    //Metodos de los componentes

    private void validarsync(){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sincronizando");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        new Sincronizar( this,progressDialog).execute();
    }

    public class Sincronizar extends AsyncTask<Void,Void,Void>
    {
        Context context;
        ProgressDialog progressDialog;
        public Sincronizar(Context context,ProgressDialog progressDialog)
        {
            this.progressDialog = progressDialog;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids){

            try {
                Thread.sleep(9000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            return null;
        }
    //Agregar la sincronización de las tablas creadas despues
        @Override
        protected void onPreExecute(){
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.show();

            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);

            Toast.makeText(context, "Sincronizado Correctamente", Toast.LENGTH_LONG).show();

            progressDialog.hide();
        }
    }

    public void uuid(){
        SQLiteDatabase db = conn.getWritableDatabase();
        @SuppressLint("HardwareIds") String fech =Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        ContentValues values = new ContentValues();
        values.put(DBContract.Usuario.UUID, fech);
        db.update(DBContract.TABLE_USUARIO,values, "usr_id=?",new String[]{usrid});
        db.close();
    }

    private void validasalir(){
        Preferences.savePreferenceBoolean(PrincipalActivity.this,false, Preferences.PREFERENCE_ESTADO_BUTTON_SESION);
        Intent intent = new Intent(PrincipalActivity.this, com.example.intranet.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this will clear all the stack
        startActivity(intent);
        finish();
    }

    private void validarTicket(){
        Intent intent=new Intent(getApplicationContext(), TicketActivity.class);
        intent.putExtra("dato",textView.getText().toString());
        startActivity(intent);
        finish();
    }

    private void validarGasolina(){
        Intent intent=new Intent(getApplicationContext(), GasolinaActivity.class);
        intent.putExtra("dato",textView.getText().toString());
        startActivity(intent);
        finish();
    }

    private void validarInventario(){
        Intent intent=new Intent(getApplicationContext(), InventarioActivity.class);
        intent.putExtra("dato",textView.getText().toString());
        startActivity(intent);
        finish();
    }

    public static boolean checkNetworkConnection(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo!=null && networkInfo.isConnected());
    }


    //Métodos de sincronización agregar



    //Método para recibir los datos de la base de datos interna

    void validafecha(){
        SQLiteDatabase db = conn.getWritableDatabase();
        DateTimeFormatter dtf5 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fech =(dtf5.format(LocalDateTime.now()));
        ContentValues values = new ContentValues();
        values.put(DBContract.Usuario.FECHALOG, fech);
//        db.update(DBContract.TABLE_USUARIO,values, "usr_nick=?",new String[]{textView.getText().toString()});
        db.close();
    }

    @SuppressLint("Recycle")
    public void cargarsubdo(){
        SQLiteDatabase db=conn.getReadableDatabase();
        Cursor fila= db.rawQuery("select subdominio,dominio from "+ DBContract.TABLE_LOGIN, null);
        if(fila.moveToFirst()){
            do{
                subdo= fila.getString(0).toLowerCase();
                domine= fila.getString(1).toLowerCase();
            }while (fila.moveToNext());
        }

    }
    @SuppressLint("Recycle")
    public void cargarsubd(){
        SQLiteDatabase db=conn.getReadableDatabase();
        Cursor fila= db.rawQuery("select cat_rsc_database from usuario", null);
        if(fila.moveToFirst()){
            do{
                subd= fila.getString(0).toLowerCase();
            }while (fila.moveToNext());
        }

    }

}
