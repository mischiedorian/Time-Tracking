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
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.dorian.licenta.RestService.MyLocation;
import com.dorian.licenta.RestService.RestService;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by doriancristian on 17.02.2017.
 */

public class RSSPullService extends IntentService implements LocationListener {

    LocationManager locationManager;
    String provider;

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
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this); //la fiecare 0.4 secunde sau 1 metru

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

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                if (ActivityCompat.checkSelfPermission(RSSPullService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RSSPullService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                Location location = locationManager.getLastKnownLocation(provider);
                // onLocationChanged(location);

                try {
                    MyLocation locatie = new MyLocation(Calendar.getInstance().getTime().toString(), location.getLatitude(), location.getLongitude());

                    RestService.Factory.getIstance().getLocAccess(locatie).enqueue(new Callback<MyLocation>() {
                        @Override
                        public void onResponse(Call<MyLocation> call, Response<MyLocation> response) {
                            Log.i("raspuns", response.code() + "");
                        }

                        @Override
                        public void onFailure(Call<MyLocation> call, Throwable t) {
                        }
                    });

                    Log.i("service", locatie.toString());
                } catch (Exception e) {
                    Log.e("locatie", e.getMessage());
                }
                locationManager.removeUpdates(RSSPullService.this);
                onHandleIntent(intent);
            }
        }, 10000);


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
