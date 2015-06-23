package net.nueca.imonggosdk.swable;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;

import com.android.volley.RequestQueue;

import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.operations.http.HTTPRequests;

public class SwableTools {
	public static Intent startSwable(Activity activity) {
		Intent service = new Intent(activity,ImonggoSwable.class);
		activity.startService(service);
		return service;
	}
	
	public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}
