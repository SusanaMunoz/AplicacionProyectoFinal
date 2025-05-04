package com.example.tdc;

public class Orden {
    private int id; // Nuevo campo para el ID
    private String ordenTrabajo;
    private String codigo;
    private double puntos;
    private String fecha;

    public Orden(int id, String ordenTrabajo, String codigo, double puntos, String fecha) {
        this.id = id;
        this.ordenTrabajo = ordenTrabajo;
        this.codigo = codigo;
        this.puntos = puntos;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public String getOrdenTrabajo() {
        return ordenTrabajo;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getPuntos() {
        return puntos;
    }

    public String getFecha() {
        return fecha;
    }
}