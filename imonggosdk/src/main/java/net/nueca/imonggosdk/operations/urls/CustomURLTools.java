package net.nueca.imonggosdk.operations.urls;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

/**
 * Created by rhymart on 7/11/14.
 * NuecaLibrary (c)2014
 */
public class CustomURLTools {

    private static final String URL_LIST = "url_list";
    private static final String IS_URL = "_IS_URL";
    private static final String CUSTOM_URL = "_CUSTOM_URL";
    private static final String IS_SECURED = "_IS_SECURED";

    private static SharedPreferences customURLSP;

    public static void init(Context context) {
        customURLSP = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences getCustomURLSP(Context context) {
        if(customURLSP == null)
            init(context);
        return customURLSP;
    }

    public static void updateUrlList(Context context, String urlList) {
        SharedPreferences.Editor editor = getCustomURLSP(context).edit();
        editor.putString(URL_LIST, urlList);
        editor.commit();
    }

    /**
     * This is in JSONObject.
     * @param context
     * @return
     */
    public static String urlList(Context context) {
        return getCustomURLSP(context).getString(URL_LIST, "");
    }

    public static void updateIsCustomURL(Context context, boolean isCustom) {
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = getCustomURLSP(context).edit();
            editor.putBoolean(pinfo.packageName + IS_URL, isCustom);
            editor.commit();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean isCustomURL(Context context) {
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return getCustomURLSP(context).getBoolean(pinfo.packageName + IS_URL, false);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     *
     * domain only (e.g. http://northstart.iretailcloud.com/
     *
     * @param context
     * @param custom_url
     */
    public static void updateCustomURL(Context context, String custom_url) {
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            SharedPreferences.Editor editor = getCustomURLSP(context).edit();
            editor.putString(pinfo.packageName + CUSTOM_URL, custom_url);
            editor.commit();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getCustomURL(Context context) {
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return getCustomURLSP(context).getString(pinfo.packageName + CUSTOM_URL, "");
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

}
