package net.nueca.concessioengine.dialogs;

import android.content.Context;
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

    private String title, inStock;
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

        btnDone = (Button) super.findViewById(R.id.btnDone);
        tvTitle = (TextView) super.findViewById(R.id.tvTitle);
        tvInStock = (TextView) super.findViewById(R.id.tvInStock);
        ivStatus = (ImageView) super.findViewById(R.id.ivStatus);
        llDetails = (LinearLayout) super.findViewById(R.id.llDetails);

        tvTitle.setText(title);
        tvInStock.setText(inStock);
        ivStatus.setImageResource(statusResource);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transactionDialogListener != null)
                    transactionDialogListener.whenDismissed();
                dismiss();
            }
        });

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle(ConcessioModule concessioModule) {
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

    private String getTitle(ConcessioModule concessioModule) {
        switch (concessioModule) {
            case STOCK_REQUEST:
                return "Order Items Saved";
            case RECEIVE_SUPPLIER:
                return "Received Items Saved";
            case RELEASE_SUPPLIER:
                return "Pullout Successful";
            case RELEASE_ADJUSTMENT:
                return "MSO Successful";
            default:
                return "Sales Items Saved";
        }
    }

    public void setTransactionDialogListener(TransactionDialogListener transactionDialogListener) {
        this.transactionDialogListener = transactionDialogListener;
    }
}
