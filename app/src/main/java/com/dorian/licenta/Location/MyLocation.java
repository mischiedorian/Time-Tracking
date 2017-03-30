package com.dorian.licenta.Location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyLocation {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("zi")
    @Expose
    private int zi;
    @SerializedName("luna")
    @Expose
    private int luna;
    @SerializedName("ziDinLuna")
    @Expose
    private int ziDinLuna;
    @SerializedName("oraInceput")
    @Expose
    private String oraInceput;
    @SerializedName("oraSfarsit")
    @Expose
    private String oraSfarsit;
    @SerializedName("lat")
    @Expose
    private double lat;
    @SerializedName("lgn")
    @Expose
    private double lgn;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLuna() {
        return luna;
    }

    public void setLuna(int luna) {
        this.luna = luna;
    }

    public int getZi() {
        return zi;
    }

    public void setZi(int zi) {
        this.zi = zi;
    }

    public String getOraInceput() {
        return oraInceput;
    }

    public void setOraInceput(String oraInceput) {
        this.oraInceput = oraInceput;
    }

    public String getOraSfarsit() {
        return oraSfarsit;
    }

    public void setOraSfarsit(String oraSfarsit) {
        this.oraSfarsit = oraSfarsit;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLgn() {
        return lgn;
    }

    public void setLgn(double lgn) {
        this.lgn = lgn;
    }

    public int getZiDinLuna() {
        return ziDinLuna;
    }

    public void setZiDinLuna(int ziDinLuna) {
        this.ziDinLuna = ziDinLuna;
    }

    @Override
    public String toString() {
        return "MyLocation{" +
                "id=" + id +
                ", zi=" + zi +
                ", luna=" + luna +
                ", ziDinLuna=" + ziDinLuna +
                ", oraInceput='" + oraInceput + '\'' +
                ", oraSfarsit='" + oraSfarsit + '\'' +
                ", lat=" + lat +
                ", lgn=" + lgn +
                '}';
    }

    public MyLocation(int id, int zi, int luna, int ziDinLuna, String oraInceput, String oraSfarsit, double lat, double lgn) {
        this.id = id;
        this.luna = luna;
        this.ziDinLuna = ziDinLuna;
        this.zi = zi;
        this.oraInceput = oraInceput;
        this.oraSfarsit = oraSfarsit;
        this.lat = lat;
        this.lgn = lgn;
    }

    public MyLocation(int zi, int luna, int ziDinLuna, String oraInceput, String oraSfarsit, double lat, double lgn) {
        this.luna = luna;
        this.zi = zi;
        this.ziDinLuna = ziDinLuna;
        this.oraInceput = oraInceput;
        this.oraSfarsit = oraSfarsit;
        this.lat = lat;
        this.lgn = lgn;
    }

}