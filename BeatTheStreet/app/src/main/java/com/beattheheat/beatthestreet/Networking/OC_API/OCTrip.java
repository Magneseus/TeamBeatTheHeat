package com.beattheheat.beatthestreet.Networking.OC_API;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO: Convert to something that isn't java.util.Date

/**
 * Created by Matt on 24-Oct-17.
 */

public class OCTrip {
    private String tripId;
    private List<String> stops;
    private List<Date> stopTimes;

    private OCTrip(String tripId, List<String> stops, List<Date> stopTimes) {
        setTripId(tripId);
        setStops(stops);
        setStopTimes(stopTimes);
    }

    private OCTrip(String tripId) {
        this(tripId, new ArrayList<String>(), new ArrayList<Date>());
    }


    /** The GTFS entry is assumed to be from the "stop_times.txt" table
     *
     * Example:
     *
     * trip_id	                            arrival_time	departure_time	stop_id	 stop_sequence	pickup_type	 drop_off_type
     * 49754901-SEPT17-SEPDA17-Weekday-21	05:48:00	    05:48:00	    RF900	 1	            0	         0
     *
     */
    public static void LoadTrip(GTFS gtfs, String gtfsEntry) {
        if (gtfsEntry.isEmpty())
            return;

        String[] entries = gtfsEntry.split(",");
        if (entries.length < 7)
            return;

        String tripId = entries[0];

        // Try to parse the timestamp into a Date obj
        String stopTimeString = entries[1];
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date stopTime = null;
        try {
            stopTime = sdf.parse(stopTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("DATE", "Unable to parse GTFS stop time. Trip ID: " + tripId);
        }

        String stopId = entries[3];

        // Check if this trip has already been entered
        if (gtfs.tripTable.containsKey(tripId)) {
            gtfs.tripTable.get(tripId).addStop(stopId);
            gtfs.tripTable.get(tripId).addStopTime(stopTime);
        }
        else {
            List<String> stops = new ArrayList<String>();
            stops.add(stopId);
            List<Date> stopTimes = new ArrayList<Date>();
            stopTimes.add(stopTime);

            gtfs.tripTable.put(tripId, new OCTrip(tripId, stops, stopTimes));

        }
    }



    /*********************
     * GETTERS & SETTERS *
     *********************/

    public String getTripId() {
        return tripId;
    }

    private void setTripId(String tripId) {
        this.tripId = tripId;
    }


    public List<String> getStops() {
        return stops;
    }

    private void setStops(List<String> stops) {
        this.stops = stops;
    }

    private void addStop(String stop) {
        this.stops.add(stop);
    }

    public List<Date> getStopTimes() {
        return stopTimes;
    }

    private void setStopTimes(List<Date> stopTimes) {
        this.stopTimes = stopTimes;
    }

    private void addStopTime(Date stopTime) {
        this.stopTimes.add(stopTime);
    }
}
