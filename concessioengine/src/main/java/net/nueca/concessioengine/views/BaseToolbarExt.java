package net.nueca.concessioengine.views;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by gama on 9/1/15.
 */
public abstract class BaseToolbarExt implements View.OnClickListener {
    protected int ToolbarResource;
    protected View toolbarExtension;
    protected boolean isAttached = false;

    public BaseToolbarExt(int receiveLayoutRes) {
        ToolbarResource = receiveLayoutRes;
    }

    public void attachAfter(Context context, Toolbar toolbar) {
        attachAfter(context, toolbar, true);
    }

    public void attachAfter(Context context, Toolbar toolbar, boolean adaptToolbarBg) {
        attachAfter(context, (View)toolbar, adaptToolbarBg);
    }

    public void attachAfter(Context context, ActionBar actionBar) {
        attachAfter(context, actionBar, true);
    }

    public void attachAfter(Context context, ActionBar actionBar, boolean adaptToolbarBg) {
        attachAfter(context, actionBar.getCustomView(), adaptToolbarBg);
    }

    public void attachAfter(Context context, View toolbar) {
        attachAfter(context, toolbar, true);
    }

    public void attachAfter(Context context, View toolbar, boolean adaptToolbarBg) {
        if(isAttached) {
            Log.e(getClass().getSimpleName(), "attachAfter : isAttached? true");
            return;
        }
        ViewGroup parent = (ViewGroup) toolbar.getParent();
        int index = parent.indexOfChild(toolbar);

        if(toolbarExtension == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            toolbarExtension = inflater.inflate(ToolbarResource, parent, false);
            //toolbarExtension.setOnClickListener(this);
            whenInflated(toolbarExtension);
        }

        if(adaptToolbarBg)
            toolbarExtension.setBackground(toolbar.getBackground());

        parent.addView(toolbarExtension, index + 1);
        isAttached = true;
    }

    public void attachBefore(Context context, Toolbar toolbar) {
        attachBefore(context, toolbar, true);
    }

    public void attachBefore(Context context, Toolbar toolbar, boolean adaptToolbarBg) {
        attachBefore(context, (View) toolbar, adaptToolbarBg);
    }

    public void attachBefore(Context context, ActionBar actionBar) {
        attachBefore(context, actionBar, true);
    }

    public void attachBefore(Context context, ActionBar actionBar, boolean adaptToolbarBg) {
        attachBefore(context, actionBar.getCustomView(), adaptToolbarBg);
    }

    public void attachBefore(Context context, View toolbar) {
        attachBefore(context, toolbar, true);
    }

    public void attachBefore(Context context, View toolbar, boolean adaptToolbarBg) {
        if(isAttached) {
            Log.e(getClass().getSimpleName(), "attachBefore : isAttached? true");
            return;
        }
        ViewGroup parent = (ViewGroup) toolbar.getParent();
        int index = parent.indexOfChild(toolbar);

        if(toolbarExtension == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            toolbarExtension = inflater.inflate(ToolbarResource, parent, false);
            //toolbarExtension.setOnClickListener(this);
            whenInflated(toolbarExtension);
        }

        if(adaptToolbarBg)
            toolbarExtension.setBackground(toolbar.getBackground());

        if(index > 0)
            parent.addView(toolbarExtension, index - 1);
        else
            parent.addView(toolbarExtension, 0);

        isAttached = true;
    }

    public void detach() {
        if(toolbarExtension != null) {
            ViewGroup parent = (ViewGroup) toolbarExtension.getParent();
            if(parent != null)
                parent.removeView(toolbarExtension);
            isAttached = false;
        }
    }

    public boolean isAttached() {
        return isAttached;
    }

    protected abstract void whenInflated(View view);

    public View getToolbarExtensionView() {
        return toolbarExtension;
    }
}
