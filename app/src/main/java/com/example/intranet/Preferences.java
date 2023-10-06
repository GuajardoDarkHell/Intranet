package com.example.intranet;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {


    public static final String STRING_PREFERENCES = "proyecto.PrincipalActivity";
    public static final String PREFERENCE_ESTADO_BUTTON_SESION = "estado.button.sesion";

    public static void savePreferenceBoolean(Context c, boolean b, String key) {
        SharedPreferences preferences = c.getSharedPreferences(STRING_PREFERENCES, c.MODE_PRIVATE);
        preferences.edit().putBoolean(key, b).apply();
    }

    public static boolean obtenerPreferenceBoolean(Context c, String key) {
        SharedPreferences preferences = c.getSharedPreferences(STRING_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);//Si es que nunca se ha guardado nada en esta key pues retornara false
    }
}

