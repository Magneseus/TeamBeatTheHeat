package com.beattheheat.beatthestreet;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.beattheheat.beatthestreet.Networking.OC_API.OCBus;
import com.beattheheat.beatthestreet.Networking.OC_API.OCRoute;
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

// TODO: Add main menu bar from MainActivity
    // Every OCBus in the HashMap also has a routeNo stored in it so we can just pull the buses out
public class DisplayRoutesForStopActivity extends AppCompatActivity {

    String stopCode; // stopCode of the stop we want to display
    OCTranspo octAPI;
    ArrayList<OCBus[]> busList;
    RoutesForStopAdapter rfsAdapter; //Places route data into the RecyclerView's layout
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_routes_for_stop);

        // Get stop info based on the stopcode we're given
        stopCode = getIntent().getStringExtra("STOPCODE");
        octAPI = OCTranspo.getInstance();
        final Context context = this;
        // TODO: Catch errors if no connection
        octAPI.GetNextTripsForStopAllRoutes(stopCode, new SCallable<HashMap<Integer, OCBus[]>>() {
            @Override
            public void call(HashMap<Integer, OCBus[]> arg) {
                busList = new ArrayList<>();
                // Filter out routes that have no upcoming stops
                for (OCBus[] busArray : arg.values()) {
                    if (busArray != null && busArray.length > 0)
                        busList.add(busArray);
                }

                // Sort ArrayList of OCBus arrays by route number
                Collections.sort(busList, new Comparator<OCBus[]>() {
                    public int compare(OCBus[] busArray, OCBus[] otherArray) {
                        return busArray[0].compareTo(otherArray[0]);
                    }
                });

                // Set up the RecyclerView to display route and time info
                rv = (RecyclerView) findViewById(R.id.rfs_recycler_view);
                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                rv.setLayoutManager(llm); // llm makes rv have a linear layout
                rfsAdapter = new RoutesForStopAdapter(context, busList);
                rv.setAdapter(rfsAdapter);
            }
        });
    }

}
