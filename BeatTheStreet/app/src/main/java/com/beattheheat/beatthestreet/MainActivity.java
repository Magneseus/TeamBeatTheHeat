package com.beattheheat.beatthestreet;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;
import com.beattheheat.beatthestreet.Networking.SCallable;

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
        octAPI = new OCTranspo(this.getApplicationContext());
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

        if (id == R.id.nav_get_routes_1929) {
            octAPI.GetRouteSummaryForStop("1929", new SCallable<String>() {
                @Override
                public void call(String arg) {
                    tv.setText(arg);
                }
            });
        } else if (id == R.id.nav_get_routes) {
            octAPI.GetRouteSummaryForStop(stopNum.getText().toString(), new SCallable<String>() {
                @Override
                public void call(String arg) {
                    tv.setText(arg);
                }
            });
        } else if (id == R.id.nav_get_times_stop) {
            octAPI.GetNextTripsForStopAllRoutes(stopNum.getText().toString(), new SCallable<String>() {
                @Override
                public void call(String arg) {
                    tv.setText(arg);
                }
            });
        } else if (id == R.id.nav_get_gtfs) {
            octAPI.GTFS(this);

            String list = "";
            for (String s : getFilesDir().list()) {
                list += s;
                list += "\n";
            }

            tv.setText(list);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
