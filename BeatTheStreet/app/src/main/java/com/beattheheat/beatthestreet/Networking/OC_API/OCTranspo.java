package com.beattheheat.beatthestreet.Networking.OC_API;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.beattheheat.beatthestreet.Networking.SCallable;
import com.beattheheat.beatthestreet.Networking.VolleyRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matt on 27-Sep-17.
 *
 * A class to simplify calls to the OCTranspo API. Uses Volley to post GET requests to the OCTranspo
 * servers, and parse the JSON returned into separate data classes we've defined.
 */

public class OCTranspo {
    // Volley request queue
    private RequestQueue req;

    // GTFS Tables
    public GTFS gtfsTable;

    // Types of requests that can be made to the server
    public enum OC_TYPE {
        ROUTES_FOR_STOP,
        TIMES_FOR_STOP_ROUTE,
        TIMES_FOR_STOP,
        GTFS,
        SIZE
    }

    // List of URLs to contact for data
    static String[] apiURLs;

    // API Key information (tied to a personal account created for this application)
    static final String appID =  "628f2e92";
    static final String apiKey = "88f44bc3e17f5880763b436cff9a779d";

    public OCTranspo(Context ctx) {
        // Create the volley request queue
        req = VolleyRequest.getInstance(ctx.getApplicationContext()).getRequestQueue();

        // Set the API URLs
        apiURLs = new String[OC_TYPE.SIZE.ordinal()];
        apiURLs[OC_TYPE.ROUTES_FOR_STOP.ordinal()] = "https://api.octranspo1.com/v1.2/GetRouteSummaryForStop";
        apiURLs[OC_TYPE.TIMES_FOR_STOP_ROUTE.ordinal()] = "https://api.octranspo1.com/v1.2/GetNextTripsForStop";
        apiURLs[OC_TYPE.TIMES_FOR_STOP.ordinal()] = "https://api.octranspo1.com/v1.2/GetNextTripsForStopAllRoutes";
        apiURLs[OC_TYPE.GTFS.ordinal()] = "https://api.octranspo1.com/v1.2/Gtfs";

        // Load the GTFS
        gtfsTable = new GTFS(ctx.getApplicationContext());
    }

    public final void LoadGTFS(SCallable<Boolean> sCallable) {
        gtfsTable.LoadGTFS(sCallable);
    }

    /**
     * Makes a volley POST to get information from the OCTranspo API servers
     *
     * requestType:   Type of Request to be made, specified by the OCAPI. (See enum at top)
     * requestParams: Parameters to give the POST request, specified by the type of request. (See public functions below)
     * callback:      Callback function, given the raw RESPONSE string of the POST request.
     */
    void MakeVolleyPOST(OC_TYPE requestType, final HashMap<String, String> requestParams, final SCallable<String> callback) {
        StringRequest jReq = new StringRequest(
                Request.Method.POST,
                apiURLs[requestType.ordinal()],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            callback.call(response);
                        } catch (Exception e) {
                            Log.e("OC_ERR", "Error with callback response: " + e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("OC_ERR", "Error with OC_API POST request: " + error.toString());
                    }
                }
        ) {
            /**
             * Passing some request parameters
             */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return requestParams;
            }
        };

        req.add(jReq);
    }

    // Retrieves the routes for a given stop number.
    public void GetRouteSummaryForStop(final String stopNo, final SCallable<int[]> callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("appID", appID);
        params.put("apiKey", apiKey);
        params.put("stopNo", stopNo);
        params.put("format", "json");

        MakeVolleyPOST(OC_TYPE.ROUTES_FOR_STOP, params, new SCallable<String>() {
            @Override
            public void call(String arg) {
                try {
                    JSONObject json = new JSONObject(arg);
                    JSONArray routeList = json.getJSONObject("GetRouteSummaryForStopResult").getJSONObject("Routes").getJSONArray("Route");

                    // Parse the json into a list of route numbers
                    int[] routeNoList = new int[routeList.length()];
                    for (int i = 0; i < routeList.length(); i++) {
                        routeNoList[i] = routeList.getJSONObject(i).getInt("RouteNo");
                    }

                    // Give the callback the list of route numbers (can look them up in the gtfs table)
                    callback.call(routeNoList);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("OC_ERR", "Failed to parse json for route summary of stop no: " + stopNo);
                }
            }
        });
    }

    // Retrieves next three trips for all routes for a given stop number.
    public void GetNextTripsForStopAllRoutes(final String stopNo, final SCallable<HashMap<Integer, OCBus[]>> callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("appID", appID);
        params.put("apiKey", apiKey);
        params.put("stopNo", stopNo);
        params.put("format", "json");

        MakeVolleyPOST(OC_TYPE.TIMES_FOR_STOP, params, new SCallable<String>() {
            @Override
            public void call(String arg) {
                try {
                    JSONObject json = new JSONObject(arg);
                    JSONArray routeList = json.getJSONObject("GetRouteSummaryForStopResult").getJSONObject("Routes").getJSONArray("Route");

                    HashMap<Integer, OCBus[]> returnMap = new HashMap<>();

                    // Go through the list of routes
                    for (int i = 0; i < routeList.length(); i++) {
                        // Pull out the object and the list of trips
                        JSONObject route = routeList.getJSONObject(i);

                        // Check if trips exist
                        OCBus[] busList = null;
                        if (route.has("Trips")) {
                            JSONArray tripList = route.getJSONArray("Trips");
                             busList = new OCBus[tripList.length()];

                            // Go through the list of trips
                            for (int j = 0; j < tripList.length(); j++) {
                                JSONObject trip = tripList.getJSONObject(j);
                                busList[j] = new OCBus(trip, route.getInt("RouteNo"), route.getString("RouteHeading"));
                            }
                        }

                        // Put the list of buses in the hashmap
                        returnMap.put(route.getInt("RouteNo"), busList);
                    }

                    callback.call(returnMap);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("OC_ERR", "Failed to parse json for next trip summary of stop no: " + stopNo);
                }
            }
        });
    }
}
