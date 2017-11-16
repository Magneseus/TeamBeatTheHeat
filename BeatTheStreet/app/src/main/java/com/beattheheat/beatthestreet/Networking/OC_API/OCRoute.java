package com.beattheheat.beatthestreet.Networking.OC_API;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matt on 24-Oct-17.
 */

public class OCRoute implements Comparable {
    private int routeNo;
    private List<String> routeNames;
    private List<String> trips;

    private OCRoute(int routeNo, List<String> routeNames, List<String> trips) {
        setRouteNo(routeNo);
        setRouteNames(routeNames);
        setTrips(trips);
    }

    public OCRoute(int routeNo, List<String> routeNames) {
        this(routeNo, routeNames, new ArrayList<String>());
    }


    /** The GTFS entry is assumed to be from the "trips.txt" table
     *
     * Example:
     *
     * route_id	 service_id	                trip_id	                            trip_headsign	direction_id  block_id
     * 6-277	 SEPT17-SEPDA17-Weekday-21	49754901-SEPT17-SEPDA17-Weekday-21	Rockcliffe	    1	          5778334
     *
     */
    public static void LoadRoute(GTFS gtfs, String gtfsEntry) {
        if (gtfsEntry.isEmpty())
            return;

        String[] entries = gtfsEntry.split(",");
        if (entries.length < 5)
            return;

        int routeNo = Integer.parseInt(entries[0].split("-")[0]);

        String tripId = entries[2];
        String routeName = entries[3];

        /*
        // Check if the trip exists yet
        if (gtfs.tripTable.containsKey(tripId)) {
            tripToAdd = gtfs.tripTable.get(tripId);
        }
        else {
            throw new GTFSException("Unable to find trip ID: " + tripId + " for route: " + routeNo);
        }
        */

        // Check if this route has already been entered
        if (gtfs.routeTable.containsKey(routeNo)) {
            gtfs.routeTable.get(routeNo).addRouteName(routeName);
            gtfs.routeTable.get(routeNo).addTrip(tripId);
        }
        else {
            List<String> routeNames = new ArrayList<String>();
            routeNames.add(routeName);
            List<String> trips = new ArrayList<String>();
            trips.add(tripId);

            gtfs.routeTable.put(routeNo, new OCRoute(routeNo, routeNames, trips));
        }
    }

    public int compareTo(@NonNull Object otherRoute) {
        return this.getRouteNo() - ((OCRoute)otherRoute).getRouteNo();
    }

    /*********************
     * Getters & Setters *
     *********************/

    public int getRouteNo() {
        return routeNo;
    }

    private void setRouteNo(int routeNo) {
        this.routeNo = routeNo;
    }

    public List<String> getRouteNames() {
        return routeNames;
    }

    private void setRouteNames(List<String> routeNames) {
        this.routeNames = routeNames;
    }

    private void addRouteName(String routeName) {
        this.routeNames.add(routeName);
    }

    public List<String> getTrips() {
        return trips;
    }

    private void setTrips(List<String> trips) {
        this.trips = trips;
    }

    private void addTrip(String trip) {
        this.trips.add(trip);
    }
}
