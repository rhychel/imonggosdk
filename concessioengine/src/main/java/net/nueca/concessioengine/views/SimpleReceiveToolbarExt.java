package net.nueca.concessioengine.views;

import android.view.View;
import android.widget.ImageButton;

import net.nueca.concessioengine.R;

/**
 * Created by gama on 9/1/15.
 */
public class SimpleReceiveToolbarExt extends BaseToolbarExt {

    protected OnToolbarClickedListener onClickListener;
    private ImageButton ibtnEdit;

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

    @Override
    protected void whenInflated(View view) {
        ibtnEdit = (ImageButton)view.findViewById(R.id.ibtnEdit);
        if(ibtnEdit != null)
            ibtnEdit.setOnClickListener(this);
    }

    public OnToolbarClickedListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnToolbarClickedListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
