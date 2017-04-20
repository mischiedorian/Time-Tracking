package com.dorian.licenta.Location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class History {
    @SerializedName("loc")
    @Expose
    private String loc;
    @SerializedName("produs")
    @Expose
    private String produs;
    @SerializedName("ora")
    @Expose
    private String ora;

    public History(String loc, String produs, String ora) {
        this.loc = loc;
        this.produs = produs;
        this.ora = ora;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getProdus() {
        return produs;
    }

    public void setProdus(String produs) {
        this.produs = produs;
    }

    public String getOra() {
        return ora;
    }

    public void setOra(String ora) {
        this.ora = ora;
    }

    @Override
    public String toString() {
        return "History{" +
                "loc='" + loc + '\'' +
                ", produs='" + produs + '\'' +
                ", ora='" + ora + '\'' +
                '}';
    }
}
