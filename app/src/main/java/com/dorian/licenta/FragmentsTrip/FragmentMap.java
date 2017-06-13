package com.dorian.licenta.FragmentsTrip;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.dorian.licenta.Activities.Main2Activity;
import com.dorian.licenta.FragmentsMenu.FragmentTrips;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class FragmentMap extends Fragment implements OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<MyLocation>,
        ClusterManager.OnClusterInfoWindowClickListener<MyLocation>,
        ClusterManager.OnClusterItemClickListener<MyLocation>,
        ClusterManager.OnClusterItemInfoWindowClickListener<MyLocation> {

    private MapView mapView;
    private LatLng latLng;
    private GoogleMap map;
    private ClusterManager<MyLocation> clusterManager;
    private Button pickDate;
    private Button myLocation;
    private SharedPreferences sharedPreferences;
    private int idUser;
    private int year, month, day;

    public FragmentMap(LatLng latLng) {
        this.latLng = latLng;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        pickDate = (Button) view.findViewById(R.id.btnPickDate);
        myLocation = (Button) view.findViewById(R.id.btnMyLocation);
        sharedPreferences = getActivity().getSharedPreferences("id", MODE_PRIVATE);

        mapView = (MapView) view.findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        map = googleMap;
        try {
            FragmentTrips.progressBar.dismiss();
        } catch (Exception e) {

        }
        LatLng sydney = new LatLng(latLng.latitude, latLng.longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        clusterManager = new ClusterManager<>(getContext(), map);

        listenere(googleMap, clusterManager);

        idUser = sharedPreferences.getInt("idUser", 0);
        RestServices.Factory.getIstance().getLocationsAfterUser(idUser).enqueue(new Callback<List<MyLocation>>() {
            @Override
            public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                response.body().stream().filter(location -> new MyLocationHelper(location).minutesLocation() > 10).forEach(location -> clusterManager.addItem(location));
                clusterManager.cluster();
            }

            @Override
            public void onFailure(Call<List<MyLocation>> call, Throwable t) {

            }
        });

        pickDate.setOnClickListener(v -> new DatePickerDialog(getContext(), datePickerListener, year, month, day).show());
        myLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            Location location = LocationServices.FusedLocationApi.getLastLocation(Main2Activity.googleApiClient);
            CameraPosition cameraPos = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        });
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year1, int month1, int dayOfMonth1) {
            idUser = sharedPreferences.getInt("idUser", 0);
            year = year1;
            month = month1;
            day = dayOfMonth1;
            RestServices.Factory.getIstance().getLocationsAferMonthAndDay(month, day, idUser)
                    .enqueue(new Callback<List<MyLocation>>() {
                @Override
                public void onResponse(Call<List<MyLocation>> call,
                                       Response<List<MyLocation>> response) {
                    map.clear();
                    clusterManager = new ClusterManager<>(getContext(), map);
                    response.body().stream().filter(location ->
                            new MyLocationHelper(location).minutesLocation() > 10).
                            forEach(location -> clusterManager.addItem(location));
                    clusterManager.cluster();
                    listenere(map, clusterManager);
                }

                @Override
                public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                }
            });
        }
    };

    @Override
    public boolean onClusterClick(Cluster<MyLocation> cluster) {
        int time = cluster.getItems().iterator().next().getId();

        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }

        final LatLngBounds bounds = builder.build();

        try {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {

        }

        int suma = 0;
        for (MyLocation location : cluster.getItems()) {
            suma += new MyLocationHelper(location).minutesLocation();
        }
        Toast.makeText(getContext(), "Ati stat in aceasta arie " + suma + " minute", Toast.LENGTH_LONG).show();

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MyLocation> cluster) {

    }

    @Override
    public boolean onClusterItemClick(MyLocation myLocation) {
        Toast.makeText(getContext(), myLocation.getOraInceput() + " -> " + myLocation.getOraSfarsit() + " : " + new MyLocationHelper(myLocation).minutesLocation() + "", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onClusterItemInfoWindowClick(MyLocation myLocation) {

    }

    private void listenere(GoogleMap googleMap, ClusterManager<MyLocation> clusterManager) {
        final CameraPosition[] mPreviousCameraPosition = {null};

        map.setOnCameraIdleListener(() -> {
            CameraPosition position = googleMap.getCameraPosition();
            if (mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position.zoom) {
                mPreviousCameraPosition[0] = googleMap.getCameraPosition();
                clusterManager.cluster();
            }
        });

        map.setOnMarkerClickListener(clusterManager);
        map.setOnInfoWindowClickListener(clusterManager);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterInfoWindowClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setOnClusterItemInfoWindowClickListener(this);
        clusterManager.setRenderer(new MarkerRender(getContext(), map, clusterManager));
    }

    private class MarkerRender extends DefaultClusterRenderer<MyLocation> {

        public MarkerRender(Context context, GoogleMap map, ClusterManager<MyLocation> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyLocation item, MarkerOptions markerOptions) {
            /*
            Log.wtf("before cluster", "intra");
            super.onBeforeClusterItemRendered(item, markerOptions);
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(getContext(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(item.getLat(), item.getLgn(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0);
                markerOptions.title(address).snippet(address);
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }
    }
}
