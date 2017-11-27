package com.beattheheat.beatthestreet;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTrips;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class TimetableActivity extends AppCompatActivity  {

    OCTranspo octAPI;
    int routeNo;
    int stopCode;
    OCTrips[] tripTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        // Get our API instance
        octAPI = OCTranspo.getInstance();

        // Get the route number and stop code out of the intent
        routeNo  = getIntent().getIntExtra("ROUTENUMBER", -1);
        stopCode = getIntent().getIntExtra("STOPCODE", -1);

        // Set the activity title
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Route " + routeNo + " Timetable");

        // Start loading trips from the database
        new LoadTrips().execute();
    }

    /* Helper class to load trip info from database in backgroud */
    private class LoadTrips extends AsyncTask<Void, Void, Void> {
        @Override
        protected final Void doInBackground(Void... nothing) {
            // Populate our list of trips
            tripTimes = octAPI.gtfsTable.getTripsForRouteAtStop(routeNo, stopCode);
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            // Done loading trips, show them on the screen
            displayTimes();
        }
    }

    // Dynamically create TextViews to display trip times
    private void displayTimes() {
        /* Separate our trips list into Weekday trips, Saturday trips, and Sunday trips */
        ArrayList<OCTrips> weekdayTrips = new ArrayList<>();
        for (OCTrips trip : tripTimes) {
            if (trip.getTripID().toLowerCase().contains("weekday"))
                weekdayTrips.add(trip);
        }

        /* Sort the list of trips by arrival time */
        Collections.sort(weekdayTrips, new Comparator<OCTrips>() {
            @Override
            public int compare(OCTrips trip, OCTrips otherTrip) {
                if (trip.getArrivalHour() > otherTrip.getArrivalHour()) return 1;
                else if (trip.getArrivalHour() < otherTrip.getArrivalHour()) return -1;
                else { // Same hour
                    if (trip.getArrivalMinute() > otherTrip.getArrivalMinute()) return 1;
                    else if (trip.getArrivalMinute() < otherTrip.getArrivalMinute()) return -1;
                    else return 0; // Same hour and minute
                }
            }
        });

        /* Format trips as 2D array, 3 columns wide */
        ArrayList<OCTrips[]> outerList = new ArrayList<>();
        OCTrips[] innerList = new OCTrips[3];
        int count = 0;
        for (OCTrips trip : weekdayTrips) {
            if (count > 2) {
                outerList.add(innerList);
                innerList = new OCTrips[3];
                count = 0;
            }
            innerList[count++] = trip;
        }

        /* Set up the view of weekday trips */
        LinearLayout outerLayout = findViewById(R.id.timetable_layout);

        for (OCTrips[] tripArray : outerList) {
            // Set up a horizontal linear layout to hold 3 trips
            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.HORIZONTAL);
            innerLayout.setWeightSum(3);
            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                                     LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                                     1.0f);

            // Populate innerLayout
            for(OCTrips currentTrip : tripArray) {
                // Set up the textview
                TextView textview = new TextView(this);
                textview.setLayoutParams(textViewParams);
                textview.setGravity(Gravity.CENTER);
                String arrivalHour = String.format(Locale.CANADA, "%2d", currentTrip.getArrivalHour());
                String arrivalMins = String.format(Locale.CANADA, "%02d", currentTrip.getArrivalMinute());
                String arrivalTime = arrivalHour + ":" + arrivalMins;

                // Format the textview
                textview.setText(arrivalTime);
                textview.setTextColor(Color.BLACK);
                textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

                innerLayout.addView(textview);
            }

            // Add innerLayout to outerLayout
            outerLayout.addView(innerLayout);
        }
    }
}
