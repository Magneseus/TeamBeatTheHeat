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

/**
 * Created by kylec on 2017-10-24.
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

    private LocationWrapper(Context ctx) {
        // Get application context to avoid leaks
        appCtx = ctx.getApplicationContext();

        googleApiClient = new GoogleApiClient.Builder(appCtx)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locReq = new LocationRequest();
        locReq.setInterval(5 * 1000); // every 5s
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
        //.addLocationRequest(locReq);
    }

    public static LocationWrapper getInstance(Context ctx) {
        if (myObj == null) {
            myObj = new LocationWrapper(ctx);
        }

        // Set proper context
        myObj.appCtx = ctx.getApplicationContext();

        return myObj;
    }

    public void connect() {
        googleApiClient.connect();
    }

    public void disconnect() {
        googleApiClient.disconnect();
    }

    private void queryLocation() {
        // ensure we have permission
        if (ActivityCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
    }

    public Location getLocation()
    {
        //if(lastLocation == null) {
        //    queryLocation();
        //}

        return lastLocation;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        int i = ActivityCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_FINE_LOCATION);

        if (ActivityCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            int x = 9;
            x++;
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locReq, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
    }
}
