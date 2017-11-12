package com.beattheheat.beatthestreet.Networking.OC_API;


import android.arch.persistence.room.*;

/**
 * Created by Matt on 24-Oct-17.
 */

@Entity
public class OCTrips {
    /*
    trip_id	                            arrival_time	departure_time	stop_id	 stop_sequence	pickup_type	 drop_off_type
    49754901-SEPT17-SEPDA17-Weekday-21	05:48:00	    05:48:00	    RF900	 1	            0	         0
     */
    public OCTrips() {

    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "trip_id")
    private String tripID;

    @ColumnInfo(name = "stop_id")
    private String stopID;


    @ColumnInfo(name = "arrival_hour")
    private int arrivalHour;

    @ColumnInfo(name = "arrival_minute")
    private int arrivalMinute;

    @ColumnInfo(name = "arrival_second")
    private int arrivalSecond;

    @ColumnInfo(name = "stop_sequence")
    private int stopSequence;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTripID() {
        return tripID;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

    public String getStopID() {
        return stopID;
    }

    public void setStopID(String stopID) {
        this.stopID = stopID;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    public int getArrivalHour() {
        return arrivalHour;
    }

    public void setArrivalHour(int arrivalHour) {
        this.arrivalHour = arrivalHour;
    }

    public int getArrivalMinute() {
        return arrivalMinute;
    }

    public void setArrivalMinute(int arrivalMinute) {
        this.arrivalMinute = arrivalMinute;
    }

    public int getArrivalSecond() {
        return arrivalSecond;
    }

    public void setArrivalSecond(int arrivalSecond) {
        this.arrivalSecond = arrivalSecond;
    }
}
