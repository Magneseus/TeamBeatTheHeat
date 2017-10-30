package com.beattheheat.beatthestreet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.beattheheat.beatthestreet.Networking.LocationWrapper;
import com.beattheheat.beatthestreet.Networking.NotificationUtil;
import com.beattheheat.beatthestreet.Networking.OC_API.OCBus;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;
import com.beattheheat.beatthestreet.Networking.SCallable;

import java.util.HashMap;
import java.util.Map;

/**
 * The main activity for our application. Based off the side-menu navigation activity.
 *
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Our OCAPI instance, for bus/stop information
    private OCTranspo octAPI;


    // Initialization function (Constructor)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Scrollbar for text view
        TextView tv = (TextView) findViewById(R.id.textView3);
        tv.setMovementMethod(new ScrollingMovementMethod());

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

        /* TODO: Remove test code
           This code shows off some features.
              - subscribe(SCallable): calls the given function whenever we recieve a location update
              - notify(Context, id, Title, Content): creates/updates notification

              Then calculates the distance between your current location and my house.

              Since this function is subscribed to location updates, whenever a new location update
                is recieved, it raises a notification that gives the approximate distance the phone
                is from my house.
        */

        final Context ctx = this;
        LocationWrapper.getInstance(this).subscribe(new SCallable() {
            @Override
            public void call(Object arg) {
                Location x1 = LocationWrapper.getInstance(ctx).getLocation();
                if(x1 == null) return;

                Location x2 = new Location("");
                x2.setLatitude(44.7518833);
                x2.setLongitude(-79.7102258);

                float[] dist = new float[1];
                Location.distanceBetween(x1.getLatitude(), x1.getLongitude(),
                        x2.getLatitude(), x2.getLongitude(), dist);

                NotificationUtil.getInstance().notify(ctx, 1, "Distance",
                        dist[0] + "m R: " + Math.random());

                if(dist[0] < 100) {
                    NotificationUtil.getInstance().notify(ctx, 2, "You're here!",
                            dist[0] + "m R: " + Math.random(), -1, false,
                            Settings.System.DEFAULT_ALARM_ALERT_URI);

                    LocationWrapper.getInstance(ctx).unsubscribe(this);
                }
            }
        });

        NotificationUtil.getInstance().notify(this, 0, "Welcome to Test1");
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Get Stop number
        TextView stopNum = (TextView) findViewById(R.id.text_stopNo);

        // Text View Stuff
        final TextView tv = (TextView) findViewById(R.id.textView3);

        if (id == R.id.nav_view_stops) {
            // View all stops
            /* Starts a new activity that will display all stops saved from GTFS
               From there you can search for a stop and then go to a detailed stop view */
            Intent intent = new Intent(this, DisplayStopsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_get_location) {
            Location loc = LocationWrapper.getInstance(this).getLocation();

            if(loc == null)
            {
                tv.setText("No Location yet.");
            } else {
                tv.setText("Lat: " + loc.getLatitude() + " Lon: " + loc.getLongitude() + " R: " + Math.random());
            }
        } else if (id == R.id.nav_get_routes) {
            octAPI.GetRouteSummaryForStop(stopNum.getText().toString(), new SCallable<int[]>() {
                @Override
                public void call(int[] arg) {
                    String s = "[";
                    for (int i : arg) {
                        s += i;
                        s += ",";
                    }
                    s = s.substring(0, s.length()-1);
                    s += "]";
                    tv.setText(s);
                }
            });
        } else if (id == R.id.nav_get_times_stop) {
            octAPI.GetNextTripsForStopAllRoutes(stopNum.getText().toString(), new SCallable<HashMap<Integer,OCBus[]>>() {
                @Override
                public void call(HashMap<Integer,OCBus[]> arg) {
                    for (Map.Entry<Integer,OCBus[]> entry : arg.entrySet()) {

                    }
                }
            });
        } else if (id == R.id.nav_get_gtfs) {
            octAPI.LoadGTFS(new SCallable<Boolean>() {
                @Override
                public void call(Boolean arg) {
                    tv.setText("" + arg.booleanValue());
                }
            });
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
