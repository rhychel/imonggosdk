package net.nueca.concessio;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import net.nueca.concessioengine.activities.checkout.CheckoutActivity;
import net.nueca.concessioengine.adapters.SimpleSplitPaymentAdapter;
import net.nueca.concessioengine.enums.DialogType;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.dialogs.SimplePaymentDialog;
import net.nueca.concessioengine.dialogs.TransactionDialog;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.ArrayList;

/**
 * Created by rhymart on 12/3/15.
 */
public class C_Checkout extends CheckoutActivity {

    private Toolbar tbActionBar;
    private RecyclerView rvPayments;
    private SimpleSplitPaymentAdapter simpleSplitPaymentAdapter;
    private LinearLayout llBalance;
    private Button btn1, btn2;

    private ArrayList<InvoicePayment> invoicePayments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_checkout_activity);

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        rvPayments = (RecyclerView) findViewById(R.id.rvPayments);
        llBalance = (LinearLayout) findViewById(R.id.llBalance);
        btn1 = (Button) findViewById(R.id.btn1);

        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Checkout");

        simpleSplitPaymentAdapter = new SimpleSplitPaymentAdapter(this, invoicePayments, getHelper(), ListingType.COLORED_PAYMENTS);
        simpleSplitPaymentAdapter.initializeRecyclerView(this, rvPayments);
        rvPayments.setAdapter(simpleSplitPaymentAdapter);

        btn1.setText("PAY");
        btn1.setOnClickListener(onClickListener);

        llBalance.setVisibility(View.VISIBLE);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rvPayments);
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if(target != null)
                Log.e("target", "Not null");

            if(viewHolder != null)
                Log.e("viewHolder", "Not null");
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            Log.e("onSwiped", "Index=" + viewHolder.getAdapterPosition());
            simpleSplitPaymentAdapter.remove(viewHolder.getAdapterPosition()-1);
            simpleSplitPaymentAdapter.notifyItemChanged(0);
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if(viewHolder instanceof SimpleSplitPaymentAdapter.ListViewHolder) {
                return ((SimpleSplitPaymentAdapter.ListViewHolder) viewHolder).isLastItem() ? 0 : super.getSwipeDirs(recyclerView, viewHolder);
            }
            return super.getSwipeDirs(recyclerView, viewHolder);
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(btn1.getText().toString().equals("PAY")) {
                SimplePaymentDialog dialog = new SimplePaymentDialog(C_Checkout.this,
                        new ArrayList<>(simpleSplitPaymentAdapter.getPaymentTypes().values()), R.style.AppCompatDialogStyle_Light_NoTitle);
                dialog.setDialogType(DialogType.ADVANCED_PAY);
                dialog.setListener(new SimplePaymentDialog.PaymentDialogListener() {
                    @Override
                    public void onAddPayment(PaymentType paymentType, String paymentValue) {
                        InvoicePayment.Builder builder = new InvoicePayment.Builder();
                        builder.tender(NumberTools.toDouble(paymentValue));

                        if(paymentType != null)
                            builder.payment_type_id(paymentType.getId());

                        InvoicePayment invoicePayment = builder.build();
                        simpleSplitPaymentAdapter.add(invoicePayment);
                        simpleSplitPaymentAdapter.notifyItemInserted(simpleSplitPaymentAdapter.getItemCount());
                        btn1.setText("SUBMIT");
                    }
                });
                dialog.show();
            }
            else {
                TransactionDialog transactionDialog = new TransactionDialog(C_Checkout.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                transactionDialog.show();
            }
        }
    };

}
