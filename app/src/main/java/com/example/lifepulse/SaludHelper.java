package com.example.lifepulse;

import android.database.Cursor;

public class SaludHelper {

    public static String obtenerEstado(int sistolica, int diastolica) {

        if (sistolica >= 180 || diastolica >= 110) {
            return "Hipertensión Grado 3";
        }

        if (sistolica >= 160 || diastolica >= 100) {
            return "Hipertensión Grado 2";
        }

        if (sistolica >= 140 || diastolica >= 90) {
            return "Hipertensión Grado 1";
        }

        if (sistolica >= 130 || diastolica >= 85) {
            return "Presión fronteriza";
        }

        if (sistolica >= 120 || diastolica >= 80) {
            return "Normal";
        }

        return "Óptima";
    }

    public static String obtenerRecomendacion(int sistolica, int diastolica) {

        if (sistolica >= 180 || diastolica >= 120) {
            return "La medición alcanza valores muy elevados. " +
                    "Repite la medición después de un minuto. " +
                    "Si continúa elevada, busca atención médica inmediata. " +
                    "Ante síntomas graves, solicita atención de emergencia.";
        }

        if (sistolica >= 140 || diastolica >= 90) {
            return "La medición presenta valores compatibles con hipertensión. " +
                    "Se recomienda una valoración médica para confirmar el resultado.";
        }

        if (sistolica >= 130 || diastolica >= 85) {
            return "La presión se encuentra en un rango fronterizo. " +
                    "Continúa monitoreando y consulta a un profesional de salud.";
        }

        if (sistolica >= 120 || diastolica >= 80) {
            return "La presión se encuentra dentro del rango normal. " +
                    "Continúa con tus hábitos saludables y monitoreando tus valores.";
        }

        return "Presión arterial óptima. " +
                "Continúa manteniendo hábitos saludables.";
    }

    public static String analizarRiesgo(Cursor cursor) {

        if (cursor == null || cursor.getCount() == 0) {
            return "SIN DATOS\n\n" +
                    "Registra mediciones para consultar tu seguimiento.";
        }

        int altos = 0;
        int fronterizos = 0;
        int total = 0;

        if (cursor.moveToFirst()) {
            do {
                int sistolica = cursor.getInt(2);
                int diastolica = cursor.getInt(3);

                total++;

                if (sistolica >= 140 || diastolica >= 90) {
                    altos++;
                } else if (sistolica >= 130 || diastolica >= 85) {
                    fronterizos++;
                }

            } while (cursor.moveToNext());
        }

        if (altos >= 4) {
            return "SEGUIMIENTO PRIORITARIO\n\n" +
                    "Se encontraron " + altos +
                    " mediciones en rangos de hipertensión, de un total de " +
                    total + ".\n" +
                    "Se recomienda una valoración médica.";
        }

        if (altos >= 2 || fronterizos >= 3) {
            return "SEGUIMIENTO PREVENTIVO\n\n" +
                    "Se detectaron varias mediciones elevadas o fronterizas.\n" +
                    "Continúa el monitoreo y consulta a un profesional.";
        }

        return "SIN ELEVACIONES RECURRENTES DETECTADAS\n\n" +
                "No se identificó una recurrencia importante " +
                "en las mediciones registradas.\n" +
                "Continúa monitoreando tu presión arterial.";
    }

    public static String alertaMedica(int sistolica, int diastolica) {

        if (sistolica >= 180 || diastolica >= 120) {
            return "ALERTA MÉDICA\n\n" +
                    "La medición alcanza valores muy elevados. " +
                    "Repite la medición después de un minuto. " +
                    "Si persiste, busca atención médica inmediata. " +
                    "Ante síntomas graves, solicita atención de emergencia.";
        }

        if (sistolica >= 140 || diastolica >= 90) {
            return "PRESIÓN ELEVADA\n\n" +
                    "Se recomienda seguimiento y valoración médica.";
        }

        return "";
    }
}
