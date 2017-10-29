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
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: Replace up arrow with menu bar from main activity
// TODO: Replace dummy data with real data. ArrayList -> HashMap

public class DisplayStopsActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener {

    OCTranspo octAPI;
    StopAdapter stopAdapter; // Takes OCStop data and puts it into stop_layout.xml
    RecyclerView rv; // Only shows items on or near the screen, more efficient for long lists

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_stops);

        octAPI = OCTranspo.getInstance();

        /* Set up a RecyclerView so we can display the stops nicely */
        rv = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm); // llm makes rv have a linear layout (default is vertical)
        stopAdapter = new StopAdapter(this, octAPI.gtfsTable.stopTable);
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
        for(HashMap.Entry<String, OCStop> stop : octAPI.gtfsTable.stopTable.entrySet()) {
            /* Stop names should all be in uppercase by default but search results were
               behaving oddly so we're setting everything to lowercase */
            String stopName = stop.getValue().getStopName().toLowerCase();
            String stopCode = "" + stop.getValue().getStopCode();

            // We search by stop name and by stop code so check both
            if(stopName.contains(newText) || stopCode.contains(newText)) {
                newList.add(stop.getValue());
            }
        }

        // Update the adapter with the newly filtered list
        stopAdapter.setFilter(newList);
        return true;
    }
}
