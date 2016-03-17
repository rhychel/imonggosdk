package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.enums.DialogType;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.tools.NumberTools;
import net.nueca.imonggosdk.widgets.Numpad;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by gama on 9/1/15.
 */
public class SimplePaymentDialog extends BaseAppCompatDialog {

    private DialogType dialogType = DialogType.BASIC_PAY;

    private EditText etPayment;
    private Spinner spnPaymentType;

    private TextView tvLabelBalance, tvBalance, tvTotalAmount, tvPointsInPeso, tvAvailablePoints;
    private Button btnPay;

    private Button btnAdd, btnCancel;
    private Numpad npInput;

    private LinearLayout llCheckName, llCheckNumber, llBankBranch, llCheckDate, llPointsToPeso, llAvailablePoints;
    private EditText etCheckName, etCheckNumber, etBankBranch;
    private Button btnCheckDate;
    private InvoicePayment invoicePayment = null;

    private PaymentDialogListener listener;

    private List<PaymentType> paymentTypes;

    private String totalAmount = "";
    private double balance = 0.0;
    private double pointsInPeso = 0d, availablePoints = 0d;

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
                togglePointsDetails(paymentTypes.get(position).getName().trim().toLowerCase().equals("points") || paymentTypes.get(position).getName().trim().toLowerCase().equals("rewards"));
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

        if(invoicePayment != null) {
            etPayment.setText(String.valueOf(invoicePayment.getTender()));
            int selectedPosition = paymentTypes.indexOf(new PaymentType(invoicePayment.getPayment_type_id()));
            spnPaymentType.setSelection(selectedPosition);
            if(invoicePayment.getExtras() != null) {
                toggleCheckDetails(true);
                etBankBranch.setText(invoicePayment.getExtras().getBank_branch());
                etCheckName.setText(invoicePayment.getExtras().getCheck_name());
                etCheckNumber.setText(invoicePayment.getExtras().getCheck_number());
                btnCheckDate.setText(invoicePayment.getExtras().getCheck_date());
            }
        }

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
            tvLabelBalance = (TextView) super.findViewById(R.id.tvLabelBalance);
            tvPointsInPeso = (TextView) super.findViewById(R.id.tvPointsInPeso);
            tvAvailablePoints = (TextView) super.findViewById(R.id.tvAvailablePoints);
            tvTotalAmount = (TextView) super.findViewById(R.id.tvTotalAmount);
            llPointsToPeso = (LinearLayout) super.findViewById(R.id.llPointsToPeso);
            llAvailablePoints = (LinearLayout) super.findViewById(R.id.llAvailablePoints);
            btnPay = (Button) super.findViewById(R.id.btnPay);

            balanceColor(balance);
            tvBalance.setText(NumberTools.separateInCommas(balance));
            tvTotalAmount.setText(totalAmount);

            btnPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(etPayment.length() == 0) {
                        Toast.makeText(getContext(), "Tender payment cannot be empty.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(listener != null)
                        listener.onAddPayment((PaymentType) spnPaymentType.getSelectedItem(), etPayment.getText().toString(), generateExtrasForCheck());
                    dismiss();
                }
            });
            etPayment.addTextChangedListener(new TextWatcher() {
                private String newText;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    newText = "0" + s.toString();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    BigDecimal currentBalance = new BigDecimal(balance);
                    BigDecimal payment = new BigDecimal(newText);
                    double newBalance;

                    if(newText.length() == 0) {
                        newBalance = balance;
                    }
                    else if(invoicePayment != null) {
                        BigDecimal prevPayment = new BigDecimal(invoicePayment.getTender());
                        newBalance = currentBalance.add(prevPayment).subtract(payment).doubleValue();
                    }
                    else {
                        newBalance = currentBalance.subtract(payment).doubleValue();
                    }
                    tvBalance.setText(NumberTools.separateInCommas(Math.abs(newBalance)));
                    balanceColor(newBalance);
                }
            });
            etPayment.setSelection(0, etPayment.getText().length());
        }

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
        });
    }

    private void balanceColor(double balance) {
        if(balance > 0) {
            tvLabelBalance.setText("Balance");
            tvBalance.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
        }
        else {
            tvLabelBalance.setText("Change");
            tvBalance.setTextColor(getContext().getResources().getColor(R.color.payment_color));
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
        if(show)
            btnCheckDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeliveryDatePicker(fragmentManager, btnCheckDate);
                }
            });
        else
            btnCheckDate.setOnClickListener(null);
    }

    private void togglePointsDetails(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        llAvailablePoints.setVisibility(visibility);
        tvAvailablePoints.setText(NumberTools.separateInCommas(availablePoints));

        llPointsToPeso.setVisibility(visibility);
        tvPointsInPeso.setText(NumberTools.separateInCommas(pointsInPeso));
    }

    public void setDialogType(DialogType dialogType) {
        this.dialogType = dialogType;
    }

    public void showWithText(String txt) {
        show();
        etPayment.setText(txt);
    }

    public SimplePaymentDialog setTotalAmountText(String totalAmountText) {
        totalAmount = totalAmountText;
        return this;
    }

    public SimplePaymentDialog setBalanceText(double balanceText) {
        balance = balanceText;
        return this;
    }

    public SimplePaymentDialog setPointsInPesoText(double pointsInPeso) {
        this.pointsInPeso = pointsInPeso;
        return this;
    }

    public SimplePaymentDialog setAvailablePoints(double availablePoints) {
        this.availablePoints = availablePoints;
        return this;
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

    public void setInvoicePayment(InvoicePayment invoicePayment) {
        this.invoicePayment = invoicePayment;
    }
}
