package com.beattheheat.beatthestreet.Networking;

import android.annotation.SuppressLint;
import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


/**
 * Created by Matt on 27-Sep-17.
 *
 * https://developer.android.com/training/volley/requestqueue.html
 *
 */

public class VolleyRequest {
    @SuppressLint("StaticFieldLeak")
    private static VolleyRequest mInstance;
    @SuppressLint("StaticFieldLeak")
    private static Context mCtx;
    private RequestQueue mRequestQueue;

    private VolleyRequest(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyRequest getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyRequest(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
