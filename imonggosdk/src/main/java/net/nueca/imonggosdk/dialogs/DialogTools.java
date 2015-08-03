package net.nueca.imonggosdk.dialogs;

import android.app.ProgressDialog;
import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by Jn on 6/10/2015.
 * imonggosdk (c)2015
 */
public class DialogTools {

    static ProgressDialog mProgressDialog;


    /**
     * Material Alert Dialog with Title
     * TODO: don't use afollestad's library
     *
     * @param context      The Context
     * @param title        AlertDialog's Title
     * @param content      AlertDialog's Message
     * @param positiveText Positive button message
     * @param negativeText Negative button message
     * @param cancelable   True if you want to cancel dialog by tapping the side of the screen
     * @param callback     Listener for AlertDialog's button
     */
    public static void showBasicWithTitle(Context context, String title, String content,
                                          String positiveText, String negativeText, Boolean cancelable, MaterialDialog.ButtonCallback callback) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText(positiveText)
                .negativeText(negativeText)
                .callback(callback)
                .cancelable(cancelable)
                .show();
    }


    /**
     * Simple Indeterminate Progress Dialog
     *
     * @param context    A Context
     * @param title      Title of Progress Dialog | Null if you don't want
     * @param content    Message of the Progress Dialog
     * @param cancelable True if you want to cancel dialog by tapping the side of the screen
     */
    public static void showIndeterminateProgressDialog(Context context, String title,
                                                       String content, Boolean cancelable) {
        mProgressDialog = new ProgressDialog(context);

        mProgressDialog.setIndeterminate(true);

        if (title != null) {
            mProgressDialog.setTitle(title);
        }

        mProgressDialog.setCancelable(cancelable);
        mProgressDialog.setMessage(content);

        mProgressDialog.show();
    }

    /**
     * Hides the Indeterminate Progress Dialog and sets it to null
     */
    public static void hideIndeterminateProgressDialog() {
        if(mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public static void updateMessage(String message) {
        if(mProgressDialog != null)
            mProgressDialog.setMessage(message);
    }
}
