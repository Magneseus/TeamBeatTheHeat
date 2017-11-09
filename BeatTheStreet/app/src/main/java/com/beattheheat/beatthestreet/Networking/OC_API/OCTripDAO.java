package com.beattheheat.beatthestreet.Networking.OC_API;

import android.arch.persistence.room.*;

/**
 * Created by Matt on 09-Nov-17.
 */

@Dao
public interface OCTripDAO {
    @Query("SELECT * FROM octrip LIMIT 1")
    public OCTrip loadFirstRow();

    @Query("SELECT * FROM octrip WHERE trip_id = :searchTripID")
    public OCTrip[] loadAllTripsWithID(String searchTripID);

    @Query("SELECT * FROM octrip WHERE stop_id = :searchStopID")
    public OCTrip[] loadALlTripsWithStopID(String searchStopID);


    @Insert
    public void insertTrip(OCTrip tripToInsert);

    @Insert
    public void insertTrips(OCTrip... tripsToInsert);


    @Delete
    public void deleteTrip(OCTrip tripToDelete);

    @Delete
    public int deleteTrips(OCTrip... tripsToDelete);


    @Update
    public int updateTrips(OCTrip... tripsToUpdate);
}
