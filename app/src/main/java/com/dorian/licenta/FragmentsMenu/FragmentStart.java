package com.dorian.licenta.FragmentsMenu;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dorian.licenta.Activities.Main2Activity;
import com.dorian.licenta.NetworkAvailable;
import com.dorian.licenta.R;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class FragmentStart extends Fragment implements LocationListener {

    private SharedPreferences sharedPreferences;
    private int idUser;

    private TextView lat;
    private TextView lng;
    private TextView address;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);

        sharedPreferences = getActivity().getSharedPreferences("id", MODE_PRIVATE);
        idUser = sharedPreferences.getInt("idUser", 0);

        lat = (TextView) view.findViewById(R.id.textViewLat);
        lng = (TextView) view.findViewById(R.id.textViewLng);
        address = (TextView) view.findViewById(R.id.textViewAddress2);
        Button btnRefresh = (Button) view.findViewById(R.id.btnRefresh);

        if (!NetworkAvailable.isNetworkAvailable(getActivity())) {
            Toast.makeText(getContext(), R.string.networkMsg, Toast.LENGTH_LONG).show();
        }

        putLocation();

        btnRefresh.setOnClickListener(v -> putLocation());

        return view;
    }

    @Override
    public void onLocationChanged(Location location) {
        putLocation();
    }

    private void putLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(Main2Activity.googleApiClient);

        if (location != null) {
            lat.setText(String.valueOf(location.getLatitude()));
            lng.setText(String.valueOf(location.getLongitude()));

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(getContext(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String addressStreet = addresses.get(0).getAddressLine(0);
                address.setText(addressStreet);
            } catch (IOException e) {
                Log.wtf("geocoder", "nu a putut extrage adresa");
            }
            Log.i("location", " a fost gasita " + location.getLatitude() + ": " + location.getLongitude());

        } else {
            Log.wtf("location", "locatia este null");

            lat.setText("Undefined");
            lng.setText("Undefined");
            address.setText("Undefined");
        }
    }
}
