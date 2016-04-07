package net.nueca.concessioengine.dialogs;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.tools.DateTimeTools;

import java.util.List;

/**
 * Created by rhymart on 7/16/15.
 * imonggosdk (c)2015
 */
public class BaseQuantityDialog extends BaseAppCompatDialog {

    public interface QuantityDialogListener {
        void onSave(SelectedProductItem selectedProductItem, int position);
        void onDismiss();
    }

    public interface MultiQuantityDialogListener {
        void onSave(Values values);
        void onDismiss();
    }

    protected SelectedProductItem selectedProductItem;
    protected Button btnSave, btnCancel;
    protected List<InvoicePurpose> invoicePurposeList;
    protected List<Unit> unitList;
    protected List<String> brandList;
    protected QuantityDialogListener quantityDialogListener;
    protected MultiQuantityDialogListener multiQuantityDialogListener;
//    protected FragmentManager fragmentManager;
//    protected String deliveryDate;

    protected boolean hasSubtotal = false, hasInvoicePurpose = false,
            hasExpiryDate = false, hasBadStock = false, hasStock = true, hasPrice = true,
            hasExpectedQty = false, hasOutright =false, hasDiscrepancy = false;

    protected ArrayAdapter<InvoicePurpose> invoicePurposesAdapter;
    protected ArrayAdapter<Unit> unitsAdapter;
    protected ArrayAdapter<String> brandsAdapter;

    private ImonggoDBHelper2 dbHelper2;
    protected Customer salesCustomer;
    protected CustomerGroup salesCustomerGroup;
    protected Branch salesBranch;

    protected boolean isUnitDisplay = false, hasUnits = false, hasBrand = false, hasDeliveryDate = false,
            hasBatchNo = false, isMultiValue = false;
    protected int valuePosition = -1;
    protected int listPosition = -1;

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
        if (addBaseUnit)
            addBaseUnit(selectedProductItem.getProduct().getBase_unit_name());
    }

    public void setBrandList(List<String> brandList) {
        setBrandList(brandList, false);
    }

    public void setBrandList(List<String> brandList, boolean addNoBrand) {
        this.brandList = brandList;
        if (addNoBrand)
            this.brandList.add(0, "No Brand");
    }

    public void setSelectedProductItem(SelectedProductItem selectedProductItem) {
        this.selectedProductItem = selectedProductItem;
    }

    public void setQuantityDialogListener(QuantityDialogListener quantityDialogListener) {
        this.quantityDialogListener = quantityDialogListener;
    }

    public void setMultiQuantityDialogListener(MultiQuantityDialogListener multiQuantityDialogListener) {
        this.multiQuantityDialogListener = multiQuantityDialogListener;
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

    public void setHasBatchNo(boolean hasBatchNo) {
        this.hasBatchNo = hasBatchNo;
    }

    public void setIsMultiValue(boolean isMultiValue) {
        this.isMultiValue = isMultiValue;
    }

    public void setValuePosition(int valuePosition) {
        this.valuePosition = valuePosition;
    }

    public void setHasSubtotal(boolean hasSubtotal) {
        this.hasSubtotal = hasSubtotal;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (multiQuantityDialogListener != null)
            multiQuantityDialogListener.onDismiss();
        if(quantityDialogListener != null)
            quantityDialogListener.onDismiss();
    }

    /**
     * Spinner Dropdown Configuration
     */
    protected void offsetSpinnerBelowv21(Spinner spinner) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (Build.VERSION.SDK_INT < 21)
            spinner.setDropDownVerticalOffset((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, -(spinner.getHeight() + 14), metrics));
    }
//
//    /**
//     * Show the delivery date picker. You need to pass the button where to set the new date.
//     *
//     * @param button
//     */
//    protected void showDeliveryDatePicker(final Button button) {
//        deliveryDate = button.getText().toString();
//        deliveryDate = deliveryDate.replaceAll("[^0-9]","-");
//        String[] date = deliveryDate.split("-");
//        final DatePickerDialog deliveryDatePicker = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
//                String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
//                deliveryDate = DateTimeTools.convertToDate(date, "yyyy-M-d", "yyyy-MM-dd");
//                button.setText(deliveryDate);
//            }
//        }, Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
//        deliveryDatePicker.show(fragmentManager, "delivery_date_picker");
//    }


    public ImonggoDBHelper2 getHelper() {
        return dbHelper2;
    }

    public void setHelper(ImonggoDBHelper2 dbHelper2) {
        this.dbHelper2 = dbHelper2;
    }

    public Customer getSalesCustomer() {
        return salesCustomer;
    }

    public void setSalesCustomer(Customer salesCustomer) {
        this.salesCustomer = salesCustomer;
    }

    public CustomerGroup getSalesCustomerGroup() {
        return salesCustomerGroup;
    }

    public void setSalesCustomerGroup(CustomerGroup salesCustomerGroup) {
        this.salesCustomerGroup = salesCustomerGroup;
    }

    public Branch getSalesBranch() {
        return salesBranch;
    }

    public void setSalesBranch(Branch salesBranch) {
        this.salesBranch = salesBranch;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public void setInvoicePurposeList(List<InvoicePurpose> invoicePurposeList) {
        this.invoicePurposeList = invoicePurposeList;
    }

    public void setHasInvoicePurpose(boolean hasInvoicePurpose) {
        this.hasInvoicePurpose = hasInvoicePurpose;
    }

    public void setHasExpiryDate(boolean hasExpiryDate) {
        this.hasExpiryDate = hasExpiryDate;
    }

    public void setHasBadStock(boolean hasBadStock) {
        this.hasBadStock = hasBadStock;
    }

    public void setHasDiscrepancy(boolean hasDiscrepancy) {
        this.hasDiscrepancy = hasDiscrepancy;
    }

    public void setHasStock(boolean hasStock) {
        this.hasStock = hasStock;
    }

    public void setHasExpectedQty(boolean hasExpectedQty) {
        this.hasExpectedQty = hasExpectedQty;
    }

    public void setHasOutright(boolean hasOutright) {
        this.hasOutright = hasOutright;
    }

    public void setHasPrice(boolean hasPrice) {
        this.hasPrice = hasPrice;
    }

    public void setUnitDisplay(boolean unitDisplay) {
        isUnitDisplay = unitDisplay;
    }
}
