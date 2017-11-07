package com.beattheheat.beatthestreet.Networking;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * Created by Matt on 24-Oct-17.
 *
 * Just a small request extension to handle the Byte[] type
 */

public class ByteRequest extends Request<Byte[]> {
    private final Listener<Byte[]> mListener;

    public ByteRequest(int method, String url, Listener<Byte[]> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response<Byte[]> parseNetworkResponse(NetworkResponse response) {
        // Convert from byte[] to Byte[]
        Byte[] bytes = new Byte[response.data.length];
        for (int i = 0; i < response.data.length; i++) {
            bytes[i] = response.data[i];
        }

        return Response.success(bytes, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(Byte[] response) {
        mListener.onResponse(response);
    }
}
