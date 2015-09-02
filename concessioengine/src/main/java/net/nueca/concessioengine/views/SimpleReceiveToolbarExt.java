package net.nueca.concessioengine.views;

import android.view.View;

import net.nueca.concessioengine.R;

/**
 * Created by gama on 9/1/15.
 */
public class SimpleReceiveToolbarExt extends BaseReceiveToolbarExt {

    protected OnToolbarClickedListener onClickListener;

    public interface OnToolbarClickedListener {
        void onClick();
    }

    public SimpleReceiveToolbarExt() {
        super(R.layout.receive_toolbar_ext);
    }

    public SimpleReceiveToolbarExt(int customResource) {
        super(customResource);
    }

    @Override
    public void onClick(View v) {
        if(onClickListener != null)
            onClickListener.onClick();
    }

    public OnToolbarClickedListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnToolbarClickedListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
