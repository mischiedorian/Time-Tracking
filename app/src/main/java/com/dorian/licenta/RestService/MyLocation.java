package com.dorian.licenta.RestService;

import com.dorian.licenta.UtilsLocations;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyLocation implements UtilsLocations {

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
    }
}