package com.example.tdc;

public class Codigo {
    private int id;
    private String codigo;
    private double puntos;

    // Constructor
    public Codigo(int id, String codigo, double puntos) {
        this.id = id;
        this.codigo = codigo;
        this.puntos = puntos;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public double getPuntos() {
        return puntos;
    }

    public void setPuntos(double puntos) {
        this.puntos = puntos;
    }

    @Override
    public String toString() {
        return "Codigo{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", puntos=" + puntos +
                '}';
    }
}