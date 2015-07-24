package net.nueca.concessioengine.dialogs;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.tools.DateTimeTools;

import java.util.List;

/**
 * Created by rhymart on 7/16/15.
 * imonggosdk (c)2015
 */
public class BaseQuantityDialog extends AppCompatDialog {

    public interface QuantityDialogListener {
        void onSave(SelectedProductItem selectedProductItem);
        void onDismiss();
    }

    protected SelectedProductItem selectedProductItem;
    protected Button btnSave, btnCancel;
    protected List<Unit> unitList;
    protected List<String> brandList;
    protected QuantityDialogListener quantityDialogListener;
    protected FragmentManager fragmentManager;
    protected String deliveryDate;

    protected boolean hasUnits = true, hasBrand = false, hasDeliveryDate = false;

    public BaseQuantityDialog(Context context) {
        super(context);
    }

    public BaseQuantityDialog(Context context, int theme) {
        super(context, theme);
    }

    protected BaseQuantityDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void addBaseUnit(String baseUnit) {
        Unit unit = new Unit();
        unit.setId(-1);
        unit.setName(baseUnit);
        unitList.add(0, unit);
    }

    public void setUnitList(List<Unit> unitList) {
        setUnitList(unitList, false);
    }

    public void setUnitList(List<Unit> unitList, boolean addBaseUnit) {
        this.unitList = unitList;
        if(addBaseUnit)
            addBaseUnit(selectedProductItem.getProduct().getBase_unit_name());
    }

    public void setBrandList(List<String> brandList) {
        this.brandList = brandList;
    }

    public void setSelectedProductItem(SelectedProductItem selectedProductItem) {
        this.selectedProductItem = selectedProductItem;
    }

    public void setQuantityDialogListener(QuantityDialogListener quantityDialogListener) {
        this.quantityDialogListener = quantityDialogListener;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void setHasUnits(boolean hasUnits) {
        this.hasUnits = hasUnits;
    }

    public void setHasBrand(boolean hasBrand) {
        this.hasBrand = hasBrand;
    }

    public void setHasDeliveryDate(boolean hasDeliveryDate) {
        this.hasDeliveryDate = hasDeliveryDate;
    }

    /**
     * Spinner Dropdown Configuration
     */
    protected void offsetSpinnerBelowv21(Spinner spinner) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if(Build.VERSION.SDK_INT < 21)
            spinner.setDropDownVerticalOffset((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, -(spinner.getHeight() + 14), metrics));
    }

    /**
     * Configure the width of the dialog to acquire the desired width.
     */
    @Override
    public void show() {
        super.show();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int pxPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, metrics); // Margin
        int pxP = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, metrics); // Ideal Width

        getWindow().setLayout(pxP + pxPadding, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Show the delivery date picker. You need to pass the button where to set the new date.
     * @param button
     */
    protected void showDeliveryDatePicker(final Button button) {
        String []date = deliveryDate.split("-");
        final DatePickerDialog deliveryDatePicker = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
                String date = year+"-"+(monthOfYear+1)+"-"+dayOfMonth;
                deliveryDate = DateTimeTools.convertToDate(date, "yyyy-M-d", "yyyy-MM-dd");
                button.setText(deliveryDate);
            }
        }, Integer.valueOf(date[0]), Integer.valueOf(date[1])-1, Integer.valueOf(date[2]));
        deliveryDatePicker.show(fragmentManager, "delivery_date_picker");
    }
}
