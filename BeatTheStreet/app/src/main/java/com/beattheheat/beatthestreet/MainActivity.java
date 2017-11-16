package com.beattheheat.beatthestreet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.LocationWrapper;
import com.beattheheat.beatthestreet.Networking.NotificationUtil;
import com.beattheheat.beatthestreet.Networking.OC_API.OCBus;
import com.beattheheat.beatthestreet.Networking.OC_API.OCStop;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;
import com.beattheheat.beatthestreet.Networking.SCallable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The main activity for our application. Based off the side-menu navigation activity.
 *
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {

    // Our OCAPI instance, for bus/stop information
    private OCTranspo octAPI;
    ArrayList<OCStop> stopList;
    StopAdapter stopAdapter; // Takes OCStop data and puts it into stop_layout.xml
    RecyclerView rv; // Only shows items on or near the screen, more efficient for long lists


    // Initialization function (Constructor)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // OCTranspo API caller
        octAPI = OCTranspo.getInstance();
        octAPI.setup(this.getApplicationContext());

        // TODO: handle location being turned off.
        // Could just treat all null location the same, regardless of reason
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);

            return;
        }

        final Context ctx = this;

        octAPI.LoadGTFS(new SCallable<Boolean>() {
            @Override
            public void call(Boolean arg) {
                stopList = new ArrayList<>(octAPI.gtfsTable.getStopList());
                // Trim excess quotes from stopName
                for(OCStop stop : stopList)
                    stop.setStopName(stop.getStopName().replaceAll("\"", ""));

                /* Set up a RecyclerView so we can display the stops nicely */
                rv = (RecyclerView) findViewById(R.id.display_stops_recycler_view);
                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                rv.setLayoutManager(llm); // llm makes rv have a linear layout (default is vertical)
                stopAdapter = new StopAdapter(ctx, stopList);
                rv.setAdapter(stopAdapter);
            }
        });
    }

    // called when app is opened
    @Override
    protected void onStart() {
        LocationWrapper.getInstance(this).connect();
        super.onStart();
    }

    // called when app is turned off
    @Override
    protected void onDestroy() {
        LocationWrapper.getInstance(this).disconnect();
        super.onDestroy();
    }

    // Closes navigation drawer if open, does default action if not.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
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

        for (OCStop stop : stopList) {
            /* Stop names should all be in uppercase by default but search results were
               behaving oddly so we're setting everything to lowercase */
            String stopName = stop.getStopName().toLowerCase();
            String stopCode = "" + stop.getStopCode();

            // We search by stop name and by stop code so check both
            if (stopName.contains(newText) || stopCode.contains(newText)) {
                newList.add(stop);
            }
        }

        // Update the adapter with the newly filtered list
        stopAdapter.setFilter(newList);
        return true;
    }

    // User has tapped a stop, go to detailed stop page
    public void onClick(String stopCodeStr) {
        Intent intent = new Intent(this, DisplayRoutesForStopActivity.class);
        intent.putExtra("STOPCODE", stopCodeStr);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_view_stops) {
            // View all stops
            Intent intent = new Intent(this, DisplayStopsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_view_routes) {
            // View all routes
            /* Starts a new activity that will display all routes saved from GTFS
               From there you can search for a route and then go to a detailed route view */
            Intent intent = new Intent(this, DisplayRoutesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_notify) {
            NotificationUtil.getInstance().notify(this, 1, "New Notification", "yo you pressed the button", -1, true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        LocationWrapper.getInstance(this).startRequestingUpdates();
    }
}
