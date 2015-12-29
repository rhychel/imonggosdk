package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import net.nueca.imonggosdk.tools.NumberTools;

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
        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnSave = (Button) super.findViewById(R.id.btnSave);
        llSubtotal = (LinearLayout) super.findViewById(R.id.llSubtotal);

        etQuantity.setSelectAllOnFocus(true);
        
        unitsAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_light, unitList);
        unitsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        spUnits.setAdapter(unitsAdapter);
        if (selectedProductItem.getValues().size() > 0) {
            if (isMultiValue) {
                if (valuePosition > -1)
                    spUnits.setSelection(unitList.indexOf(selectedProductItem.getValues().get(valuePosition).getUnit()));
            } else
                spUnits.setSelection(unitList.indexOf(selectedProductItem.getValues().get(0).getUnit()));
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
                else
                    values.setValue(quantity, unit,
                            PriceTools.identifyRetailPrice(getHelper(), selectedProductItem.getProduct(),
                                    salesBranch, salesCustomerGroup, salesCustomer, unit));
                if (isMultiValue) {
                    if (multiQuantityDialogListener != null)
                        multiQuantityDialogListener.onSave(values);
                    dismiss();
                    return;
                } else
                    selectedProductItem.addValues(values);
            }

            if (quantityDialogListener != null)
                quantityDialogListener.onSave(selectedProductItem);
            dismiss();
        }
    };

}
