package com.beattheheat.beatthestreet.Networking;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.beattheheat.beatthestreet.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


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
    private RequestQueue mNonSSLRequestQueue;

    // A Longer wait policy for requests
    public static DefaultRetryPolicy longerWaitPolicy = new DefaultRetryPolicy(
                                              4000,
                                              10,
                                              DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    // Initialization, requires a context from an android activity
    private VolleyRequest(Context context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
        mCtx = context;

        // Load our CA Certificate
        InputStream in = context.getResources().openRawResource(R.raw.globalsign_dv);
        Certificate ca;
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ca = cf.generateCertificate(in);

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        HurlStack hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return httpsURLConnection;
            }
        };

        mRequestQueue = getRequestQueue(hurlStack);
        mNonSSLRequestQueue = getNonSSLRequestQueue();
    }

    // Returns the singleton instance (or creates a new one if non-existent)
    public static synchronized VolleyRequest getInstance(Context context) {
        if (mInstance == null) {
            try {
                mInstance = new VolleyRequest(context);
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            }
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return getRequestQueue(null);
    }

    // Returns the current request queue, so you can add requests to it
    private RequestQueue getRequestQueue(HurlStack hurlStack) {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(), hurlStack);
        }
        return mRequestQueue;
    }

    public RequestQueue getNonSSLRequestQueue() {
        if (mNonSSLRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mNonSSLRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mNonSSLRequestQueue;
    }

    // Allows requests to be added directly, instead of getting the request queue and adding to it
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
