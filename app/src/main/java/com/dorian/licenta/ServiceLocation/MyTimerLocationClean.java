package com.dorian.licenta.ServiceLocation;

import android.util.Log;

import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.RestServices.RestServices;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by misch on 30.03.2017.
 */

public class MyTimerLocationClean extends TimerTask {
    private final static long oncePerDay = 1000 * 60 * 60 * 24;

    private final static int hour = 11;
    private final static int minutes = 59;

    private ArrayList<MyLocation> locatiiDate;

    @Override
    public void run() {
        Log.wtf("Start Job", "start job");
        RestServices.Factory.getIstance().getLocationsAferMonthAndDay(Calendar.getInstance().getTime().getMonth() + 1 + "", Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "").enqueue(new Callback<List<MyLocation>>() {
            @Override
            public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                locatiiDate = new ArrayList<>();
                for (MyLocation location : response.body()) {
                    locatiiDate.add(location);
                }
                checkLocations(locatiiDate);
            }

            @Override
            public void onFailure(Call<List<MyLocation>> call, Throwable t) {
            }
        });
        Log.wtf("End Job", "end job");

    }

    private static Date getTomorrowMorning() {

        Date date = new java.util.Date();
        date.setHours(hour);
        date.setMinutes(minutes);

        return date;
    }

    //call this method from your servlet init method
    public static void startTask() {
        Log.wtf("timer location clean", "a inceput");
        MyTimerLocationClean task = new MyTimerLocationClean();
        Timer timer = new Timer();
        timer.schedule(task, getTomorrowMorning(), oncePerDay);// for your case u need to give 1000*60*60*24
    }

    private void checkLocations(ArrayList<MyLocation> locatiiDate) {
        MyLocation referinta = locatiiDate.get(0);
        MyLocation local = referinta;
        int nrLocatii = 0;
        int i = 1;
        try {
            do {
                if (new MyLocationHelper(referinta).distanceBetween2Locations(locatiiDate.get(i)) < 0.030) {
                    new MyLocationHelper(locatiiDate.get(i)).deleteLocation();
                    nrLocatii++;
                    local = locatiiDate.get(i);
                } else {
                    if (nrLocatii == 0 && referinta.getOraSfarsit() == null) {
                        new MyLocationHelper(referinta).deleteLocation();
                    } else {
                        new MyLocationHelper(referinta).updateLocation(local.getOraInceput());
                    }
                    nrLocatii = 0;
                    referinta = locatiiDate.get(i);
                    local = referinta;
                }
                i++;
            } while (i < locatiiDate.size());
            if (referinta != local) {
                new MyLocationHelper(referinta).updateLocation(local.getOraInceput());
            }
           /* if(new MyLocationHelper(referinta).minutesLocation(referinta) < 11) {
                new MyLocationHelper(referinta).deleteLocation();
            }*/
        } catch (Exception e) {
            Log.e("checkLocations", e.getMessage());
        }
    }
}
