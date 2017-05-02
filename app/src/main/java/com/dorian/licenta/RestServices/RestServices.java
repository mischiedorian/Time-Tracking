package com.dorian.licenta.RestServices;

import com.dorian.licenta.Location.History;
import com.dorian.licenta.Location.MyLocation;

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

public interface RestServices {
    public static final String baseUrl = "https://licenta-mischiedorian.c9users.io/";
    //public static final String baseUrl = "http://192.168.1.144:8082/";

    @GET("locations")
    Call<List<MyLocation>> getLocations();

    @GET("locations/{month}/{dayOfMonth}")
    Call<List<MyLocation>> getLocationsAferMonthAndDay(@Path("month") String month, @Path("dayOfMonth") String dayOfMonth);

    @GET("locations/{day}")
    Call<List<MyLocation>> getLocationsAferDay(@Path("day") String day);

    @POST("location")
    Call<MyLocation> postLocAccess(@Body MyLocation locationResponse);

    @DELETE("location/{id}")
    Call<MyLocation> deleteLocation(@Path("id") String id);

    @PUT("location/{id}")
    Call<MyLocation> modifyLocation(@Path("id") String id, @Body MyLocation locationModify);

    @POST("history")
    Call<History> sendRezervation(@Body History history);

    class Factory {
        private static RestServices service = null;

        public static RestServices getIstance() {
            if (service == null) {
                Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(baseUrl).build();
                service = retrofit.create(RestServices.class);
                return service;
            } else {
                return service;
            }
        }
    }
}
