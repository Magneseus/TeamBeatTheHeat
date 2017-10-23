package com.beattheheat.beatthestreet.Networking;

/**
 * Created by Matt on 27-Sep-17.
 *
 * Small convenience interface for callbacks in the OCTranspo api calls
 */

public interface SCallable<A> {
    void call(A arg);
}
