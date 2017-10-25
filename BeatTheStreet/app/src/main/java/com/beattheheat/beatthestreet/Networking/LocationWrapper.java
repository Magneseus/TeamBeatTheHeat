package com.beattheheat.beatthestreet.Networking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by kylec on 2017-10-24.
 */

public class LocationWrapper {
    private static LocationWrapper myObj;
    private static Location lastLocation;
    private static GoogleApiClient googleApiClient;

    private LocationWrapper() {

    }

    public static LocationWrapper getInstance() {
        if (myObj == null) {
            myObj = new LocationWrapper();
        }

        return myObj;
    }

    public void setClient(GoogleApiClient client) {
        googleApiClient = client;
    }

    private static void queryLocation() {
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
    }

    public static Location getLocation()
    {
        if(lastLocation == null) {
            queryLocation();
        }

        return lastLocation;
    }

}
