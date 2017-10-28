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

    StopAdapter stopAdapter; // Takes OCStop data and puts it into stop_layout.xml
    RecyclerView rv; // Only shows items on or near the screen, more efficient for long lists
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
           stop Lat and Lon are currently just blank */
        // TODO: Implement location-based search i.e."find stops near me"
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
        rv.setLayoutManager(llm); // llm makes rv have a linear layout (default is vertical)
        stopAdapter = new StopAdapter(this, dummyStops);
        rv.setAdapter(stopAdapter);
    }

    // Set up the search menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    // Do nothing, we update search results live so we don't need this method
    @Override
    public boolean onQueryTextSubmit(String query){
        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();

        // Set up a new list that will contain the search results
        ArrayList<OCStop> newList = new ArrayList<OCStop>();

        // TODO: Replace this dummy data
        for(OCStop stop : dummyStops) {
            /* Stop names should all be in uppercase by default but search results were
               behaving oddly so we're setting everything to lowercase */
            String stopName = stop.getStopName().toLowerCase();
            String stopCode = "" + stop.getStopCode();

            // We search by stop name and by stop code so check both
            if(stopName.contains(newText) || stopCode.contains(newText)) {
                newList.add(stop);
            }
        }

        // Update the adapter with the newly filtered list
        stopAdapter.setFilter(newList);
        return true;
    }
}
