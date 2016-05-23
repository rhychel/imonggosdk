package net.nueca.dizonwarehouse.views;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.nueca.concessioengine.views.BaseToolbarExt;
import net.nueca.dizonwarehouse.R;

/**
 * Created by gama on 18/05/2016.
 */
public class ToolbarExtension extends BaseToolbarExt {
    protected OnToolbarClickedListener onClickListener;
    private ImageButton ibtnEdit;
    private TextView tvLabel, tvValue;
    private String label, value;

    public interface OnToolbarClickedListener {
        void onClick();
    }

    public ToolbarExtension() {
        super(R.layout.wh_toolbar_ext);
    }

    public ToolbarExtension(int receiveLayoutRes) {
        super(receiveLayoutRes);
    }

    @Override
    public void onClick(View v) {
        if(onClickListener != null)
            onClickListener.onClick();
    }

    @Override
    protected void whenInflated(View view) {
        ibtnEdit = (ImageButton)view.findViewById(net.nueca.concessioengine.R.id.ibtnEdit);
        if(ibtnEdit != null)
            ibtnEdit.setOnClickListener(this);

        tvLabel = (TextView)view.findViewById(R.id.tvLabel);
        tvLabel.setText(label);
        tvValue = (TextView)view.findViewById(R.id.tvValue);
        tvValue.setText(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        if(tvValue != null)
            tvValue.setText(this.value);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
        if(tvLabel != null)
            tvLabel.setText(this.label);
    }

    public OnToolbarClickedListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnToolbarClickedListener onClickListener) {
        this.onClickListener = onClickListener;
    }

}
