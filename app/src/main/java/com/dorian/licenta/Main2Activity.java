package com.dorian.licenta;

import android.Manifest;
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
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.RestService.RestService;
import com.dorian.licenta.Service.RSSPullService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int MAKE_LOCATION_PERMISSION_REQUEST_CODE = 1;
    ArrayList<MyLocation> locatiiDate;
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            RestService.Factory.getIstance().getLocations().enqueue(new Callback<List<MyLocation>>() {
                @Override
                public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                    locatiiDate = new ArrayList<>();
                    for (MyLocation location : response.body()) {
                        if (location.getZi() == Calendar.getInstance().getTime().getDay()) {
                            locatiiDate.add(location);
                        }
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
        for(int i=1;i<locatiiDate.size();i++){
            MyLocation referinta = locatiiDate.get(i);
            if(new MyLocationHelper(referinta).minutesLocation() < 11){
                Log.i("ceva",new MyLocationHelper(referinta).minutesLocation()+"");
                new MyLocationHelper(referinta).deleteLocation();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.nav_main:
                getFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentStart()).commit();
               // getSupportFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentStart()).commitNow();
                break;
            case R.id.nav_your_trips:
                //getSupportFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentTrips()).commitNow();
                getFragmentManager().beginTransaction().replace(R.id.contentFragment, new FragmentTrips()).commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startService() {
        Intent itn = new Intent(getApplicationContext(), RSSPullService.class);
        startService(itn);

        Log.i("start", "servicul a inceput");
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
}
