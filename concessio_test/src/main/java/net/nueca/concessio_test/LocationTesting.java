package net.nueca.concessio_test;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by rhymart on 11/23/15.
 */
public class LocationTesting extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.e("Latitude", location.getLatitude() + "");
                Log.e("Longitude", location.getLongitude()+"");
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.e("onStatusChanged", status+"");
            }
            public void onProviderEnabled(String provider) {
                Log.e("onProviderEnabled", provider);
            }
            public void onProviderDisabled(String provider) {
                Log.e("onProviderEnabled", provider);
            }
        };
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }
}
