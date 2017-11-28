package com.beattheheat.beatthestreet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;

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

        rv = findViewById(R.id.recycler_view);
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

            ArrayList<String> routeNames1 = new ArrayList<>();
            ArrayList<String> routeNames2 = new ArrayList<>();
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
        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    // Do nothing, we update search results live so we don't need this method
    @Override
    public boolean onQueryTextSubmit(String searchText) { return false; }

    @Override
    public boolean onQueryTextChange(String searchText) {
        searchText = searchText.toLowerCase();

        // Set up a list that will contain the search results
        ArrayList<OCRoute> newList = new ArrayList<>();

        for (OCRoute route : routeList) {
            String routeNumber = "" + route.getRouteNo();
            String routeName   = route.getRouteNames().get(0).toLowerCase().replaceAll("\"", "");

            // We search by route name and number so check both
            if (routeNumber.contains(searchText) || routeName.contains(searchText)) {
                newList.add(route);
            }
        }

        // Update the adapter with the newly filtered list
        routeAdapter.setFilter(newList);
        return true;
    }

    public void onClick(String route) {
        Intent intent = new Intent(this, DisplayStopsForRouteActivity.class);
        intent.putExtra("ROUTE", route);
        startActivity(intent);
    }
}

