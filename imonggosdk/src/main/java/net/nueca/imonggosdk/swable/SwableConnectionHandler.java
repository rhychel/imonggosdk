package net.nueca.imonggosdk.swable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SwableConnectionHandler extends BroadcastReceiver {

	public interface OnConnectionChangedListener {
		void onConnectivityChanged(boolean isConnected);
	}

	private OnConnectionChangedListener onConnectionChangedListener = null;
	
	private boolean hasConnectivity = false;

	public void setOnConnectionChangedListener(
			OnConnectionChangedListener onConnectionChangedListener) {
		this.onConnectionChangedListener = onConnectionChangedListener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    
	    hasConnectivity = (haveConnectedMobile || haveConnectedWifi);
	    if(hasConnectivity)
			onConnectionChangedListener.onConnectivityChanged(true);
	    else
			onConnectionChangedListener.onConnectivityChanged(false);
	}

	public boolean isHasConnectivity() {
		return hasConnectivity;
	}

	public void setHasConnectivity(boolean hasConnectivity) {
		this.hasConnectivity = hasConnectivity;
	}

}
