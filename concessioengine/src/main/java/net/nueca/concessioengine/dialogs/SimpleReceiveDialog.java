package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.widgets.Numpad;

/**
 * Created by gama on 9/7/15.
 */
public class SimpleReceiveDialog extends BaseAppCompatDialog {
    private Button btnSave, btnCancel;

    private TextView tvProductName;
    private EditText etReceive, etReturn;

    private Numpad npInput;

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

        tvProductName.setText(productName);
        etReceive.setText(receiveText);
        etReturn.setText(returnText);

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

    private String receiveText = "0", returnText = "0", productName = "";
    public void setReceiveText(String str) {
        receiveText = str;
        if(etReceive != null)
            etReceive.setText(receiveText);
    }
    public void setReturnText(String str) {
        returnText = str;
        if(etReturn != null)
            etReturn.setText(returnText);
    }
    public void setProductName(String str) {
        productName = str;
        if(tvProductName != null)
            tvProductName.setText(productName);
    }
    public String getReceiveText() {
        return etReceive.getText().toString();
    }
    public String getReturnText() {
        return etReturn.getText().toString();
    }
    public String getProductName() {
        return tvProductName.getText().toString();
    }

    public interface SimpleReceiveDialogListener {
        /** return true if should dismiss dialog **/
        boolean onCancel();
        void onSearch(String receivetxt, String returntxt);
    }
}
