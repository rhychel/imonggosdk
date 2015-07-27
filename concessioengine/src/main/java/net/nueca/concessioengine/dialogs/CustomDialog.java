package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;

/**
 * Created by Jn on 7 /7/2015.
 * imonggosdk (c)2015
 */
public class CustomDialog extends AppCompatDialog {

    public CustomDialog(Context context) {
        super(context);
    }
    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
