package com.dorian.licenta.FragmentsMenu;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.dorian.licenta.FragmentsTrip.LocationFinder;
import com.dorian.licenta.R;

import java.io.UnsupportedEncodingException;

/**
 * Created by Dorian on 22/03/2017.
 */

public class FragmentTrips extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trips,null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((Button)view.findViewById(R.id.btn_test)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationFinder locationFinder = new LocationFinder("london");
                try {
                    locationFinder.execute();
                    Toast.makeText(getContext(),locationFinder.latLng.toString(),Toast.LENGTH_LONG).show();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
