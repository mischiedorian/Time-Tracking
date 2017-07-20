package com.dorian.licenta;

import android.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.dorian.licenta.Activities.Main2Activity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ResponseActivityOnMap extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private LatLng latLng;
    private TextView voucherTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_on_map);

        voucherTv = (TextView) findViewById(R.id.textViewVoucher);
        voucherTv.setText(getIntent().getExtras().getString("voucher"));
        mapView = (MapView) findViewById(R.id.mapViewResponse);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

        latLng = new LatLng(
                getIntent().getExtras().getDouble("latitude"),
                getIntent().getExtras().getDouble("longitude")
        );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getApplicationContext());

        Log.wtf("latLng", latLng.toString());
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(20).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        googleMap.addMarker(new MarkerOptions().position(latLng)
                .title("Here!"));

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(Main2Activity.googleApiClient);

        googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("Your location!"));
    }
}
