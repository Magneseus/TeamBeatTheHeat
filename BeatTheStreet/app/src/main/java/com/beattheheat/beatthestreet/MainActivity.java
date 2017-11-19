package com.beattheheat.beatthestreet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.beattheheat.beatthestreet.Networking.LocationWrapper;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;
import com.beattheheat.beatthestreet.Networking.SCallable;

/**
 * The main activity for our application. Based off the side-menu navigation activity.
 *
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    MainAdapter mainAdapter;
    RecyclerView rv;

    // Initialization function (Constructor)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocationWrapper.create(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // OCTranspo API caller
        OCTranspo octAPI = OCTranspo.getInstance();
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
        }

        final Context ctx = this;

        octAPI.LoadGTFS(new SCallable<Boolean>() {
            @Override
            public void call(Boolean arg) {
                rv = findViewById(R.id.display_stops_recycler_view);
                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                rv.setLayoutManager(llm);
                mainAdapter = new MainAdapter(ctx);
                rv.setAdapter(mainAdapter);
            }
        });
    }

    // called when app is opened
    @Override
    protected void onStart() {
        LocationWrapper.getInstance().connect();
        super.onStart();
    }

    // called when app is turned off
    @Override
    protected void onDestroy() {
        LocationWrapper.getInstance().disconnect();
        super.onDestroy();
    }

    // Closes navigation drawer if open, does default action if not.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // TODO: this
    // User has tapped a stop, go to timetable page
    public void onClick(String stopCodeStr) {
        Intent intent = new Intent(this, DisplayRoutesForStopActivity.class);
        intent.putExtra("STOPCODE", stopCodeStr);
        //startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_view_stops) {
            // View all stops
            Intent intent = new Intent(this, DisplayStopsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_view_routes) {
            // View all routes
            Intent intent = new Intent(this, DisplayRoutesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            // TODO: Settings activity
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(LocationWrapper.getInstance() != null) {
            LocationWrapper.getInstance().startRequestingUpdates();
        }
    }
}
