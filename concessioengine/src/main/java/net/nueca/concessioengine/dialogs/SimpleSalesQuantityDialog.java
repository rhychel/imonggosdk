package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.sql.SQLException;
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
    private SwitchCompat swcBadStock;

    private String subtotal, retailPrice;

    private Unit defaultUnit;

    private Product product;

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
        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnSave = (Button) super.findViewById(R.id.btnSave);
        llSubtotal = (LinearLayout) super.findViewById(R.id.llSubtotal);

        etQuantity.setSelectAllOnFocus(true);
        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                subtotal = String.valueOf(NumberTools.toDouble(etQuantity.getText().toString()) * NumberTools.toDouble(retailPrice));
                tvSubtotal.setText("P"+NumberTools.separateInCommas(subtotal));
                Log.e("SUBTOTAL AFTER", subtotal);
            }
        });

        boolean hasValues = selectedProductItem.getValues().size() > 0;

        if(hasBadStock) {
            swcBadStock = (SwitchCompat) super.findViewById(R.id.swcBadStock);
            swcBadStock.setVisibility(View.VISIBLE);
            swcBadStock.setChecked(true);
        }

        if(hasInvoicePurpose) {
            llInvoicePurpose = (LinearLayout) super.findViewById(R.id.llInvoicePurpose);
            spInvoicePurpose = (Spinner) super.findViewById(R.id.spInvoicePurpose);

            llInvoicePurpose.setVisibility(View.VISIBLE);
            invoicePurposesAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, invoicePurposeList);
            invoicePurposesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
            spInvoicePurpose.setAdapter(invoicePurposesAdapter);
            if(hasExpiryDate) { // this is for the expiry date
                llExpiryDate = (LinearLayout) super.findViewById(R.id.llExpiryDate);
                btnExpiryDate = (Button) super.findViewById(R.id.btnExpiryDate);

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

        product = selectedProductItem.getProduct();

        unitsAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, unitList);
        unitsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        spUnits.setAdapter(unitsAdapter);
        spUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                retailPrice = "P"+NumberTools.separateInCommas(PriceTools.identifyRetailPrice(getHelper(), product,
                        salesBranch, salesCustomerGroup, salesCustomer, unitsAdapter.getItem(i)));
                subtotal = String.valueOf(NumberTools.toDouble(etQuantity.getText().toString()) * NumberTools.toDouble(retailPrice));
                tvSubtotal.setText("P"+NumberTools.separateInCommas(subtotal));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                retailPrice = "P"+NumberTools.separateInCommas(PriceTools.identifyRetailPrice(getHelper(), product,
                        salesBranch, salesCustomerGroup, salesCustomer, defaultUnit));
                subtotal = String.valueOf(NumberTools.toDouble(etQuantity.getText().toString()) * NumberTools.toDouble(retailPrice));
                tvSubtotal.setText("P"+NumberTools.separateInCommas(subtotal));
            }
        });
        if (hasValues) {
            if (isMultiValue) {
                if (valuePosition > -1) {
                    Values value = selectedProductItem.getValues().get(valuePosition);
                    spUnits.setSelection(unitList.indexOf(value.getUnit() != null? value.getUnit() : defaultUnit));
                    if(hasInvoicePurpose) {
                        deliveryDate = value.getExpiry_date();
                        spInvoicePurpose.setSelection(invoicePurposeList.indexOf(value.getInvoicePurpose()));
                    }
                    if(hasBadStock)
                        swcBadStock.setChecked(value.isBadStock());
                }
            } else {
                Values value = selectedProductItem.getValues().get(0);
                spUnits.setSelection(unitList.indexOf(value.getUnit() != null? value.getUnit() : defaultUnit));
                if(hasInvoicePurpose) {
                    deliveryDate = value.getExpiry_date();
                    spInvoicePurpose.setSelection(invoicePurposeList.indexOf(value.getInvoicePurpose()));
                }
                if(hasBadStock)
                    swcBadStock.setChecked(value.isBadStock());
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

        if(getHelper() != null && (salesCustomer != null || salesCustomerGroup != null || salesBranch != null)) {
            defaultUnit = spUnits.getSelectedItem() instanceof Unit? (Unit)spUnits.getSelectedItem() : null;
            if(product.getExtras() != null && defaultUnit == null) {
                try {
                    defaultUnit = getHelper().fetchObjects(Unit.class).queryBuilder().where()
                            .eq("id", product.getExtras().getDefault_selling_unit()).queryForFirst();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            Log.e(getClass().getSimpleName(), "calling PriceTools.identifyRetailPrice");
            retailPrice = "P"+NumberTools.separateInCommas(PriceTools.identifyRetailPrice(getHelper(), product,
                    salesBranch, salesCustomerGroup, salesCustomer, (Unit) spUnits.getSelectedItem()));
        }

        tvProductName.setText(product.getName());

        if (isMultiValue) {
            if (valuePosition > -1)
                etQuantity.setText(selectedProductItem.getQuantity(valuePosition));
        } else
            etQuantity.setText(selectedProductItem.getQuantity());

        tvInStock.setText(String.format("In Stock: %1$s %2$s", product.getInStock(), product.getBase_unit_name()));

        tvRetailPrice.setText(retailPrice);
        subtotal = String.valueOf(NumberTools.toDouble(etQuantity.getText().toString()) * NumberTools.toDouble(retailPrice));
        tvSubtotal.setText("P"+NumberTools.separateInCommas(subtotal));

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

            if(quantity == null || quantity.length() == 0)
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

                Log.e("SIMPLE_SALES_QUANTITY_DIALOG", unit != null? unit.getName() : "null");
                Log.e("SIMPLE_SALES_QUANTITY_DIALOG", "is Helper NULL? " + (getHelper() == null));

                if(getHelper() == null)
                    values.setValue(quantity, unit);
                else {
                    Price price = PriceTools.identifyPrice(getHelper(), selectedProductItem.getProduct(),
                            salesBranch, salesCustomerGroup, salesCustomer, unit);

                    Log.e(getClass().getSimpleName(), "VALUES PRICE : isNull? " + (price == null));
                    if(price != null)
                        values.setValue(quantity, price, salesCustomer != null? salesCustomer.getDiscount_text() : null);
                    else
                        values.setValue(quantity, unit, selectedProductItem.getRetail_price(),
                                salesCustomer != null? salesCustomer.getDiscount_text() : null);
                }
                Log.e(getClass().getSimpleName(), "VALUES QTY : " + values.getQuantity());

                if(hasInvoicePurpose) {
                    values.setInvoicePurpose((InvoicePurpose) spInvoicePurpose.getSelectedItem());
                    values.setExpiry_date(deliveryDate);
                }
                if(hasBadStock)
                    values.setBadStock(swcBadStock.isChecked());

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
