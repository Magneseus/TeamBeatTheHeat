package com.beattheheat.beatthestreet.Networking.OC_API;

/**
 * Created by Matt on 24-Oct-17.
 */

public class OCStop {
    private String stopId;
    private int stopCode;
    private String stopName;
    // LAT AND LON

    // TODO: Add lat/lon coord vars

    private OCStop(String stopId, int stopCode, String stopName) {
        setStopId(stopId);
        setStopCode(stopCode);
        setStopName(stopName);
    }

    /** The GTFS entry is assumed to be from the "stops.txt" table.
     *
     * Example:
     *
     * stop_id	stop_code  stop_name	          stop_desc	 stop_lat	 stop_lon	 zone_id  stop_url  location_type
     * AA010	8767	   SUSSEX / RIDEAU FALLS             45.439869	 -75.695839	 0
     */
    public static void LoadStop(GTFS gtfs, String gtfsEntry) {
        if (gtfsEntry.isEmpty())
            return;

        String[] entries = gtfsEntry.split(",");
        if (entries.length < 9)
            return;

        String stopId = entries[0];
        int stopCode;
        try {
            stopCode = Integer.parseInt(entries[1]);
        } catch (NumberFormatException nfe) {
            // Stop code was missing, or not a number
            stopCode = -1;
        }
        String stopName = entries[2];

        gtfs.stopTable.put(stopId, new OCStop(stopId, stopCode, stopName));
    }



    /*********************
     * GETTERS & SETTERS *
     *********************/

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public int getStopCode() {
        return stopCode;
    }

    public void setStopCode(int stopCode) {
        this.stopCode = stopCode;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }
}
