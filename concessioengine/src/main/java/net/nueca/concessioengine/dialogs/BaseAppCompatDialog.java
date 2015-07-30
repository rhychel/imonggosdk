package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;

/**
 * Created by rhymart on 7/24/15.
 * imonggosdk (c)2015
 */
public class BaseAppCompatDialog extends AppCompatDialog {
    public BaseAppCompatDialog(Context context) {
        super(context);
    }

    public BaseAppCompatDialog(Context context, int theme) {
        super(context, theme);
    }

    protected BaseAppCompatDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    /**
     * Configure the width of the dialog to acquire the desired width.
     */
    @Override
    public void show() {
        super.show();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int pxPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, metrics); // Margin
        int pxP = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, metrics); // Ideal Width

        getWindow().setLayout(pxP + pxPadding, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

}
