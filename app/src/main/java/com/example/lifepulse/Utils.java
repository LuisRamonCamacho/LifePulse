package com.example.lifepulse;

import android.app.AlertDialog;
import android.content.Context;

public class Utils {

    public static void mostrarMensaje(
            Context context,
            String titulo,
            String mensaje
    ){

        new AlertDialog.Builder(context)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton(
                        "Aceptar",
                        null
                )
                .show();
    }
}
