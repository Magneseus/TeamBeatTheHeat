package com.beattheheat.beatthestreet;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.beattheheat.beatthestreet.Networking.OC_API.OCBus;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;
import com.beattheheat.beatthestreet.Networking.SCallable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 *  Created by Matt on 2017-10-30
 *
 *  Displays a list of all routes that service a stop and the next (up to) 3 stop times
 */

public class DisplayRoutesForStopActivity extends AppCompatActivity {

    String stopCode; // stopCode of the stop we want to display
    String stopName;
    int routeName;
    OCTranspo octAPI;
    ArrayList<OCBus[]> busList;
    RoutesForStopAdapter rfsAdapter; //Places route data into the RecyclerView's layout
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_routes_for_stop);

        // Get stop info based on the stopCode we're given
        octAPI = OCTranspo.getInstance();
        stopCode = getIntent().getStringExtra("STOPCODE");
        stopName = getIntent().getStringExtra("STOPNAME");
        routeName = getIntent().getIntExtra("ROUTECODE", 0);

        // Set the activity title
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(stopName);

        final Context context = this;
        // TODO: Error screen if no connection
        // TODO: Loading screen?
        octAPI.GetNextTripsForStopAllRoutes(stopCode, new SCallable<HashMap<Integer, OCBus[]>>() {
            @Override
            public void call(HashMap<Integer, OCBus[]> arg) {
                busList = new ArrayList<>();
                // Filter out routes that have no upcoming stops
                for (OCBus[] busArray : arg.values()) {
                    if (busArray != null && busArray.length > 0 && (busArray[0].getRouteNo() == routeName || routeName == 0))
                        busList.add(busArray);
                }

                // Sort ArrayList of OCBus arrays by route number
                Collections.sort(busList, new Comparator<OCBus[]>() {
                    public int compare(OCBus[] busArray, OCBus[] otherArray) {
                        return busArray[0].compareTo(otherArray[0]);
                    }
                });

                // Set up the RecyclerView to display route and time info
                rv = findViewById(R.id.rfs_recycler_view);
                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                rv.setLayoutManager(llm); // llm makes rv have a linear layout
                rfsAdapter = new RoutesForStopAdapter(context, stopCode, busList);
                rv.setAdapter(rfsAdapter);
            }
        });
    }

    // User has tapped a route at this stop, go to timetable page
    public void onClick(int routeNumber) {
        Intent intent = new Intent(this, TimetableActivity.class);
        intent.putExtra("ROUTENUMBER", routeNumber);
        intent.putExtra("STOPCODE", Integer.parseInt(this.stopCode));
        startActivity(intent);
    }
}
