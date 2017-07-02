package com.dorian.licenta.RestServices;

import com.dorian.licenta.Authentication.User;
import com.dorian.licenta.Location.History;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Product.Product;

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

    @GET("locations/{idUser}")
    Call<List<MyLocation>> getLocationsAfterUser(@Path("idUser") int idUser);

    @GET("locations/{month}/{dayOfMonth}/{idUser}")
    Call<List<MyLocation>> getLocationsAferMonthAndDay(@Path("month") int month, @Path("dayOfMonth") int dayOfMonth, @Path("idUser") int idUser);

    @GET("locations/{day}/{idUser}")
    Call<List<MyLocation>> getLocationsAferDay(@Path("day") int day, @Path("idUser") int idUser);

    @POST("location")
    Call<MyLocation> postLocAccess(@Body MyLocation locationResponse);

    @DELETE("location/{id}")
    Call<MyLocation> deleteLocation(@Path("id") int id);

    @PUT("location/{id}")
    Call<MyLocation> modifyLocation(@Path("id") int id, @Body MyLocation locationModify);

    @POST("history")
    Call<History> sendRezervation(@Body History history);

    @POST("user")
    Call<User> postUser(@Body User user);

    @GET("user/{email}")
    Call<User> getUser(@Path("email") String email);

    @GET("userId/{idUser}")
    Call<User> getUserAfterId(@Path("idUser") int id);

    @POST("product")
    Call<Product> postProduct(@Body Product product);

    @GET("products/{idUser}")
    Call<List<Product>> getProductsAfterUser(@Path("idUser") int idUser);

    @GET("products")
    Call<List<Product>> getAllProducts();

    @GET("productsName/{name}")
    Call<Product> getProductsAfterName(@Path("name") String name);

    @PUT("product/{id}")
    Call<Product> modifyProduct(@Path("id") int id, @Body Product product);

    @PUT("user/{id}")
    Call<User> modifyUser(@Path("id") int id, @Body User user);

    @DELETE("product/{name}")
    Call<Product> deleteProduct(@Path("name") String name);

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
