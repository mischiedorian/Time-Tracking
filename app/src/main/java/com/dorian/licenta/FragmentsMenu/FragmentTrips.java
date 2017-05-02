package com.dorian.licenta.FragmentsMenu;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.dorian.licenta.FragmentsTrip.LocationFinder;
import com.dorian.licenta.R;

import java.io.UnsupportedEncodingException;

public class FragmentTrips extends Fragment {

    private EditText editTextLocatie;
    public static ProgressDialog progressBar;

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
                        progressBar = new ProgressDialog(getContext());
                        progressBar.setTitle("Loading Maps");
                        progressBar.setMessage("Searching Location...");
                        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressBar.setIndeterminate(true);
                        progressBar.setCancelable(false);
                        progressBar.show();
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