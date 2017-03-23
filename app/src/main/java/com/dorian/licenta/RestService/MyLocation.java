package com.dorian.licenta.RestService;

import com.dorian.licenta.UtilsLocations;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyLocation {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("ziSaptamana")
    @Expose
    private String ziSaptamana;
    @SerializedName("luna")
    @Expose
    private int luna;
    @SerializedName("zi")
    @Expose
    private int zi;
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

    public String getZiSaptamana() {
        return ziSaptamana;
    }

    public void setZiSaptamana(String ziSaptamana) {
        this.ziSaptamana = ziSaptamana;
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

    public MyLocation(int id, String ziSaptamana, int luna, int zi, String oraInceput, String oraSfarsit, double lat, double lgn) {
        this.id = id;
        this.ziSaptamana = ziSaptamana;
        this.luna = luna;
        this.zi = zi;
        this.oraInceput = oraInceput;
        this.oraSfarsit = oraSfarsit;
        this.lat = lat;
        this.lgn = lgn;
    }

    public MyLocation(String ziSaptamana, int luna, int zi, String oraInceput, String oraSfarsit, double lat, double lgn) {
        this.ziSaptamana = ziSaptamana;
        this.luna = luna;
        this.zi = zi;
        this.oraInceput = oraInceput;
        this.oraSfarsit = oraSfarsit;
        this.lat = lat;
        this.lgn = lgn;
    }

    @Override
    public String toString() {
        return "MyLocation{" +
                "id=" + id +
                ", ziSaptamana='" + ziSaptamana + '\'' +
                ", luna='" + luna + '\'' +
                ", zi=" + zi +
                ", oraInceput='" + oraInceput + '\'' +
                ", oraSfarsit='" + oraSfarsit + '\'' +
                ", lat=" + lat +
                ", lgn=" + lgn +
                '}';
    }

    /*
    @Override
    public double distanceBetween2Locations(MyLocation location1) {
        final int R = 6371;
        double distanceLongitude = Math.toRadians(location1.getLgn() - this.getLgn());
        double distanceLatitude = Math.toRadians(location1.getLat() - this.getLat());
        double a = Math.pow((Math.sin(distanceLatitude / 2)), 2)
                + Math.cos(Math.toRadians(this.getLat()))
                * Math.cos(Math.toRadians(location1.getLat()))
                * Math.pow((Math.sin(distanceLongitude / 2)), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }*/
}