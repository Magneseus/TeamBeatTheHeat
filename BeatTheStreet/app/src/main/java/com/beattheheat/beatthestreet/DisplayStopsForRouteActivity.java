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
        System.out.println("BUSDIR:" + busDir);

        //TODO: catch errors if trip not found
        int i = 0;
        while (!thisRoute.getRouteNames().get(i).replace("\"", "").equals(busDir) && i < thisRoute.getRouteNames().size()){
            System.out.println("ROUTE: " + thisRoute.getRouteNames().get(i));
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

    // User has tapped a stop, go to timetable page
    public void onClick(int stopCode) {
        Intent intent = new Intent(this, TimetableActivity.class);
        intent.putExtra("ROUTENUMBER", busNo);
        intent.putExtra("STOPCODE", stopCode);
        startActivity(intent);

    }
}
