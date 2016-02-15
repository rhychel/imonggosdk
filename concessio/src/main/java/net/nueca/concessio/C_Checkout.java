package net.nueca.concessio;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import net.nueca.concessioengine.activities.checkout.CheckoutActivity;
import net.nueca.concessioengine.adapters.SimpleSplitPaymentAdapter;
import net.nueca.concessioengine.adapters.base.BaseSplitPaymentAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.enums.DialogType;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.dialogs.SimplePaymentDialog;
import net.nueca.concessioengine.dialogs.TransactionDialog;
import net.nueca.concessioengine.fragments.SimpleCheckoutFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.concessioengine.tools.LocationTools;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.NumberTools;
import net.nueca.imonggosdk.tools.ReferenceNumberTool;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 12/3/15.
 */
public class C_Checkout extends CheckoutActivity implements SetupActionBar {

    private Toolbar tbActionBar;
    private RecyclerView rvPayments;
    private SimpleSplitPaymentAdapter simpleSplitPaymentAdapter;
    private LinearLayout llBalance, llTotalAmount;
    private TextView tvLabelBalance, tvBalance, tvTotalAmount;
    private Button btn1, btn2;

    @Override
    protected void initializeFragment() {
        checkoutFragment = new SimpleCheckoutFragment();
        checkoutFragment.setSetupActionBar(this);
        checkoutFragment.setLayaway(isLayaway);
        Log.e("C_Checkout", "initializeFragment " + (checkoutFragment == null));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_checkout_activity);

//        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
//        rvPayments = (RecyclerView) findViewById(R.id.rvPayments);

        llBalance = (LinearLayout) findViewById(R.id.llBalance);
        llTotalAmount = (LinearLayout) findViewById(R.id.llTotalAmount);
        tvBalance = (TextView) findViewById(R.id.tvBalance);
        tvLabelBalance = (TextView) findViewById(R.id.tvLabelBalance);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);

        Gson gson = new Gson();
        Log.e("C_Checkout", "checkoutFragment " + (checkoutFragment == null));
        Log.e("C_Checkout", gson.toJson(checkoutFragment.getComputation().getPayments()));
        Log.e("C_Checkout", checkoutFragment.getComputation().getRemaining().toPlainString());

        try {
            List<PaymentType> paymentTypes = getHelper().fetchIntId(PaymentType.class).queryBuilder().orderBy("id", true).where().eq("status", "A").query();
            for(PaymentType paymentType : paymentTypes) {
                Log.e("id", paymentType.getId()+"---"+paymentType.getName());
            }
            simpleSplitPaymentAdapter = new SimpleSplitPaymentAdapter(this, getHelper(), ListingType
                    .COLORED_PAYMENTS, null, paymentTypes);
            simpleSplitPaymentAdapter.setPaymentUpdateListener(new BaseSplitPaymentAdapter.OnPaymentUpdateListener() {
                @Override
                public void onAddPayment(InvoicePayment invoicePayment) {
                    tvBalance.setText(NumberTools.separateInCommas(checkoutFragment.getRemainingBalance(true)));

                    if(simpleSplitPaymentAdapter.isFullyPaid()) {
                        tvLabelBalance.setText("Change");
                        tvBalance.setTextColor(getResources().getColor(R.color.payment_color));
                        btn1.setText("SUBMIT");
                        btn2.setVisibility(View.GONE);
                    }
                    else {
                        tvLabelBalance.setText("Balance");
                        tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        btn2.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onUpdatePayment(int location, InvoicePayment invoicePayment) {
                    tvBalance.setText(NumberTools.separateInCommas(checkoutFragment.getRemainingBalance(true)));

                    if(simpleSplitPaymentAdapter.isFullyPaid()) {
                        tvLabelBalance.setText("Change");
                        tvBalance.setTextColor(getResources().getColor(R.color.payment_color));
                        btn1.setText("SUBMIT");
                        btn2.setVisibility(View.GONE);
                    }
                    else {
                        tvLabelBalance.setText("Balance");
                        tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        btn2.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onDeletePayment(int location) {
                    tvBalance.setText(NumberTools.separateInCommas(checkoutFragment.getRemainingBalance(true)));

                    if(!simpleSplitPaymentAdapter.isFullyPaid()) {
                        tvLabelBalance.setText("Balance");
                        tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        btn1.setText("PAY");
                        btn2.setVisibility(simpleSplitPaymentAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ((SimpleCheckoutFragment)checkoutFragment).setSplitPaymentAdapter(simpleSplitPaymentAdapter);
        //simpleSplitPaymentAdapter.initializeRecyclerView(this, rvPayments);
        //rvPayments.setAdapter(simpleSplitPaymentAdapter);

        btn1.setText("PAY");
        btn2.setText("PARTIAL");

        btn1.setOnClickListener(onClickListener);
        btn2.setOnClickListener(onClickListener);

        llBalance.setVisibility(View.VISIBLE);
        llTotalAmount.setVisibility(View.VISIBLE);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rvPayments);

        ((SimpleCheckoutFragment) checkoutFragment).setAmountDueTextView(tvTotalAmount);
        ((SimpleCheckoutFragment) checkoutFragment).setBalanceTextView(tvBalance);

        //Log.e(">>>>>>>>>>>",checkoutFragment.getComputation().getTotalPayable().toPlainString());
        //simpleSplitPaymentAdapter.setComputation(checkoutFragment.getComputation());

        getSupportFragmentManager().beginTransaction()
                .add(R.id.flContent, checkoutFragment, "checkout")
                .commit();

        simpleSplitPaymentAdapter.setTotalAmount(NumberTools.separateInCommas(checkoutFragment.getAmountDue()));
        simpleSplitPaymentAdapter.setBalance(checkoutFragment.getRemainingBalance());
        simpleSplitPaymentAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                tvBalance.setText(NumberTools.separateInCommas(checkoutFragment.getRemainingBalance()));
                simpleSplitPaymentAdapter.setBalance(checkoutFragment.getRemainingBalance());
            }
        });
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
            if(simpleSplitPaymentAdapter.getItem(viewHolder.getAdapterPosition()).getPaymentBatchNo() == null) {
                simpleSplitPaymentAdapter.remove(viewHolder.getAdapterPosition());
                simpleSplitPaymentAdapter.notifyItemChanged(0);
            }
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
            if(v.getId() == R.id.btn1) {
                if (btn1.getText().toString().equals("PAY")) {
                    SimplePaymentDialog dialog = new SimplePaymentDialog(C_Checkout.this, simpleSplitPaymentAdapter.getPaymentTypeList(), R.style.AppCompatDialogStyle_Light_NoTitle);
                    dialog.setDialogType(DialogType.ADVANCED_PAY);
                    dialog.setBalanceText(checkoutFragment.getRemainingBalance());
                    dialog.setTotalAmountText(tvTotalAmount.getText().toString());
                    dialog.setListener(new SimplePaymentDialog.PaymentDialogListener() {
                        @Override
                        public void onAddPayment(PaymentType paymentType, String paymentValue, Extras extras) {
                            InvoicePayment.Builder builder = new InvoicePayment.Builder();
                            builder.tender(NumberTools.toDouble(paymentValue));

                            if (paymentType != null) {
                                builder.payment_type_id(paymentType.getId());
                            }

                            InvoicePayment invoicePayment = builder.build();
                            invoicePayment.setExtras(extras);
                            // temp
                            if(simpleSplitPaymentAdapter.getItemCount() % 2 == 0)
                                invoicePayment.setPaymentBatchNo(1);
                            simpleSplitPaymentAdapter.addPayment(invoicePayment);

                            simpleSplitPaymentAdapter.setBalance(checkoutFragment.getRemainingBalance());
                            //simpleSplitPaymentAdapter.add(invoicePayment);
                            //simpleSplitPaymentAdapter.notifyItemInserted(simpleSplitPaymentAdapter.getItemCount());
                        }
                    });
                    dialog.show();
                }
                else {
                    TransactionDialog transactionDialog = new TransactionDialog(C_Checkout.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                    transactionDialog.setTitle(ConcessioModule.INVOICE);
                    transactionDialog.setAmount("P"+NumberTools.separateInCommas(checkoutFragment.getTotalPaymentMade()));
                    transactionDialog.setAmountLabel("Amount");
                    transactionDialog.setCustomerName(ProductsAdapterHelper.getSelectedCustomer().getName());
                    transactionDialog.setTransactionDialogListener(transactionDialogListener);

                    Invoice invoice = generateInvoice();

                    transactionDialog.setInStock("Transaction Ref No. " + invoice.getReference());
                    transactionDialog.show();

                    if(!isLayaway) {
                        new SwableTools.Transaction(getHelper())
                                .toSend()
                                .forBranch(ProductsAdapterHelper.getSelectedBranch())
                                .fromModule(ConcessioModule.INVOICE)
                                .object(invoice)
                                .queue();
                    } else {
                        invoice.updateTo(getHelper());
                        new SwableTools.Transaction(getHelper())
                                .toSend()
                                .forBranch(ProductsAdapterHelper.getSelectedBranch())
                                .fromModule(ConcessioModule.INVOICE)
                                .layawayOfflineData(offlineData)
                                .queue();
                    }

                    Log.e("INVOICE", invoice.toJSONString());
                }
            }
            else if(v.getId() == R.id.btn2) {
                DialogTools.showConfirmationDialog(C_Checkout.this, "Partial Payment", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO Render layaway invoice

                        TransactionDialog transactionDialog = new TransactionDialog(C_Checkout.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                        transactionDialog.setTitle(ConcessioModule.INVOICE_PARTIAL);
                        transactionDialog.setStatusResource(R.drawable.ic_alert_red);
                        transactionDialog.setAmount("P"+NumberTools.separateInCommas(checkoutFragment.getRemainingBalance()));
                        transactionDialog.setAmountLabel("Remaining Balance");
                        transactionDialog.setCustomerName(ProductsAdapterHelper.getSelectedCustomer().getName());
                        transactionDialog.setTransactionDialogListener(transactionDialogListener);

                        Invoice invoice = generateInvoice();

                        transactionDialog.setInStock("Transaction Ref No. " + invoice.getReference());
                        transactionDialog.show();

                        invoice.setStatus("L");
                        if(!isLayaway) {
                            new SwableTools.Transaction(getHelper())
                                    .toSend()
                                    .forBranch(ProductsAdapterHelper.getSelectedBranch())
                                    .fromModule(ConcessioModule.INVOICE)
                                    .object(invoice)
                                    .queue();
                        } else {
                            invoice.updateTo(getHelper());
                            new SwableTools.Transaction(getHelper())
                                    .toSend()
                                    .forBranch(ProductsAdapterHelper.getSelectedBranch())
                                    .fromModule(ConcessioModule.INVOICE)
                                    .layawayOfflineData(offlineData)
                                    .queue();
                        }

                        Log.e("INVOICE", invoice.toJSONString());
                    }
                }, "No", R.style.AppCompatDialogStyle_Light);
            }

        }
    };

    private TransactionDialog.TransactionDialogListener transactionDialogListener = new TransactionDialog.TransactionDialogListener() {
        @Override
        public void whenDismissed() {
            setResult(SUCCESS);
            finish();
        }
    };

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Checkout");
        this.tbActionBar = toolbar;

        setupNavigationListener(tbActionBar);
    }
}
