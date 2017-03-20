package com.dorian.licenta.RestService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by misch on 02.03.2017.
 */
public interface RestService {
    String baseUrl = "https://licenta-mischiedorian.c9users.io/";

    @GET("locations")
    Call<List<MyLocation>> getLocations();

    @POST("location")
    Call<MyLocation> getLocAccess(@Body MyLocation locationResponse);

    @DELETE("location/{id}")
    Call<MyLocation> deleteLocation(@Path("id") String id);

    @PUT("location/{id}")
    Call<MyLocation> modifyLocation(@Path("id") String id , @Body MyLocation locationModify);

    class Factory {
        private static RestService service = null;

        public static RestService getIstance() {
            if (service == null) {
                Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(baseUrl).build();
                service = retrofit.create(RestService.class);
                return service;
            } else {
                return service;
            }
        }
    }
}
