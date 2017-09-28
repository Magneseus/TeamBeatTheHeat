package com.beattheheat.beatthestreet.OC_API;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matt on 27-Sep-17.
 *
 */

public class OCTranspo {
    private RequestQueue req;

    private static String routesAtStopURL = "https://api.octranspo1.com/v1.2/GetRouteSummaryForStop";
    private static String tripsAtStopURL = "https://api.octranspo1.com/v1.2/GetNextTripsForStopAllRoutes";
    private static String gtfsURL = "https://api.octranspo1.com/v1.2/Gtfs";

    private static String appID =  "628f2e92";
    private static String apiKey = "88f44bc3e17f5880763b436cff9a779d";

    public OCTranspo(Context ctx) {
        req = VolleyRequest.getInstance(ctx.getApplicationContext()).getRequestQueue();
    }

    public void GetRouteSummaryForStop(final String stopNo, final SCallable<String> func) { //: Retrieves the routes for a given stop number.
        StringRequest jReq = new StringRequest(
                Request.Method.POST,
                routesAtStopURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject json = new JSONObject(response);
                            func.call(json.toString(4));
                        } catch (Exception e) {
                            Log.d("error", e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", error.toString());
                    }
                }
        ) {
            /**
             * Passing some request parameters
             */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("appID", appID);
                params.put("apiKey", apiKey);
                params.put("stopNo", stopNo);
                params.put("format", "json");
                return params;
            }
        };

        req.add(jReq);
    }

    public void GetNextTripsForStop() { //: Retrieves next three trips on the route for a given stop number.

    }

    public void GetNextTripsForStopAllRoutes() { //: Retrieves next three trips for all routes for a given stop number.

    }

    public void GTFS() { //: Retrieves specific records from all sections the of GTFS file.

    }
}
