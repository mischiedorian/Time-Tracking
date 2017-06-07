package com.dorian.licenta.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dorian.licenta.Location.History;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResponseNotificationActivity extends AppCompatActivity {

    private TextView location;
    private Button accept;
    private Button cancel;
    private Spinner menu;
    private EditText hour;
    private String loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_notification);

        loc = getIntent().getExtras().getString("loc");
        location = (TextView) findViewById(R.id.textViewLocatie);
        hour = (EditText) findViewById(R.id.editTextHour);
        menu = (Spinner) findViewById(R.id.spinnerMenu);
        accept = (Button) findViewById(R.id.btnAccept);
        cancel = (Button) findViewById(R.id.btnCancel);

        location.setText(loc);
        hour.setText("09:00");

        accept.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();
                    RestServices.Factory.getIstance().sendRezervation(new History(loc, menu.getSelectedItem().toString(), hour.getText().toString(), date.toString())).enqueue(new Callback<History>() {
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
    }
}
