package net.nueca.imonggosdk.tools;

import android.util.Log;

import java.util.Calendar;

/**
 * Created by rhymartmanchus on 12/02/2016.
 */
public class TimerTools {
    private static long startMS;

    public static void start(String label) {
        startMS = Calendar.getInstance().getTimeInMillis();
        Log.e("TimerTools", "starting---"+label);
    }

    public static void duration(String label, boolean restart) {
        if(restart) {
            long current = Calendar.getInstance().getTimeInMillis();
            long duration = current - startMS;
            startMS = current;
            Log.e("TimerTools", label+" = "+duration+"ms");
        }
        Log.e("TimerTools", label+" = "+(Calendar.getInstance().getTimeInMillis()-startMS)+"ms");
    }

    public static void duration(String label) {
        duration(label, false);
    }

}
