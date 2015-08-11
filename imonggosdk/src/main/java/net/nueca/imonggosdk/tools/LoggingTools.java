package net.nueca.imonggosdk.tools;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Jn on 6/16/2015.
 * imonggosdk (c)2015
 */
public class LoggingTools {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
