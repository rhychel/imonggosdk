package net.nueca.imonggosdk.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.objects.Session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 5/13/15.
 * Modified by Jn on 6/16/15
 * imonggosdk (c)2015
 */
public class AccountTools {
    private static final String TAG = "AccountTools";
    private static final String IS_UNLINKED = "_is_unlinked";
    private static final String IS_ACTIVE_USER = "_is_active_user";
    private static final String MODULES_TO_SYNC = "_modules_to_sync";
    private static final String MODULES_TO_SYNC_SIZE = "_modules_to_sync_size";

    /**
     * Check if the user is logged in on their Imonggo/Iretailcloud account.
     *
     * @param dbHelper
     * @return true if LoggedIn, false otherwise.
     * @throws SQLException
     */
    public static boolean isLoggedIn(ImonggoDBHelper2 dbHelper) throws SQLException {
        return (dbHelper.fetchObjects(Session.class).countOf() > 0);
    }


    /**
     * Checks if an Account is linked in the device
     *
     * @param context Current mContext
     * @return
     */
    public static boolean isUnlinked(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e("Key[isUnlinked]", pinfo.packageName + IS_UNLINKED);
            return preferences.getBoolean(pinfo.packageName + IS_UNLINKED, true);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[isUnlinked]", "Not Found");
            return true;
        }
    }

    /**
     * Update LinkedAccount
     *
     * @param context
     * @param isUnlinked
     */
    public static void updateUnlinked(Context context, boolean isUnlinked) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();
            Log.e("Key[updateIsUnlinked]", pinfo.packageName + IS_UNLINKED);
            editor.putBoolean(pinfo.packageName + IS_UNLINKED, isUnlinked);
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[updateIsUnlinked]", "Not Found");
            e.printStackTrace();
        }
    }

    /**
     * Removes user details from database
     *
     * @param context
     * @param dbHelper
     * @param accountListener
     */
    public static void logoutUser(Context context, ImonggoDBHelper2 dbHelper, AccountListener accountListener) throws SQLException {
        // Get the session and reset; AccountId, Email, and Password.
        Session session = dbHelper.fetchObjectsList(Session.class).get(0);
        session.setAccountId("");
        session.setEmail("");
        session.setPassword("");
        session.setHas_logged_in(false);

        // update database
        session.updateTo(dbHelper);

        updateUnlinked(context, false);

        // update the account listener
        if (accountListener != null) {
            accountListener.onLogoutAccount();
        }
    }

    public static void unlinkAccount(Context context, ImonggoDBHelper2 dbHelper) throws SQLException {
        unlinkAccount(context, dbHelper, null);
    }

    /**
     * Deletes Account Details from the database
     * <p>
     * TODO: Offline Data
     *
     * @param context
     * @param dbHelper
     * @param accountListener
     * @throws SQLException
     */
    public static void unlinkAccount(Context context, ImonggoDBHelper2 dbHelper, AccountListener accountListener) throws SQLException {

        updateUnlinked(context, true);
        updateUserActiveStatus(context, false);

        dbHelper.deleteAllDatabaseValues();
        SettingTools.updateSettings(context, SettingsName.SYNC_FINISHED, false);
        SettingTools.updateSettings(context, SettingsName.DEFAULT_BRANCH, "");
        SettingTools.updateSettings(context, SettingsName.CURRENT_BRANCH, "");

        // update the account listener
        if (accountListener != null) {
            accountListener.onUnlinkAccount();
        }

        Log.i("Jn-BaseLogin", "Unlinking Account");
    }

    /**
     * Checks if User is still active
     *
     * @param context Current mContext
     */
    public static boolean isUserActive(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e("Key[isActiveUser]", pinfo.packageName + IS_ACTIVE_USER);
            return preferences.getBoolean(pinfo.packageName + IS_ACTIVE_USER, true);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[isActiveUser]", "Not Found");
            return true;
        }
    }

    /**
     * Update User Status
     *
     * @param context
     * @param isActive
     */
    public static void updateUserActiveStatus(Context context, boolean isActive) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = preferences.edit();
            Log.e("Key[updateUserActiveSt]", pinfo.packageName + IS_ACTIVE_USER);
            editor.putBoolean(pinfo.packageName + IS_ACTIVE_USER, isActive);
            editor.apply();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Key[updateUserActiveSt]", "Not Found");
            e.printStackTrace();
        }
    }

    /**
     * Sets Modules Syncing
     *
     * @param context
     * @param modulesToSync
     */
    public static void setModulesToSync(Context context, int[] modulesToSync) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(MODULES_TO_SYNC_SIZE, modulesToSync == null ? 0 :modulesToSync.length);
        int x=0;

        if(modulesToSync != null) {
            for (int i : modulesToSync) {
                Log.e(TAG, "" + Table.values()[x]);
                editor.putInt(MODULES_TO_SYNC + x, i);
                x++;
            }
        } else {
            Log.e(TAG, "Account Tools: modulesToSync is null");
        }

        editor.apply();

    }

    public static int[] getModulesSyncing(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int size = preferences.getInt(MODULES_TO_SYNC_SIZE, 0);

        List<Integer> modules = new ArrayList<>();

        if(size == 0) {
            return new int[] {0};
        } else {
            for (int i =0; i<size; i++) {
                modules.add(preferences.getInt(MODULES_TO_SYNC +i, 0));
            }


            int[] modulesToReturn = new int[modules.size()];

            for (int i = 0; i < modules.size(); i++) {
                modulesToReturn[i] = modules.get(i);
            }

            return modulesToReturn;
        }

    }
}
