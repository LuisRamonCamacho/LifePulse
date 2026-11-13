package com.example.lifepulse;

import android.content.Context;
import android.content.SharedPreferences;

public class SesionManager {

    private static final String PREF_NAME = "LifePulseSesion";
    private static final String KEY_ID = "id_usuario";
    private static final String KEY_NOMBRE = "nombre_usuario";

    private final SharedPreferences preferences;

    public SesionManager(Context context) {
        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void guardarSesion(int idUsuario, String nombre) {
        preferences.edit()
                .putInt(KEY_ID, idUsuario)
                .putString(KEY_NOMBRE, nombre)
                .apply();
    }

    public int obtenerIdUsuario() {
        return preferences.getInt(KEY_ID, -1);
    }

    public String obtenerNombreUsuario() {
        return preferences.getString(KEY_NOMBRE, "");
    }

    public boolean haySesionActiva() {
        return obtenerIdUsuario() != -1;
    }

    public void cerrarSesion() {
        preferences.edit().clear().apply();
    }
}

