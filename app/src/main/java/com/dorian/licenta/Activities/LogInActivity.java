package com.dorian.licenta.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dorian.licenta.Authentication.User;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private com.google.android.gms.common.SignInButton singIn;

    public static GoogleApiClient googleApiClient;
    private static final int reqCode = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        singIn = (com.google.android.gms.common.SignInButton) findViewById(R.id.btnLogIn);

        singIn.setOnClickListener(this);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogIn:
                singIn();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void singIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, reqCode);
    }

    private void handleResult(GoogleSignInResult result) {
        Log.wtf("result", result.isSuccess() + "");
        Log.wtf("result", result.getStatus() + "");

        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            String name = account.getDisplayName();
            String email = account.getEmail();
            String img_url = account.getPhotoUrl().toString();

            Intent intent = new Intent(getApplicationContext(), Main2Activity.class);

            RestServices.Factory.getIstance().getUser(email).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    Toast.makeText(getApplicationContext(), "Bine ai revenit, " + name + "!", Toast.LENGTH_SHORT).show();
                    intent.putExtra("userId", response.body().getId());
                    startActivity(intent);
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    RestServices.Factory.getIstance().postUser(new User(0, email, name, img_url)).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            RestServices.Factory.getIstance().getUser(email).enqueue(new Callback<User>() {
                                @Override
                                public void onResponse(Call<User> call, Response<User> response) {
                                    Toast.makeText(getApplicationContext(), "Bine ai venit, " + name + "!", Toast.LENGTH_SHORT).show();
                                    intent.putExtra("userId", response.body().getId());
                                    startActivity(intent);
                                }

                                @Override
                                public void onFailure(Call<User> call, Throwable t) {

                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {

                        }
                    });
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "nu a mers login", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.wtf("req code", requestCode + "");

        if (requestCode == reqCode) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            handleResult(result);
        }
    }
}
