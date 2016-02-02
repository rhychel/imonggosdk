package net.nueca.concessioengine.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

/**
 * Created by gama on 12/01/2016.
 */
public class LocationTools {
    private static String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
    private static String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static Location currentLocation = null;
    private static LocationManager locationManager;
    private static LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (isBetterLocation(location, currentLocation))
                currentLocation = location;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    public static void startLocationSearch(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationTools", "Error : No Permission - add to manifest : ACCESS_FINE_LOCATION && ACCESS_COARSE_LOCATION");
            return;
        }
        currentLocation = getLastKnownLocation(context);
        locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public static void stopLocationSearch(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationTools", "Error : No Permission - add to manifest : ACCESS_FINE_LOCATION && ACCESS_COARSE_LOCATION");
            return;
        }
        locationManager.removeUpdates(locationListener);
        locationManager = null;
    }

    public static Location getCurrentLocation() {
        return currentLocation;
    }

    public static double limitDecimal(double value, int decimal) {
        DecimalFormat decimalFormat = new DecimalFormat("0." + StringUtils.repeat("0",decimal));
        return Double.parseDouble(decimalFormat.format(value));
    }

    @Nullable
    public static Location getLastKnownLocation(Context context) {
        Location location = getLastKnownLocationByGPS(context);
        if(location == null)
            location = getLastKnownLocationByNetwork(context);

        return location;
    }

    public static Location getLastKnownLocationByGPS(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationTools", "Error : No Permission - add to manifest : ACCESS_FINE_LOCATION && ACCESS_COARSE_LOCATION");
            return null;
        }
        return locationManager.getLastKnownLocation(GPS_PROVIDER);
    }
    public static Location getLastKnownLocationByNetwork(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationTools", "Error : No Permission - add to manifest : ACCESS_FINE_LOCATION && ACCESS_COARSE_LOCATION");
            return null;
        }
        return locationManager.getLastKnownLocation(NETWORK_PROVIDER);
    }

    protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
