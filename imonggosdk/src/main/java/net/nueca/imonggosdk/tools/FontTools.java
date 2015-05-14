package net.nueca.imonggosdk.tools;

import android.app.Activity;
import android.graphics.Typeface;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public class FontTools {
    private static Typeface font;

    public static Typeface setFont(Activity activity, String tffName) {
        font = Typeface.createFromAsset(activity.getAssets(), "fonts/"+tffName+".ttf");
        return font;
    }

    public static Typeface getFont() {
        return font;
    }
}
