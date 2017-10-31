package com.beattheheat.beatthestreet.Networking.OC_API;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// TODO: Convert to something that isn't java.util.Date

/**
 * Created by Matt on 24-Oct-17.
 */

public class OCBus {
    private int routeNo;
    private String routeHeading;
    private String tripDestination;

    private Date tripStart;
    private int minsTilArrival;
    private float updateAge;

    private boolean lastTripOfDay;
    private String busType;

    private Location gpsLocation;
    private float gpsSpeed;

    public OCBus(JSONObject json, int routeNo, String routeHeading) {
        setRouteNo(routeNo);
        setRouteHeading(routeHeading);

        // Parse the json
        try {
            setTripDestination(json.getString("TripDestination"));

            // Parse the start time
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                setTripStart(sdf.parse(json.getString("TripStartTime")));
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("OC_ERR", "Unable to parse live stop time. Route no: " + routeNo);
            }

            setMinsTilArrival(json.getInt("AdjustedScheduleTime"));
            setUpdateAge((float)(json.getDouble("AdjustmentAge")));

            setLastTripOfDay(json.getBoolean("LastTripOfSchedule"));
            setBusType(json.getString("BusType"));

            if (isTimeLive()) {
                // Parse location coordinates
                Location location = new Location("OC_API");
                location.setLatitude(Location.convert(json.getString("Latitude")));
                location.setLongitude(Location.convert(json.getString("Longitude")));
                setGpsLocation(location);

                setGpsSpeed((float) (json.getDouble("GPSSpeed")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("OC_BUS", "Error parsing OCBus #" + routeNo);
        }
    }



    public boolean isTimeLive() {
        return getUpdateAge() > 0.0f;
    }

    /*********************
     * GETTERS & SETTERS *
     *********************/

    public int getRouteNo() {
        return routeNo;
    }

    public void setRouteNo(int routeNo) {
        this.routeNo = routeNo;
    }

    public String getRouteHeading() {
        return routeHeading;
    }

    public void setRouteHeading(String routeHeading) {
        this.routeHeading = routeHeading;
    }

    public String getTripDestination() {
        return tripDestination;
    }

    public void setTripDestination(String tripDestination) {
        this.tripDestination = tripDestination;
    }

    public Date getTripStart() {
        return tripStart;
    }

    public void setTripStart(Date tripStart) {
        this.tripStart = tripStart;
    }

    public int getMinsTilArrival() {
        return minsTilArrival;
    }

    public void setMinsTilArrival(int minsTilArrival) {
        this.minsTilArrival = minsTilArrival;
    }

    public float getUpdateAge() {
        return updateAge;
    }

    public void setUpdateAge(float updateAge) {
        this.updateAge = updateAge;
    }

    public boolean isLastTripOfDay() {
        return lastTripOfDay;
    }

    public void setLastTripOfDay(boolean lastTripOfDay) {
        this.lastTripOfDay = lastTripOfDay;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public Location getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(Location gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public float getGpsSpeed() {
        return gpsSpeed;
    }

    public void setGpsSpeed(float gpsSpeed) {
        this.gpsSpeed = gpsSpeed;
    }

    public int compareTo(@NonNull Object otherBus) {
        return this.getRouteNo() - ((OCBus)otherBus).getRouteNo();
    }
}
