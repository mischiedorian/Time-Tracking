package com.dorian.licenta.RestService;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyLocation {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("ora")
    @Expose
    private String ora;
    @SerializedName("lat")
    @Expose
    private double lat;
    @SerializedName("lgn")
    @Expose
    private double lgn;

    public String getOra() {
        return ora;
    }

    public void setOra(String ora) {
        this.ora = ora;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MyLocation(int id, String ora, double lat, double lgn) {
        this.id = id;
        this.ora = ora;
        this.lat = lat;
        this.lgn = lgn;
    }

    public MyLocation(String ora, double lat, double lgn) {
        this.ora = ora;
        this.lat = lat;
        this.lgn = lgn;
    }

    public MyLocation() {
    }

    @Override
    public String toString() {
        return "MyLocation{" +
                "id=" + id +
                ", ora='" + ora + '\'' +
                ", lat=" + lat +
                ", lgn=" + lgn +
                '}';
    }
}