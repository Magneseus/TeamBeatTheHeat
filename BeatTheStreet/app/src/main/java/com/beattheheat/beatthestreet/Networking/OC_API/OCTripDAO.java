package com.beattheheat.beatthestreet.Networking.OC_API;

import android.arch.persistence.room.*;

import java.util.List;

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
    public OCTrips[] loadAllTripsWithStopID(String searchStopID);

    @Query("SELECT * FROM octrips WHERE trip_id = :searchTripID AND stop_id = :searchStopID")
    public OCTrips[] loadAllTripTimesWithStopID(String searchTripID, String searchStopID);

    @Query("SELECT * FROM octrips WHERE trip_id IN (:searchTripID) AND stop_id = :searchStopID")
    public OCTrips[] loadAllTripTimesWithStopID(List<String> searchTripID, String searchStopID);

    @Query("SELECT trip_id FROM octrips WHERE arrival_hour = :start_hour AND arrival_min = :start_min AND arrival_sec = :start_sec AND stop_sequence = 0 AND trip_id in (:tripIDs)")
    public String[] getTripIDForStartTime(List<String> tripIDs, int start_hour, int start_min, int start_sec);


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
