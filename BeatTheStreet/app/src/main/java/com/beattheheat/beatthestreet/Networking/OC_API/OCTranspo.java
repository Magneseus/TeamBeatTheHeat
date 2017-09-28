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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matt on 27-Sep-17.
 *
 */

public class OCTranspo {
    private RequestQueue req;

    public enum OC_TYPE {
        ROUTES_FOR_STOP,
        TIMES_FOR_STOP_ROUTE,
        TIMES_FOR_STOP,
        GTFS,
        SIZE
    }

    private String[] apiURLs;

    private static final String appID =  "628f2e92";
    private static final String apiKey = "88f44bc3e17f5880763b436cff9a779d";

    public OCTranspo(Context ctx) {
        req = VolleyRequest.getInstance(ctx.getApplicationContext()).getRequestQueue();

        apiURLs = new String[OC_TYPE.SIZE.ordinal()];
        apiURLs[OC_TYPE.ROUTES_FOR_STOP.ordinal()] = "https://api.octranspo1.com/v1.2/GetRouteSummaryForStop";
        apiURLs[OC_TYPE.TIMES_FOR_STOP_ROUTE.ordinal()] = "https://api.octranspo1.com/v1.2/GetNextTripsForStop";
        apiURLs[OC_TYPE.TIMES_FOR_STOP.ordinal()] = "https://api.octranspo1.com/v1.2/GetNextTripsForStopAllRoutes";
        apiURLs[OC_TYPE.GTFS.ordinal()] = "https://api.octranspo1.com/v1.2/Gtfs";
    }

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
    public void GTFS() {
        //TODO: Specify how we're going to request this, also look into download managers for large filesize
    }
}
