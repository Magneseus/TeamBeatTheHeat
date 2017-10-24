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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    // Types of requests that can be made to the server
    public enum OC_TYPE {
        ROUTES_FOR_STOP,
        TIMES_FOR_STOP_ROUTE,
        TIMES_FOR_STOP,
        GTFS,
        SIZE
    }

    // GTFS Tables (Tables for the main database in the API)
    private String[] gtfsTableNames = {"agency", "calendar", "calendar_dates", "routes", "stops", "stop_times", "trips"};
    // GTFS Zip URL (For smaller download)
    private static final String GTFS_ZIP_URL = "http://www.octranspo1.com/files/google_transit.zip";

    // List of URLs to contact for data
    private String[] apiURLs;

    // API Key information (tied to a personal account created for this application)
    private static final String appID =  "628f2e92";
    private static final String apiKey = "88f44bc3e17f5880763b436cff9a779d";

    public OCTranspo(Context ctx) {
        // Create the volley request queue
        req = VolleyRequest.getInstance(ctx.getApplicationContext()).getRequestQueue();

        // Set the API URLs
        apiURLs = new String[OC_TYPE.SIZE.ordinal()];
        apiURLs[OC_TYPE.ROUTES_FOR_STOP.ordinal()] = "https://api.octranspo1.com/v1.2/GetRouteSummaryForStop";
        apiURLs[OC_TYPE.TIMES_FOR_STOP_ROUTE.ordinal()] = "https://api.octranspo1.com/v1.2/GetNextTripsForStop";
        apiURLs[OC_TYPE.TIMES_FOR_STOP.ordinal()] = "https://api.octranspo1.com/v1.2/GetNextTripsForStopAllRoutes";
        apiURLs[OC_TYPE.GTFS.ordinal()] = "https://api.octranspo1.com/v1.2/Gtfs";
    }

    /**
     * Makes a volley POST to get information from the OCTranspo API servers
     *
     * requestType:   Type of Request to be made, specified by the OCAPI. (See enum at top)
     * requestParams: Parameters to give the POST request, specified by the type of request. (See public functions below)
     * callback:      Callback function, given the raw RESPONSE string of the POST request.
     */
    private void MakeVolleyPOST(OC_TYPE requestType, final HashMap<String, String> requestParams, final SCallable<String> callback) {
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
    public void GetRouteSummaryForStop(final String stopNo, final SCallable<String> callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("appID", appID);
        params.put("apiKey", apiKey);
        params.put("stopNo", stopNo);
        params.put("format", "json");

        MakeVolleyPOST(OC_TYPE.ROUTES_FOR_STOP, params, callback);
    }

    // Retrieves next three trips on the route for a given stop number.
    public void GetNextTripsForStop(final String stopNo, final String routeNo, final SCallable<String> callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("appID", appID);
        params.put("apiKey", apiKey);
        params.put("stopNo", stopNo);
        params.put("routeNo", routeNo);
        params.put("format", "json");

        MakeVolleyPOST(OC_TYPE.TIMES_FOR_STOP_ROUTE, params, callback);
    }

    // Retrieves next three trips for all routes for a given stop number.
    public void GetNextTripsForStopAllRoutes(final String stopNo, final SCallable<String> callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("appID", appID);
        params.put("apiKey", apiKey);
        params.put("stopNo", stopNo);
        params.put("format", "json");

        MakeVolleyPOST(OC_TYPE.TIMES_FOR_STOP, params, callback);
    }

    // Retrieves specific records from all sections the of GTFS file.
    public void GTFS(Context ctx) {
        // The HashMap to store all the tables inside of
        final HashMap<String, String> gtfsTable = new HashMap<String,String>();

        // The application context (to prevent leaks)
        final Context app_ctx = ctx.getApplicationContext();

        StringRequest jReq = new StringRequest(
                Request.Method.GET,
                "http://www.octranspo1.com/files/google_transit.zip",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            FileOutputStream os;
                            String fileName = "GTFS.zip";

                            os = app_ctx.getApplicationContext().openFileOutput(fileName, app_ctx.MODE_PRIVATE);
                            os.write(response.getBytes());
                            os.close();
                        } catch (Exception e) {
                            Log.e("OC_ERR", "Error with callback response: " + e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("OC_ERR", "Error with OC_API GET request: " + error.toString());
                    }
                }
        );

        req.add(jReq);

        // TODO: Store the parsed objects on the disk somewhere (and check for existence on call)
        // TODO: Check the "calendar" table for start and end dates of regular service
        // TODO: Download and decompress the zip file (saves ~45MB of download)

    }

    /** Checks if the GTFS needs to be updated
     *
     * @param callback
     *  - will place a -1 in the Integer argument if the operation to get date info failed
     *  - will place a 0 in the Integer argument if the date is invalid
     *  - will place a 1 in the Integer argument if the date is valid
     */
    private void CheckGTFSDateValid(final Calendar oldStartDate, final SCallable<Integer> callback) {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("appID", appID);
        params.put("apiKey", apiKey);
        params.put("table", "calendar");
        params.put("format", "json");

        MakeVolleyPOST(OC_TYPE.GTFS, params, new SCallable<String>() {
            @Override
            public void call(String arg) {
                try {
                    // Parse returned text into a json
                    JSONObject calendarJSON = new JSONObject(arg);

                    // Get the end_date of the current schedule
                    String start_date_string = calendarJSON.getJSONArray("Gtfs").getJSONObject(0).getString("start_date");
                    Calendar start_date = Calendar.getInstance();
                    start_date.set(
                            Integer.parseInt(start_date_string.substring(0,4)),
                            Integer.parseInt(start_date_string.substring(4,6)),
                            Integer.parseInt(start_date_string.substring(6,8))
                    );

                    // Call the callback and give it a return value based on whether we've validated
                    Integer returnCode = oldStartDate.equals(start_date) ? 1 : 0;
                    callback.call(returnCode);

                } catch (JSONException e) {
                    e.printStackTrace();

                    // Give the callback the error code
                    Integer returnCode = -1;
                    callback.call(returnCode);
                }
            }
        });
    }
}
