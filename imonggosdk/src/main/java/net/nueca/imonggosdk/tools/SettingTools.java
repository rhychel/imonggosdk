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
    private static final String IS_SYNC_FINISHED = "_sync_finished";


    /**
     * Add setting name to shared preferences
     * Use this if Setting does not need string value
     * @param context
     * @param settingsName
     * @param bool
     */
    public static void updateSettings(Context context, SettingsName settingsName, boolean bool) {
        updateSettings(context, settingsName, bool, "");
    }

    public static void updateSettings(Context context, SettingsName settingsName, String value) {
        updateSettings(context, settingsName, false, value);
    }

    /**
     * Add setting name to shared preferences
     *
     * @param context      current context
     * @param settingsName name of the Settings that will be updated
     * @param bool         for boolean input
     * @param value        for string input
     */
    public static void updateSettings(Context context, SettingsName settingsName, boolean bool, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();

            // AUTO_UPDATE
            if (settingsName == SettingsName.AUTO_UPDATE) {
                Log.e("Key[updateIsAutoUpdate]", pinfo.packageName + IS_AUTOUPDATE);
                editor.putBoolean(pinfo.packageName + IS_AUTOUPDATE, bool);
                editor.apply();
            } else if (settingsName == SettingsName.BRANCH_NAME) {
                Log.e("Key[defaultBranch]", pinfo.packageName + DEFAULT_BRANCH);
                editor.putString(pinfo.packageName + DEFAULT_BRANCH, value);
                editor.apply();
            } else if (settingsName == SettingsName.SYNC_FINISHED) {
                Log.e("Key[defaultBranch]", pinfo.packageName + IS_SYNC_FINISHED);
                editor.putBoolean(pinfo.packageName + IS_SYNC_FINISHED, bool);
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
     * Returns SyncFinished Setting
     *
     * @param context current context
     * @return true if syncing modules is finished, false otherwise
     */
    public static boolean isSyncFinished(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e("Key[sync_finished", pinfo.packageName + IS_SYNC_FINISHED);
            return preferences.getBoolean(pinfo.packageName + IS_SYNC_FINISHED, true);

        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[sync_finished]", "Not Found");
            return true;
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
     * @return current mServer, "" if none
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
