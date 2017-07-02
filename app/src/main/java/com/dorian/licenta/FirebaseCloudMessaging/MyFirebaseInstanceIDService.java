package com.dorian.licenta.FirebaseCloudMessaging;

import android.content.SharedPreferences;
import android.util.Log;

import com.dorian.licenta.Authentication.User;
import com.dorian.licenta.RestServices.RestServices;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author ton1n8o - antoniocarlos.dev@gmail.com on 6/13/16.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static String TAG = "Registration";
    private SharedPreferences sharedPreferences;
    private int idUser;

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sharedPreferences = getSharedPreferences("id", MODE_PRIVATE);
        idUser = sharedPreferences.getInt("idUser", 0);

        if(idUser != 0){
            RestServices.Factory.getIstance().getUserAfterId(idUser).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if(refreshedToken != response.body().getToken()) {
                        User user = response.body();
                        user.setToken(refreshedToken);
                        Log.wtf("token", "trebuie updatat");
                        RestServices.Factory.getIstance().modifyUser(user.getId(),user).enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {

                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable t) {

                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {

                }
            });
        }
    }
}
