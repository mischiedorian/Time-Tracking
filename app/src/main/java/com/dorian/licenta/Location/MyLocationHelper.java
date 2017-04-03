package com.dorian.licenta.Location;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.dorian.licenta.RestService.RestService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dorian on 24/03/2017.
 */

public class MyLocationHelper implements UtilsLocations {
    private MyLocation location;
    private ArrayList<MyLocation> locations;
    private HashMap<MyLocation, Integer> frequencyLocations;
    private ArrayList<MyLocation> uniqLocations;

    public MyLocation getLocation() {
        return location;
    }

    public MyLocationHelper() {
    }

    public MyLocationHelper(MyLocation location) {
        this.location = location;
    }

    @Override
    public double distanceBetween2Locations(MyLocation location1) {
        final int R = 6371;
        double distanceLongitude = Math.toRadians(location1.getLgn() - this.location.getLgn());
        double distanceLatitude = Math.toRadians(location1.getLat() - this.location.getLat());
        double a = Math.pow((Math.sin(distanceLatitude / 2)), 2)
                + Math.cos(Math.toRadians(this.location.getLat()))
                * Math.cos(Math.toRadians(location1.getLat()))
                * Math.pow((Math.sin(distanceLongitude / 2)), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public void deleteLocation() {
        RestService.Factory.getIstance().deleteLocation(this.location.getId() + "").enqueue(new Callback<MyLocation>() {
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
        RestService.Factory.getIstance().modifyLocation(this.location.getId() + "", this.location).enqueue(new Callback<MyLocation>() {
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
        RestService.Factory.getIstance().getLocAccess(this.location).enqueue(new Callback<MyLocation>() {
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
        Log.i("detaliiIII", hourStart + ":" + minutesStart + "-----" + hourFinish + ":" + minutesFinish);
        if (minutesStart < minutesFinish) {
            minutes += minutesFinish - minutesStart;
        } else if (minutesStart > minutesFinish) {
            minutes += 60 - minutesStart + minutesFinish;
        }
        return minutes + (hourFinish - hourStart) * 60;
    }

    @Override
    public void checkLocations() {
        locations = new ArrayList<>();
        locations = getAllLocation();
       /* for (int i = 0; i < locations.size() - 1; i++) {
            for (int j = 1; j < locations.size(); i++) {
                if (areInTheSamePlace(locations.get(i), locations.get(j)) && arrayContainsLocation(locations.get(i)) == false) {
                    uniqLocations.add(locations.get(i));
                }
            }
        }
        int nr = 0;
        for (MyLocation loc : uniqLocations) {
            nr = 0;
            for (MyLocation loc1 : locations) {
                if (areInTheSamePlace(loc, loc1)) {
                    nr++;
                }
            }
            frequencyLocations.put(loc, nr);
            Log.wtf("locations freqeuncy", loc.toString());
        }

        MyLocation maxFrequenci = getLocationWithFrequenciMax();*/
        //  Log.i("MAX FREQUNCI", maxFrequenci.toString());
    }

    @Override
    public ArrayList<MyLocation> getAllLocation() {
        RestService.Factory.getIstance().getLocations().enqueue(new Callback<List<MyLocation>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                locations = new ArrayList<MyLocation>();
                uniqLocations = new ArrayList<>();
                frequencyLocations = new HashMap<>();
                for (MyLocation location : response.body()) {
                    locations.add(new MyLocation(location.getId(), location.getZi(), location.getLuna(), location.getZiDinLuna(), location.getOraInceput(), location.getOraSfarsit(), location.getLat(), location.getLgn()));
                }
                for (int i = 0; i < locations.size() - 1; i++) {
                    for (int j = 1; j < locations.size(); j++) {
                        if (areInTheSamePlace(locations.get(i), locations.get(j)) && arrayContainsLocation(locations.get(i)) == false) {
                            uniqLocations.add(locations.get(i));
                        }
                    }
                }
                int nr = 0;
                for (MyLocation loc : uniqLocations) {
                    nr = 0;
                    for (MyLocation loc1 : locations) {
                        if (areInTheSamePlace(loc, loc1)) {
                            nr++;
                        }
                    }
                    frequencyLocations.put(loc, nr);
                    Log.wtf("locations freqeuncy", loc.toString() + "-" + nr);
                }

                MyLocation maxFrequenci = getLocationWithFrequenciMax();
                Log.wtf("locations get all return", maxFrequenci.toString());
            }

            @Override
            public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                Log.i("Failed", t.getMessage());
            }
        });
        return locations;
    }


    private boolean areInTheSamePlace(MyLocation location, MyLocation location1) {
        if (new MyLocationHelper(location).distanceBetween2Locations(location1) < 0.2) return true;
        return false;
    }

    private boolean arrayContainsLocation(MyLocation location) {
        for (int i = 0; i < uniqLocations.size(); i++) {
            if (new MyLocationHelper(uniqLocations.get(i)).distanceBetween2Locations(location) < 0.2)
                return true;
        }
        return false;
    }

    private MyLocation getLocationWithFrequenciMax() {
        int max = 0;
        MyLocation location = null;
        for (Map.Entry<MyLocation, Integer> e : frequencyLocations.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                location = e.getKey();
            }
        }
        return location;
    }
}
