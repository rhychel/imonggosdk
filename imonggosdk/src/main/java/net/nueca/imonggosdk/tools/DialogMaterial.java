package net.nueca.imonggosdk.tools;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by Jn on 6/10/2015.
 * imonggosdk (c)2015
 *
 * @uses material-dialogs Library
 * by afollestad of github.com
 */
public class DialogMaterial {

    /**
     * Default Constructor
     */
    public DialogMaterial(){

    }

    public static void showBasicNoTitle(Context context, String content, String positiveText, String negativeText, Boolean cancelable, MaterialDialog.ButtonCallback callback) {
        new MaterialDialog.Builder(context)
                .content(content)
                .positiveText(positiveText)
                .negativeText(negativeText)
                .callback(callback)
                .cancelable(cancelable)
                .show();
    }


    public static void showBasicWithTitle(Context context, String title, String content, String positiveText, String negativeText, Boolean cancelable,  MaterialDialog.ButtonCallback callback) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText(positiveText)
                .negativeText(negativeText)
                .callback(callback)
                .cancelable(cancelable)
                .show();
    }



}
