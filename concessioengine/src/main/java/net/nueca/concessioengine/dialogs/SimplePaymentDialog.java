package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.enums.DialogType;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.widgets.Numpad;

import java.util.List;

/**
 * Created by gama on 9/1/15.
 */
public class SimplePaymentDialog extends BaseAppCompatDialog {

    private DialogType dialogType = DialogType.BASIC_PAY;

    private EditText etPayment;
    private Spinner spnPaymentType;

    private TextView tvBalance, tvTotalAmount;
    private Button btnPay;

    private Button btnAdd, btnCancel;
    private Numpad npInput;

    private LinearLayout llCheckName, llCheckNumber, llBankBranch, llCheckDate;
    private EditText etCheckName, etCheckNumber, etBankBranch;
    private Button btnCheckDate;

    private PaymentDialogListener listener;

    private List<PaymentType> paymentTypes;

    public SimplePaymentDialog(Context context, List<PaymentType> paymentTypes) {
        super(context);
        this.paymentTypes = paymentTypes;
    }

    public SimplePaymentDialog(Context context, List<PaymentType> paymentTypes, int theme) {
        super(context, theme);
        this.paymentTypes = paymentTypes;
    }

    private int chooseLayout() {
        switch (dialogType) {
            case ADVANCED_PAY:
                return R.layout.add_payment_dialog;
            default:
                return R.layout.simple_payment_dialog;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(chooseLayout());
        super.setCancelable(false);

        etPayment = (EditText) super.findViewById(R.id.etPayment);
        spnPaymentType = (Spinner) super.findViewById(R.id.spnPaymentType);
        btnCancel = (Button) super.findViewById(R.id.btnCancel);
        llCheckName = (LinearLayout) super.findViewById(R.id.llCheckName);
        llCheckNumber = (LinearLayout) super.findViewById(R.id.llCheckNumber);
        llBankBranch = (LinearLayout) super.findViewById(R.id.llBankBranch);
        llCheckDate = (LinearLayout) super.findViewById(R.id.llCheckDate);
        etCheckName = (EditText) super.findViewById(R.id.etCheckName);
        etCheckNumber = (EditText) super.findViewById(R.id.etCheckNumber);
        etBankBranch = (EditText) super.findViewById(R.id.etBankBranch);
        btnCheckDate = (Button) super.findViewById(R.id.btnCheckDate);

        ArrayAdapter<PaymentType> paymentTypesAdapter = new ArrayAdapter<>(super.getContext(),
                R.layout.spinner_item_light, paymentTypes);
        paymentTypesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        spnPaymentType.setAdapter(paymentTypesAdapter);
        spnPaymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toggleCheckDetails(paymentTypes.get(position).getName().trim().toLowerCase().equals("check"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        if(dialogType == DialogType.BASIC_PAY) {
            btnAdd = (Button) super.findViewById(R.id.btnAdd);

            npInput = (Numpad) super.findViewById(R.id.npInput);
            npInput.addTextHolder(etPayment, "etPayment", false, false, null);
            npInput.getTextHolderWithTag("etPayment").setEnableDot(true);

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onAddPayment((PaymentType) spnPaymentType.getSelectedItem(),
                                etPayment.getText().toString(), generateExtrasForCheck());
                    }
                    dismiss();
                }
            });
        }
        else if(dialogType == DialogType.ADVANCED_PAY) {
            tvBalance = (TextView) super.findViewById(R.id.tvBalance);
            tvTotalAmount = (TextView) super.findViewById(R.id.tvTotalAmount);
            btnPay = (Button) super.findViewById(R.id.btnPay);

            btnPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                        listener.onAddPayment((PaymentType) spnPaymentType.getSelectedItem(), etPayment.getText().toString(), generateExtrasForCheck());
                    dismiss();
                }
            });
        }
    }

    private Extras generateExtrasForCheck() {
        Extras extras = null;
        if(llCheckName.getVisibility() == View.VISIBLE) {
            extras = new Extras();
            extras.setBank_branch(etBankBranch.getText().toString());
            extras.setCheck_name(etCheckName.getText().toString());
            extras.setCheck_number(etCheckNumber.getText().toString());
            extras.setCheck_date(btnCheckDate.getText().toString());
        }
        return extras;
    }

    private void toggleCheckDetails(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        llCheckName.setVisibility(visibility);
        llCheckNumber.setVisibility(visibility);
        llCheckDate.setVisibility(visibility);
        llBankBranch.setVisibility(visibility);
    }

    public void setDialogType(DialogType dialogType) {
        this.dialogType = dialogType;
    }

    public void showWithText(String txt) {
        show();
        etPayment.setText(txt);
    }

    @Override
    public void cancel() {
        super.cancel();
        if(npInput != null)
            npInput.setIsFirstErase(true);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if(npInput != null)
            npInput.setIsFirstErase(true);
    }


    public PaymentDialogListener getListener() {
        return listener;
    }

    public void setListener(PaymentDialogListener listener) {
        this.listener = listener;
    }

    public interface PaymentDialogListener {
        void onAddPayment(PaymentType paymentType, String paymentValue, Extras extras);
    }
}
