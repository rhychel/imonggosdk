package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.PaymentType;
import net.nueca.imonggosdk.widgets.Numpad;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 9/1/15.
 */
public class SimplePaymentDialog extends BaseAppCompatDialog {
    private EditText etPayment;
    private Spinner spnPaymentType;
    private Button btnAdd, btnCancel;
    private Numpad npInput;

    private PaymentDialogListener listener;

    private List<PaymentType> paymentTypes;

    public SimplePaymentDialog(Context context, List<PaymentType> paymentTypes) {
        super(context);
        this.paymentTypes = paymentTypes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.simple_payment_dialog);
        super.setCancelable(false);

        etPayment = (EditText) super.findViewById(R.id.etPayment);

        spnPaymentType = (Spinner) super.findViewById(R.id.spnPaymentType);

        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        btnAdd = (Button) super.findViewById(R.id.btnAdd);

        ArrayAdapter<PaymentType> paymentTypesAdapter = new ArrayAdapter<PaymentType>(super.getContext(),
                R.layout.simple_spinner_item, paymentTypes);

        spnPaymentType.setAdapter(paymentTypesAdapter);

        npInput = (Numpad) super.findViewById(R.id.npInput);
        npInput.addTextHolder(etPayment, "etPayment", false, false, null);
        npInput.getTextHolderWithTag("etPayment").setEnableDot(true);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onAddPayment((PaymentType)spnPaymentType.getSelectedItem(),
                            etPayment.getText().toString());
                }
                dismiss();
            }
        });
    }

    public void showWithText(String txt) {
        show();
        etPayment.setText(txt);
    }

    @Override
    public void cancel() {
        super.cancel();
        npInput.setIsFirstErase(true);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        npInput.setIsFirstErase(true);
    }


    public PaymentDialogListener getListener() {
        return listener;
    }

    public void setListener(PaymentDialogListener listener) {
        this.listener = listener;
    }

    public interface PaymentDialogListener {
        void onAddPayment(PaymentType paymentType, String paymentValue);
    }
}
