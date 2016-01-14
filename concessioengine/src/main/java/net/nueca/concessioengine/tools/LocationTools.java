package net.nueca.concessioengine.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

/**
 * Created by gama on 12/01/2016.
 */
public class LocationTools {
    public static double limitDecimal(double value, int decimal) {
        DecimalFormat decimalFormat = new DecimalFormat("0." + StringUtils.repeat("0",decimal));
        return Double.parseDouble(decimalFormat.format(value));
    }

    @Nullable
    public static Location getLocation(Context context) {
        Location location = getLocationByGPS(context);
        if(location == null)
            location = getLocationByNetwork(context);

        return location;
    }

    public static Location getLocationByGPS(Context context) {
        String gpsProvider = LocationManager.GPS_PROVIDER;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationTools", "Error : No Permission - add to manifest : ACCESS_FINE_LOCATION && ACCESS_COARSE_LOCATION");
            return null;
        }
        return locationManager.getLastKnownLocation(gpsProvider);
    }
    public static Location getLocationByNetwork(Context context) {
        String networkProvider = LocationManager.NETWORK_PROVIDER;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationTools", "Error : No Permission - add to manifest : ACCESS_FINE_LOCATION && ACCESS_COARSE_LOCATION");
            return null;
        }
        return locationManager.getLastKnownLocation(networkProvider);
    }
}
