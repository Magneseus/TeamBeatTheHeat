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
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collection;
/**
 *  Singleton wrapper for the GoogleApiClient Location service.
 */

public class LocationWrapper
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static LocationWrapper myObj;
    private Location lastLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locReq;
    private Context appCtx;
    private boolean requestingUpdates = false;
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
        // TODO: Consider different location update rates
        locReq = new LocationRequest();
        locReq.setInterval(5 * 1000); // every 5s
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        subscribers = new ArrayList<SCallable>();
    }

    // Returns the instance of the LocationWrapper, creating it if null
    public static LocationWrapper getInstance(Context ctx) {
        if (myObj == null) {
            myObj = new LocationWrapper(ctx);
        }

        // Set proper context
        myObj.appCtx = ctx.getApplicationContext();

        return myObj;
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

    }

    // Callback for if the googleApiClient fails to connect
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
        // don't request updates if we already are
        if(requestingUpdates) return;

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

}
