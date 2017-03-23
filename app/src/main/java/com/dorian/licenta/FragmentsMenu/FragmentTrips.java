package com.dorian.licenta.FragmentsMenu;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dorian.licenta.FragmentsTrip.LocationFinder;
import com.dorian.licenta.R;
import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;

/**
 * Created by Dorian on 22/03/2017.
 */

public class FragmentTrips extends Fragment {

    private EditText editTextLocatie;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trips, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editTextLocatie = (EditText) view.findViewById(R.id.editText_location);
        editTextLocatie.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    LocationFinder locationFinder = new LocationFinder(editTextLocatie.getText().toString(), getContext());
                    try {
                        locationFinder.execute();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }
}