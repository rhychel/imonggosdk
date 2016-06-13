package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.objects.ReceivedItemValue;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 03/06/2016.
 */
public class SimpleOrderReceiveDialog extends BaseAppCompatDialog {
    private AutofitTextView tvProductName;
    private TextView tvInStock, tvRetailPrice, tvSubtotal, tvUnit;
    private Spinner spUnits;
    private EditText etQuantity, etActualPrice;

    private View llActualPrice;

    private Button btnSave, btnCancel;

    private String productName;
    private Double expectedPrice = 0d;
    private List<Unit> unitList;

    private boolean isUnitDisplay = false;

    private ReceivedItemValue itemValue;

    public interface ReceiveDialogListener {
        void onCancel();
        void onSave(ReceivedItemValue itemValue);
    }

    private ReceiveDialogListener receiveDialogListener;

    public SimpleOrderReceiveDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_quantity_dialog2);

        tvProductName = (AutofitTextView) findViewById(R.id.tvProductName);
        tvInStock = (TextView) findViewById(R.id.tvInStock);
        tvRetailPrice = (TextView) findViewById(R.id.tvRetailPrice);
        tvSubtotal = (TextView) findViewById(R.id.tvSubtotal);
        spUnits = (Spinner) findViewById(R.id.spUnits);
        tvUnit = (TextView) findViewById(R.id.tvUnit);

        llActualPrice = findViewById(R.id.llActualPrice);
        etQuantity = (EditText) findViewById(R.id.etQuantity);
        etActualPrice = (EditText) findViewById(R.id.etActualPrice);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSave = (Button) findViewById(R.id.btnSave);

        llActualPrice.setVisibility(View.VISIBLE);
        tvInStock.setVisibility(View.GONE);

        ArrayAdapter unitsAdapter = new ArrayAdapter<Unit>(getContext(), R.layout.spinner_item_light, unitList);
        unitsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        spUnits.setAdapter(unitsAdapter);
        if(isUnitDisplay) {
            spUnits.setVisibility(View.GONE);
            tvUnit.setVisibility(View.VISIBLE);
            tvUnit.setText(((Unit)spUnits.getSelectedItem()).getName());
        }

        tvProductName.setText(productName);
        tvRetailPrice.setText("P " + NumberTools.separateInCommas(expectedPrice));

        etQuantity.setSelectAllOnFocus(true);
        etActualPrice.setSelectAllOnFocus(true);

        if(itemValue == null)
            etActualPrice.setText(String.valueOf(expectedPrice));
        else {
            etActualPrice.setText(String.valueOf(itemValue.getPrice()));
            etQuantity.setText(String.valueOf(itemValue.getQuantity()));
        }

        etQuantity.addTextChangedListener(onQuantityChanged);
        etActualPrice.addTextChangedListener(onQuantityChanged);

        String quantity = etQuantity.getText().toString();
        String subtotal = String.valueOf(NumberTools.toDouble(quantity.equals("-") ? "0" : quantity) *
                NumberTools.toDouble(etActualPrice.getText().toString()));
        tvSubtotal.setText("P"+NumberTools.separateInCommas(subtotal));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(receiveDialogListener != null)
                    receiveDialogListener.onCancel();
                cancel();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(receiveDialogListener != null)
                    receiveDialogListener.onSave(new ReceivedItemValue(NumberTools.toDouble(etQuantity.getText().toString()),
                            NumberTools.toDouble(etActualPrice.getText().toString())));
                dismiss();
            }
        });
    }

    private TextWatcher onQuantityChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable editable) {
            String quantity = etQuantity.getText().toString();
            String subtotal = String.valueOf(NumberTools.toDouble(quantity.equals("-") ? "0" : quantity) *
                    NumberTools.toDouble(etActualPrice.getText().toString()));
            tvSubtotal.setText("P"+NumberTools.separateInCommas(subtotal));
            Log.e("SUBTOTAL AFTER", subtotal);
        }
    };

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setExpectedPrice(double expectedPrice) {
        this.expectedPrice = expectedPrice;
    }

    public void setItemValue(ReceivedItemValue itemValue) {
        this.itemValue = itemValue;
    }

    public void setUnitList(List<Unit> unitList) {
        this.unitList = unitList;
    }

    public void setReceiveDialogListener(ReceiveDialogListener receiveDialogListener) {
        this.receiveDialogListener = receiveDialogListener;
    }

    public void setUnitDisplay(boolean unitDisplay) {
        isUnitDisplay = unitDisplay;
    }
}
