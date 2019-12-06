package com.pocketapps.pockalendar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * Created by chandrima on 24/04/18.
 */

public class PokalNotifications {
    private static final String CHANNEL_ID = "pokalnotifchannel";
    private Context mContext;
    private static PokalNotifications sPokalNotifications;

    private PokalNotifications(Context context) {
        mContext = context;
        createChannel();
    }

    public PokalNotifications getInstance(Context context) {
        if (sPokalNotifications == null) {
            sPokalNotifications = new PokalNotifications(context);
        }
        return sPokalNotifications;
    }

    private void setAlarm() {

    }

    private void createNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm_on_black_24dp)
                .setContentTitle(mContext.getString(R.string.AppName))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = mContext.getString(R.string.NotificationChannelName);
            String description = mContext.getString(R.string.NotificationChannelDescription);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
