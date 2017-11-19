package com.beattheheat.beatthestreet.Networking.OC_API;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.beattheheat.beatthestreet.FileManagement.FileToStrings;
import com.beattheheat.beatthestreet.FileManagement.Unzipper;
import com.beattheheat.beatthestreet.Networking.ByteRequest;
import com.beattheheat.beatthestreet.Networking.SCallable;
import com.beattheheat.beatthestreet.Networking.VolleyRequest;
import com.beattheheat.beatthestreet.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Matt on 24-Oct-17.
 *
 * GTFS container class.
 *
 * Also contains loading functions.
 *  - will check for existence of GTFS files on disk, and then validate them against the server
 *  - if not valid, or not on disk, the GTFS will be downloaded and unzipped onto the disk
 */

// TODO: Check for network access before making requests

public class GTFS {
    // Tables
    HashMap<Integer, OCRoute> routeTable;
    HashMap<String, OCStop> stopTable;
    HashMap<Integer, String> stopCodeToStopID;

    // Trip database
    private OCTripDatabase tripTable;

    // GTFS Tables (Tables for the main database in the API)
    private String[] gtfsTableNames = {"agency", "calendar", "calendar_dates", "routes", "stops", "stop_times", "trips"};
    // GTFS Zip URL (For smaller download)
    private static final String GTFS_ZIP_URL = "http://www.octranspo1.com/files/google_transit.zip";

    // Android Context
    private Context appCtx;

    // Volley queue
    private RequestQueue req;

    // Callback for when loading the GTFS is finished
    private ArrayList<SCallable<Boolean>> callbacks;
    private boolean isLoaded;
    private boolean isLoading;


    public GTFS(Context context) {
        routeTable = new HashMap<>(200);
        stopTable = new HashMap<>(5700);
        stopCodeToStopID = new HashMap<>(5700);

        appCtx = context.getApplicationContext();
        req = VolleyRequest.getInstance(appCtx.getApplicationContext()).getRequestQueue();

        callbacks = new ArrayList<>();
        isLoaded = false;
        isLoading = false;
    }

    public Collection<OCRoute> getRouteList() {
        return routeTable.values();
    }

    public OCRoute getRoute(int routeNo) {
        return routeTable.get(routeNo);
    }

    public Collection<OCStop> getStopList() {
        return stopTable.values();
    }

    public OCStop getStop(int stopCode) {
        return stopTable.get(stopCodeToStopID.get(stopCode));
    }

    public OCStop getStop(String stopID) {
        return stopTable.get(stopID);
    }

    public OCTrips[] getTripsForRouteAtStop(int routeNo, int stopCode) {
        return getTripsForRouteAtStop(routeNo, stopCodeToStopID.get(stopCode));
    }

    public OCTrips[] getTripsForRouteAtStop(int routeNo, String stopID) {
        return tripTable.tripDAO().loadAllTripTimesWithStopID(getRoute(routeNo).getTrips(), stopID);
    }

    public String getTripIDForStartTime(int routeNo, int start_hour, int start_min, int start_sec) {
        return tripTable.tripDAO().getTripIDForStartTime(getRoute(routeNo).getTrips(), start_hour, start_min, start_sec)[0];
    }

    public OCTrips[] getAllTripsWithID(String routeID) {
        return tripTable.tripDAO().loadAllTripsWithID(routeID);
    }

    public String[] getAllStopsWithID(String routeID) {
        return tripTable.tripDAO().loadAllStopsWithID(routeID);
    }

    // Starts the asynchronous load of the GTFS files
    // Callback will be alerted with a T/F when files have been loaded
    public final void LoadGTFS(SCallable<Boolean> sCallable) {
        if (isLoaded) {
            if (sCallable != null) sCallable.call(true);
        } else if (isLoading) {
            if (sCallable != null) callbacks.add(sCallable);
        } else {
            if (sCallable != null) callbacks.add(sCallable);

            isLoading = true;
            InternalLoadGTFS();
        }
    }

    // Retrieves the GTFS file if on disk and not out of date, or downloads the current one otherwise
    private void InternalLoadGTFS() {
        /* Check if the gtfs files exist on disk
         * Check for: "calendar.txt"
         */
        Log.d("GTFS", "Checking for GTFS on disk...");
        boolean gtfsOnDisk = false;
        for (File f : appCtx.getFilesDir().listFiles()) {
            if (f.getName().equals("calendar.txt")) {
                gtfsOnDisk = true;
            }
        }

        final GTFS gtfsPointer = this;

        // If GTFS is on disk, check date and if valid load all files
        if (gtfsOnDisk) {
            // Check if date is valid
            Log.d("GTFS", "Checking valid date...");
            if (CheckGTFSDateValid()) {
                // Valid GTFS
                Log.d("GTFS", "Loading from disk...");
                new LoadGTFSFromDisk().execute(gtfsPointer);
            } else {
                Log.d("GTFS", "Downloading from net...");
                // Invalid/corrupt/non-existent GTFS
                LoadGTFSFromNet();
            }
        } else {
            Log.d("GTFS", "Downloading from net...");
            LoadGTFSFromNet();
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    // Async task to load the GTFS files
    private class LoadGTFSFromDisk extends AsyncTask<GTFS, Integer, Void> {
        @Override
        protected final Void doInBackground(GTFS... gtfs) {
            /*************************
             *    OCROUTE LOADING    *
             *************************/
            // Load the "trips.txt" file
            try (FileInputStream fis = appCtx.openFileInput("trips.txt")) {
                String fileLines = (new FileToStrings(fis).toStringFast(65536));

                int start_ind = 0;
                int ind = 0;
                while ((ind = fileLines.indexOf('\n', start_ind)) != -1) {
                    if (start_ind != 0) OCRoute.LoadRoute(gtfs[0], fileLines.substring(start_ind, ind+1));
                    start_ind = ind+1;
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("GTFS", "Error opening 'trips.txt'");

                for (SCallable<Boolean> callback : callbacks)
                    callback.call(false);
            }

            /************************
             *    OCSTOP LOADING    *
             ************************/
            // Load the "stops.txt" file
            try (FileInputStream fis = appCtx.openFileInput("stops.txt")) {
                String fileLines = (new FileToStrings(fis).toStringFast(65536));

                int start_ind = 0;
                int ind = 0;
                while ((ind = fileLines.indexOf('\n', start_ind)) != -1) {
                    if (start_ind != 0) OCStop.LoadStop(gtfs[0], fileLines.substring(start_ind, ind+1));
                    start_ind = ind+1;
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("GTFS", "Error opening 'stops.txt'");

                for (SCallable<Boolean> callback : callbacks)
                    callback.call(false);
            }

            /************************
             *    OCTRIP LOADING    *
             ************************/
            // Check if the database has already been extracted
            if (!appCtx.getDatabasePath("octrips.db").exists()) {
                Log.d("GTFS", "Extracting database from apk...");

                try {
                    // Copy the zip file to internal storage
                    InputStream in = appCtx.getResources().openRawResource(R.raw.octrips);
                    OutputStream out = new FileOutputStream(new File(appCtx.getFilesDir().getPath(), "octrips.zip"));

                    byte[] buf = new byte[1024];
                    int read;
                    while ((read = in.read(buf)) != -1) {
                        out.write(buf, 0, read);
                    }

                    in.close();
                    out.flush();
                    out.close();

                    // Extract the zip file contents
                    String dbPath = appCtx.getDatabasePath("octrips.db").getPath();
                    dbPath = dbPath.substring(0, dbPath.lastIndexOf('/'));

                    Unzipper uz = new Unzipper(appCtx, "octrips.zip", dbPath);
                    uz.Unzip();
                    appCtx.deleteFile("octrips.zip");

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("GTFS", "Failed to unzip/extract database from resources.");
                }
            }

            // Setup the Room database
            Log.d("GTFS", "Preparing Room db...");
            tripTable = Room.databaseBuilder(appCtx, OCTripDatabase.class, "octrips.db").allowMainThreadQueries().build();

            isLoading = false;
            isLoaded = true;

            Log.d("GTFS", "Done loading.");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (SCallable<Boolean> callback : callbacks)
                callback.call(true);
        }
    }

    private void LoadGTFSFromNet() {
        // Delete old GTFS files, if present
        appCtx.deleteFile("GTFS.zip");
        for (String s : gtfsTableNames) {
            appCtx.deleteFile(s + ".txt");
        }

        final GTFS gtfsPointer = this;

        // Make a byte request to download the GTFS zip file
        ByteRequest bReq = new ByteRequest(
                Request.Method.GET,
                GTFS_ZIP_URL,
                new Response.Listener<Byte[]>() {
                    @Override
                    public void onResponse(Byte[] response) {
                        try {
                            // Write the bytes to a file in internal storage
                            FileOutputStream os;
                            String fileName = "GTFS.zip";

                            os = appCtx.openFileOutput(fileName, Context.MODE_PRIVATE);

                            // Convert from Byte[] to byte[]
                            byte[] bytes = new byte[response.length];
                            for (int i = 0; i < response.length; i++)
                                bytes[i] = response[i];

                            os.write(bytes);
                            os.close();

                            // Extract the files contents to the internal storage
                            Unzipper uz = new Unzipper(appCtx, fileName);
                            uz.Unzip();

                            // Delete the zip file
                            appCtx.deleteFile(fileName);

                            // Delete the unnecessary files
                            appCtx.deleteFile("stop_times.txt");

                            // Load the contents from disk now
                            new LoadGTFSFromDisk().execute(gtfsPointer);

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

        bReq.setRetryPolicy(VolleyRequest.longerWaitPolicy);

        req.add(bReq);
    }

    /// Checks if the GTFS needs to be updated
    public boolean CheckGTFSDateValid() {
        // Get the current start date from the calendar.txt file
        FileInputStream fis = null;
        try {
            fis = appCtx.openFileInput("calendar.txt");
            String[] lines = (new FileToStrings(fis)).toStringArray();

            String end_date_string = lines[1].split(",")[9];
            Calendar end_date = Calendar.getInstance();
            end_date.set(
                    Integer.parseInt(end_date_string.substring(0, 4)),
                    Integer.parseInt(end_date_string.substring(4,6)),
                    Integer.parseInt(end_date_string.substring(6,8))
            );

            Calendar currentDate = Calendar.getInstance();

            return currentDate.before(end_date);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

        // TODO: Figure out why this crashes matt & laura's devices, cert errors n stuff
        /*
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("appID", OCTranspo.appID);
        params.put("apiKey", OCTranspo.apiKey);
        params.put("table", "calendar");
        params.put("format", "json");

        // Make a request to the OC API
        StringRequest jReq = new StringRequest(
                Request.Method.POST,
                OCTranspo.apiURLs[OCTranspo.OC_TYPE.GTFS.ordinal()],
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse returned text into a json
                            JSONObject calendarJSON = new JSONObject(response);

                            // Get the end_date of the current schedule
                            String start_date = calendarJSON.getJSONArray("Gtfs").getJSONObject(0).getString("start_date");

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

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        req.add(jReq);
        */
    }
}
