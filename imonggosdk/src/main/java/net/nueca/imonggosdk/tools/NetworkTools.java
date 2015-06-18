package net.nueca.imonggosdk.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;

public class NetworkTools {
	
	/**
	 * Checks wether there is an internet connection available
	 * 
	 * */
	public static boolean isInternetAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null)
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		else
			return false;
	}
	

	public static void networkConnectionRequired(final Activity activity){
		AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		alert.setTitle("Network Connection Error");
		alert.setMessage("This app requires internet connection.");
		alert.setCancelable(false);
		alert.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.finish();
			}
		});
		alert.show();
	}
}
