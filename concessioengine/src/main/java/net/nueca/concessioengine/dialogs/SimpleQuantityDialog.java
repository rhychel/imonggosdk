package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.objects.Unit;

/**
 * Created by rhymart on 7/16/15.
 * imonggosdk (c)2015
 */
public class SimpleQuantityDialog extends BaseQuantityDialog {

    private Spinner spUnits;

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

        setTitle(selectedProductItem.getProduct().getName());

        ArrayAdapter<Unit> unitsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, unitList);

        spUnits = (Spinner) super.findViewById(R.id.spUnits);
        btnSave = (Button) super.findViewById(R.id.btnSave);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);

        spUnits.setAdapter(unitsAdapter);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }



}
