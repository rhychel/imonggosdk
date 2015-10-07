package net.nueca.concessioengine.views;

import android.view.View;

import net.nueca.concessioengine.R;

/**
 * Created by gama on 10/6/15.
 */
public class SimplePulloutToolbarExt extends BaseToolbarExt {

    protected OnToolbarClickedListener onClickListener;

    public interface OnToolbarClickedListener {
        void onClick();
    }

    public SimplePulloutToolbarExt() {
        super(R.layout.pullout_toolbar_ext);
    }

    public SimplePulloutToolbarExt(int customResource) {
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
