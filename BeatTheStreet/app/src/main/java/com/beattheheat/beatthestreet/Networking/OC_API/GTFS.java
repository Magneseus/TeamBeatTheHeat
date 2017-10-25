package com.beattheheat.beatthestreet.Networking.OC_API;

import java.util.HashMap;

/**
 * Created by Matt on 24-Oct-17.
 */

public class GTFS {
    HashMap<Integer, OCRoute> routeTable;
    HashMap<String, OCTrip> tripTable;
    HashMap<String, OCStop> stopTable;

    public GTFS() {
        routeTable = new HashMap<>(200);
        tripTable = new HashMap<>(18000);
        stopTable = new HashMap<>(5700);
    }
}
