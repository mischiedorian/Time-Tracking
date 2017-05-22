package com.dorian.licenta.FragmentsMenu;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.dorian.licenta.R;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.RestServices.RestServices;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentStart extends Fragment {
    private Button locatii;
    private ListView listViewLocatii;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayLocatii;
    private RestServices server;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(getContext());
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        locatii = (Button) view.findViewById(R.id.btn_locatie);
        listViewLocatii = (ListView) view.findViewById(R.id.listView);

        locatii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setTitle("Loading...");
                progressDialog.setMessage("Downloading data...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                arrayLocatii = new ArrayList<String>();
                ArrayList<MyLocation> loc = new ArrayList<MyLocation>();

                RestServices.Factory.getIstance().getLocations().enqueue(new Callback<List<MyLocation>>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                        for (MyLocation location : response.body()) {
                            loc.add(new MyLocation(location.getId(), location.getZi(), location.getLuna(),
                                    location.getZiDinLuna(), location.getOraInceput(), location.getOraSfarsit(),
                                    location.getLat(), location.getLgn()));
                        }

                        for (MyLocation l : loc) {
                            arrayLocatii.add(l.toString());
                        }
                        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrayLocatii);
                        adapter.notifyDataSetChanged();
                        listViewLocatii.setAdapter(adapter);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                        Log.i("Failed", t.getMessage());
                        progressDialog.dismiss();
                    }
                });
            }
        });
        server = RestServices.Factory.getIstance();

        return view;
    }
}
