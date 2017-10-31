package com.beattheheat.beatthestreet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.beattheheat.beatthestreet.Networking.OC_API.OCBus;
import com.beattheheat.beatthestreet.Networking.OC_API.OCRoute;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;
import com.beattheheat.beatthestreet.Networking.SCallable;

import java.util.ArrayList;
import java.util.HashMap;

public class DisplayRoutesForStopActivity extends AppCompatActivity {

    String stopCode; // stopCode of the stop we want to display
    OCTranspo octAPI;
    ArrayList<OCRoute> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_routes_for_stop);

        stopCode = getIntent().getStringExtra("STOPCODE");
        octAPI = OCTranspo.getInstance();

        octAPI.GetNextTripsForStopAllRoutes(stopCode, new SCallable<HashMap<Integer, OCBus[]>>() {
            @Override
            public void call(HashMap<Integer, OCBus[]> arg) {
                int i = 0;
            }
        });
    }
}
