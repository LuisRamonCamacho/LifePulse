package com.example.lifepulse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SaludHelperTest {

    @Test
    public void presionOptima() {
        assertEquals("Óptima",
                SaludHelper.obtenerEstado(110, 70));
    }

    @Test
    public void presionNormal() {
        assertEquals("Normal",
                SaludHelper.obtenerEstado(120, 80));
    }

    @Test
    public void presionFronteriza() {
        assertEquals("Presión fronteriza",
                SaludHelper.obtenerEstado(130, 85));
    }

    @Test
    public void hipertensionGrado1() {
        assertEquals("Hipertensión Grado 1",
                SaludHelper.obtenerEstado(140, 90));
    }

    @Test
    public void hipertensionGrado2() {
        assertEquals("Hipertensión Grado 2",
                SaludHelper.obtenerEstado(160, 100));
    }

    @Test
    public void hipertensionGrado3() {
        assertEquals("Hipertensión Grado 3",
                SaludHelper.obtenerEstado(180, 110));
    }

    @Test
    public void recomendacionPresionElevada() {
        String resultado =
                SaludHelper.obtenerRecomendacion(145, 95);

        assertTrue(resultado.contains("valoración médica"));
    }

    @Test
    public void alertaMedicaValoresMuyElevados() {
        String resultado =
                SaludHelper.alertaMedica(180, 120);

        assertTrue(resultado.contains("ALERTA MÉDICA"));
    }

    @Test
    public void sinAlertaEnPresionNormal() {
        assertEquals("",
                SaludHelper.alertaMedica(115, 75));
    }
}


