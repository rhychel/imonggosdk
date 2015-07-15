package net.nueca.imonggosdk.operations.sync;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import net.nueca.imonggosdk.R;
import net.nueca.imonggosdk.activities.ImonggoActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jn on 7/14/2015.
 * imonggosdk(2015)
 */
public class TestService extends ImonggoService {

    private final IBinder mLocalbinder = new LocalBinder();
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNoticationBuilder;

    private int NOTIFICATION = 200;

    public TestService(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("TestService", "onBind done");
        return mLocalbinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return false;
    }


    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public TestService getService(){
            return TestService.this;
        }
    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNoticationBuilder = new NotificationCompat.Builder(this);
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        Log.i("TestService", "OnCreateCalled");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNotificationManager.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, "Service has stopped", Toast.LENGTH_SHORT).show();


    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {


        // Set the icon, scrolling text and timestamp
        mNoticationBuilder
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentTitle("Test Service")
                .setContentText("Service is starting");

        Intent resultIntent = new Intent(this, ImonggoActivity.class);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mNoticationBuilder.setContentIntent(resultPendingIntent);


        // Send the notification.
        mNotificationManager.notify(NOTIFICATION, mNoticationBuilder.build());
    }


    public String getTime() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return mDateFormat.format(new Date());
    }

}
