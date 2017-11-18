package com.beattheheat.beatthestreet;

import android.content.Context;
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
                rv = findViewById(R.id.rfs_recycler_view);
                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                rv.setLayoutManager(llm); // llm makes rv have a linear layout
                rfsAdapter = new RoutesForStopAdapter(context, busList);
                rv.setAdapter(rfsAdapter);
            }
        });
    }

    // User has tapped a route at this stop, go to timetable page
    public void onClick(int routeNumber) {
/*        // TODO: clean up this disgusting mess
        class RunnablePointer {
            public int iterations = 0;
            public Runnable run;
            public RunnablePointer(Runnable run) {
                this.run = run;
            }
        }
        final Context ctx = this;

        final Handler handler = new Handler();
        final RunnablePointer runPointer = new RunnablePointer(null);
        runPointer.run = new Runnable() {
            @Override
            public void run() {
                // difference in milliseconds
                float millisUntilBus = minUntilBus*60000 - runPointer.iterations*1000;

                // TODO: give a little bus icon to this notification
                // TODO: ping the servers to update bus time ever so often
                // TODO: color notif red if bus is more than X minutes off (7?)
                NotificationUtil.getInstance().notify(ctx, 0, "Bus", "Bus will arrive in " +
                        (millisUntilBus/60000) + " minutes.");

                if (millisUntilBus >= 0) {
                    runPointer.iterations++;
                    handler.postDelayed(runPointer.run, 1000);
                }
            }
        };

        final long start = SystemClock.elapsedRealtime();
        final Handler handler = new Handler();
        final RunnablePointer runPointer = new RunnablePointer(null);
        runPointer.run = new Runnable() {
            @Override
            public void run() {
                NotificationUtil.getInstance().notify(ctx, 0, "Timer", "" + (SystemClock.elapsedRealtime()
                        - start)/1000 + "s");
                handler.postDelayed(runPointer.run, 1000);
            }
        };

        handler.postDelayed(runPointer.run, 1000);*/
    }
}
