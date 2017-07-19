package com.dorian.licenta.FragmentsMenu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dorian.licenta.Activities.Main2Activity;
import com.dorian.licenta.GetNameOfLocation;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.NetworkAvailable;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class FragmentStart extends Fragment implements LocationListener {

    private SharedPreferences sharedPreferences;
    private int idUser;

    private TextView lat;
    private TextView lng;
    private TextView address;


    private ArrayList<ArrayList<MyLocation>> topLocations;

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
        getTopLocations(view);
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

    private void getTopLocations(View view) {
        topLocations = new ArrayList<>();

        RestServices
                .Factory
                .getIstance()
                .getLocationsAfterUser(idUser)
                .enqueue(new Callback<List<MyLocation>>() {
                    public boolean contor;

                    @Override
                    public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                        if (response.body().size() != 0) {
                            ArrayList<MyLocation> local = new ArrayList<>();
                            local.add(response.body().get(0));
                            topLocations.add(local);

                            Log.wtf("lungime response", response.body().size() + "");
                            for (int i = 1; i < response.body().size() - 1; i++) {
                                contor = false;
                                for (int j = 0; j < topLocations.size() - 1; j++) {
                                    if (new MyLocationHelper(response.body().get(i)).distanceBetween2Locations(topLocations.get(j).get(0)) < 0.4) {
                                        topLocations.get(j).add(response.body().get(i));
                                        contor = true;
                                        break;
                                    }
                                }
                                if (!contor) {
                                    local = new ArrayList<>();
                                    local.add(response.body().get(i));
                                    topLocations.add(local);
                                }
                            }

                            Log.wtf("locatii", topLocations.size() + "");

                            sortTopLocations();

                            ArrayList<PieEntry> pieEntries = new ArrayList<>();

                            for (int i = 0; i < 4; i++) {
                                int finalI = i;
                                GetNameOfLocation getNameOfLocation = new GetNameOfLocation() {
                                    @Override
                                    protected void onPostExecute(String s) {
                                        pieEntries.add(new PieEntry(Float.parseFloat(topLocations.get(finalI).size() + ""), s.split(",")[0]));

                                        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
                                        PieData pieData = new PieData(pieDataSet);

                                        pieData.setValueTextColor(Color.BLUE);
                                        pieData.setValueTextSize(20f);

                                        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                                        pieDataSet.setValueTextColor(Color.BLACK);

                                        PieChart pieChart = (PieChart) view.findViewById(R.id.pieChartTopLocatii);

                                        pieChart.setData(pieData);
                                        pieChart.invalidate();

                                        Description description = new Description();
                                        description.setText("Top Locations");
                                        pieChart.setDescription(description);
                                        pieChart.setHoleColor(R.drawable.blue);
                                        pieChart.setHoleRadius(10f);
                                        pieChart.setTransparentCircleAlpha(0);
                                        pieChart.setDrawEntryLabels(true);
                                    }
                                };

                                getNameOfLocation.execute(
                                        "http://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                                                topLocations.get(i).get(0).getLat() + "," +
                                                topLocations.get(i).get(0).getLgn() +
                                                "&sensor=false&language=RO"
                                );


                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                        if (t.getMessage().contains("Failed to connect to /192.168.")) {
                            Log.i("onResponseUser", "Server down!");
                            Toast.makeText(getContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sortTopLocations() {
        for (int i = 0; i < topLocations.size() - 2; i++) {
            for (int j = i + 1; j < topLocations.size() - 1; j++) {
                if (topLocations.get(i).size() < topLocations.get(j).size()) {
                    ArrayList<MyLocation> tempi = topLocations.get(i);
                    topLocations.set(i, topLocations.get(j));
                    topLocations.set(j, tempi);
                }
            }
        }
    }

    private void generatePieChart(PieData pieData, View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        //  View view = inflater.inflate(R.layout.alert_dialog_pie_chart_top_locatii, null);
        // alertDialog.setView(view);
        AlertDialog alert = alertDialog.create();

        //  LinearLayout container = (LinearLayout) view.findViewById(R.id.containerPieChart);
        PieChart pieChart = (PieChart) view.findViewById(R.id.pieChartTopLocatii);

        pieChart.setData(pieData);
        pieChart.invalidate();

        alert.show();
    }
}
