package com.dorian.licenta.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dorian.licenta.Authentication.User;
import com.dorian.licenta.NetworkAvailable;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.firebase.iid.FirebaseInstanceId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private SignInButton singIn;

    private SharedPreferences sharedPreferences;
    private int idUser;

    private static GoogleApiClient googleApiClient;
    private static GoogleSignInOptions googleSignInOptions;
    private static final int REQ_CODE = 9001;

    private String name;
    private String email;
    private String img_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        singIn = (SignInButton) findViewById(R.id.btnLogIn);

        singIn.setOnClickListener(v -> singIn());

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();

        sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
        idUser = sharedPreferences.getInt("idUser", 0);

        if (idUser != 0) {
            Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
            intent.putExtra("userId", idUser);
            startActivity(intent);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void singIn() {
        try {
            logOut(getApplicationContext(), idUser);
        } catch (Exception e) {
        }

        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
    }

    private void handleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            name = account.getDisplayName();
            email = account.getEmail();
            img_url = null;

            try {
                img_url = account.getPhotoUrl().toString();
            } catch (Exception e) {
                Log.i("img user", "nu exista imagine pentru account!");
            }

            Intent intent = new Intent(getApplicationContext(), Main2Activity.class);

            RestServices
                    .Factory
                    .getIstance()
                    .getUser(email)
                    .enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            try {
                                User user = response.body();
                                user.setToken(FirebaseInstanceId.getInstance().getToken());
                                updateToken(user);
                                logInUser("Welcome, ", response.body().getId(), intent);
                            } catch (Exception e) {
                                Log.i("onResponseUser", "Server down!");
                                Toast.makeText(getApplicationContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            insertUser(new User(0, email, name, img_url, FirebaseInstanceId.getInstance().getToken()));

                            RestServices
                                    .Factory
                                    .getIstance()
                                    .getUser(email)
                                    .enqueue(new Callback<User>() {
                                        @Override
                                        public void onResponse(Call<User> call, Response<User> response) {
                                            logInUser("Welcome, ", response.body().getId(), intent);
                                        }

                                        @Override
                                        public void onFailure(Call<User> call, Throwable t) {
                                        }
                                    });
                        }
                    });

        } else {
            Toast.makeText(getApplicationContext(), R.string.logInErrMsg,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateToken(User user) {
        RestServices
                .Factory
                .getIstance()
                .modifyUser(user.getId(), user)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        if (t.getMessage().contains("Failed to connect to /192.168.")) {
                            Log.i("onResponseUser", "Server down!");
                            Toast.makeText(getApplicationContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void logInUser(String mesaje, int id, Intent intent) {
        Toast.makeText(getApplicationContext(), mesaje + name + "!",
                Toast.LENGTH_LONG).show();

        intent.putExtra("userId", id);
        startActivity(intent);
    }

    private void insertUser(User user) {
        RestServices
                .Factory
                .getIstance()
                .postUser(user)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        if (t.getMessage().contains("Failed to connect to /192.168.")) {
                            if (!NetworkAvailable.isNetworkAvailable(LogInActivity.this)) {
                                Toast.makeText(getApplicationContext(), R.string.networkMsg, Toast.LENGTH_LONG).show();
                            } else {
                                Log.i("onResponseUser", "Server down!");
                                Toast.makeText(getApplicationContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
        idUser = sharedPreferences.getInt("idUser", 0);

        if (idUser != 0) {
            Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
            intent.putExtra("userId", idUser);
            startActivity(intent);
        }
    }

    public static void logOut(Context context, int id) {
        if (id == 0) {
            Auth
                    .GoogleSignInApi
                    .signOut(googleApiClient)
                    .setResultCallback(status -> {
                    });
        } else {
            Auth
                    .GoogleSignInApi
                    .signOut(googleApiClient)
                    .setResultCallback(status -> Toast.makeText(context, "La revedere!", Toast.LENGTH_LONG).show());
        }
    }
}
