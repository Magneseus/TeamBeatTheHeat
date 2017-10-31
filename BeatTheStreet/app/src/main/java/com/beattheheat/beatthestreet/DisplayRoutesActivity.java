package com.beattheheat.beatthestreet;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.beattheheat.beatthestreet.Networking.OC_API.OCRoute;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;

import java.util.ArrayList;

public class DisplayRoutesActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener {

    OCTranspo octAPI;
    RouteAdapter routeAdapter;
    RecyclerView rv;
    ArrayList<OCRoute> routeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_routes);

        octAPI = OCTranspo.getInstance();
        routeList = new ArrayList<> (octAPI.gtfsTable.getRouteList());

        rv = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        routeAdapter = new RouteAdapter(this.getApplicationContext(), routeList);
        rv.setAdapter(routeAdapter);
    }

    // Set up the search menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    //search for route number
    @Override
    public boolean onQueryTextSubmit(String searchText){
        ArrayList<OCRoute> newList = new ArrayList<OCRoute>();

        for( OCRoute route : routeList) {
            int routeName = route.getRouteNo();

            if(routeName == Integer.parseInt(searchText)) {
                newList.add(route);
            }
        }
        routeAdapter.setFilter(newList);
        return true;
    }

    //wait until user has submitted route to search
    //@Override
    public boolean onQueryTextChange(String searchText) {
        return false;
    }

}

