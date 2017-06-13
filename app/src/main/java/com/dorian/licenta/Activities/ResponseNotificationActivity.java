package com.dorian.licenta.Activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dorian.licenta.Location.History;
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

    private TextView location;
    private Button accept;
    private Button cancel;
    private EditText hour;
    private String loc;
    private ListView produse;
    private String produsSelectat = null;
    private ArrayList<String> prod;

    private SharedPreferences sharedPreferences;
    private int idUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_notification);

        loc = getIntent().getExtras().getString("loc");
        location = (TextView) findViewById(R.id.textViewLocatie);
        hour = (EditText) findViewById(R.id.editTextHour);
        accept = (Button) findViewById(R.id.btnAccept);
        cancel = (Button) findViewById(R.id.btnCancel);
        produse = (ListView) findViewById(R.id.listViewProduse);

        sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
        idUser = sharedPreferences.getInt("idUser", 0);

        location.setText(loc);
        hour.setText("09:00");

        accept.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();

                    RestServices.Factory.getIstance().sendRezervation(new History(loc, produsSelectat, hour.getText().toString(), date.toString(), idUser)).enqueue(new Callback<History>() {
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

        RestServices.Factory.getIstance().getProductsAfterUser(idUser).enqueue(new Callback<List<Product>>() {
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
}
