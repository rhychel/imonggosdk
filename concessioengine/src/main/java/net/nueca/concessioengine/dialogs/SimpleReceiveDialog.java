package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.tools.NumberTools;
import net.nueca.imonggosdk.widgets.Numpad;

import java.math.BigDecimal;

/**
 * Created by gama on 9/7/15.
 */
public class SimpleReceiveDialog extends BaseAppCompatDialog {
    private Button btnSave, btnCancel;

    private TextView tvProductName;
    private EditText etReceive, etReturn, etQuantity, etDiscrepancy;

    private Numpad npInput;
    private String receiveText = "0", returnText = "0",
            productName = "", quantityText = "0", discrepancyText = "0";

    private boolean isManual = false;

    private SimpleReceiveDialogListener dialogListener;

    public void setDialogListener(SimpleReceiveDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    public SimpleReceiveDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_receive_dialog);

        btnSave = (Button) super.findViewById(R.id.btnSave);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);

        tvProductName = (TextView) super.findViewById(R.id.tvProductName);
        etReceive = (EditText) super.findViewById(R.id.etReceive);
        etReturn = (EditText) super.findViewById(R.id.etReturn);
        etQuantity = (EditText) super.findViewById(R.id.etQuantity);
        etDiscrepancy = (EditText) super.findViewById(R.id.etDiscrepancy);

        etReceive.addTextChangedListener(fieldChanged);
        etReturn.addTextChangedListener(fieldChanged);

        tvProductName.setText(productName);
        etReceive.setText(receiveText);
        etReturn.setText(returnText);
        if(!isManual) {
            etQuantity.setText(quantityText);
            etDiscrepancy.setText(discrepancyText);
            etQuantity.setVisibility(View.VISIBLE);
            etDiscrepancy.setVisibility(View.VISIBLE);
        }
        else {
            etQuantity.setVisibility(View.GONE);
            etDiscrepancy.setVisibility(View.GONE);
        }

        npInput = (Numpad) super.findViewById(R.id.npInput);

        npInput.addTextHolder(etReceive,"etReceive",false,false,null);

        npInput.addTextHolder(etReturn,"etReturn",false,false,null);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogListener != null)
                    dialogListener.onSearch(getReceiveText(), getReturnText());
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogListener == null)
                    cancel();

                if (dialogListener != null && dialogListener.onCancel())
                    cancel();
            }
        });
    }

    private TextWatcher fieldChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(isManual)
                return;

            BigDecimal orig_qty = NumberTools.toBigDecimal(etQuantity.getText().toString());

            BigDecimal rcv_qty = NumberTools.toBigDecimal(etReceive.getText().toString());

            BigDecimal ret_qty = NumberTools.toBigDecimal(etReturn.getText().toString());

            BigDecimal dsc_qty = orig_qty.subtract(rcv_qty.add(ret_qty));

            setDiscrepancy(NumberTools.separateInCommas(dsc_qty));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void setReceiveText(String str) {
        receiveText = NumberTools.separateInCommasHideZeroDecimals(str);
        if(etReceive != null)
            etReceive.setText(receiveText);
    }
    public void setReturnText(String str) {
        returnText = NumberTools.separateInCommasHideZeroDecimals(str);
        if(etReturn != null)
            etReturn.setText(returnText);
    }
    public void setProductName(String str) {
        productName = str;
        if(tvProductName != null)
            tvProductName.setText(productName);
    }
    public void setQuantity(String str) {
        quantityText = NumberTools.separateInCommas(str);
        if(etQuantity != null)
            etQuantity.setText(quantityText);
    }
    public void setDiscrepancy(String str) {
        discrepancyText = NumberTools.separateInCommas(str);
        if(etDiscrepancy != null)
            etDiscrepancy.setText(discrepancyText);
    }

    public String getReceiveText() {
        return etReceive.getText().toString();
    }
    public String getReturnText() {
        return etReturn.getText().toString();
    }
    public String getDiscrepancyText() {
        return etDiscrepancy.getText().toString();
    }
    public String getQuantityText() {
        return etQuantity.getText().toString();
    }

    public String getProductName() {
        return tvProductName.getText().toString();
    }

    public boolean isManual() {
        return isManual;
    }

    public void setIsManual(boolean isManual) {
        this.isManual = isManual;
    }

    public interface SimpleReceiveDialogListener {
        /** return true if should dismiss dialog **/
        boolean onCancel();
        void onSearch(String receivetxt, String returntxt);
    }
}
