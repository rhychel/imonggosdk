package net.nueca.imonggosdk.tools;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Jn on 6/16/2015.
 * imonggosdk (c)2015
 */
public class LoggingTools {

    /**
     * show Toast message with short length
     * @param context
     * @param message
     */
    public static void showToast(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showToastLong(Context context, String message) {
        showToast(context, message, Toast.LENGTH_LONG);
    }

    public static void showToastShort(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String message, int length) {
        Toast.makeText(context, message, length).show();
    }
}
