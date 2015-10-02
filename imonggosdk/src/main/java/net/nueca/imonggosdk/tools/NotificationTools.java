package net.nueca.imonggosdk.tools;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import net.nueca.imonggosdk.R;

/**
 * Created by gama on 7/15/15.
 */
public class NotificationTools {
    public static void postNotification(@NonNull Context context, int notificationId, @DrawableRes int
            iconDrawableResource, String title, String message) {
        postNotification(context, notificationId, iconDrawableResource, title, message, null, null);
    }

    public static void postNotification(@NonNull Context context, int notificationId, @DrawableRes int
            iconDrawableResource, String title, String message, @Nullable PendingIntent whenSelectedIntent) {
        postNotification(context, notificationId, iconDrawableResource, title, message, whenSelectedIntent, null);
    }

    public static void postNotification(@NonNull Context context, int notificationId, @DrawableRes int
                        iconDrawableResource, String title, String message, @Nullable PendingIntent whenSelectedIntent,
                        @Nullable PendingIntent whenDeletedIntent) {
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService
                (Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mNoticationBuilder = new NotificationCompat.Builder(context);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if(whenSelectedIntent != null)
            mNoticationBuilder.setContentIntent(whenSelectedIntent);
        if(whenDeletedIntent != null)
            mNoticationBuilder.setDeleteIntent(whenDeletedIntent);

        mNoticationBuilder
                .setSmallIcon(iconDrawableResource)
                .setContentTitle(title)
                .setSound(alarmSound)
                .setAutoCancel(true)
                .setContentText(message);

        mNotificationManager.notify(notificationId, mNoticationBuilder.build());
    }

    public static void cancelNotification(@NonNull Context context, int notificationId) {
        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService
                (Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationId);
    }
}
