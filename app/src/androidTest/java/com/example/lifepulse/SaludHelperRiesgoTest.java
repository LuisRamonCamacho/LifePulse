package com.example.lifepulse;

import android.database.MatrixCursor;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SaludHelperRiesgoTest {

    private MatrixCursor crearCursor() {
        String[] columnas = {
                "id",
                "id_usuario",
                "sistolica",
                "diastolica",
                "pulso",
                "fecha",
                "estado",
                "recomendacion"
        };

        return new MatrixCursor(columnas);
    }

    @Test
    public void sinElevacionesRecurrentes() {
        MatrixCursor cursor = crearCursor();

        cursor.addRow(new Object[]{
                1, 1, 120, 80, 75, "2026-11-10", "Normal", ""
        });

        String resultado = SaludHelper.analizarRiesgo(cursor);

        assertTrue(resultado.contains(
                "SIN ELEVACIONES RECURRENTES DETECTADAS"
        ));

        cursor.close();
    }

    @Test
    public void seguimientoPreventivo() {
        MatrixCursor cursor = crearCursor();

        cursor.addRow(new Object[]{
                1, 1, 140, 90, 75, "2026-11-10", "Grado 1", ""
        });

        cursor.addRow(new Object[]{
                2, 1, 145, 95, 78, "2026-11-11", "Grado 1", ""
        });

        String resultado = SaludHelper.analizarRiesgo(cursor);

        assertTrue(resultado.contains("SEGUIMIENTO PREVENTIVO"));

        cursor.close();
    }

    @Test
    public void seguimientoPrioritario() {
        MatrixCursor cursor = crearCursor();

        for (int i = 1; i <= 4; i++) {
            cursor.addRow(new Object[]{
                    i, 1, 145, 95, 80, "2026-11-10", "Grado 1", ""
            });
        }

        String resultado = SaludHelper.analizarRiesgo(cursor);

        assertTrue(resultado.contains("SEGUIMIENTO PRIORITARIO"));

        cursor.close();
    }

    @Test
    public void cursorVacio() {
        MatrixCursor cursor = crearCursor();

        String resultado = SaludHelper.analizarRiesgo(cursor);

        assertTrue(resultado.contains("SIN DATOS"));

        cursor.close();
    }
}

