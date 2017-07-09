package com.dorian.licenta.Activities;


import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dorian.licenta.Location.History;
import com.dorian.licenta.NearbyPlace.DataParser;
import com.dorian.licenta.NearbyPlace.GetNearbyPlacesData;
import com.dorian.licenta.NearbyPlace.NearbyPlace;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.dorian.licenta.Product.Product;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResponseNotificationActivity extends AppCompatActivity {

    private TextView ratingTv;
    private TextView probabilityTv;
    private TextView addressTv;
    private Button accept;
    private Button cancel;
    private EditText hour;
    private Spinner spinnerLocations;

    private String loc;
    private double rating;
    private double probability;
    private String address;
    private double latitude;
    private double longitude;
    private ListView produse;
    private String produsSelectat = null;
    private ArrayList<String> prod;
    private ArrayList<String> nearbyPlaces;
    private ArrayList<NearbyPlace> nearbyPlaceArrayList;

    private SharedPreferences sharedPreferences;
    private int idUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_notification);

        loc = getIntent().getExtras().getString("loc");
        rating = getIntent().getExtras().getDouble("rating");
        probability = getIntent().getExtras().getDouble("probability");
        address = getIntent().getExtras().getString("address");
        latitude = getIntent().getExtras().getDouble("latitude");
        longitude = getIntent().getExtras().getDouble("longitude");

        spinnerLocations = (Spinner) findViewById(R.id.spinnerLocations);

        addressTv = (TextView) findViewById(R.id.textViewAddress);
        probabilityTv = (TextView) findViewById(R.id.textViewProbability);
        ratingTv = (TextView) findViewById(R.id.textViewRating);
        hour = (EditText) findViewById(R.id.editTextHour);
        accept = (Button) findViewById(R.id.btnAccept);
        cancel = (Button) findViewById(R.id.btnCancel);
        produse = (ListView) findViewById(R.id.listViewProduse);

        sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
        idUser = sharedPreferences.getInt("idUser", 0);

        addressTv.setText("Ne gasesti pe strada: " + address);
        ratingTv.setText("Rating: " + rating);
        probabilityTv.setText("Probabilitate: " + probability + "%");
        hour.setText("09:00");

        nearbyPlaces = new ArrayList<>();
        nearbyPlaceArrayList = new ArrayList<>();

        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData() {
            @Override
            protected void onPostExecute(String result) {
                List<List<NearbyPlace>> nearbyPlacesList;
                DataParser dataParser = new DataParser();
                nearbyPlacesList = dataParser.parse(result);
                for (List<NearbyPlace> map : nearbyPlacesList) {
                    nearbyPlaces.addAll(map.stream().map(NearbyPlace::getPlaceName).collect(Collectors.toList()));
                    nearbyPlaceArrayList.addAll(map);
                }

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, nearbyPlaces);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerLocations.setAdapter(spinnerArrayAdapter);

                spinnerLocations.setSelection(getIndex(loc));
            }
        };


        getNearbyPlacesData.execute(null, getUrl(latitude, longitude, "restaurant"));

        spinnerLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                NearbyPlace nearbyPlace = nearbyPlaceArrayList.get(position);
                ratingTv.setText("Rating: " + nearbyPlace.getRating());
                addressTv.setText("Ne gasesti pe strada: " + nearbyPlace.getVicinity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        accept.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();

                    RestServices
                            .Factory
                            .getIstance()
                            .sendRezervation(new History(spinnerLocations.getSelectedItem().toString(), produsSelectat, hour.getText().toString(), date.toString(), idUser))
                            .enqueue(new Callback<History>() {
                                @Override
                                public void onResponse(Call<History> call, Response<History> response) {
                                }

                                @Override
                                public void onFailure(Call<History> call, Throwable t) {
                                }
                            });

                    Toast.makeText(getApplicationContext(), "Rezervarea ta a fost primita!", Toast.LENGTH_LONG).show();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
        );

        cancel.setOnClickListener(v -> finish());

        RestServices
                .Factory
                .getIstance()
                .getProductsAfterUser(idUser)
                .enqueue(new Callback<List<Product>>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        prod = new ArrayList<>();
                        prod.addAll(response.body().stream().map(Product::getName).collect(Collectors.toList()));
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, prod);
                        Log.wtf("produse", prod.toString());
                        produse.setAdapter(adapter);
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {

                    }
                });

        produse.setOnItemClickListener((parent, view, position, id) -> {
            produsSelectat = prod.get(position);
        });

        produse.setSelector(android.R.drawable.dialog_holo_light_frame);
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

    private int getIndex(String txt) {
        int contor = 0;
        for (String poz : nearbyPlaces) {
            Log.wtf("locatie: ", poz);
            if (poz.equals(txt)) {
                Log.wtf("contor", contor + " ");
                return contor;
            }
            contor++;
        }

        Log.wtf("contor", contor + " ");
        return 0;
    }
}
