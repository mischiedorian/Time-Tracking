package com.dorian.licenta.ServiceLocation;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.Product.Product;
import com.dorian.licenta.ResponseActivityOnMap;
import com.dorian.licenta.RestServices.RestServices;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends Service implements LocationListener {
    private GoogleApiClient googleApiClient;
    private SharedPreferences sharedPreferences;
    private int idUser;

    private static LatLng home = new LatLng(44.444111, 26.004878);

    private static ArrayList<LatLng> locationsProduct;
    private static int max = 10;
    private static int min = 1;

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

        getLocationProduct();

        Intent locationIntent = new Intent(getApplicationContext(),
                LocationService.class);
        sendBroadcast(locationIntent);

        SharedPreferences.Editor editor = getSharedPreferences("id", MODE_PRIVATE).edit();
        editor.putInt("day", Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        editor.apply();

        startTimer();
        return START_STICKY;
    }

    private void getLocationProduct() {
        locationsProduct = new ArrayList<>();

        idUser = sharedPreferences.getInt("idUser", 0);

        RestServices
                .Factory
                .getIstance()
                .getProductsAfterUser(idUser)
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        for (Product product : response.body()) {

                            RestServices
                                    .Factory
                                    .getIstance()
                                    .getLocationId(product.getIdLocatie())
                                    .enqueue(new Callback<MyLocation>() {
                                        @Override
                                        public void onResponse(Call<MyLocation> call, Response<MyLocation> response) {
                                            locationsProduct.add(new LatLng(response.body().getLat(), response.body().getLgn()));
                                        }

                                        @Override
                                        public void onFailure(Call<MyLocation> call, Throwable t) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {

                    }
                });
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

                SharedPreferences sh = getSharedPreferences("id", MODE_PRIVATE);
                boolean isNotification = sh.getBoolean("notification", false);

                if (!isNotification) {
                    for (LatLng latLng : locationsProduct) {
                        if (distanceBetween2Locations(latLng, new LatLng(location.getLatitude(), location.getLongitude())) < 0.5) {
                            showNotification(latLng);
                            break;
                        }
                    }
                }

                int day = sharedPreferences.getInt("day", 0);
                int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                if (day != currentDay) {
                    SharedPreferences.Editor editor = getSharedPreferences("id", MODE_PRIVATE).edit();
                    editor.putInt("day", Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                    editor.apply();

                    SharedPreferences.Editor editor2 = getSharedPreferences("service", MODE_PRIVATE).edit();
                    editor2.putBoolean("notification", true);
                    editor2.apply();
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

    public double distanceBetween2Locations(LatLng location1, LatLng location2) {
        final int R = 6371;
        double distanceLongitude = Math.toRadians(location2.longitude
                - location1.longitude);
        double distanceLatitude = Math.toRadians(location2.latitude
                - location1.latitude);
        double a = Math.pow((Math.sin(distanceLatitude / 2)), 2)
                + Math.cos(Math.toRadians(location1.latitude))
                * Math.cos(Math.toRadians(location2.longitude))
                * Math.pow((Math.sin(distanceLongitude / 2)), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void showNotification(LatLng latLng) {
        SharedPreferences.Editor editor = getSharedPreferences("id", MODE_PRIVATE).edit();
        editor.putBoolean("notification", true);
        editor.apply();

        String voucher = generateVoucher();

        Intent intent = new Intent(getApplicationContext(), ResponseActivityOnMap.class);
        intent.putExtra("latitude", latLng.latitude);
        intent.putExtra("longitude", latLng.longitude);
        intent.putExtra("voucher", voucher);


        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(android.R.drawable.ic_menu_send)
                .setTicker("notificare")
                .setContentTitle("Doar acum avem ceva pentru tine")
                .setContentText(voucher)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }

    private String generateVoucher() {
        String start = "10%";

        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            start += random.nextInt(max - min + 1) + min;
        }

        return start;
    }
}
