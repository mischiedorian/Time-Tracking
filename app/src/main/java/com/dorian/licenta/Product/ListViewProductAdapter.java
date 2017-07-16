package com.dorian.licenta.Product;

import android.content.Context;
import android.location.Geocoder;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListViewProductAdapter extends ArrayAdapter<Product> {

    private ArrayList<Product> products;

    public ListViewProductAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Product> objects) {
        super(context, resource, objects);
        products = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater li = LayoutInflater.from(getContext());
        View view = li.inflate(R.layout.list_view_item_products, null);

        Product product = products.get(position);

        TextView productName = (TextView) view.findViewById(R.id.textViewProduct);
        TextView productLocation = (TextView) view.findViewById(R.id.textViewLocationProduct);
        TextView productQuantity = (TextView) view.findViewById(R.id.textViewQuantity);

        productName.setText("Product: " + product.getName());
        productQuantity.setText("Quantity: " + product.getQuantity() + " pieces");

        if (product.getIdLocatie() != 0) {
            RestServices
                    .Factory
                    .getIstance()
                    .getLocationId(product.getIdLocatie())
                    .enqueue(new Callback<MyLocation>() {
                        @Override
                        public void onResponse(Call<MyLocation> call, Response<MyLocation> response) {
                            Log.wtf("locatie ",response.body().toString());
                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            try {
                                String address = geocoder
                                        .getFromLocation(response.body().getLat(), response.body().getLgn(), 1)
                                        .get(0)
                                        .getAddressLine(0);
                                productLocation.setText("Location: " + address);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<MyLocation> call, Throwable t) {
                            Log.wtf("fail ", t.getMessage());
                        }
                    });
        } else {
            productLocation.setText("Location: undefined");
        }

        return view;
    }
}
