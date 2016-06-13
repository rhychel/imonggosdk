package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.enums.ConcessioModule;

/**
 * Created by rhymart on 12/3/15.
 */
public class TransactionDialog extends BaseAppCompatDialog {

    public interface TransactionDialogListener {
        void whenDismissed();
    }

    private Button btnDone;
    private TextView tvTitle, tvInStock;
    private ImageView ivStatus;
    private LinearLayout llDetails;

    private TextView tvCustomerName, tvAmount, tvAmountLabel, tvDatetime;

    private String title, inStock;
    private String customerName, amount, amountLabel, datetime;
    private ConcessioModule concessioModule = ConcessioModule.STOCK_REQUEST;
    private int statusResource = R.drawable.ic_check_round_teal;

    private TransactionDialogListener transactionDialogListener;

    public TransactionDialog(Context context) {
        super(context);
    }

    public TransactionDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.transaction_complete_dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        btnDone = (Button) super.findViewById(R.id.btnDone);
        tvTitle = (TextView) super.findViewById(R.id.tvTitle);
        tvInStock = (TextView) super.findViewById(R.id.tvInStock);
        ivStatus = (ImageView) super.findViewById(R.id.ivStatus);
        llDetails = (LinearLayout) super.findViewById(R.id.llDetails);
        tvDatetime = (TextView) super.findViewById(R.id.tvDatetime);

        tvCustomerName = (TextView) super.findViewById(R.id.tvCustomerName);
        tvAmount = (TextView) super.findViewById(R.id.tvAmount);
        tvAmountLabel = (TextView) super.findViewById(R.id.tvAmountLabel);

        setHasDetails(concessioModule == ConcessioModule.INVOICE || concessioModule == ConcessioModule.INVOICE_PARTIAL);
        tvCustomerName.setText(customerName);
        tvAmount.setText(amount);
        if(amountLabel != null)
            tvAmountLabel.setText(amountLabel);

        tvTitle.setText(title);
        tvInStock.setText(inStock);
        ivStatus.setImageResource(statusResource);
        tvDatetime.setText(datetime);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transactionDialogListener != null)
                    transactionDialogListener.whenDismissed();
                dismiss();
            }
        });
    }

    public void setAmountLabel(String amountLabel) {
        this.amountLabel = amountLabel;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle(ConcessioModule concessioModule) {
        this.concessioModule = concessioModule;
        this.title = getTitle(concessioModule);
    }

    public void setInStock(String inStock) {
        this.inStock = inStock;
    }

    public void setStatusResource(int statusResource) {
        this.statusResource = statusResource;
    }

    public void setHasDetails(boolean hasDetails) {
        if(hasDetails)
            llDetails.setVisibility(View.VISIBLE);
    }

    // -- Handle custom
    private String getTitle(ConcessioModule concessioModule) {
        switch (concessioModule) {
            case STOCK_REQUEST:
                return "Order Items Saved";
            case RECEIVE_SUPPLIER:
                return "Received Items Saved";
            case RELEASE_SUPPLIER:
                return "Salesman RGS Successful";
            case RELEASE_ADJUSTMENT:
                return "MSO Successful";
            case INVOICE_PARTIAL:
                return "Partial Payment Successful";
            case PHYSICAL_COUNT:
                return "Physical Count Successful";
            case RELEASE_BRANCH:
                return "Pullout Request Saved";
            case RECEIVE_BRANCH_PULLOUT:
                return "Pullout Confirmation Successful";
            case RECEIVE_BRANCH:
                return "Receiving Successful";
            default:
                return "Sales Items Saved";
        }
    }

    public void setTransactionDialogListener(TransactionDialogListener transactionDialogListener) {
        this.transactionDialogListener = transactionDialogListener;
    }
}
