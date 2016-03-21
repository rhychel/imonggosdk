package net.nueca.concessio;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.google.gson.Gson;

import net.nueca.concessioengine.activities.checkout.CheckoutActivity;
import net.nueca.concessioengine.adapters.SimpleSplitPaymentAdapter;
import net.nueca.concessioengine.adapters.base.BaseSplitPaymentAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.SimplePaymentDialog;
import net.nueca.concessioengine.dialogs.TransactionDialog;
import net.nueca.concessioengine.enums.DialogType;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.fragments.SimpleCheckoutFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.printer.epson.listener.PrintListener;
import net.nueca.concessioengine.printer.epson.tools.EpsonPrinterTools;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.concessioengine.tools.PointsTools;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by rhymart on 12/3/15.
 */
public class C_Checkout extends CheckoutActivity implements SetupActionBar {

    private Toolbar tbActionBar;
    private RecyclerView rvPayments;
    private SimpleSplitPaymentAdapter simpleSplitPaymentAdapter;
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
    private LinearLayout llBalance, llTotalAmount;
    private TextView tvLabelBalance, tvBalance, tvTotalAmount;
    private Button btn1, btn2;
    private SalesPromotion salesPromotion;
    private TransactionDialog.TransactionDialogListener transactionDialogListener = new TransactionDialog.TransactionDialogListener() {
        @Override
        public void whenDismissed() {
            if(getAppSetting().isCan_change_inventory())
                updateInventoryFromSelectedItemList(false);
            Intent intent = new Intent();
            intent.putExtra(FOR_HISTORY_DETAIL, offlineData.getId());
            setResult(SUCCESS, intent);
            finish();
        }
    };
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn1) {
                if (btn1.getText().toString().equals("PAY")) {
                    SimplePaymentDialog dialog = new SimplePaymentDialog(C_Checkout.this, simpleSplitPaymentAdapter.getPaymentTypeList(), R.style.AppCompatDialogStyle_Light_NoTitle);
                    dialog.setDialogType(DialogType.ADVANCED_PAY);

                    Double availablePoints = Double.parseDouble(ProductsAdapterHelper.getSelectedCustomer().getAvailable_points());
                    Double pointsInPeso = 0d;

                    if(salesPromotion != null && salesPromotion.getSettings() != null)
                        pointsInPeso = PointsTools.pointsToAmount(salesPromotion.getSettings(), availablePoints);

                    dialog.setAvailablePoints(availablePoints);
                    dialog.setPointsInPesoText(pointsInPeso);

                    dialog.setFragmentManager(getFragmentManager());
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

                            simpleSplitPaymentAdapter.addPayment(invoicePayment);

                            simpleSplitPaymentAdapter.setBalance(checkoutFragment.getRemainingBalance());
                            //simpleSplitPaymentAdapter.add(invoicePayment);
                            //simpleSplitPaymentAdapter.notifyItemInserted(simpleSplitPaymentAdapter.getItemCount());
                        }
                    });
                    dialog.show();
                }
                else { /** SEND **/
                    TransactionDialog transactionDialog = new TransactionDialog(C_Checkout.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                    transactionDialog.setTitle(ConcessioModule.INVOICE);
                    transactionDialog.setAmount("P"+NumberTools.separateInCommas(checkoutFragment.getTotalPaymentMade()));
                    transactionDialog.setAmountLabel("Amount");
                    transactionDialog.setCustomerName(ProductsAdapterHelper.getSelectedCustomer().getName());
                    transactionDialog.setTransactionDialogListener(transactionDialogListener);
                    transactionDialog.setCanceledOnTouchOutside(false);
                    transactionDialog.setCancelable(false);

                    Invoice invoice = generateInvoice();

                    // Print
                    if(getAppSetting().isCan_print())
                        printTransaction(invoice, "*Salesman Copy*", "*Customer Copy*", "*Office Copy*");
                    // Print

                    transactionDialog.setInStock("Transaction Ref No. " + invoice.getReference());

                    // Transaction Date
                    SimpleDateFormat fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    fromDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("cccc, MMM. dd, yyyy, h:mma");
                    simpleDateFormat.setTimeZone(TimeZone.getDefault());
                    try {
                        Date date = fromDate.parse(invoice.getInvoice_date().split("T")[0]+" "+invoice.getInvoice_date().split("T")[1].replace("Z", ""));
                        transactionDialog.setDatetime(simpleDateFormat.format(date));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // Transaction Date

                    transactionDialog.show();

//                    invoice.setStatus("S"); TODO Status
                    if(!isLayaway) {
                        offlineData = new SwableTools.Transaction(getHelper())
                                .toSend()
                                .forBranch(ProductsAdapterHelper.getSelectedBranch())
                                .fromModule(ConcessioModule.INVOICE)
                                .object(invoice)
                                .queue();
                    } else {
                        Branch branch = Branch.fetchById(getHelper(), Branch.class, offlineData.getBranch_id());
                        invoice.updateTo(getHelper());
                        offlineData = new SwableTools.Transaction(getHelper())
                                .toSend()
                                .forBranch(branch)
                                .fromModule(ConcessioModule.INVOICE)
                                .layawayOfflineData(offlineData)
                                .queue();
                    }

                    customer = ProductsAdapterHelper.getSelectedCustomer();
                    Double availablePoints = Double.parseDouble(customer.getAvailable_points());
                    Double pointsUsed = 0d;

                    if(salesPromotion != null && salesPromotion.getSettings() != null)
                        pointsUsed = PointsTools.amountToPoints(salesPromotion.getSettings(),
                                getNewPointsInAmountUsed(invoice, checkoutFragment.getComputation().getPoints()));

                    Log.e("C_Checkout", "SEND " + availablePoints + " ~ " + pointsUsed);

                    customer.setAvailable_points(String.valueOf(NumberTools.formatDouble(availablePoints - pointsUsed,
                            ProductsAdapterHelper.getDecimalPlace())));
                    customer.updateTo(getHelper());

                    Log.e("INVOICE", invoice.toJSONString());
                }
            }
            else if(v.getId() == R.id.btn2) { /** PARTIAL **/
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
                        transactionDialog.setCanceledOnTouchOutside(false);
                        transactionDialog.setCancelable(false);

                        Invoice invoice = generateInvoice();

                        if(getAppSetting().isCan_print())
                            printTransaction(invoice, "*Salesman Copy*", "*Customer Copy*", "*Office Copy*");

                        transactionDialog.setInStock("Transaction Ref No. " + invoice.getReference());

                        // Transaction Date
                        SimpleDateFormat fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        fromDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("cccc, MMM. dd, yyyy, h:mma");
                        simpleDateFormat.setTimeZone(TimeZone.getDefault());
                        try {
                            Date date = fromDate.parse(invoice.getInvoice_date().split("T")[0]+" "+invoice.getInvoice_date().split("T")[1].replace("Z", ""));
                            transactionDialog.setDatetime(simpleDateFormat.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        // Transaction Date

                        transactionDialog.show();

                        invoice.setStatus("L");
                        if(!isLayaway) {
                            offlineData = new SwableTools.Transaction(getHelper())
                                    .toSend()
                                    .forBranch(ProductsAdapterHelper.getSelectedBranch())
                                    .fromModule(ConcessioModule.INVOICE)
                                    .object(invoice)
                                    .queue();
                        } else {
                            Branch branch = Branch.fetchById(getHelper(), Branch.class, offlineData.getBranch_id());
                            invoice.updateTo(getHelper());
                            offlineData = new SwableTools.Transaction(getHelper())
                                    .toSend()
                                    .forBranch(branch)
                                    .fromModule(ConcessioModule.INVOICE)
                                    .layawayOfflineData(offlineData)
                                    .queue();
                        }

                        customer = ProductsAdapterHelper.getSelectedCustomer();
                        Double availablePoints = Double.parseDouble(customer.getAvailable_points());
                        Double pointsUsed = 0d;

                        if(salesPromotion != null && salesPromotion.getSettings() != null)
                            pointsUsed = PointsTools.amountToPoints(salesPromotion.getSettings(),
                                    getNewPointsInAmountUsed(invoice, checkoutFragment.getComputation().getPoints()));

                        Log.e("C_Checkout", "PARTIAL " + availablePoints + " ~ " + pointsUsed);

                        customer.setAvailable_points(String.valueOf(NumberTools.formatDouble(availablePoints - pointsUsed,
                                ProductsAdapterHelper.getDecimalPlace())));
                        customer.updateTo(getHelper());

                        Gson gson = new Gson();
                        Log.e("INVOICE", gson.toJson(invoice));
                    }
                }, "No", R.style.AppCompatDialogStyle_Light);
            }

        }
    };

    @Override
    protected void initializeFragment() {
        checkoutFragment = new SimpleCheckoutFragment();
        checkoutFragment.setSetupActionBar(this);
        checkoutFragment.setLayaway(isLayaway);
        checkoutFragment.setHelper(getHelper());
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
            for (PaymentType paymentType : paymentTypes) {
                Log.e("id", paymentType.getId() + "---" + paymentType.getName());
            }
            simpleSplitPaymentAdapter = new SimpleSplitPaymentAdapter(this, getHelper(), ListingType
                    .COLORED_PAYMENTS, null, paymentTypes);
            simpleSplitPaymentAdapter.setPaymentUpdateListener(new BaseSplitPaymentAdapter.OnPaymentUpdateListener() {
                @Override
                public void onAddPayment(InvoicePayment invoicePayment) {
                    tvBalance.setText(NumberTools.separateInCommas(checkoutFragment.getRemainingBalance(true)));

                    if (simpleSplitPaymentAdapter.isFullyPaid()) {
                        tvLabelBalance.setText("Change");
                        tvBalance.setTextColor(getResources().getColor(R.color.payment_color));
                        btn1.setText("SUBMIT");
                        btn2.setVisibility(View.GONE);
                    } else {
                        tvLabelBalance.setText("Balance");
                        tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        if(getModuleSetting(ConcessioModule.INVOICE).isHas_partial())
                            btn2.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onUpdatePayment(int location, InvoicePayment invoicePayment) {
                    tvBalance.setText(NumberTools.separateInCommas(checkoutFragment.getRemainingBalance(true)));

                    if (simpleSplitPaymentAdapter.isFullyPaid()) {
                        tvLabelBalance.setText("Change");
                        tvBalance.setTextColor(getResources().getColor(R.color.payment_color));
                        btn1.setText("SUBMIT");
                        btn2.setVisibility(View.GONE);
                    } else {
                        tvLabelBalance.setText("Balance");
                        tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        if(getModuleSetting(ConcessioModule.INVOICE).isHas_partial())
                            btn2.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onDeletePayment(int location) {
                    tvBalance.setText(NumberTools.separateInCommas(checkoutFragment.getRemainingBalance(true)));

                    if (!simpleSplitPaymentAdapter.isFullyPaid()) {
                        tvLabelBalance.setText("Balance");
                        tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        btn1.setText("PAY");
                        if(getModuleSetting(ConcessioModule.INVOICE).isHas_partial())
                            btn2.setVisibility(simpleSplitPaymentAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ((SimpleCheckoutFragment) checkoutFragment).setSplitPaymentAdapter(simpleSplitPaymentAdapter);
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

        try {
            salesPromotion = PointsTools.getPointSalesPromotion(getHelper());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Checkout");
        this.tbActionBar = toolbar;

        setupNavigationListener(tbActionBar);
    }

    private void printTransaction(final Invoice invoice, final String... labels) {
        String targetPrinter = EpsonPrinterTools.targetPrinter(getApplicationContext());
        if(targetPrinter != null) {

            EpsonPrinterTools.print(targetPrinter, new PrintListener() {

                @Override
                public Printer initializePrinter() {
                    try {
                        return new Printer(Printer.TM_T20, Printer.MODEL_ANK, getApplicationContext());
                    } catch (Epos2Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public Printer onBuildPrintData(Printer printer) {
                    Branch branch = getBranches().get(0);
                    for(int i = 0;i < labels.length;i++) {
                        StringBuilder printText = new StringBuilder();
                        try {
                            // ---------- HEADER
                            printer.addTextFont(Printer.FONT_A);
                            printText.append(branch.getName());
                            printText.append("\n");
                            printText.append(branch.generateAddress());
                            printText.append("\n");
                            printer.addTextAlign(Printer.ALIGN_CENTER);
                            printer.addFeedLine(1);
                            printer.addText(printText.toString());
                            printer.addFeedLine(2);
                            printer.addText("ORDER SLIP");
                            printer.addFeedLine(2);
                            printer.addText("Salesman: " + getSession().getUser().getName() + "\n");
                            printText.delete(0, printText.length());
                            printer.addTextAlign(Printer.ALIGN_LEFT);
                            printer.addText("Ref #: " + invoice.getReference() + "\n");
                            String invoiceDate = invoice.getInvoice_date();
                            SimpleDateFormat fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            fromDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                            //2016-02-22T09:58:24Z
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                            simpleDateFormat.setTimeZone(TimeZone.getDefault());
                            try {
                                Date date = fromDate.parse(invoiceDate.split("T")[0]+" "+invoiceDate.split("T")[1].replace("Z", ""));
                                printer.addText("Date: " + simpleDateFormat.format(date) + "\n\n");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            // ---------- HEADER

                            double totalQuantity = 0.0;
                            printer.addText("ORDERS\n");
                            printer.addText("================================");
                            printer.addText("Quantity                  Amount");
                            printer.addText("================================");
                            for (InvoiceLine invoiceLine : invoice.getSalesInvoiceLines()) {
                                printer.addTextAlign(Printer.ALIGN_LEFT);
                                Product product = Product.fetchById(getHelper(), Product.class, invoiceLine.getProduct_id());
                                printer.addText(product.getName() + "\n");
                                if(invoiceLine.getUnit_id() != null) {
                                    totalQuantity += invoiceLine.getUnit_quantity();
                                    printer.addText("  " + invoiceLine.getUnit_quantity() + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(invoiceLine.getRetail_price())+"\n");
                                    printer.addTextAlign(Printer.ALIGN_RIGHT);
                                    printer.addText(NumberTools.separateInCommas(invoiceLine.getSubtotal())+"\n");
                                }
                                else {
                                    totalQuantity += invoiceLine.getQuantity();
                                    printer.addText("  " + invoiceLine.getQuantity() + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(invoiceLine.getRetail_price())+"\n");
                                    printer.addTextAlign(Printer.ALIGN_RIGHT);
                                    printer.addText(NumberTools.separateInCommas(invoiceLine.getSubtotal())+"\n");
                                }
                            }
                            printer.addText("--------------------------------");
                            printer.addTextAlign(Printer.ALIGN_LEFT);

                            InvoiceTools.PaymentsComputation paymentsComputation = new InvoiceTools.PaymentsComputation();
                            paymentsComputation.addAllInvoiceLines(invoice.getInvoiceLines());
                            paymentsComputation.addAllPayments(invoice.getPayments());

                            printer.addText(EpsonPrinterTools.spacer("Total Quantity: ", NumberTools.separateInCommas(totalQuantity), 32)+"\n");
                            printer.addText(EpsonPrinterTools.spacer("Gross Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayableNoDiscount().doubleValue(), 2)), 32)+"\n");
//                            printer.addText(spacer("Gross Amount: ", invoice.getExtras().getTotal_selling_price(), 32)+"\n");

                            if(invoice.getExtras().getCustomer_discount_text_summary() != null) {
                                printer.addText(EpsonPrinterTools.spacer("LESS Customer Discount: ", invoice.getExtras().getCustomer_discount_text_summary(), 32) + "\n");
                                printer.addTextAlign(Printer.ALIGN_RIGHT);
                                for (Double cusDisc : paymentsComputation.getCustomerDiscount())
                                    printer.addText("(" + NumberTools.separateInCommas(cusDisc) + ")\n");
                            }
                            if(invoice.getExtras().getTotal_company_discount() != null) {
                                printer.addText(EpsonPrinterTools.spacer("LESS Company Discount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalCompanyDiscount().doubleValue(), 2))+")", 32) + "\n");
                                printer.addTextAlign(Printer.ALIGN_RIGHT);
                            }
                            if(paymentsComputation.getTotalProductDiscount() != BigDecimal.ZERO) {
                                printer.addText(EpsonPrinterTools.spacer("LESS Product Discount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalProductDiscount().doubleValue(), 2))+")", 32) + "\n");
                                printer.addTextAlign(Printer.ALIGN_RIGHT);
                            }

                            printer.addTextAlign(Printer.ALIGN_LEFT);
                            printer.addText(EpsonPrinterTools.spacer("Net Order Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayableNoReturns(true).doubleValue(), 2)), 32)+"\n\n");

                            invoice.getReturnInvoiceLines();
                            if(invoice.getBoInvoiceLines().size() > 0) {
                                totalQuantity = 0.0;
                                printer.addText("BAD ORDERS\n");
                                printer.addText("================================");
                                printer.addText("Quantity                  Amount");
                                printer.addText("================================");
                                for (InvoiceLine invoiceLine : invoice.getBoInvoiceLines()) {
                                    printer.addTextAlign(Printer.ALIGN_LEFT);
                                    Product product = Product.fetchById(getHelper(), Product.class, invoiceLine.getProduct_id());
                                    printer.addText(product.getName() + "\n");
                                    if (invoiceLine.getUnit_id() != null) {
                                        totalQuantity += invoiceLine.getUnit_quantity();
                                        printer.addText("  " + Math.abs(invoiceLine.getUnit_quantity()) + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\n");
                                        printer.addTextAlign(Printer.ALIGN_RIGHT);
                                        printer.addText(NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\n");
                                        printer.addTextAlign(Printer.ALIGN_LEFT);
                                        printer.addText("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\n");
                                    }
                                    else {
                                        totalQuantity += invoiceLine.getQuantity();
                                        printer.addText("  " + Math.abs(invoiceLine.getQuantity()) + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\n");
                                        printer.addTextAlign(Printer.ALIGN_RIGHT);
                                        printer.addText(NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\n");
                                        printer.addTextAlign(Printer.ALIGN_LEFT);
                                        printer.addText("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\n");
                                    }
                                }
                                printer.addText("--------------------------------");
                                printer.addText(EpsonPrinterTools.spacer("Total Quantity: ", NumberTools.separateInCommas(NumberTools.formatDouble(Math.abs(totalQuantity), 2)), 32)+"\n");
                                printer.addText(EpsonPrinterTools.spacer("Net BO Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(Math.abs(paymentsComputation.getReturnsPayments().get(0).getAmount()),2)), 32)+"\n\n");
                            }
                            if(invoice.getRgsInvoiceLines().size() > 0) {
                                totalQuantity = 0.0;
                                printer.addText("RGS\n");
                                printer.addText("================================");
                                printer.addText("Quantity                  Amount");
                                printer.addText("================================");
                                for (InvoiceLine invoiceLine : invoice.getRgsInvoiceLines()) {
                                    printer.addTextAlign(Printer.ALIGN_LEFT);
                                    Product product = Product.fetchById(getHelper(), Product.class, invoiceLine.getProduct_id());
                                    printer.addText(product.getName() + "\n");
                                    if (invoiceLine.getUnit_id() != null) {
                                        totalQuantity += invoiceLine.getUnit_quantity();
                                        printer.addText("  " + Math.abs(invoiceLine.getUnit_quantity()) + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\n");
                                        printer.addTextAlign(Printer.ALIGN_RIGHT);
                                        printer.addText(NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\n");
                                        printer.addTextAlign(Printer.ALIGN_LEFT);
                                        printer.addText("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\n");
                                    }
                                    else {
                                        totalQuantity += invoiceLine.getQuantity();
                                        printer.addText("  " + Math.abs(invoiceLine.getQuantity()) + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\n");
                                        printer.addTextAlign(Printer.ALIGN_RIGHT);
                                        printer.addText(NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\n");
                                        printer.addTextAlign(Printer.ALIGN_LEFT);
                                        printer.addText("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\n");
                                    }
                                }
                                printer.addText("--------------------------------");
                                printer.addText(EpsonPrinterTools.spacer("Total Quantity: ", NumberTools.separateInCommas(Math.abs(totalQuantity)), 32)+"\n");
                                if(paymentsComputation.getReturnsPayments().size() > 1)
                                    printer.addText(EpsonPrinterTools.spacer("Net RGS Amount: ", NumberTools.separateInCommas(Math.abs(paymentsComputation.getReturnsPayments().get(1).getAmount())), 32)+"\n\n");
                                else
                                    printer.addText(EpsonPrinterTools.spacer("Net RGS Amount: ", NumberTools.separateInCommas(Math.abs(paymentsComputation.getReturnsPayments().get(0).getAmount())), 32)+"\n\n");
                            }

                            printer.addText(EpsonPrinterTools.spacer("Amount Due: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayable(true).doubleValue(), 2)), 32)+"\n\n");

                            printer.addText("PAYMENTS\n");
                            printer.addText("================================");
                            printer.addText("Payments                  Amount");
                            for(InvoicePayment invoicePayment : invoice.getPayments()) {
                                PaymentType paymentType = PaymentType.fetchById(getHelper(), PaymentType.class, invoicePayment.getPayment_type_id());
                                printer.addText(EpsonPrinterTools.spacer(paymentType.getName(), NumberTools.separateInCommas(invoicePayment.getTender()), 32)+"\n");
                            }
                            printer.addText(EpsonPrinterTools.spacer("Paid Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPaymentMade().doubleValue(), 2)), 32)+"\n");
                            printer.addText("--------------------------------");
                            if(paymentsComputation.getRemaining().doubleValue() < 0) {
                                printer.addText(EpsonPrinterTools.spacer("Balance: ", "0.00", 32) + "\n\n");
                                printer.addText(EpsonPrinterTools.spacer("Change: ", NumberTools.separateInCommas(Math.abs(NumberTools.formatDouble(paymentsComputation.getRemaining().doubleValue(), 2))), 32) + "\n\n");
                            }
                            else
                                printer.addText(EpsonPrinterTools.spacer("Balance: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getRemaining().doubleValue(), 2)), 32) + "\n\n");
                            SimpleDateFormat nowFormat = new SimpleDateFormat("yyyy-MM-dd");
                            printer.addText("Available Points("+nowFormat.format(Calendar.getInstance().getTime())+"):\n");
                            printer.addTextAlign(Printer.ALIGN_RIGHT);
                            printer.addText(NumberTools.separateInCommas(ProductsAdapterHelper.getSelectedCustomer().getAvailable_points())+"\n");

                            printer.addTextAlign(Printer.ALIGN_LEFT);
                            printer.addText("\n\nCustomer Name: "+ProductsAdapterHelper.getSelectedCustomer().generateFullName()+"\n");
                            printer.addText("Customer Code: "+ProductsAdapterHelper.getSelectedCustomer().getCode()+"\n");
                            printer.addText("Address: "+ProductsAdapterHelper.getSelectedCustomer().generateAddress()+"\n");
                            printer.addText("Signature:______________________\n");
                            if(ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms() != null)
                                printer.addText("Terms: "+ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms().getName()+"\n\n");
                            else
                                printer.addText("\n");

                            printer.addTextAlign(Printer.ALIGN_CENTER);
                            printer.addText(labels[i]);
                            if(i < labels.length-1) {
                                printer.addFeedLine(3);
                                printer.addText("- - - - - - CUT HERE - - - - - -\n");
                            }
                            else
                                printer.addFeedLine(5);

                        } catch (Epos2Exception | SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    return printer;
                }

                @Override
                public void onPrintSuccess() {
                    Log.e("Printer", "onPrintSuccess");
                }

                @Override
                public void onPrinterWarning(String message) {

                }

                @Override
                public void onPrinterReceive(Printer printerObj, int code, PrinterStatusInfo status, String printJobId) {

                }

                @Override
                public void onPrintError(String message) {

                }
            }, getApplicationContext());
        }
    }
}
