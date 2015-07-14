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
    private static final String DEFAULT_BRANCH = "_default_branch";
    private static final String CURRENT_SERVER = "_current_server";

    /**
     * Add setting name to shared preferences
     *
     * @param context current context
     * @param settingsName name of the Settings that will be updated
     * @param bool for boolean input
     * @param value for string input
     */
    public static void updateSettings(Context context, SettingsName settingsName, boolean bool, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            // AUTO_UPDATE
            if(settingsName == SettingsName.AUTO_UPDATE) {
                Log.e("Key[updateIsAutoUpdate]", pinfo.packageName + IS_AUTOUPDATE);
                editor.putBoolean(pinfo.packageName + IS_AUTOUPDATE, bool);
                editor.apply();
            } else if(settingsName == SettingsName.BRANCH_NAME){
                Log.e("Key[defaultBranch]", pinfo.packageName + DEFAULT_BRANCH);
                editor.putString(pinfo.packageName + IS_AUTOUPDATE, value);
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
            return preferences.getBoolean(pinfo.packageName + IS_AUTOUPDATE, false);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[isAutoUpdate]", "Not Found");
            return true;
        }
    }

    /**
     * Returns Default Branch
     *
     * @param context current context
     * @return Branch name, "" if none
     */
    public static String defaultBranch(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e("Key[defaultBranch]", pinfo.packageName + DEFAULT_BRANCH);
            return preferences.getString(pinfo.packageName + DEFAULT_BRANCH, "");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[defaultBranch]", "Not Found");
            return "";
        }

    }

    /**
     * Returns Current Selected Server
     *
     * @param context current context
     * @return current server, "" if none
     */
    public static String currentServer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e("Key[currentServer]", pinfo.packageName + CURRENT_SERVER);
            return preferences.getString(pinfo.packageName + CURRENT_SERVER, "");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[currentServer]", "Not Found");
            return "";
        }
    }
}
