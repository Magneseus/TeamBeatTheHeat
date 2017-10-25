package com.beattheheat.beatthestreet.Networking.OC_API;

import java.util.HashMap;

/**
 * Created by Matt on 24-Oct-17.
 */

public class GTFS {
    public HashMap<Integer, OCRoute> routeTable;
    public HashMap<String, OCTrip> tripTable;
    public HashMap<String, OCStop> stopTable;

    public GTFS() {
        routeTable = new HashMap<>(150);
        tripTable = new HashMap<>(17000);
        stopTable = new HashMap<>(5600);
    }
}
