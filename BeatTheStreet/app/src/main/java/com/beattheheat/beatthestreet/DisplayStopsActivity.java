package com.beattheheat.beatthestreet;

import android.content.Intent;
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

/**
 *  Created by Matt on 2017-10-27
 *
 *  Displays a list of all stops in the GTFS file that can be searched through.
 *  Selecting a stop brings you to a detailed page showing live bus info.
 */

// TODO: Add main menu bar from MainActivity
// TODO: If no GPS available, sort by number
public class DisplayStopsActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener {

    OCTranspo octAPI;
    ArrayList<OCStop> stopList;
    StopAdapter stopAdapter; // Takes OCStop data and puts it into stop_layout.xml
    RecyclerView rv; // Only shows items on or near the screen, more efficient for long lists

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_stops);

        octAPI = OCTranspo.getInstance();
        stopList = new ArrayList<>(octAPI.gtfsTable.getStopList());

        // Trim excess quotes from stopName
        for(OCStop stop : stopList)
            stop.setStopName(stop.getStopName().replaceAll("\"", ""));

        /* Set up a RecyclerView so we can display the stops nicely */
        rv = (RecyclerView) findViewById(R.id.display_stops_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm); // llm makes rv have a linear layout (default is vertical)
        stopAdapter = new StopAdapter(this, stopList);
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
    public boolean onQueryTextSubmit(String query) { return false; }


    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();

        // Set up a new list that will contain the search results
        ArrayList<OCStop> newList = new ArrayList<>();

        for(OCStop stop : stopList) {
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

    // User has tapped a stop, go to detailed stop page
    public void onClick(String stopCodeStr) {
        //int stopCode = Integer.parseInt(stopCodeStr);
        Intent intent = new Intent(this, DisplayRoutesForStopActivity.class);
        intent.putExtra("STOPCODE", stopCodeStr);
        startActivity(intent);
    }
}
