package net.nueca.concessioengine.views;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;

/**
 * Created by gama on 10/6/15.
 */
@Deprecated
public class SimplePulloutToolbarExt extends BaseToolbarExt {

    protected OnToolbarClickedListener onClickListener;
    private ImageButton ibtnEdit;
    private TextView tvReason;

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
    protected void whenInflated(View view) {
        ibtnEdit = (ImageButton)view.findViewById(R.id.ibtnEdit);
        tvReason = (TextView)view.findViewById(R.id.tvReason);

        ibtnEdit.setOnClickListener(this);
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

    public void renderReason() {
        tvReason.setText(ProductsAdapterHelper.getReason().getName());
    }
}
