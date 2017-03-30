package com.dorian.licenta.Service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Dorian on 29/03/2017.
 */

public class LocationService extends Service implements LocationListener {
    private LocationManager locationManager;
    private String provider;
    private GoogleApiClient googleApiClient;

    public LocationService(Context applicationContext) {
        super();
        Log.wtf("HERE", "constructor");
    }

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.wtf("Service:", " onStartCommand");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_STICKY;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            Log.wtf("locatie ", "gasita");
        } else {
            Log.wtf("locatie ", "negasita");
        }

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
        Intent incomingSms = new Intent(getApplicationContext(), LocationService.class);
        sendBroadcast(incomingSms);
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
                if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                try {
                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();
                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    new MyLocationHelper(new MyLocation(date.getDay(), date.getMonth() + 1, dayOfMonth, date.getHours() + ":" + date.getMinutes(),
                            "", location.getLatitude(), location.getLongitude()))
                            .insertLocation();
                    Log.wtf("locatie ", "insereaza");
                } catch (Exception e) {
                    Log.wtf("locatie", e.getMessage());
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
