package com.dorian.licenta.FragmentsMenu;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dorian.licenta.Authentication.User;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.Product.ListViewProductAdapter;
import com.dorian.licenta.Product.Product;
import com.dorian.licenta.R;
import com.dorian.licenta.RestServices.RestServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class FragmentProducts extends Fragment {

    private ListView products;
    private TextView tvUser;

    private ArrayList<String> productsString;
    private ListViewProductAdapter adapter;

    private int idUser;

    private ArrayList<String> locationsAddress;

    private ArrayList<MyLocation> locationProduct;

    private ArrayList<Product> productsList;

    private int idLocation = 0;

    private ProgressDialog progressDialog;
    private AlertDialog.Builder alertDialog;
    private AlertDialog alert;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        alertDialog = new AlertDialog.Builder(getActivity());
        alert = alertDialog.create();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Downloading data...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        productsList = new ArrayList<>();

        products = (ListView) view.findViewById(R.id.listViewProducts);
        tvUser = (TextView) view.findViewById(R.id.textViewUserProducts);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("id", MODE_PRIVATE);
        idUser = sharedPreferences.getInt("idUser", 0);


        RestServices
                .Factory
                .getIstance()
                .getUserAfterId(idUser)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        try {
                            tvUser.setText("Products bought by " + response.body().getName());
                        } catch (Exception e) {
                            Log.i("onResponseUser", "Server down!");
                            Toast.makeText(getContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {

                    }
                });

        loadData();

        products.setOnItemLongClickListener((parent, view1, position, id) -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        RestServices
                                .Factory
                                .getIstance()
                                .deleteProduct(productsString.get(position).split("-")[0])
                                .enqueue(new Callback<Product>() {
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

        products.setOnItemClickListener((parent, view1, position, id) -> {
            progressDialog.show();
            RestServices
                    .Factory
                    .getIstance()
                    .getLocationsAfterUser(idUser)
                    .enqueue(new Callback<List<MyLocation>>() {
                        @Override
                        public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                            try {
                                locationsAddress = new ArrayList<>();
                                locationProduct = new ArrayList<>();
                                int contor = 2;
                                MyLocation tmp = response.body().get(response.body().size() - 1);
                                locationProduct.add(tmp);
                                int dayRef = tmp.getZiDinLuna();
                                int start = response.body().size() - 2;
                                while (true) {
                                    if (contor == 0) {
                                        break;
                                    } else {
                                        if (response.body().get(start).getZiDinLuna() != dayRef) {
                                            dayRef = response.body().get(start).getZiDinLuna();
                                            contor--;
                                        }
                                        locationProduct.add(response.body().get(start));
                                        start--;
                                    }
                                }

                                for (MyLocation location : locationProduct) {
                                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                                    try {
                                        String address = geocoder.
                                                getFromLocation(location.getLat(), location.getLgn(), 1)
                                                .get(0)
                                                .getAddressLine(0);
                                        locationsAddress.add(address);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                showAlertDialog(productsList.get(position).getId());
                            } catch (Exception e) {
                                Log.i("onResponseUser", "Server down!");
                                Toast.makeText(getContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                        }
                    });
        });

        return view;
    }

    private void loadData() {
        progressDialog.show();
        try {
            productsList.clear();
            adapter.notifyDataSetChanged();
        } catch (Exception e) {

        }

        RestServices
                .Factory
                .getIstance()
                .getProductsAfterUser(idUser)
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        try {
                            productsList = new ArrayList<>();
                            productsList.addAll(response.body());

                            productsString = new ArrayList<>();
                            Log.wtf("lungime response", response.body().size() + "");

                            productsString.addAll(response.body().stream().map(product -> product.getName() + " - " + product.getQuantity() + " quantity").collect(Collectors.toList()));

                            adapter = new ListViewProductAdapter(getContext(), R.layout.list_view_item_products, productsList);

                            Log.wtf("lungime", productsString.size() + "");

                            products.setAdapter(adapter);

                            progressDialog.dismiss();
                        } catch (Exception e) {
                            Log.i("onResponseUser", "Server down!");
                            Toast.makeText(getContext(), R.string.msgServerDown, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {

                    }
                });
    }

    private void showAlertDialog(int idProduct) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_dialog_locations, null);
        alertDialog.setView(view);

        LinearLayout container = (LinearLayout) view.findViewById(R.id.containerLinearLayout);
        ListView listView = (ListView) container.findViewById(R.id.listViewLocations);
        listView.setSelector(android.R.drawable.dialog_holo_light_frame);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, locationsAddress);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> idLocation = locationProduct.get(position).getId());

        container.findViewById(R.id.okButton).setOnClickListener(v -> {
            RestServices
                    .Factory
                    .getIstance()
                    .getProductAfterId(idProduct)
                    .enqueue(new Callback<Product>() {
                        @Override
                        public void onResponse(Call<Product> call, Response<Product> response) {
                            Product product = response.body();

                            product.setIdLocatie(idLocation);
                            RestServices
                                    .Factory
                                    .getIstance()
                                    .modifyProduct(product.getId(), product)
                                    .enqueue(new Callback<Product>() {
                                        @Override
                                        public void onResponse(Call<Product> call, Response<Product> response) {

                                        }

                                        @Override
                                        public void onFailure(Call<Product> call, Throwable t) {

                                        }
                                    });
                            loadData();
                        }

                        @Override
                        public void onFailure(Call<Product> call, Throwable t) {

                        }
                    });
            alert.dismiss();
        });

        alert.show();
        progressDialog.dismiss();
    }
}
