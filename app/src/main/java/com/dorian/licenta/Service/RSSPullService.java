package com.dorian.licenta.Service;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by doriancristian on 17.02.2017.
 */

public class RSSPullService extends IntentService implements LocationListener {

    LocationManager locationManager;
    String provider;
    GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("service", "serviciu s-a creat");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            Log.i("locatie ", "gasita");
        } else {
            Log.i("locatie ", "negasita");
        }

        if(googleApiClient == null){
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // locationManager.requestLocationUpdates(provider, 400, 1, this); //la fiecare 0.4 secunde sau 1 metru

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public RSSPullService() {
        super("");

    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        synchronized (this) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    if (ActivityCompat.checkSelfPermission(RSSPullService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RSSPullService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    // Location location = locationManager.getLastKnownLocation(provider);
                    // onLocationChanged(location);
                    Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                    try {
                        Date date = Calendar.getInstance().getTime();
                        new MyLocationHelper(new MyLocation(date.getDay(), date.getMonth() + 1, date.getHours() + ":" + date.getMinutes(),
                                "", location.getLatitude(), location.getLongitude()))
                                .insertLocation();
                        Log.i("locatie ", "insereaza");
                    } catch (Exception e) {
                        Log.e("locatie", e.getMessage());
                    }
                    locationManager.removeUpdates(RSSPullService.this);
                    onHandleIntent(intent);
                    //startService(intent);
                }
            }, 10000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(),"Service distrus",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Double lat = location.getLatitude();
        Double lng = location.getLongitude();

        Log.i("Latitude: ", lat.toString());
        Log.i("Longitude: ", lng.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}