package com.dorian.licenta.FragmentsTrip;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dorian.licenta.FragmentsMenu.FragmentTrips;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentMap extends Fragment implements OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<MyLocation>,
        ClusterManager.OnClusterInfoWindowClickListener<MyLocation>,
        ClusterManager.OnClusterItemClickListener<MyLocation>,
        ClusterManager.OnClusterItemInfoWindowClickListener<MyLocation> {

    private MapView mapView;
    private LatLng latLng;
    private GoogleMap map;
    private ClusterManager<MyLocation> clusterManager;

    public FragmentMap(LatLng latLng) {
        this.latLng = latLng;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        clusterManager = new ClusterManager<MyLocation>(getContext(), map);
        final CameraPosition[] mPreviousCameraPosition = {null};
        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                CameraPosition position = googleMap.getCameraPosition();
                if (mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position.zoom) {
                    mPreviousCameraPosition[0] = googleMap.getCameraPosition();
                    clusterManager.cluster();
                }
            }
        });

        map.setOnMarkerClickListener(clusterManager);
        map.setOnInfoWindowClickListener(clusterManager);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterInfoWindowClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setOnClusterItemInfoWindowClickListener(this);

        RestServices.Factory.getIstance().getLocations().enqueue(new Callback<List<MyLocation>>() {
            @Override
            public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                for (MyLocation location : response.body()) {
                    clusterManager.addItem(location);
                }
                clusterManager.cluster();
            }

            @Override
            public void onFailure(Call<List<MyLocation>> call, Throwable t) {

            }
        });

        //googleMap.setMyLocationEnabled(true);
    }

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
        Toast.makeText(getContext(), new MyLocationHelper(myLocation).minutesLocation() + "", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onClusterItemInfoWindowClick(MyLocation myLocation) {

    }
}
