package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.tools.PriceTools;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.Calendar;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymart on 11/26/15.
 */
public class SimpleSalesQuantityDialog extends BaseQuantityDialog {

    private AutofitTextView tvProductName;
    private TextView tvRetailPrice, tvInStock, tvSubtotal;
    private EditText etQuantity;
    private Spinner spUnits;
    private Button btnCancel, btnSave;
    private LinearLayout llSubtotal;

    private LinearLayout llInvoicePurpose, llExpiryDate;
    private Spinner spInvoicePurpose;
    private Button btnExpiryDate;

    private String subtotal, retailPrice;

    protected SimpleSalesQuantityDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public SimpleSalesQuantityDialog(Context context, int theme) {
        super(context, theme);
    }

    public SimpleSalesQuantityDialog(Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_quantity_dialog2);

        tvProductName = (AutofitTextView) super.findViewById(R.id.tvProductName);
        tvRetailPrice = (TextView) super.findViewById(R.id.tvRetailPrice);
        tvInStock = (TextView) super.findViewById(R.id.tvInStock);
        tvSubtotal = (TextView) super.findViewById(R.id.tvSubtotal);
        etQuantity = (EditText) super.findViewById(R.id.etQuantity);
        spUnits = (Spinner) super.findViewById(R.id.spUnits);
        spInvoicePurpose = (Spinner) super.findViewById(R.id.spInvoicePurpose);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnSave = (Button) super.findViewById(R.id.btnSave);
        llSubtotal = (LinearLayout) super.findViewById(R.id.llSubtotal);
        llInvoicePurpose = (LinearLayout) super.findViewById(R.id.llInvoicePurpose);
        llExpiryDate = (LinearLayout) super.findViewById(R.id.llExpiryDate);
        btnExpiryDate = (Button) super.findViewById(R.id.btnExpiryDate);

        etQuantity.setSelectAllOnFocus(true);

        boolean hasValues = selectedProductItem.getValues().size() > 0;

        if(hasInvoicePurpose) {
            llInvoicePurpose.setVisibility(View.VISIBLE);
            invoicePurposesAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, invoicePurposeList);
            invoicePurposesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
            spInvoicePurpose.setAdapter(invoicePurposesAdapter);
            if(hasExpiryDate) { // this is for the expiry date
                spInvoicePurpose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(invoicePurposeList.get(position).getExtras().require_date()) {
                            llExpiryDate.setVisibility(View.VISIBLE);
                        }
                        else
                            llExpiryDate.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                btnExpiryDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeliveryDatePicker(btnExpiryDate);
                    }
                });
            }

        }

        unitsAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, unitList);
        unitsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        spUnits.setAdapter(unitsAdapter);
        if (hasValues) {
            if (isMultiValue) {
                if (valuePosition > -1) {
                    Values value = selectedProductItem.getValues().get(valuePosition);
                    spUnits.setSelection(unitList.indexOf(value.getUnit()));
                    if(hasInvoicePurpose) {
                        deliveryDate = value.getExpiry_date();
                        spInvoicePurpose.setSelection(invoicePurposeList.indexOf(value.getInvoicePurpose()));
                    }
                }
            } else {
                Values value = selectedProductItem.getValues().get(0);
                spUnits.setSelection(unitList.indexOf(value.getUnit()));
                if(hasInvoicePurpose) {
                    deliveryDate = value.getExpiry_date();
                    spInvoicePurpose.setSelection(invoicePurposeList.indexOf(value.getInvoicePurpose()));
                }
            }
            if(hasExpiryDate) {
                if(deliveryDate == null) {
                    Calendar now = Calendar.getInstance();
                    deliveryDate = now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH);
                }
                deliveryDate = DateTimeTools.convertToDate(deliveryDate, "yyyy-M-d", "yyyy-MM-dd");
                btnExpiryDate.setText(deliveryDate);
            }
        }

        Product product = selectedProductItem.getProduct();
        tvProductName.setText(product.getName());
        tvInStock.setText(String.format("In Stock: %1$s %2$s", product.getInStock(), product.getBase_unit_name()));

        if(getHelper() != null && (salesCustomer != null || salesCustomerGroup != null || salesBranch != null)) {
            retailPrice = "P"+NumberTools.separateInCommas(PriceTools.identifyRetailPrice(getHelper(), product,
                    salesBranch, salesCustomerGroup, salesCustomer));
        }
        tvRetailPrice.setText(retailPrice);
        tvSubtotal.setText(subtotal);
        if (isMultiValue) {
            if (valuePosition > -1)
                etQuantity.setText(selectedProductItem.getQuantity(valuePosition));
        } else
            etQuantity.setText(selectedProductItem.getQuantity());

        llSubtotal.setVisibility(!hasSubtotal ? View.GONE : View.VISIBLE);

        btnSave.setOnClickListener(onSaveClicked);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etQuantity, InputMethodManager.SHOW_IMPLICIT);

                offsetSpinnerBelowv21(spUnits);
            }
        });
    }

    public void setRetailPrice(String retailPrice) {
        this.retailPrice = retailPrice;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }


    private View.OnClickListener onSaveClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String quantity = etQuantity.getText().toString().replace(",", "");

            if(quantity.length() == 0)
                quantity = "0";

            if (quantity.equals("0") && !isMultiValue)
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

                //Log.e("SIMPLE_SALES_QUANTITY_DIALOG", unit != null? unit.getName() : "null");

                if(getHelper() == null)
                    values.setValue(quantity, unit);
                else {
                    Price price = PriceTools.identifyPrice(getHelper(), selectedProductItem.getProduct(),
                            salesBranch, salesCustomerGroup, salesCustomer, unit);
                    if(price != null)
                        values.setValue(quantity, price);
                    else
                        values.setValue(quantity, unit, selectedProductItem.getRetail_price());
                }

                if(hasInvoicePurpose) {
                    values.setInvoicePurpose((InvoicePurpose) spInvoicePurpose.getSelectedItem());
                    values.setExpiry_date(deliveryDate);
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

}
