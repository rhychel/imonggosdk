package net.nueca.concessio_test;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;

/**
 * Created by rhymart on 6/26/15.
 * imonggosdk (c)2015
 */
public class SampleAppCompatDialog extends AppCompatDialog {
    public SampleAppCompatDialog(Context context) {
        super(context);
    }

    public SampleAppCompatDialog(Context context, int theme) {
        super(context, theme);
    }

    protected SampleAppCompatDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.c_login);
    }
}
