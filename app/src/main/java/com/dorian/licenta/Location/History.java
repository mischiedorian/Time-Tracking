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
    @SerializedName("data")
    @Expose
    private String data;

    public History(String loc, String produs, String ora, String data) {
        this.loc = loc;
        this.produs = produs;
        this.ora = ora;
        this.data = data;
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
