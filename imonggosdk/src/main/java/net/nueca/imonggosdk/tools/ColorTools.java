package net.nueca.imonggosdk.tools;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

import net.nueca.imonggosdk.R;

/**
 * Created by gama on 8/28/15.
 */
public class ColorTools {

    public static int fetchAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    public static int fetchPrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimary });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    public static int fetchPrimaryColorDark(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimaryDark });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
}
