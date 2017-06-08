package com.dorian.licenta.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.dorian.licenta.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class StartActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private int idUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
        idUser = sharedPreferences.getInt("idUser", 0);
        if (idUser == 0) {
            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
            intent.putExtra("idUser", idUser);
            startActivity(intent);
        }
    }
}
