package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.widgets.Keypad;
import net.nueca.imonggosdk.widgets.Numpad;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by rhymart on 7/16/15.
 * imonggosdk (c)2015
 */
public class SimpleQuantityDialog extends BaseQuantityDialog {

    private Spinner spUnits, spBrands;
    private Button btnDeliveryDate;
    private EditText etQuantity;
    private Numpad npInput;
    private LinearLayout llBrand, llDeliveryDate;

    private ArrayAdapter<Unit> unitsAdapter;
    private ArrayAdapter<String> brandsAdapter;

    protected SimpleQuantityDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public SimpleQuantityDialog(Context context, int theme) {
        super(context, theme);
    }

    public SimpleQuantityDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_quantity_dialog);

        super.setTitle(selectedProductItem.getProduct().getName());

        spUnits = (Spinner) super.findViewById(R.id.spUnits);
        llBrand = (LinearLayout) super.findViewById(R.id.llBrand);
        llDeliveryDate = (LinearLayout) super.findViewById(R.id.llDeliveryDate);
        etQuantity = (EditText) super.findViewById(R.id.etQuantity);
        npInput = (Numpad) super.findViewById(R.id.npInput);
        btnSave = (Button) super.findViewById(R.id.btnSave);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);

        unitsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, unitList);

        if(hasBrand) {
            llBrand.setVisibility(View.VISIBLE);
            spBrands = (Spinner) super.findViewById(R.id.spBrands);
            brandsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, brandList);
            spBrands.setAdapter(brandsAdapter);

            if(selectedProductItem.getValues().size() > 0)
                spBrands.setSelection(brandList.indexOf(selectedProductItem.getValues().get(0).getExtendedAttributes().getBrand()));
        }
        if(hasDeliveryDate) {
            llDeliveryDate.setVisibility(View.VISIBLE);
            btnDeliveryDate = (Button) super.findViewById(R.id.btnDeliveryDate);
            btnDeliveryDate.setOnClickListener(onDeliveryDateClicked);

            Calendar now = Calendar.getInstance();
            deliveryDate = now.get(Calendar.YEAR)+"-"+(now.get(Calendar.MONTH)+1)+"-"+now.get(Calendar.DAY_OF_MONTH);
            deliveryDate = DateTimeTools.convertToDate(deliveryDate, "yyyy-M-d", "yyyy-MM-dd");
            btnDeliveryDate.setText(deliveryDate);

            if(selectedProductItem.getValues().size() > 0)
                btnDeliveryDate.setText(selectedProductItem.getValues().get(0).getExtendedAttributes().getDelivery_date());
        }
        if(hasUnits) {
            spUnits.setAdapter(unitsAdapter);
            if(selectedProductItem.getValues().size() > 0)
                spUnits.setSelection(unitList.indexOf(selectedProductItem.getValues().get(0).getUnit()));
        }
        else {
            spUnits.setVisibility(View.GONE);
            TextInputLayout tilQuantity = (TextInputLayout) super.findViewById(R.id.tilQuantity);
            tilQuantity.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        }

        etQuantity.setText(selectedProductItem.getQuantity());
        npInput.addTextHolder(etQuantity, "etQuantity", false, 6, 2, false, null);
        npInput.getTextHolderWithTag("etQuantity").setEnableDot(selectedProductItem.getProduct().isAllow_decimal_quantities());

        btnSave.setOnClickListener(onSaveClicked);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if(spBrands != null)
                    offsetSpinnerBelowv21(spBrands);
                if(spUnits.getVisibility() == View.VISIBLE)
                    offsetSpinnerBelowv21(spUnits);
            }
        });
    }

    private View.OnClickListener onSaveClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String quantity = etQuantity.getText().toString().replace(",","");
            if(quantity.equals("0") && !selectedProductItem.isMultiline())
                selectedProductItem.removeAll();
            else {
                Unit unit = hasUnits ? ((Unit) spUnits.getSelectedItem()) : null;
                Values values = new Values(unit, quantity);
                if(hasBrand || hasDeliveryDate) {
                    ExtendedAttributes extendedAttributes = new ExtendedAttributes();
                    if (hasBrand)
                        extendedAttributes.setBrand(((String) spBrands.getSelectedItem()));
                    if (hasDeliveryDate)
                        extendedAttributes.setDelivery_date(btnDeliveryDate.getText().toString());
                    values.setExtendedAttributes(extendedAttributes);
                }
                selectedProductItem.addValues(values);
            }

            if(quantityDialogListener != null)
                quantityDialogListener.onSave(selectedProductItem);
            dismiss();
        }
    };

    private View.OnClickListener onDeliveryDateClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showDeliveryDatePicker(btnDeliveryDate);
        }
    };

}
