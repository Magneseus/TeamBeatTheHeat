package com.beattheheat.beatthestreet.Networking;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.beattheheat.beatthestreet.Networking.OC_API.OCBus;
import com.beattheheat.beatthestreet.Networking.OC_API.OCTranspo;
import com.beattheheat.beatthestreet.R;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by kylec on 2017-11-17.
 */

public class ActionReceiver extends BroadcastReceiver {
    private static float recentNotificationTimestamp = -1;
    private static boolean shouldNotify = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = (String)intent.getExtras().get("action");

        if(action.equals("start")) {    // starting a new notif
            makeNotification(context, intent);
        } else if (action.equals("next_bus")) {     // clicked the "next bus" button
            nextBus(context, intent);
        } else if (action.equals("cancel")) {       // swiped away the notification
            cancelTimer();
        }
    }

    public static void makeNotification(Context context, final Intent intent)
    {
        class RunnablePointer {
            float start;
            Integer mins_until_bus;
            boolean last_bus;

            float time_of_last_check = -1;

            public Runnable run;
            public RunnablePointer(Runnable run) {
                this.run = run;
            }
        }

        final Context ctx = context;

        shouldNotify = true;

        final Handler handler = new Handler();
        final RunnablePointer runPointer = new RunnablePointer(null);
        runPointer.start = SystemClock.elapsedRealtime();
        recentNotificationTimestamp = runPointer.start;

        final Integer route_num = (Integer) intent.getExtras().get("route_num");
        final Date start_time = (Date) intent.getExtras().get("start_time");
        final String stop_code = (String) intent.getExtras().get("stop_code");
        runPointer.mins_until_bus = (Integer) intent.getExtras().get("mins_until_bus");
        runPointer.last_bus = (Boolean) intent.getExtras().get("last_available");

        Intent buttons = new Intent(ctx, ActionReceiver.class);
        buttons.putExtra("action", "next_bus");
        buttons.putExtra("route_num", route_num);
        buttons.putExtra("stop_code", stop_code);
        buttons.putExtra("start_time", start_time);
        buttons.putExtra("last_available", runPointer.last_bus);

        Intent cancel = new Intent(context, ActionReceiver.class);
        cancel.putExtra("action", "cancel");

        PendingIntent pi_next = PendingIntent.getBroadcast(ctx, 2, buttons,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final PendingIntent pi_cancel = PendingIntent.getBroadcast(ctx, 1, cancel,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Action next_action = new NotificationCompat.Action
                .Builder(NotificationUtil.defaultIcon, "Next Bus", pi_next)
                .build();

        final NotificationCompat.Action cancel_action = new NotificationCompat.Action
                .Builder(NotificationUtil.defaultIcon, "Cancel", pi_cancel)
                .build();

        runPointer.run = new Runnable() {
            @Override
            public void run() {
                // check if we should re-notify
                // reasons not to: user has cancelled, bus has passed, new notif is here

                // if there's a more recent notification than us, just stop
                if (runPointer.start != recentNotificationTimestamp)
                    return;

                if(shouldNotify) {

                    final float curMillis = SystemClock.elapsedRealtime();

                    final int remainingMillis = (int) ((runPointer.start + runPointer.mins_until_bus * 60000)
                            - curMillis);

                    final int remainingMins = remainingMillis / 60000;
                    int millis_since_check = (int) (curMillis - runPointer.time_of_last_check);

                    if (remainingMillis <= 0)
                        return;

                /*
                    The data on when the bus is coming is not static, so we may need to update
                    what time the bus is coming at (because it may be later or earlier now)

                    re-check if:
                        The bus is less than 45 minutes away.
                                        AND any of the following
                        The bus is more than 15 minutes away, and we haven't checked within
                        the last 4 minutes
                                        OR
                        The bus is less than 15 minutes away, and we haven't checked in the last
                        2 minutes.
                                        OR
                        The bus is less than 7 minutes away, and we haven't checked in the last 45s.
                */

                    boolean outdated = (remainingMins < 45) &&
                            ((remainingMins > 15 && millis_since_check > 4 * 60 * 1000)
                                    || (remainingMins <= 15 && millis_since_check > 2 * 60 * 1000)
                                    || (remainingMins < 7 && millis_since_check > 45 * 1000));

                    // if we are outdated, we want to attempt to update the arrival with GPS time
                    if (outdated) {
                        OCTranspo.getInstance().GetNextTripsForStopAndRoute(stop_code,
                                route_num.toString(), new SCallable<OCBus[]>() {
                                    @Override
                                    public void call(OCBus[] arg) {
                                        // check if the bus is in the upcoming busses
                                        // if it's not, then we won't update the time (because there
                                        // isn't a GPS time to update with)

                                        int n = -1;
                                        for (int i = 0; i < arg.length; i++) {
                                            if (arg[i].getTripStart().equals(start_time)) {
                                                n = i;
                                                break;
                                            }
                                        }

                                        // if we found no bus, we do nothing else
                                        if (n == -1)
                                            return;

                                        if (runPointer.start != recentNotificationTimestamp)
                                            return;

                                        // if we were the last bus and now we're not, allow us to next
                                        if (runPointer.last_bus && n != 2)
                                            runPointer.last_bus = false;

                                        // if we're mostly lined up with the bus time, don't
                                        // update it
                                        if(Math.abs(remainingMins - arg[n].getMinsTilArrival()) > 1) {
                                            runPointer.mins_until_bus = arg[n].getMinsTilArrival();
                                            runPointer.start = SystemClock.elapsedRealtime();
                                            recentNotificationTimestamp = runPointer.start;
                                        }
                                    }
                                });

                        runPointer.time_of_last_check = curMillis;
                    }

                    DecimalFormat df = new DecimalFormat("00");

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)
                            .setContentTitle("Bus ETA")
                            .setSmallIcon(R.drawable.ic_bus)
                            .setContentText("Bus " + route_num + " at " + stop_code
                                    + " will arrive in "
                                    + remainingMillis / 60000
                                    + ":"
                                    + df.format((remainingMillis % 60000) / 1000) + ".")
                            .setColorized(true)
                            .setColor(Color.argb(255, 180, 18, 0))
                            .setOngoing(true)
                            .addAction(cancel_action);
                            //.setDeleteIntent(pi_cancel);

                    if (!runPointer.last_bus)
                        builder.addAction(next_action);

                    Notification n = builder.build();

                    NotificationUtil.getInstance().notify(ctx, 0, n);
                }

                handler.postDelayed(runPointer.run, 1000);
            }
        };

        handler.postDelayed(runPointer.run, 1000);
    }

    // we want to set the timer for the next bus
    public void nextBus(final Context context, Intent intent)
    {
        final Integer route_num = (Integer) intent.getExtras().get("route_num");
        final Date start_time = (Date) intent.getExtras().get("start_time");
        final String stop_code = (String) intent.getExtras().get("stop_code");

        if(route_num == null || start_time == null || stop_code == null)
            return;

        cancelTimer();

        NotificationUtil.getInstance().notify(context, 0, "Acquiring information for the next bus...");

        OCTranspo.getInstance().GetNextTripsForStopAndRoute(stop_code, route_num.toString(),
                new SCallable<OCBus[]>() {
                    @Override
                    public void call(OCBus[] arg) {
                        int n = -1;
                        for(int i=0; i<arg.length-1; i++)
                        {
                            if(arg[i].getTripStart().equals(start_time))
                            {
                                n = i+1;
                                break;
                            }
                        }

                        if(n == -1)
                            return;

                        Intent newIntent = new Intent(context, ActionReceiver.class);
                        newIntent.putExtra("action", "start");
                        newIntent.putExtra("route_num", route_num);
                        newIntent.putExtra("stop_code", stop_code);
                        newIntent.putExtra("start_time", arg[n].getTripStart());
                        newIntent.putExtra("mins_until_bus", arg[n].getMinsTilArrival());
                        newIntent.putExtra("last_available", (n==2));

                        onReceive(context, newIntent);
                    }
                });
    }

    public void cancelTimer() {
        shouldNotify = false;
    }
}
