package com.example.tdc;

import java.util.Date;

public class OrdenTrabajo {
    private int id;
    private int codigoId;
    private Date fecha;

    // Constructor
    public OrdenTrabajo(int id, int codigoId, Date fecha) {
        this.id = id;
        this.codigoId = codigoId;
        this.fecha = fecha;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCodigoId() {
        return codigoId;
    }

    public void setCodigoId(int codigoId) {
        this.codigoId = codigoId;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "OrdenTrabajo{" +
                "id=" + id +
                ", codigoId=" + codigoId +
                ", fecha=" + fecha +
                '}';
    }
}