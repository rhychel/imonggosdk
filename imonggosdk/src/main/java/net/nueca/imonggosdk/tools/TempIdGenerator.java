package net.nueca.imonggosdk.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
/**
 *
 * Temporary ID generator from -1 to MAX_INT
 *
 * Created by rhymart on 18/12/2015.
 * imonggosdk (c)2015
 */
public class TempIdGenerator {
    private static int getTempId(Context context, Class<?> classClass, SharedPreferences preferences) {
        return preferences.getInt(classClass.getSimpleName(), 0);
    }

    public static int getTempId(Context context, Class<?> classClass) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return getTempId(context, classClass, preferences);
    }

    public static void setTempId(Context context, Class<?> classClass, int tempId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(classClass.getSimpleName(), tempId);
        editor.apply();
    }
    public static int generateTempId(Context context, Class<?> classClass) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int currTempId = getTempId(context, classClass, preferences);
        currTempId--;
        setTempId(context, classClass, currTempId);
        return currTempId;
    }
}
