package com.example.tdc;

import android.os.Parcel;
import android.os.Parcelable;

public class Aviso implements Parcelable {
    private String descripcion;
    private String fecha;
    private boolean cobrado;
    private boolean pendiente;

    public Aviso(String descripcion, String fecha, boolean cobrado) {
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.cobrado = cobrado;
        this.pendiente = !cobrado;
    }

    // Getter y Setter
    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public boolean isCobrado() {
        return cobrado;
    }

    public void setCobrado(boolean cobrado) {
        this.cobrado = cobrado;
        this.pendiente = !cobrado;
    }

    public boolean isPendiente() {
        return pendiente;
    }

    // Métodos requeridos por Parcelable
    protected Aviso(Parcel in) {
        descripcion = in.readString();
        fecha = in.readString();
        cobrado = in.readByte() != 0; // true si es 1, false si es 0
        pendiente = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(descripcion);
        dest.writeString(fecha);
        dest.writeByte((byte) (cobrado ? 1 : 0));
        dest.writeByte((byte) (pendiente ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Aviso> CREATOR = new Creator<Aviso>() {
        @Override
        public Aviso createFromParcel(Parcel in) {
            return new Aviso(in);
        }

        @Override
        public Aviso[] newArray(int size) {
            return new Aviso[size];
        }
    };
}
