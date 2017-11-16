package com.beattheheat.beatthestreet;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

/**
 * Created by Matt on 15-Nov-17.
 */

public class FavoritesStorage {
    private Context appCtx;
    private SharedPreferences stopPrefs;
    private SharedPreferences routePrefs;
    private Map<String, Integer> stopFavs;
    private Map<String, Integer> routeFavs;

    public enum FAV_TYPE{
        STOP,
        ROUTE
    }

    public FavoritesStorage(Context ctx) {
        appCtx = ctx.getApplicationContext();

        stopPrefs = appCtx.getSharedPreferences(
                appCtx.getString(R.string.fav_stop_prefs),
                Context.MODE_PRIVATE
        );
        routePrefs = appCtx.getSharedPreferences(
                appCtx.getString(R.string.fav_route_prefs),
                Context.MODE_PRIVATE
        );

        stopFavs = (Map<String, Integer>) stopPrefs.getAll();
        routeFavs = (Map<String, Integer>) routePrefs.getAll();
    }

    // Get the set of all favorite stops
    public Set<String> getAllFavStops() {
        return stopFavs.keySet();
    }

    // Get the set of all favorite routes
    public Set<String> getAllFavRoutes() {
        return routeFavs.keySet();
    }

    // Check if a stop/route is currently favorited
    public boolean isFav(String id, FAV_TYPE type) {
        // Select the proper favs file
        Map<String, Integer> favs = null;
        switch (type) {
            case STOP:
                favs = stopFavs;
                break;
            case ROUTE:
                favs = routeFavs;
                break;
        }

        return favs.containsKey(id);
    }

    // Add a route/stop to the favorites list
    public void addFav(String id, FAV_TYPE type) {
        // Select the proper prefs file
        SharedPreferences prefs = null;
        Map<String, Integer> favs = null;
        switch (type) {
            case STOP:
                prefs = stopPrefs;
                favs = stopFavs;
                break;
            case ROUTE:
                prefs = routePrefs;
                favs = routeFavs;
                break;
        }

        if (!prefs.contains(id)) {
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putInt(id, type.ordinal());
            prefsEditor.apply();

            // Update the local copy
            favs.put(id, 0);
        }
    }

    // Remove a route/stop from the favorites list (Returns true if completed, false if it was not previously favorited)
    public boolean delFav(String id, FAV_TYPE type) {
        // Select the proper prefs file
        SharedPreferences prefs = null;
        Map<String, Integer> favs = null;
        switch (type) {
            case STOP:
                prefs = stopPrefs;
                favs = stopFavs;
                break;
            case ROUTE:
                prefs = routePrefs;
                favs = routeFavs;
                break;
        }

        // If the map contains the value, remove it
        if (prefs.contains(id)) {
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.remove(id);
            prefsEditor.apply();

            favs.remove(id);

            return true;
        }

        // Otherwise we removed nothing, so inform the user
        return false;
    }

    // Toggle the favorite status of a route/stop
    public boolean toggleFav(String id, FAV_TYPE type) {
        // Select the proper favs file
        Map<String, Integer> favs = null;
        switch (type) {
            case STOP:
                favs = stopFavs;
                break;
            case ROUTE:
                favs = routeFavs;
                break;
        }

        // Toggle the favorite, and return the new value
        if (favs.containsKey(id)) {
            delFav(id, type);

            // Return false, we've removed it from the favorites
            return false;
        }
        else {
            addFav(id, type);

            // Return true, we added it to the favorites
            return true;
        }
    }
}
