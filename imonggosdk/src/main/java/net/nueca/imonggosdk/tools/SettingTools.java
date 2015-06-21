package net.nueca.imonggosdk.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import net.nueca.imonggosdk.enums.SettingsName;

/**
 * Created by Jn on 6/19/2015.
 * imonggosdk (c)2015
 */
public class SettingTools {

    private static final String IS_AUTOUPDATE = "_is_auto_update";

    /**
     * Add setting name to shared preferences
     *
     * @param context current context
     * @param settingsName name of the Settings that will be updated
     * @param bool true or false
     */
    public static void updateSettings(Context context, SettingsName settingsName, boolean bool) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            // AUTO_UPDATE
            if(settingsName == SettingsName.AUTO_UPDATE) {
                Log.e("Key[updateIsAutoUpdate]", pinfo.packageName + IS_AUTOUPDATE);
                editor.putBoolean(pinfo.packageName + IS_AUTOUPDATE, bool);
                editor.apply();
            } else {
                // OTHER SETTINGS NAME
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[updatingSettings]", "Not Found");
            e.printStackTrace();
        }
    }

    /**
     * Returns AutoUpdate Settings
     *
     * @param context current context
     * @return true if AutoUpdate is on, false otherwise.
     */
    public static boolean isAutoUpdate(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e("Key[isAutoUpdate]", pinfo.packageName + IS_AUTOUPDATE);
            return preferences.getBoolean(pinfo.packageName + IS_AUTOUPDATE, true);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[isAutoUpdate]", "Not Found");
            return true;
        }
    }
}
