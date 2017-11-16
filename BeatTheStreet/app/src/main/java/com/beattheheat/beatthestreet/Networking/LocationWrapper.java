package com.beattheheat.beatthestreet.Networking;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IllegalFormatException;

/**
 *  Singleton wrapper for the GoogleApiClient Location service.
 */

public class LocationWrapper
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // Singleton instance
    private static LocationWrapper myObj;

    private Location lastLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locReq;

    // Android Context
    private Context appCtx;

    // are we currently listening for updates?
    private boolean requestingUpdates = false;

    // callbacks to excecute when new locations are received
    private Collection<SCallable> subscribers;

    // Constructor for Singleton class.
    // Takes the context, which is needed for creation of the GoogleApiClient.
    private LocationWrapper(Context ctx) {
        // Get application context to avoid leaks
        appCtx = ctx.getApplicationContext();

        googleApiClient = new GoogleApiClient.Builder(appCtx)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // creates a request for frequent location updates
        //  TODO: Consider different location update rates
        locReq = new LocationRequest().setInterval(5 * 1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        subscribers = new ArrayList<SCallable>();
    }

    public static void create(Context ctx) {
        myObj = new LocationWrapper(ctx);

        // Set proper context
        myObj.appCtx = ctx.getApplicationContext();
    }

    // Returns the instance of the LocationWrapper
    public static LocationWrapper getInstance() {
        return myObj;
    }

    /*
        Sets the location request and starts listening for updates with it.
        Throws IllegalArgumentException if accuracy isn't one of the accepted
        4 values (check LocationRequest for valid values).
    */
    public void setLocationRequest(int frequencyInMillis, int accuracy)
            throws IllegalArgumentException {

        locReq = new LocationRequest()
                .setInterval(frequencyInMillis)
                .setPriority(accuracy);

        startRequestingUpdates();
    }

    // Attempts to connect the googleApiClient
    public void connect() {
        if(googleApiClient.isConnected())
           return;

        googleApiClient.connect();
    }

    // Disconnects the googleApiClient
    public void disconnect() {
        googleApiClient.disconnect();
    }

    // Returns the last known location
    public Location getLocation() {
        return lastLocation;
    }

    public void subscribe(SCallable sub) {
        // TODO: should we check for uniqueness?
        subscribers.add(sub);
    }

    public void unsubscribe(SCallable sub) {
        subscribers.remove(sub);
    }

    // Callback for when the googleApiClient connects
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startRequestingUpdates();
    }

    // Callback for if the googleApiClient connection is suspended
    @Override
    public void onConnectionSuspended(int i) {
        // TODO: onConnectionSuspended for location services

        System.out.println("what");
    }

    // Callback for if the googleApiClient fails to connect
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: onConnectionFailed for location services

        System.out.println("maybe this?");
    }

    // Callback for when LocationServices sends a Location Update
    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        for(SCallable sub : subscribers) {
            sub.call(null);
        }
    }

    // start listening for location updates
    public void startRequestingUpdates() {
        // if we're already listening for updates, but we receive this call, assume we've changed
        // the location request
        if(requestingUpdates) {
            stopRequestingUpdates();
        };

        if (ActivityCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            return;
        }

        requestingUpdates = true;
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locReq, this);
    }

    public void stopRequestingUpdates() {
        requestingUpdates = false;

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }
}
