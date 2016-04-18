package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.tools.PriceTools;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.widgets.Numpad;

import java.util.Calendar;

/**
 * Created by rhymart on 7/16/15.
 * imonggosdk (c)2015
 */
@Deprecated
public class SimpleQuantityDialog extends BaseQuantityDialog {

    private Spinner spUnits, spBrands;
    private Button btnDeliveryDate;
    private EditText etQuantity, etBatchNo;
    private TextInputLayout tilBatchNumber;
    private Numpad npInput;
    private LinearLayout llBrand, llDeliveryDate;


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
        super.setContentView(hasBatchNo ? R.layout.simple_quantity_scroll_dialog : R.layout.simple_quantity_dialog);

        super.setTitle(selectedProductItem.getProduct().getName());

        spUnits = (Spinner) super.findViewById(R.id.spUnits);
        llBrand = (LinearLayout) super.findViewById(R.id.llBrand);
        llDeliveryDate = (LinearLayout) super.findViewById(R.id.llDeliveryDate);
        etQuantity = (EditText) super.findViewById(R.id.etQuantity);
        etBatchNo = (EditText) super.findViewById(R.id.etBatchNumber);
        npInput = (Numpad) super.findViewById(R.id.npInput);
        btnSave = (Button) super.findViewById(R.id.btnSave);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);

        etQuantity.setSelectAllOnFocus(true);

        unitsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, unitList);

        if(hasBatchNo) {
            tilBatchNumber = (TextInputLayout) super.findViewById(R.id.tilBatchNumber);
            tilBatchNumber.setVisibility(View.VISIBLE);

            // Check if there are existing values
            if(selectedProductItem.getValues().size() > 0) {
                if(isMultiValue) {
                    if(valuePosition > -1) // check if you are editing a value from the list
                        etBatchNo.setText(selectedProductItem.getValues().get(valuePosition).getExtendedAttributes().getBatch_no());
                }
                else
                    etBatchNo.setText(selectedProductItem.getValues().get(0).getExtendedAttributes().getBatch_no());
            }
        }
        if(hasBrand) {
            llBrand.setVisibility(View.VISIBLE);
            spBrands = (Spinner) super.findViewById(R.id.spBrands);
            brandsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, brandList);
            spBrands.setAdapter(brandsAdapter);

            // Check if there are existing values
            if (selectedProductItem.getValues().size() > 0) {
                if (isMultiValue) {
                    if (valuePosition > -1) // check if you are editing a value from the list
                        spBrands.setSelection(brandList.indexOf(selectedProductItem.getValues().get(valuePosition).getExtendedAttributes().getBrand()));
                } else
                    spBrands.setSelection(brandList.indexOf(selectedProductItem.getValues().get(0).getExtendedAttributes().getBrand()));
            }
        }
        if (hasDeliveryDate) {
            llDeliveryDate.setVisibility(View.VISIBLE);
            btnDeliveryDate = (Button) super.findViewById(R.id.btnDeliveryDate);
            btnDeliveryDate.setOnClickListener(onDeliveryDateClicked);

            Calendar now = Calendar.getInstance();
            date = now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH);
            date = DateTimeTools.convertToDate(date, "yyyy-M-d", "yyyy-MM-dd");
            btnDeliveryDate.setText(date);

            if (selectedProductItem.getValues().size() > 0) {
                if (isMultiValue) {
                    if (valuePosition > -1)
                        btnDeliveryDate.setText(selectedProductItem.getValues().get(valuePosition).getExtendedAttributes().getDelivery_date());
                } else
                    btnDeliveryDate.setText(selectedProductItem.getValues().get(0).getExtendedAttributes().getDelivery_date());
            }
        }
        if (hasUnits) {
            spUnits.setAdapter(unitsAdapter);
            if (selectedProductItem.getValues().size() > 0) {
                if (isMultiValue) {
                    if (valuePosition > -1)
                        spUnits.setSelection(unitList.indexOf(selectedProductItem.getValues().get(valuePosition).getUnit()));
                } else
                    spUnits.setSelection(unitList.indexOf(selectedProductItem.getValues().get(0).getUnit()));
            }
        } else {
            spUnits.setVisibility(View.GONE);
            TextInputLayout tilQuantity = (TextInputLayout) super.findViewById(R.id.tilQuantity);
            tilQuantity.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        }

        if (isMultiValue) {
            if (valuePosition > -1)
                etQuantity.setText(selectedProductItem.getQuantity(valuePosition));
        } else
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
                if (spBrands != null)
                    offsetSpinnerBelowv21(spBrands);
                if (spUnits.getVisibility() == View.VISIBLE)
                    offsetSpinnerBelowv21(spUnits);

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        View focused = getCurrentFocus();
        if(focused == null) {
            focused = etQuantity;
            focused.requestFocus();
        }
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
    }

    private View.OnClickListener onSaveClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String quantity = etQuantity.getText().toString().replace(",", "");

            if(quantity.length() == 0)
                quantity = "0";

            if (Double.valueOf(quantity) == 0 && !isMultiValue)
                selectedProductItem.removeAll(); // TODO handle this
            else if (quantity.equals("0") && isMultiValue) {
                if (multiQuantityDialogListener != null)
                    multiQuantityDialogListener.onSave(null);
                dismiss();
                return;
            } else {
                Unit unit = hasUnits ? ((Unit) spUnits.getSelectedItem()) : null;
                Values values;
                if (valuePosition > -1)
                    values = selectedProductItem.getValues().get(valuePosition);
                else
                    values = new Values();
                //Log.e("SIMPLE_QUANTITY_DIALOG", unit != null? unit.getName() : "null");

                if(getHelper() == null)
                    values.setValue(quantity, unit);
                else {
                    Price price = PriceTools.identifyPrice(getHelper(), selectedProductItem.getProduct(),
                            salesBranch, salesCustomerGroup, salesCustomer, unit);
                    if(price != null)
                        values.setValue(quantity, price, salesCustomer != null? salesCustomer.getDiscount_text() : null);
                    else {
                        Log.e(getClass().getSimpleName(), "calling PriceTools.identifyRetailPrice");
                        values.setValue(quantity, unit, PriceTools.identifyRetailPrice(getHelper(), selectedProductItem.getProduct(),
                                salesBranch, salesCustomerGroup, salesCustomer, unit),
                                salesCustomer != null ? salesCustomer.getDiscount_text() : null);
                    }
                }

                    selectedProductItem.getValues().set(valuePosition, values);
                if (hasBrand || hasDeliveryDate) {
                    ExtendedAttributes extendedAttributes = new ExtendedAttributes();
                    if (hasBrand)
                        extendedAttributes.setBrand(((String) spBrands.getSelectedItem()));
                    if (hasDeliveryDate)
                        extendedAttributes.setDelivery_date(btnDeliveryDate.getText().toString());
                    if (hasBatchNo)
                        extendedAttributes.setBatch_no(etBatchNo.getText().toString().trim());
                    values.setExtendedAttributes(extendedAttributes);
                }
                if (isMultiValue) {
                    if (multiQuantityDialogListener != null)
                        multiQuantityDialogListener.onSave(values);
                    dismiss();
                    return;
                } else
                    selectedProductItem.addValues(values);
            }

            if (quantityDialogListener != null)
                quantityDialogListener.onSave(selectedProductItem, listPosition);
            dismiss();
        }
    };

    private View.OnClickListener onDeliveryDateClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showDeliveryDatePicker(fragmentManager, btnDeliveryDate);
        }
    };
}
