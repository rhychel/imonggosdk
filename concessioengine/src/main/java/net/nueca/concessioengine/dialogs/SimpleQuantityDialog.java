package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.widgets.Keypad;
import net.nueca.imonggosdk.widgets.Numpad;

import java.util.List;

/**
 * Created by rhymart on 7/16/15.
 * imonggosdk (c)2015
 */
public class SimpleQuantityDialog extends BaseQuantityDialog {

    private Spinner spUnits;
    private EditText etQuantity;
//    private Keypad kpInput;
    private Numpad npInput;

    private ArrayAdapter<Unit> unitsAdapter;

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

        unitsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, unitList);

        spUnits = (Spinner) super.findViewById(R.id.spUnits);
        btnSave = (Button) super.findViewById(R.id.btnSave);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);

        etQuantity = (EditText) super.findViewById(R.id.etQuantity);
        npInput = (Numpad) super.findViewById(R.id.npInput);
//        kpInput = (Keypad) super.findViewById(R.id.kpInput);

        etQuantity.setText(selectedProductItem.getQuantity());
        npInput.addTextHolder(etQuantity, "etQuantity", false, false, null);

        spUnits.setAdapter(unitsAdapter);

        if(selectedProductItem.getValues().size() > 0) {
            spUnits.setSelection(unitList.indexOf(selectedProductItem.getValues().get(0).getUnit()));
        }
        btnSave.setOnClickListener(onSaveClicked);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private View.OnClickListener onSaveClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String quantity = etQuantity.getText().toString();
            if(quantity.equals("0") && !selectedProductItem.isMultiline())
                selectedProductItem.removeAll();
            else {
                Values values = new Values(((Unit) spUnits.getSelectedItem()), quantity);
                selectedProductItem.addValues(values);
            }

            if(quantityDialogListener != null)
                quantityDialogListener.onSave(selectedProductItem);
            dismiss();
        }
    };

}
