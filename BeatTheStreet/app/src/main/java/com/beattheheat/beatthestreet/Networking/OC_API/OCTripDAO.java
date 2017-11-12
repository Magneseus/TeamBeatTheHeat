package com.beattheheat.beatthestreet.Networking.OC_API;

import android.arch.persistence.room.*;

/**
 * Created by Matt on 09-Nov-17.
 */

@Dao
public interface OCTripDAO {
    @Query("SELECT Count(*) FROM octrips")
    public int numRows();

    @Query("SELECT * FROM OCTrips WHERE trip_id = :searchTripID")
    public OCTrips[] loadAllTripsWithID(String searchTripID);

    @Query("SELECT * FROM OCTrips WHERE stop_id = :searchStopID")
    public OCTrips[] loadALlTripsWithStopID(String searchStopID);


    @Insert
    public void insertTrip(OCTrips tripToInsert);

    @Insert
    public void insertTrips(OCTrips... tripsToInsert);


    @Delete
    public void deleteTrip(OCTrips tripToDelete);

    @Delete
    public int deleteTrips(OCTrips... tripsToDelete);


    @Update
    public int updateTrips(OCTrips... tripsToUpdate);
}
