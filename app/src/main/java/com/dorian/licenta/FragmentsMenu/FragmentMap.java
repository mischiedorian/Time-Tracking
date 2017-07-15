package com.dorian.licenta.FragmentsMenu;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dorian.licenta.Activities.Main2Activity;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.NetworkAvailable;
import com.dorian.licenta.Product.Product;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        sharedPreferences = getActivity().getSharedPreferences("id", MODE_PRIVATE);

        if (!new NetworkAvailable().isNetworkAvailable(getActivity())) {

            idUser = sharedPreferences.getInt("idUser", 0);

            Toast.makeText(getContext(), R.string.networkMsg, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getContext(), Main2Activity.class);
            intent.putExtra("idUser", idUser);
            startActivity(intent);
        }
        super.onViewCreated(view, savedInstanceState);

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        pickDate = (Button) view.findViewById(R.id.btnPickDate);
        myLocation = (Button) view.findViewById(R.id.btnMyLocation);

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
        LatLng position = new LatLng(latLng.latitude, latLng.longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        clusterManager = new ClusterManager<>(getContext(), map);

        listenere(googleMap, clusterManager);

        idUser = sharedPreferences.getInt("idUser", 0);

        RestServices
                .Factory
                .getIstance()
                .getLocationsAfterUser(idUser)
                .enqueue(new Callback<List<MyLocation>>() {
                    @Override
                    public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                        if (response.body().size() == 0) {
                            Toast.makeText(getContext(), R.string.msgNoData, Toast.LENGTH_LONG).show();
                        } else {
                            try {
                                response
                                        .body()
                                        .stream()
                                        .filter(location -> new MyLocationHelper(location).minutesLocation() > 10)
                                        .forEach(location -> clusterManager.addItem(location));

                                clusterManager.cluster();
                            } catch (Exception e) {
                                Log.i("onResponseUser", "Server down!");
                                Toast.makeText(getContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                            }
                        }
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
            month = month1 + 1;
            day = dayOfMonth1;

            RestServices
                    .Factory
                    .getIstance()
                    .getLocationsAferMonthAndDay(month, day, idUser)
                    .enqueue(new Callback<List<MyLocation>>() {
                        @Override
                        public void onResponse(Call<List<MyLocation>> call,
                                               Response<List<MyLocation>> response) {
                            if (response.body().size() == 0) {
                                Toast.makeText(getContext(), R.string.msgNoData, Toast.LENGTH_LONG).show();
                            } else {
                                try {
                                    map.clear();
                                    clusterManager = new ClusterManager<>(getContext(), map);
                                    response.body().stream().filter(location ->
                                            new MyLocationHelper(location).minutesLocation() > 10).
                                            forEach(location -> clusterManager.addItem(location));
                                    clusterManager.cluster();
                                    listenere(map, clusterManager);
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                        }
                    });
        }
    };

    @Override
    public boolean onClusterClick(Cluster<MyLocation> cluster) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }

        final LatLngBounds bounds = builder.build();

        try {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {

        }

        int sum = 0;
        for (MyLocation location : cluster.getItems()) {
            sum += new MyLocationHelper(location).minutesLocation();
        }

        int hours = sum / 60;
        int minutes = sum % 60;

        Toast.makeText(getContext(), "Ati stat in aceasta arie " + hours + " ore si " + minutes + " minute", Toast.LENGTH_LONG).show();

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MyLocation> cluster) {
    }

    @Override
    public boolean onClusterItemClick(MyLocation myLocation) {
        showDialogDetails(myLocation);

        return true;
    }

    private void showDialogDetails(MyLocation location) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_dialog_products, null);
        alertDialog.setView(view);
        AlertDialog alert = alertDialog.create();

        LinearLayout container = (LinearLayout) view.findViewById(R.id.containerLinearLayoutProducts);
        ListView listView = (ListView) container.findViewById(R.id.listViewProductsDialog);

        TextView timeSpend = (TextView) container.findViewById(R.id.textViewTimeSpend);
        TextView msg = (TextView) container.findViewById(R.id.textViewMsj);

        int minutesTotal = new MyLocationHelper(location).minutesLocation();
        int hours = minutesTotal / 60;
        int minutes = minutesTotal % 60;

        Toast.makeText(getContext(), "Ati stat in aceasta arie " + hours + " ore si" + minutes + " minute", Toast.LENGTH_LONG).show();


        timeSpend.setText("Ai fost aici de la " +
                location.getOraInceput() +
                " la " + location.getOraSfarsit() +
                ", deci ai stat " + hours + " ore si " + minutes + " minute");

        RestServices
                .Factory
                .getIstance()
                .getProductsAfterIdLocation(location.getId())
                .enqueue(new Callback<List<Product>>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        try {
                            if (response.body().size() > 0) {
                                msg.setText("Ai consumat produsele: ");
                                ArrayList<String> products = response
                                        .body()
                                        .stream()
                                        .map(product -> product.getName() + " - " + product.getQuantity()).collect(Collectors.toCollection(ArrayList::new));
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, products);
                                listView.setAdapter(adapter);
                            } else {
                                msg.setText("Nu exista produse cumparate in acesta locatie.");
                                listView.setVisibility(View.INVISIBLE);
                            }
                        } catch (Exception e) {
                            Log.i("onResponseUser", "Server down!");
                            Toast.makeText(getContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                            alert.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {

                    }
                });
        alert.show();
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
