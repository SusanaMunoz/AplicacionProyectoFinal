package com.example.tdc;

import java.util.Date;

public class TrabajoEspecial {
    private String id;
    private String descripcion;
    private String fecha;
    private boolean cobrado;
    private double cantidad;

    public TrabajoEspecial(String id, String descripcion, String fecha, boolean cobrado, double cantidad) {
        this.id = id;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.cobrado = cobrado;
        this.cantidad = cantidad;
    }

    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public boolean isCobrado() {
        return cobrado;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCobrado(boolean cobrado) {
        this.cobrado = cobrado;
    }
}
