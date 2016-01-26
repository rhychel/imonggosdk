package net.nueca.imonggosdk.tools;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import net.nueca.imonggosdk.interfaces.SyncServiceConnectionListener;

/**
 * Created by Jn on 20/01/16.
 */
public class SyncServiceHelper {

    private static String TAG = "SyncServiceHelper";

    /**
     * For Sync Service only
     * Defines callbacks for service binding, passed to bindService()
     */
    public static ServiceConnection SyncServiceConnection(final SyncServiceConnectionListener serviceConnectionListener) {

        return new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceConnectionListener.onServiceConnected(name,service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceConnectionListener.onServiceDisconnected(name);
            }
        };

    }

    public static boolean isSyncServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.e(TAG, "Service is Running");
                return true;
            }
        }
        Log.e(TAG, "Service is not Running");
        return false;
    }

}
