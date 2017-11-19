package com.beattheheat.beatthestreet;

import com.beattheheat.beatthestreet.Networking.OC_API.OCBus;

/**
 * Created by Matt on 2017-11-19.
 */

public class MainAdapterHelper {
    // Helper class to hold info for each route/stop combo so we can easily pass what we need
    // to onBindViewHolder
    OCBus[] busArray;
    String stopName;
    String stopCode;
    String routeNumberName;

    /*OCHelper() {
        this.stopName = "Test stop";
        this.stopCode = "1234";
        this.routeNumberName = "123 Fake Route";

        this.busArray = new OCBus[3];
        busArray[0] = new OCBus();
        busArray[0].setRouteNo(123);
        busArray[0].setRouteHeading("Fake Route");
        busArray[0].setMinsTilArrival(10);
        busArray[0].setUpdateAge(10.0f);
        busArray[1] = new OCBus();
        busArray[1].setRouteNo(123);
        busArray[1].setRouteHeading("Fake Route");
        busArray[1].setMinsTilArrival(15);
        busArray[1].setUpdateAge(0.0f);
        busArray[2] = new OCBus();
        busArray[2].setRouteNo(123);
        busArray[2].setRouteHeading("Fake Route");
        busArray[2].setMinsTilArrival(25);
        busArray[2].setUpdateAge(25.0f);
    }*/

    MainAdapterHelper(OCBus[] busArray, String stopName, String stopCode) {
        this.busArray = busArray;
        this.stopName = stopName;
        this.stopCode = stopCode;
        this.routeNumberName = busArray[0].getRouteNo() + " " + busArray[0].getRouteHeading();
    }
}