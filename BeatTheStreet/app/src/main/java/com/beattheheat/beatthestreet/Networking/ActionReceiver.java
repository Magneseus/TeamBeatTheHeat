package com.beattheheat.beatthestreet.Networking;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.beattheheat.beatthestreet.R;

/**
 * Created by kylec on 2017-11-17.
 */

public class ActionReceiver extends BroadcastReceiver {
    private static float recentNotificationTimestamp = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
       makeNotification(context, intent);
    }

    public static void makeNotification(Context context, Intent intent)
    {
        class RunnablePointer {
            float timestamp;
            public Runnable run;
            public RunnablePointer(Runnable run) {
                this.run = run;
            }
        }

        final Context ctx = context;
        final long start = SystemClock.elapsedRealtime();
        recentNotificationTimestamp = start;

        final Handler handler = new Handler();

        final RunnablePointer runPointer = new RunnablePointer(null);
        runPointer.timestamp = start;
        runPointer.run = new Runnable() {
            @Override
            public void run() {
                // check if we should re-notify
                // reasons not to: user has cancelled, bus has passed, new notif is here

                // if there's a more recent notification than us, just stop
                if (runPointer.timestamp != recentNotificationTimestamp)
                    return;

                Intent intent = new Intent(ctx, ActionReceiver.class);
                intent.putExtra("action", "reset");


                PendingIntent pi = PendingIntent.getBroadcast(ctx, 1, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                Notification n = new NotificationCompat.Builder(ctx)
                        .setContentTitle("Bus")
                        .setSmallIcon(NotificationUtil.defaultIcon)
                        .setContentText("Bus will arrive in " +
                                (SystemClock.elapsedRealtime() - start)/1000)
                        .addAction(NotificationUtil.defaultIcon, "Reset", pi)
                        .build();

                NotificationUtil.getInstance().notify(ctx, 0, n);
                //NotificationUtil.getInstance().notify(ctx, 0, "Timer", "" + (SystemClock.elapsedRealtime()
                //        - start)/1000 + "s");
                handler.postDelayed(runPointer.run, 1000);
            }
        };

        handler.postDelayed(runPointer.run, 1000);
    }

    public void reset()
    {

    }
}
