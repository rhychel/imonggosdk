package net.nueca.imonggosdk.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by gama on 6/25/15.
 */
public class ReferenceNumberTool {
    private static final String CURRENT_REF_NUMBER = "_current_ref_no";

    public static String generateRefNo(@NonNull Context context, int deviceId) {
        String refnoStr = deviceId + "-";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            // get current reference number
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String current_refno = preferences.getString(pinfo.packageName + CURRENT_REF_NUMBER, deviceId + "-" + 0);

            String refno_component[] = current_refno.split("-");
            int refno_devid = Integer.parseInt(refno_component[0]);
            int refno_counter = Integer.parseInt(refno_component[1]);

            if(refno_devid != deviceId) // Started a new session with new POS device id
                refno_counter = 1;
            else
                refno_counter++;

            refnoStr += refno_counter;

            // save
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(pinfo.packageName + CURRENT_REF_NUMBER, refnoStr);
            editor.apply();

        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
        return refnoStr;
    }
}
