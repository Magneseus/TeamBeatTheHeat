package com.beattheheat.beatthestreet.Networking;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import android.R;
import android.support.v4.app.NotificationCompat;

/**
 * Created by kylec on 2017-10-25.
 */

public class NotificationUtil {
    private static NotificationUtil myObj;
    final private int defaultIcon = R.drawable.btn_default;

    private NotificationUtil() {

    }

    public static NotificationUtil getInstance() {
        if(myObj == null) {
            myObj = new NotificationUtil();
        }

        return myObj;
    }

    public void notify(Context ctx, int id, String title)
    {
        notify(ctx, id, title, null);
    }

    public void notify(Context ctx, int id, String title, String text)
    {
        notify(ctx, id, title, text, -1);
    }

    public void notify(Context ctx, int id, String title, String text, int icon)
    {
        notify(ctx, id, title, text, -1, false);
    }

    public void notify(Context ctx, int id, String title, String text, int icon, boolean persistent) {
        if(icon < 0)
        {
            icon = defaultIcon;
        }

        Notification n = new Notification.Builder(ctx)
                .setContentTitle(title)
                .setContentText(text)
                .setOngoing(persistent)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .build();

        notify(ctx, id, n);
    }

    public void notify(Context ctx, int id, Notification n) {
        NotificationManager notificationManager = (NotificationManager)
                ctx.getSystemService(ctx.NOTIFICATION_SERVICE);

        notificationManager.notify(id, n);
    }
}
