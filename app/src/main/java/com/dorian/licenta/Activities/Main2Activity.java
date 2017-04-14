package com.dorian.licenta.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dorian.licenta.FragmentsMenu.FragmentStart;
import com.dorian.licenta.FragmentsMenu.FragmentTrips;
import com.dorian.licenta.GetMaxFreqLocation;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.NearbyPlace.DataParser;
import com.dorian.licenta.NearbyPlace.GetNearbyPlacesData;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.dorian.licenta.ServiceLocation.LocationService;
import com.dorian.licenta.ServiceNotification.ServiceNotification;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int MAKE_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ArrayList<MyLocation> locatiiDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentStart()).commit();
        //getSupportFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentStart()).commitNow();

        if (isNetworkAvailable() == true) {
            if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) && checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                startService();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        MAKE_LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else
            Toast.makeText(getApplicationContext(), "Acces retea indisponibil", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            RestServices.Factory.getIstance().getLocations().enqueue(new Callback<List<MyLocation>>() {
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkLocations(ArrayList<MyLocation> locatiiDate) {
        for (int i = 0; i < locatiiDate.size(); i++) {
            MyLocationHelper referinta = new MyLocationHelper(locatiiDate.get(i));
            if (referinta.minutesLocation() < 11) {
                referinta.deleteLocation();
            }
            Log.i("ceva", referinta.minutesLocation() + "");
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_main:
                getFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentStart()).commit();
                // getSupportFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentStart()).commitNow();
                break;
            case R.id.nav_your_trips:
                //getSupportFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentTrips()).commitNow();
                getFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentTrips()).commit();
                break;
            case R.id.nav_share:
                GetMaxFreqLocation task = new GetMaxFreqLocation() {
                    @Override
                    protected void onPostExecute(MyLocation myLocation) {
                        showNotification(new LatLng(myLocation.getLat(), myLocation.getLgn()));
                    }
                };
                task.execute();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startService() {
        Intent itn = new Intent(getApplicationContext(), new LocationService(getApplicationContext()).getClass());
        if (!isMyServiceRunning(new LocationService(getApplicationContext()).getClass())) {
            startService(itn);
            Log.i("start", "servicul a inceput");
        }

        Intent intent = new Intent(getApplicationContext(), new ServiceNotification().getClass());
        startService(intent);
        Log.i("start", "pentru notificari a inceput");
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    private boolean checkPermission(String accessCoarseLocation) {
        return ContextCompat.checkSelfPermission(this, accessCoarseLocation) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MAKE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startService();
                }
                return;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNotification(LatLng latLng) {
        String url = getUrl(latLng.latitude, latLng.longitude, "restaurant");
        Object[] DataTransfer = new Object[2];
        DataTransfer[0] = null;
        DataTransfer[1] = url;
        final String[] placeName = new String[1];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData() {
            @Override
            protected void onPostExecute(String result) {
                List<HashMap<String, String>> nearbyPlacesList = null;
                DataParser dataParser = new DataParser();
                nearbyPlacesList = dataParser.parse(result);
                Log.d("onPostExecute", "Entered into showing locations");
                HashMap<String, String> googlePlace = nearbyPlacesList.get(0);
                placeName[0] = googlePlace.get("place_name");
                Log.wtf("place name", placeName[0]);

                Intent intent = new Intent(getApplicationContext(), ResponseNotificationActivity.class);
                intent.putExtra("loc", placeName[0]);
                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());
                if (googlePlace.size() != 0) {
                    b.setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.drawable.ic_menu_send)
                            .setTicker("notificare")
                            .setContentTitle("Doresti o masa buna? Te invitam la")
                            .setContentText(placeName[0])
                            .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                            .setContentIntent(contentIntent)
                            .setContentInfo("Info");
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, b.build());
                }
            }
        };
        getNearbyPlacesData.execute(DataTransfer);
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + 3000);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }
}