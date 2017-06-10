package com.dorian.licenta.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dorian.licenta.Authentication.User;
import com.dorian.licenta.FragmentsMenu.FragmentStart;
import com.dorian.licenta.FragmentsMenu.FragmentTrips;
import com.dorian.licenta.FragmentsTrip.FragmentMap;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.dorian.licenta.ServiceLocation.LocationService;
import com.dorian.licenta.ServiceNotification.ServiceNotification;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener {
    private static final int MAKE_LOCATION_PERMISSION_REQUEST_CODE = 1;

    public static GoogleApiClient googleApiClient;
    private String name;
    private String email;
    private String img_url;
    private int idUser;
    private TextView userName;
    private TextView userEmail;
    private ImageView userPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);

        userName = (TextView) hView.findViewById(R.id.textViewUsrName);
        userPic = (ImageView) hView.findViewById(R.id.imageViewUser);
        userEmail = (TextView) hView.findViewById(R.id.textViewUserEmail);

        Bundle bundle = getIntent().getExtras();
        idUser = bundle.getInt("userId");

        SharedPreferences.Editor editor = getSharedPreferences("id", MODE_PRIVATE).edit();
        editor.putInt("idUser", idUser);
        editor.apply();

        RestServices.Factory.getIstance().getUserAfterId(idUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                email = response.body().getEmail();
                name = response.body().getName();
                img_url = response.body().getImageUrl();

                userName.setText(name);
                userEmail.setText(email);
                if (img_url != null) {
                    Glide.with(getApplicationContext()).load(img_url).into(userPic);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(LocationServices.API).build();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

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
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
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
            case R.id.nav_locations:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }
                try {
                    Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    getFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentMap(new LatLng(location.getLatitude(), location.getLongitude()))).commit();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Locatie indisponibila!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_log_out:
                SharedPreferences.Editor editor = getSharedPreferences("id", MODE_PRIVATE).edit();
                editor.putInt("idUser", 0);
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startService() {
        Intent itn = new Intent(getApplicationContext(), LocationService.class);
        if (!isMyServiceRunning(LocationService.class)) {
            startService(itn);
            Log.i("start", "servicul a inceput");
        }

        Intent intent = new Intent(getApplicationContext(), ServiceNotification.class);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}