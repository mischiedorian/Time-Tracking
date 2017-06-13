package com.dorian.licenta.Product;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dorian.licenta.Authentication.User;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;
import com.google.android.gms.vision.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class FragmentProducts extends Fragment {

    private ListView products;
    private TextView tvUser;

    private ArrayList<String> productsString;
    private ArrayAdapter<String> adapter;

    private SharedPreferences sharedPreferences;
    private int idUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        products = (ListView) view.findViewById(R.id.listViewProducts);
        tvUser = (TextView) view.findViewById(R.id.textViewUserProducts);

        sharedPreferences = getActivity().getSharedPreferences("id", MODE_PRIVATE);
        idUser = sharedPreferences.getInt("idUser", 0);

        RestServices.Factory.getIstance().getUserAfterId(idUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                tvUser.setText("Products bought by " + response.body().getName());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });

        RestServices.Factory.getIstance().getProductsAfterUser(idUser).enqueue(new Callback<List<Product>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                productsString = new ArrayList<>();
                Log.wtf("lungime response", response.body().size() + "");

                productsString.addAll(response.body().stream().map(product -> product.getName() + " - " + product.getQuantity() + " quantity").collect(Collectors.toList()));

                adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, productsString);

                Log.wtf("lungime", productsString.size() + "");

                products.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {

            }
        });

        products.setOnItemLongClickListener((parent, view1, position, id) -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        RestServices.Factory.getIstance().deleteProduct(productsString.get(position).split("-")[0]).enqueue(new Callback<Product>() {
                            @Override
                            public void onResponse(Call<Product> call, Response<Product> response) {

                            }

                            @Override
                            public void onFailure(Call<Product> call, Throwable t) {

                            }
                        });
                        productsString.remove(position);
                        adapter.notifyDataSetChanged();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

            return false;
        });

        return view;
    }
}
