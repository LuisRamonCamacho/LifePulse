
package com.example.lifepulse;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DBHelperRendimientoTest {

    @Test
    public void pruebaRendimientoCon500Registros() {

        Context context = ApplicationProvider.getApplicationContext();

        String nombreBD = "lifepulse_rendimiento_test.db";

        // Eliminar solamente una posible base temporal anterior.
        context.deleteDatabase(nombreBD);

        DBHelper dbHelper = new DBHelper(context, nombreBD);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long inicioInsercion = System.nanoTime();

        db.beginTransaction();

        try {
            for (int i = 1; i <= 500; i++) {

                db.execSQL(
                        "INSERT INTO usuarios " +
                                "(nombre, correo, password_hash) " +
                                "VALUES (?, ?, ?)",
                        new Object[]{
                                "Usuario Prueba",
                                "rendimiento" + i + "@test.com",
                                "hash_prueba"
                        }
                );

                db.execSQL(
                        "INSERT INTO registros " +
                                "(id_usuario, sistolica, diastolica, pulso, fecha, estado, recomendacion) " +
                                "VALUES (1, ?, ?, ?, ?, ?, ?)",
                        new Object[]{
                                110 + (i % 40),
                                70 + (i % 20),
                                70 + (i % 30),
                                "2026-11-12",
                                "Normal",
                                "Registro de prueba"
                        }
                );
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        long finInsercion = System.nanoTime();

        long inicioConsulta = System.nanoTime();

        int cantidad = 0;

        try (Cursor cursor = dbHelper.obtenerTodos(1)) {

            while (cursor.moveToNext()) {
                cantidad++;
            }
        }

        long finConsulta = System.nanoTime();

        long tiempoInsercion =
                (finInsercion - inicioInsercion) / 1_000_000;

        long tiempoConsulta =
                (finConsulta - inicioConsulta) / 1_000_000;

        android.util.Log.i(
                "LIFEPULSE_RENDIMIENTO",
                "Registros recuperados: " + cantidad
        );

        android.util.Log.i(
                "LIFEPULSE_RENDIMIENTO",
                "Tiempo de inserción: " + tiempoInsercion + " ms"
        );

        android.util.Log.i(
                "LIFEPULSE_RENDIMIENTO",
                "Tiempo de consulta: " + tiempoConsulta + " ms"
        );

        assertEquals(500, cantidad);

        assertTrue(
                "La consulta superó los 2 segundos",
                tiempoConsulta < 2000
        );

        dbHelper.close();

        context.deleteDatabase(nombreBD);
    }
}

