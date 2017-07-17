package com.dorian.licenta.ServiceLocation;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service implements LocationListener {
    private GoogleApiClient googleApiClient;
    private SharedPreferences sharedPreferences;
    private int idUser;

    private static LatLng home = new LatLng(44.444111, 26.004878);

    public LocationService(Context applicationContext) {
        super();
        Log.wtf("HERE", "constructor");
    }

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.wtf("Service:", " onStartCommand location");

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
        sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);

        Intent locationIntent = new Intent(getApplicationContext(),
                LocationService.class);
        sendBroadcast(locationIntent);

        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.wtf("Service: ", "Destroyed");
        super.onDestroy();
        sendBroadcast(new Intent("ReloadIt"));
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        catchLocations();
        timer.schedule(timerTask, 1000, 10000);
    }

    public void catchLocations() {
        timerTask = new TimerTask() {
            public void run() {
                if (ActivityCompat.checkSelfPermission(LocationService.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(LocationService.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (new MyLocationHelper(new MyLocation(0, 0, 0, null, null, location.getLatitude(), location.getLongitude(), 0)).distanceBetween2Locations(
                        new MyLocation(0, 0, 0, null, null, home.latitude, home.longitude, 0)) > 0.5) {
                    try {
                        idUser = sharedPreferences.getInt("idUser", 0);
                        Calendar calendar = Calendar.getInstance();
                        Date date = calendar.getTime();

                        int minutes = date.getMinutes();
                        if (minutes < 10) {
                            minutes = Integer.parseInt("0" + minutes);
                        }
                        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                        new MyLocationHelper(new MyLocation(date.getDay(), date.getMonth() + 1,
                                dayOfMonth, date.getHours() + ":" + minutes, date.getHours() + ":" + minutes,
                                location.getLatitude(), location.getLongitude(), idUser))
                                .insertLocation();
                        Log.wtf("locatie ", "insereaza");
                    } catch (Exception e) {
                        Log.wtf("locatie", e.getMessage());
                    }
                }
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
