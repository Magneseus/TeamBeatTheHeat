package com.beattheheat.beatthestreet.Networking;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


/**
 * Created by Matt on 27-Sep-17.
 *
 * https://developer.android.com/training/volley/requestqueue.html
 *
 * Singleton implementation of the VolleyRequest class
 */

public class VolleyRequest {
    @SuppressLint("StaticFieldLeak")
    private static VolleyRequest mInstance;
    @SuppressLint("StaticFieldLeak")
    private static Context mCtx;
    private RequestQueue mRequestQueue;

    // A Longer wait policy for requests
    public static DefaultRetryPolicy longerWaitPolicy = new DefaultRetryPolicy(4000,
                                              DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                              DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    // Initialization, requires a context from an android activity
    private VolleyRequest(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    // Returns the singleton instance (or creates a new one if non-existent)
    public static synchronized VolleyRequest getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyRequest(context);
        }
        return mInstance;
    }

    // Returns the current request queue, so you can add requests to it
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    // Allows requests to be added directly, instead of getting the request queue and adding to it
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
