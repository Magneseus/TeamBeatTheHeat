package com.beattheheat.beatthestreet.Networking.OC_API;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Matt on 09-Nov-17.
 */


@Database(entities = {OCTrips.class}, version = 1)
public abstract class OCTripDatabase extends RoomDatabase {
    public abstract OCTripDAO tripDAO();
}
