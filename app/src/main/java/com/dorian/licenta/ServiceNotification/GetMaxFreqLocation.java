package com.dorian.licenta.ServiceNotification;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.RestServices.RestServices;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetMaxFreqLocation extends AsyncTask<Integer, Void, MyLocation> {
    private ArrayList<MyLocation> locations;
    private HashMap<MyLocation, Integer> frequencyLocations;
    private ArrayList<MyLocation> uniqLocations;
    MyLocation maxFrequenci;

    @Override
    protected MyLocation doInBackground(Integer... params) {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();

        int day;
        if (date.getDay() == 6) {
            day = 0;
        } else {
            day = date.getDay() + 1;
        }

        Log.wtf("day", day + "");

        RestServices.Factory.getIstance().getLocationsAferDay(2, params[0]).enqueue(new Callback<List<MyLocation>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                locations = new ArrayList<>();
                uniqLocations = new ArrayList<>();
                frequencyLocations = new HashMap<>();
                locations.addAll(response.body().stream().map(location -> new MyLocation(location.getId(), location.getZi(), location.getLuna(),
                        location.getZiDinLuna(), location.getOraInceput(), location.getOraSfarsit(),
                        location.getLat(), location.getLgn(), params[0])).collect(Collectors.toList()));

                for (int i = 0; i < locations.size() - 1; i++) {
                    for (int j = 1; j < locations.size(); j++) {
                        if (areInTheSamePlace(locations.get(i), locations.get(j)) && !arrayContainsLocation(locations.get(i))) {
                            uniqLocations.add(locations.get(i));
                        }
                    }
                }

                int nr;
                for (MyLocation loc : uniqLocations) {
                    nr = 0;
                    for (MyLocation loc1 : locations) {
                        if (areInTheSamePlace(loc, loc1) && new MyLocationHelper(loc).minutesLocation() != 0 && new MyLocationHelper(loc1).minutesLocation() != 0) {
                            nr++;
                        }
                    }
                    frequencyLocations.put(loc, nr);
                    Log.wtf("locations freqeuncy", loc.toString() + "-" + nr);
                }

                maxFrequenci = getLocationWithFrequenciMax();
                Log.wtf("locations get all return", maxFrequenci.toString());
            }

            @Override
            public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                Log.i("Failed", t.getMessage());
            }
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return maxFrequenci;
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
