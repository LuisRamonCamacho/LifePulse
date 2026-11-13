package com.example.lifepulse;

public class Registro {

    private int id;
    private int sistolica;
    private int diastolica;
    private int pulso;

    private String fecha;
    private String estado;
    private String recomendacion;

    public Registro(
            int id,
            int sistolica,
            int diastolica,
            int pulso,
            String fecha,
            String estado,
            String recomendacion
    ) {

        this.id = id;
        this.sistolica = sistolica;
        this.diastolica = diastolica;
        this.pulso = pulso;
        this.fecha = fecha;
        this.estado = estado;
        this.recomendacion = recomendacion;
    }

    public int getId() {
        return id;
    }

    public int getSistolica() {
        return sistolica;
    }

    public int getDiastolica() {
        return diastolica;
    }

    public int getPulso() {
        return pulso;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public String getRecomendacion() {
        return recomendacion;
    }
}
