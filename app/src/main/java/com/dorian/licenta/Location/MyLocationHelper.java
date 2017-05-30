package com.dorian.licenta.Location;

import android.util.Log;

import com.dorian.licenta.RestServices.RestServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyLocationHelper implements UtilsLocations {
    private MyLocation location;

    public MyLocation getLocation() {
        return location;
    }

    public MyLocationHelper(MyLocation location) {
        this.location = location;
    }

    @Override
    public double distanceBetween2Locations(MyLocation location1) {
        final int R = 6371;
        double distanceLongitude = Math.toRadians(location1.getLgn()
                - this.location.getLgn());
        double distanceLatitude = Math.toRadians(location1.getLat()
                - this.location.getLat());
        double a = Math.pow((Math.sin(distanceLatitude / 2)), 2)
                + Math.cos(Math.toRadians(this.location.getLat()))
                * Math.cos(Math.toRadians(location1.getLat()))
                * Math.pow((Math.sin(distanceLongitude / 2)), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public void deleteLocation() {
        RestServices.Factory.getIstance().deleteLocation(this.location.getId() + "").enqueue(new Callback<MyLocation>() {
            @Override
            public void onResponse(Call<MyLocation> call, Response<MyLocation> response) {
            }

            @Override
            public void onFailure(Call<MyLocation> call, Throwable t) {
            }
        });
    }

    @Override
    public void updateLocation(String oraSfarsit) {
        this.location.setOraSfarsit(oraSfarsit);
        RestServices.Factory.getIstance().modifyLocation(this.location.getId() + "", this.location).enqueue(new Callback<MyLocation>() {
            @Override
            public void onResponse(Call<MyLocation> call, Response<MyLocation> response) {
            }

            @Override
            public void onFailure(Call<MyLocation> call, Throwable t) {
            }
        });
    }

    @Override
    public void insertLocation() {
        RestServices.Factory.getIstance().postLocAccess(this.location).enqueue(new Callback<MyLocation>() {
            @Override
            public void onResponse(Call<MyLocation> call, Response<MyLocation> response) {
                Log.i("raspuns", response.code() + "");
            }

            @Override
            public void onFailure(Call<MyLocation> call, Throwable t) {
            }
        });
    }

    @Override
    public int minutesLocation() {
        int minutes = 0;
        int hourStart = Integer.parseInt(this.location.getOraInceput().split(":")[0]);
        int minutesStart = Integer.parseInt(this.location.getOraInceput().split(":")[1]);
        int hourFinish = Integer.parseInt(this.location.getOraSfarsit().split(":")[0]);
        int minutesFinish = Integer.parseInt(this.location.getOraSfarsit().split(":")[1]);
        Log.i("detalii", hourStart + ":" + minutesStart + "-----" + hourFinish + ":" + minutesFinish);
        if (minutesStart < minutesFinish) {
            minutes += minutesFinish - minutesStart;
        } else if (minutesStart > minutesFinish) {
            minutes += 60 - minutesStart + minutesFinish;
        }
        return minutes + (hourFinish - hourStart) * 60;
    }
}
