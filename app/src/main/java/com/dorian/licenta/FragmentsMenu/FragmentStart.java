package com.dorian.licenta.FragmentsMenu;

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
import android.widget.Toast;

import com.dorian.licenta.Location.MyLocationHelper;
import com.dorian.licenta.R;
import com.dorian.licenta.Location.MyLocation;
import com.dorian.licenta.RestService.RestService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dorian on 19/03/2017.
 */

public class FragmentStart extends Fragment {
    private Button locatii;
    private Button date;
    private ListView listViewLocatii;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayLocatii;
    private ArrayList<MyLocation> locatiiDate;
    private RestService server;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        locatii = (Button) view.findViewById(R.id.btn_locatie);
        listViewLocatii = (ListView) view.findViewById(R.id.listView);
        date = (Button) view.findViewById(R.id.btn_date);

        locatii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayLocatii = new ArrayList<String>();
                ArrayList<MyLocation> loc = new ArrayList<MyLocation>();

                RestService.Factory.getIstance().getLocations().enqueue(new Callback<List<MyLocation>>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                        for (MyLocation location : response.body()) {
                            loc.add(new MyLocation(location.getId(), location.getZi(), location.getLuna(), location.getOraInceput(), location.getOraSfarsit(), location.getLat(), location.getLgn()));
                        }

                        for (MyLocation l : loc) {
                            arrayLocatii.add(l.toString());
                        }
                        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrayLocatii);
                        adapter.notifyDataSetChanged();
                        listViewLocatii.setAdapter(adapter);
                    }

                    @Override
                    public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                        Log.i("Failed", t.getMessage());
                    }
                });
            }
        });
        server = RestService.Factory.getIstance();

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestService.Factory.getIstance().getLocations().enqueue(new Callback<List<MyLocation>>() {
                    @Override
                    public void onResponse(Call<List<MyLocation>> call, Response<List<MyLocation>> response) {
                        locatiiDate = new ArrayList<>();
                        for (MyLocation location : response.body()) {
                            if (location.getZi() == Calendar.getInstance().getTime().getDay()) {
                                locatiiDate.add(location);
                            }
                        }
                        checkLocations(locatiiDate);
                    }

                    @Override
                    public void onFailure(Call<List<MyLocation>> call, Throwable t) {
                    }
                });
            }
        });
        return view;
    }

    private void checkLocations(ArrayList<MyLocation> locatiiDate) {
        MyLocation referinta = locatiiDate.get(0);
        MyLocation local = referinta;
        int nrLocatii = 0;
        int i = 1;
        try {
            do {
                if (new MyLocationHelper(referinta).distanceBetween2Locations(locatiiDate.get(i)) < 0.015) {
                    new MyLocationHelper(locatiiDate.get(i)).deleteLocation();
                    nrLocatii++;
                    local = locatiiDate.get(i);
                } else {
                    if (nrLocatii == 0 && referinta.getOraSfarsit() == null) {
                        new MyLocationHelper(referinta).deleteLocation();
                    } else {
                        new MyLocationHelper(referinta).updateLocation(local.getOraInceput());
                    }
                    nrLocatii = 0;
                    referinta = locatiiDate.get(i);
                    local = referinta;
                }
                i++;
            } while (i < locatiiDate.size());
            new MyLocationHelper(referinta).updateLocation(local.getOraInceput());
            Toast.makeText(getContext(), "s-a terminat", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e("checkLocations",e.getMessage());
        }
    }
}
