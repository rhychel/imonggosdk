package net.nueca.dizonwarehouse.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.nueca.imonggosdk.swable.ImonggoSwableServiceConnection;

/**
 * Created by gama on 30/05/2016.
 */
public class SwableTools extends net.nueca.imonggosdk.swable.SwableTools {
    public static Intent startSwable(Activity activity) {
        Log.e("SwableTools", "startSwable in " + activity.getClass().getSimpleName());
        Intent service = new Intent(activity,WarehouseSwable.class);
        activity.startService(service);
        return service;
    }
    public static Intent stopSwable(Activity activity) {
        Log.e("SwableTools", "stopSwable in " + activity.getClass().getSimpleName());
        Intent service = new Intent(activity,WarehouseSwable.class);
        activity.stopService(service);
        return service;
    }
    public static boolean startAndBindSwable(Activity activity, ImonggoSwableServiceConnection swableServiceConnection) {
        return activity.bindService(startSwable(activity), swableServiceConnection, Context.BIND_AUTO_CREATE);
    }
    public static Intent stopAndUnbindSwable(Activity activity, ImonggoSwableServiceConnection swableServiceConnection) {
        activity.unbindService(swableServiceConnection);
        return stopSwable(activity);
    }
    public static boolean bindSwable(Activity activity, ImonggoSwableServiceConnection swableServiceConnection) {
        Intent service = new Intent(activity,WarehouseSwable.class);
        return activity.bindService(service, swableServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void unbindSwable(Activity activity, ImonggoSwableServiceConnection swableServiceConnection) {
        try {
            activity.unbindService(swableServiceConnection);
        } catch (Exception e) {
            Log.e("Oops!", e.getMessage());
        }
    }
}
