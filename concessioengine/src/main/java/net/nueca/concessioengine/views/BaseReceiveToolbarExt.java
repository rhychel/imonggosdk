package net.nueca.concessioengine.views;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.concessioengine.R;

/**
 * Created by gama on 9/1/15.
 */
public abstract class BaseReceiveToolbarExt implements View.OnClickListener {
    protected int ReceiveToolbarResource;
    protected View toolbarExtension;

    public BaseReceiveToolbarExt(int receiveLayoutRes) {
        ReceiveToolbarResource = receiveLayoutRes;
    }

    public void attachAfter(Context context, Toolbar toolbar) {
        attachAfter(context, toolbar, true);
    }

    public void attachAfter(Context context, Toolbar toolbar, boolean adaptToolbarBg) {
        ViewGroup parent = (ViewGroup) toolbar.getParent();
        int index = parent.indexOfChild(toolbar);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        toolbarExtension = inflater.inflate(ReceiveToolbarResource, parent, false);
        toolbarExtension.setOnClickListener(this);

        if(adaptToolbarBg)
            toolbarExtension.setBackground(toolbar.getBackground());

        parent.addView(toolbarExtension, index + 1);
    }

    public void attachBefore(Context context, Toolbar toolbar) {
        attachBefore(context, toolbar, true);
    }

    public void attachBefore(Context context, Toolbar toolbar, boolean adaptToolbarBg) {
        ViewGroup parent = (ViewGroup) toolbar.getParent();
        int index = parent.indexOfChild(toolbar);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        toolbarExtension = inflater.inflate(ReceiveToolbarResource, parent, false);
        toolbarExtension.setOnClickListener(this);

        if(adaptToolbarBg)
            toolbarExtension.setBackground(toolbar.getBackground());

        if(index > 0)
            parent.addView(toolbarExtension, index - 1);
        else
            parent.addView(toolbarExtension, 0);
    }

    public View getToolbarExtensionView() {
        return toolbarExtension;
    }
}
