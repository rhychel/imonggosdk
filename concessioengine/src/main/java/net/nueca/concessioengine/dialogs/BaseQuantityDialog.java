package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;

import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.objects.Unit;

import java.util.List;

/**
 * Created by rhymart on 7/16/15.
 * imonggosdk (c)2015
 */
public class BaseQuantityDialog extends AppCompatDialog {

    protected SelectedProductItem selectedProductItem;
    protected Button btnSave, btnCancel;
    protected List<Unit> unitList;

    public BaseQuantityDialog(Context context) {
        super(context);
    }

    public BaseQuantityDialog(Context context, int theme) {
        super(context, theme);
    }

    protected BaseQuantityDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void show() {
        super.show();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int pxPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, metrics); // Margin
        int pxP = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, metrics); // Ideal Width

        getWindow().setLayout(pxP + pxPadding, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setUnitList(List<Unit> unitList) {
        this.unitList = unitList;
    }

    public void setSelectedProductItem(SelectedProductItem selectedProductItem) {
        this.selectedProductItem = selectedProductItem;
    }
}
