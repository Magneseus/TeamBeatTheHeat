package com.beattheheat.beatthestreet;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.beattheheat.beatthestreet.Networking.NotificationUtil;
import com.beattheheat.beatthestreet.Networking.OC_API.GTFS;
import com.beattheheat.beatthestreet.Networking.OC_API.OCStop;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTrips;
import com.beattheheat.beatthestreet.Networking.OC_API.OCRoute;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;


import java.util.ArrayList;

/**
 * Created by lauramcdougall on 2017-11-14.
 */

public class DisplayStopsForRouteActivity extends AppCompatActivity {

    OCTranspo octAPI;
    ArrayList<OCRoute> routeList;
    StopsForRouteAdapter sfrAdapter;
    String routeCode;
    int busNo;
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_routes_for_stop);

        octAPI = OCTranspo.getInstance();

        routeList = new ArrayList<>(octAPI.gtfsTable.getRouteList());

        routeCode = getIntent().getStringExtra("ROUTE");

        String[] routeInfo = routeCode.split("~");

        busNo = Integer.parseInt(routeInfo[0]);
        String busDir = routeInfo[1];
        String tripId = "";

        OCRoute thisRoute = octAPI.gtfsTable.getRoute(busNo);

        //TODO: catch errors if trip not found
        int i = 0;
        while (!thisRoute.getRouteNames().get(i).equals(busDir) && i < thisRoute.getRouteNames().size() - 1){
            i++;
        }
        tripId = thisRoute.getTrips().get(i);

        String [] stopIds = octAPI.gtfsTable.getAllStopsWithID(tripId);

        ArrayList<OCStop> stopNames = new ArrayList<OCStop>();
        for (int j = 0; j < stopIds.length; j++){
            stopNames.add(octAPI.gtfsTable.getStop(stopIds[j]));
        }

        rv = (RecyclerView) findViewById(R.id.rfs_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        sfrAdapter = new StopsForRouteAdapter(this, stopNames);
        rv.setAdapter(sfrAdapter);
        }

    // User has tapped a stop, go to detailed stop page
    public void onClick(String stopCodeStr, String stopNameStr) {
        class RunnablePointer {
            public Runnable run;
            public RunnablePointer(Runnable run) {
                this.run = run;
            }
        }

        class TimeHolder {
            public long time;
            public TimeHolder(long t) { this.time = t; }
        }

        final Context ctx = this;

        final TimeHolder time = new TimeHolder(SystemClock.elapsedRealtime());

        final Handler handler = new Handler();
        final RunnablePointer runPointer = new RunnablePointer(null);
        runPointer.run = new Runnable() {
            @Override
            public void run() {
                int minutesInMillis = (int)(0.5*60*1000);
                float elapsedMillis = (SystemClock.elapsedRealtime() - time.time);
                int remainingMillis = (int) (minutesInMillis - elapsedMillis);

                NotificationUtil.getInstance().notify(ctx, 0, "Timer", ""
                        + (int) (remainingMillis / (1000 * 60)) + ":" // minutes
                        + (int) (remainingMillis % (1000 * 60))/1000); // seconds

                if(remainingMillis < 0) {
                    time.time = SystemClock.elapsedRealtime();
                }

                handler.postDelayed(runPointer.run, 1000);
            }
        };

        handler.postDelayed(runPointer.run, 1000);

        Intent intent = new Intent(this, DisplayRoutesForStopActivity.class);
        intent.putExtra("STOPCODE", stopCodeStr);
        intent.putExtra("STOPNAME", stopNameStr);
        intent.putExtra("ROUTECODE", busNo);
        startActivity(intent);

    }
}
