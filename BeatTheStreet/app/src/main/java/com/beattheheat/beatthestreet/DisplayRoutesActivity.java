package com.beattheheat.beatthestreet;

import android.content.Intent;
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
        routeList = new ArrayList<>(octAPI.gtfsTable.getRouteList());

        ArrayList<OCRoute> newRouteList = organizeRoutes(routeList);

        rv = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        routeAdapter = new RouteAdapter(this, newRouteList);
        rv.setAdapter(routeAdapter);
    }


    protected ArrayList<OCRoute> organizeRoutes(ArrayList<OCRoute> rL) {
        ArrayList<OCRoute> returnList = new ArrayList<>();

        for (OCRoute route : rL) {

            String dir1 = route.getRouteNames().get(0);
            String dir2 = "";
            for (int i = 0; i < route.getRouteNames().size(); i++) {
                if (!route.getRouteNames().get(i).equals(dir1)) {
                    dir2 = route.getRouteNames().get(i);
                    break;
                }
            }

            ArrayList<String> routeNames1 = new ArrayList<String>();
            ArrayList<String> routeNames2 = new ArrayList<String>();
            routeNames1.add(dir1);
            routeNames2.add(dir2);

            OCRoute newRoute1 = new OCRoute(route.getRouteNo(), routeNames1);
            OCRoute newRoute2 = new OCRoute(route.getRouteNo(), routeNames2);

            returnList.add(newRoute1);
            returnList.add(newRoute2);

        }


        return returnList;
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
    public boolean onQueryTextSubmit(String searchText) {
        ArrayList<OCRoute> newList = new ArrayList<OCRoute>();

        for (OCRoute route : routeList) {
            int routeName = route.getRouteNo();

            if (routeName == Integer.parseInt(searchText)) {
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

    //@Override
    public void onClick(String route) {
        Intent intent = new Intent(this, DisplayStopsForRouteActivity.class);
        intent.putExtra("ROUTE", route);
        startActivity(intent);
    }
}

