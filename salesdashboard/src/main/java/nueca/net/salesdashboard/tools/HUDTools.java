package nueca.net.salesdashboard.tools;

import android.content.Context;
import android.util.Log;

import nueca.net.salesdashboard.R;
import nueca.net.salesdashboard.dialog.ProgressHUD;


/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class HUDTools {

    static public ProgressHUD mProgressHUD;
    static public String TAG = "HUDTools";

    public static void showIndeterminateProgressHUD(Context context, String message, Boolean cancelable) {
        Log.e(TAG, "showing progress hud");
        mProgressHUD = new ProgressHUD(context, R.style.ProgressHUD);
        mProgressHUD.show(context, message, false, null);
    }

    /**
     * Hides the Indeterminate Progress Dialog and sets it to null
     */
    public static void hideIndeterminateProgressDialog() {
        Log.e(TAG, "dismissing progress hud");
        if(mProgressHUD != null) {
            mProgressHUD.hide();
        }
    }

}



