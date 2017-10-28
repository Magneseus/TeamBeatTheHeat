package com.beattheheat.beatthestreet;

import android.location.Location;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.beattheheat.beatthestreet.Networking.OC_API.OCStop;

import java.util.ArrayList;

// TODO: Replace up arrow with menu bar from main activity
// TODO: Replace dummy data with real data. ArrayList -> HashMap

public class DisplayStopsActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener {

    StopAdapter stopAdapter;
    RecyclerView rv;
    // Dummy data
    ArrayList<OCStop> dummyStops = new ArrayList<OCStop>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_stops);

        // Initialise the dummy stops
        /* stopId is string in format "AA000"
           stopCode is int from 0 to 9999
           stopName is string eg "Bank / Walkley"
           stop Lat and Lon are not currently implemented */
        Location l = new Location("");
        dummyStops.add(new OCStop("AA000", 0, "Carleton Station", l));
        dummyStops.add(new OCStop("BB105", 105, "Bank / Walkley", l));
        dummyStops.add(new OCStop("CC568", 5687, "Charlemagne / Watters", l));
        dummyStops.add(new OCStop("DD684", 6849, "Prince of Wales / Dynes", l));
        dummyStops.add(new OCStop("EE638", 6382, "Matt's Curbside Couch", l));
        dummyStops.add(new OCStop("FF668", 6687, "The Shadowfell", l));
        dummyStops.add(new OCStop("GG876", 8764, "My Apartment", l));
        dummyStops.add(new OCStop("HH683", 6832, "Herzberg Labs", l));
        dummyStops.add(new OCStop("II037", 374, "UC Tim's", l));
        dummyStops.add(new OCStop("JJ983", 9831, "Loeb Cafe", l));

        /* Set up a RecyclerView so we can display the stops nicely */
        rv = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        stopAdapter = new StopAdapter(this, dummyStops);
        rv.setAdapter(stopAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query){
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();
        ArrayList<OCStop> newList = new ArrayList<OCStop>();
        for(OCStop stop : dummyStops) {
            String stopName = stop.getStopName().toLowerCase();
            String stopCode = "" + stop.getStopCode();
            if(stopName.contains(newText) || stopCode.contains(newText)) {
                // We search by stop name and by stop code so check both
                newList.add(stop);
            }
        }
        stopAdapter.setFilter(newList);
        return true;
    }
}
