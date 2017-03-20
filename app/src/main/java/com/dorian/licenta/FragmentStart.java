package com.dorian.licenta;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.dorian.licenta.RestService.MyLocation;
import com.dorian.licenta.RestService.RestService;

import java.util.ArrayList;
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

        locatiiDate = new ArrayList<>();

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
                            loc.add(new MyLocation(location.getId(), location.getOra(), location.getLat(), location.getLgn()));
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
                        String zi = response.body().get(0).getOra().split(" ")[0];
                        for (MyLocation location : response.body()) {
                            if (location.getOra().split(" ")[0].equals(zi)) {
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
        String ora = referinta.getOra();
        for (MyLocation location : locatiiDate) {
            double latDifference = Math.abs(referinta.getLat()) - Math.abs(location.getLat());
            double lgnDifference = Math.abs(referinta.getLgn()) - Math.abs(location.getLgn());
            if ((latDifference > 0.0013 && latDifference < 0.0014) || (lgnDifference > 0.0013 && lgnDifference < 0.0014)) {
                server.deleteLocation(location.getId() + "");
                Log.i("STERS", "STERS");
            } else {
                String oldHour = referinta.getOra().split(" ")[3];
                String newHour = ora.split(" ")[3];
                String[] tmp = referinta.getOra().split(" ");
                referinta.setOra(tmp[0] + " " + tmp[1] + " " + tmp[2] + " " + oldHour + "-" + newHour);
                server.modifyLocation(referinta.getId() + "", referinta);

                referinta = location;
            }

            ora = location.getOra();
        }

        Toast.makeText(getContext(), "s-a terminat", Toast.LENGTH_SHORT).show();
    }
}
