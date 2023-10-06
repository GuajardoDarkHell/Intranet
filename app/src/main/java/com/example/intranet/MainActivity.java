package com.example.intranet;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private EditText edtUsuario, edtPassword, edtUuid, edtEmpresa, edtHost;
    public static String usuario, password, uuid, empresa, host;
    Button btnLogin;
    private boolean isActivateRadioButton;
    private RadioButton RBsesion;
    private static ArrayList<Usuario> arrayList5 = new ArrayList<Usuario>();
    //private static ArrayList<Almdisp> arrayList6 = new ArrayList<Almdisp>();
    //private static ArrayList<Configuracion> arrayList7 = new ArrayList<Configuracion>();
    Database conn;
    // Banderas que indicarán si tenemos permisos
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
            // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISOS_GPS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permisoDeGPSConcedido();
            } else {
                permisoDeGPSDenegado();
            }
        }
    }

    private void permisoDeGPSConcedido() {
        Toast.makeText(MainActivity.this, "Bienvenido a Unikasoft Mobile", Toast.LENGTH_SHORT).show();
        tienePermisoGPS = true;
    }

    private void permisoDeGPSDenegado() {
        conn = new Database(getApplicationContext(), "intmov.db", null, 2);
        final Database database = new Database(this);
        final SQLiteDatabase db = database.getWritableDatabase();
        @SuppressLint("Recycle") Cursor gps = db.rawQuery("select valor from configuracion where nombre='pvMovilCheck4GPS'",null);
        gps.moveToFirst();
        if(gps.getInt(0)==0) {
            Toast.makeText(MainActivity.this, "Debe aceptar el permiso de ubicacion para usar la APP", Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
    }


    private static final int CODIGO_PERMISOS_GPS = 1;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verificarYPedirPermisosDeGPS();


        conn = new Database(getApplicationContext(), "intmov.db", null, 2);

        //desactivar boton retroceso de android
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        if(Preferences.obtenerPreferenceBoolean(this, Preferences.PREFERENCE_ESTADO_BUTTON_SESION)){
            iniciarActividadSiguiente();
        }

        //Enlazamos las variables con sus componentes correspondientes
        edtUsuario = findViewById(R.id.edtUsuario);
        edtPassword = findViewById(R.id.edtPassword);
        edtUuid = findViewById(R.id.edtUuid);
        edtHost = findViewById(R.id.edtHost);
        edtEmpresa = findViewById(R.id.edtEmpresa);
        btnLogin = findViewById(R.id.btnLogin);
        RBsesion = findViewById(R.id.RBSecion);

        //Invocacion del metodo para guardar credenciales en el dispositivo
        isActivateRadioButton = RBsesion.isChecked(); //DESACTIVADO
        recuperarPreferencias();


        RBsesion.setOnClickListener(new View.OnClickListener() {
            //ACTIVADO
            @Override
            public void onClick(View v) {
                if(isActivateRadioButton){
                    RBsesion.setChecked(false);
                }
                isActivateRadioButton = RBsesion.isChecked();
            }
        });

        //Funcion para cargar el UUID del sistema android.
        edtUuid.setText(Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID));

        btnLogin.setOnClickListener(view -> {
            edtUuid.setText(Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            usuario=edtUsuario.getText().toString();
            password=edtPassword.getText().toString();
            uuid=edtUuid.getText().toString();
            empresa=edtEmpresa.getText().toString().toLowerCase();
            host=edtHost.getText().toString().toLowerCase();
            System.out.println(usuario+' '+password+' '+uuid+' '+empresa+' '+host);
            if (!usuario.isEmpty() && !password.isEmpty() && !uuid.isEmpty() && !empresa.isEmpty() && !host.isEmpty()){
                validaUsuario("http://www."+host+"/WS/PDVMovil/loginuser.php");
                usuarios();
            }else{
                Toast.makeText(MainActivity.this, "No se permiten campos vacios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Método usado para validar un login a traves del uso de webservices
    private void validaUsuario(String URL){
        System.out.println(URL);
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL, response -> {
            // System.out.println(response+"  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+URL);
            if (!response.isEmpty()) {
                 System.out.println(response);
                try{

                    JSONObject objeto = new JSONObject(response);

                    switch (objeto.getString("pvM_dev_status")){
                        case "1":
                            validarsync();
                            Preferences.savePreferenceBoolean(MainActivity.this,RBsesion.isChecked(), Preferences.PREFERENCE_ESTADO_BUTTON_SESION);
                            guardarPreferencias();
                            Intent intent=new Intent(MainActivity.this ,PrincipalActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(MainActivity.this,"Datos validados",Toast.LENGTH_SHORT).show();
                            break;
                        case "2":
                            final Toast toast = Toast.makeText(getApplicationContext(), "Dispositivo no autorizado",
                                    Toast.LENGTH_LONG); toast.show(); Handler handler = new Handler(); handler.postDelayed(new Runnable() { @Override public void run() { toast.cancel(); } }, 50000);
                            //Toast.makeText(MainActivity.this,"Dispositivo no autorizado",Toast.LENGTH_SHORT).show();
                            break;
                        case "3":
                            Toast.makeText(MainActivity.this,"Usuario incorrecto",Toast.LENGTH_SHORT).show();
                            break;
                        case "4":
                            Toast.makeText(MainActivity.this,"Contraseña incorrecta",Toast.LENGTH_SHORT).show();
                            break;
                        case "5":
                            Toast.makeText(MainActivity.this,"Usuario no tiene asignada una razon social",Toast.LENGTH_SHORT).show();
                            break;
                        case "7":
                            Toast.makeText(MainActivity.this,"Error al buscar informacion de la razon social asignada al usuario",Toast.LENGTH_SHORT).show();
                            break;
                        case "8":
                            Toast.makeText(MainActivity.this,"El usuario no esta ligado a un vendedor",Toast.LENGTH_SHORT).show();
                            break;
                        case "9":
                            Toast.makeText(MainActivity.this,"El vendedor ligado al usuario fue desactivado",Toast.LENGTH_SHORT).show();
                            break;
                        case "10":
                            Toast.makeText(MainActivity.this,"Error al buscar almacenes ligados al usuario",Toast.LENGTH_SHORT).show();
                            break;
                        case "11":
                            Toast.makeText(MainActivity.this,"El usuario no tiene ningún almacen ligado abierto",Toast.LENGTH_SHORT).show();
                            break;
                    }


                }catch (Exception e){

                }



            }else{
                Toast.makeText(MainActivity.this,"Usuario o contraseña incorrectos",Toast.LENGTH_SHORT).show();
            }
        }, error ->  Toast.makeText(MainActivity.this,"Hay problemas con el servidor",Toast.LENGTH_SHORT).show()){
            @Override

            protected Map<String, String> getParams() {
                Map<String,String> parametros= new HashMap<>();
                parametros.put("usuario", usuario);
                parametros.put("password",password);
                parametros.put("uuid",uuid);
                parametros.put("empresa",empresa);
                parametros.put("host",host);
                return parametros;

            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    //Método que guarda las preferencias de conexion
    private void guardarPreferencias (){

        SharedPreferences preferencias = getSharedPreferences("preferenciasLogin",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferencias.edit();
        editor.putString("usuario",usuario);
        editor.putString("password",password);
        editor.putString("empresa",empresa);
        editor.putBoolean("sesion",true);
        editor.apply();
    }

    //Método que recupera las prefeencias de conexion para no tener que escribir los datos de usuario
    private void recuperarPreferencias(){
        SharedPreferences preferencias=getSharedPreferences("preferenciasLogin",Context.MODE_PRIVATE);
        edtUsuario.setText(preferencias.getString("usuario",""));
        edtPassword.setText(preferencias.getString("password",""));
        edtEmpresa.setText(preferencias.getString("empresa",""));
        edtHost.setText(preferencias.getString("host",""));

    }

    private void  validarsync(){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        new Sincronizar(this,progressDialog).execute();

    }

    public static class Sincronizar extends AsyncTask<Void,Void,Void>
    {
        Context context;
        //ProgressDialog progressDialog;
        public Sincronizar(Context context,ProgressDialog progressDialog)
        {
            // this.progressDialog = progressDialog;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            sincronizarUsuario(context);
            //sincronizarAlmdisp(context);
            //sincronizarConfiguracion(context);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //  progressDialog.hide();
            readUsuario(context);
        }
    }

    public void iniciarActividadSiguiente(){
        Intent i = new Intent(MainActivity.this,PrincipalActivity.class);
        startActivity(i);
        finish();
    }

    public static boolean checkNetworkConnection(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo!=null && networkInfo.isConnected());
    }

    public static void sincronizarUsuario(Context context)
    {
        if(checkNetworkConnection(context))
        {
            final Database database = new Database(context);
            final SQLiteDatabase db = database.getWritableDatabase();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://www."+host+"/WS/PDVMovil/validar_usuario2.php?usuario="+usuario+"&password="+password+"&uuid="+uuid+"&empresa="+empresa, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println("http://www."+host+"/WS/PDVMovil/validar_usuario2.php?usuario="+usuario+"&password="+password+"&uuid="+uuid+"&empresa="+empresa);
                    try {
                        JSONArray array = new JSONArray(response);
                        db.execSQL("DELETE FROM usuario");
                        for(int i = 0; i<array.length(); i++)
                        {
                            JSONObject object = array.getJSONObject(i);
                            database.saveUsuario(object.getInt("usr_id"),object.getString("usr_nick"),object.getString("usr_nombre"),object.getString("usr_app"),
                                    object.getString("usr_apm"), object.getString("cat_rsc_database"), db);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        ContentValues conten=new ContentValues();
                        conten.put("uuid",uuid);
                        db.update("usuario",conten," usr_nick=?",new String[]{usuario.toUpperCase()});
                    }catch (Exception l){
                        l.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("error",""+error);
                }
            });
            MySingleton.getInstance(context).addToRequestQue(stringRequest);
        }

    }

    @SuppressLint("Range")
    public static void readUsuario(Context context)
    {
        arrayList5.clear();
        Database database = new Database(context);
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor c = database.readUsuario(db);
        while (c.moveToNext())
        {
            arrayList5.add(new Usuario(c.getString(c.getColumnIndex(DBContract.Usuario.USU_USUARIO)),c.getString(c.getColumnIndex(DBContract.Usuario.USU_PASSWORD)),
                    c.getString(c.getColumnIndex(DBContract.Usuario.USU_UUID)), c.getString(c.getColumnIndex(DBContract.Usuario.NOMBRE)),
                    c.getString(c.getColumnIndex(DBContract.Usuario.APELLIDOS))));
        }

        c.close();
        database.close();
    }

//--------------------------------------------------------------------------------------------------



//--------------------------------------------------------------------------------------------------

    /*public static void sincronizarConfiguracion(Context context)
    {
        if(checkNetworkConnection(context))
        {
            final Database database = new Database(context);
            final SQLiteDatabase db = database.getWritableDatabase();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://www.dooplererp.com/WS/PDVMovil/getconf.php?usudb="+empresa, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONArray array = new JSONArray(response);
                        db.execSQL("DELETE FROM configuracion");
                        for(int i = 0; i<array.length(); i++)
                        {
                            JSONObject object = array.getJSONObject(i);
                            database.saveConf(object.getString("nombre"),object.getString("valor"),db);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("error",""+error);
                }
            });
            MySingleton.getInstance(context).addToRequestQue(stringRequest);
        }
    }*/
    public void usuarios(){
        com.example.intranet.MainActivity main;
        Database database = new Database(getApplicationContext());
        SQLiteDatabase db = database.getWritableDatabase();
        db.delete("login",null,null);
        ContentValues content = new ContentValues();
        content.put("usuario", com.example.intranet.MainActivity.usuario.trim());
        content.put("password", com.example.intranet.MainActivity.password.trim());
        content.put("subdominio", com.example.intranet.MainActivity.empresa.toUpperCase().trim());
        content.put("dominio", MainActivity.host.toLowerCase().trim());
        db.insert(DBContract.TABLE_LOGIN,null,content);


    }

}