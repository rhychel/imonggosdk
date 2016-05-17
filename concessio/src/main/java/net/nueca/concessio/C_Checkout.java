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
import net.nueca.concessioengine.printer.starmicronics.enums.StarIOPaperSize;
import net.nueca.concessioengine.printer.starmicronics.tools.StarIOPrinterTools;
import net.nueca.concessioengine.tools.BluetoothTools;
import net.nueca.concessioengine.tools.InventoryTools;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.imonggosdk.tools.Configurations;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.PointsTools;
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
import java.util.ArrayList;
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
//    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//        @Override
//        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//            if(target != null)
//                Log.e("target", "Not null");
//
//            if(viewHolder != null)
//                Log.e("viewHolder", "Not null");
//            return false;
//        }
//
//        @Override
//        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//            Log.e("onSwiped", "Index=" + viewHolder.getAdapterPosition());
//            Log.e("onSwiped", "Payment Batch No=" + simpleSplitPaymentAdapter.getItem(viewHolder.getAdapterPosition()).getPaymentBatchNo());
//
//            if(simpleSplitPaymentAdapter.getItem(viewHolder.getAdapterPosition()).getPaymentBatchNo() == null) {
//                simpleSplitPaymentAdapter.remove(viewHolder.getAdapterPosition());
//                simpleSplitPaymentAdapter.notifyItemChanged(0);
//            }
//        }
//
//        @Override
//        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//            if(viewHolder instanceof SimpleSplitPaymentAdapter.ListViewHolder) {
//                return ((SimpleSplitPaymentAdapter.ListViewHolder) viewHolder).isLastItem() ? 0 : super.getSwipeDirs(recyclerView, viewHolder);
//            }
//            return super.getSwipeDirs(recyclerView, viewHolder);
//        }
//    };
    private LinearLayout llBalance, llTotalAmount;
    private TextView tvLabelBalance, tvBalance, tvTotalAmount;
    private Button btn1, btn2;
    private SalesPromotion salesPromotion;
    private TransactionDialog.TransactionDialogListener transactionDialogListener = new TransactionDialog.TransactionDialogListener() {
        @Override
        public void whenDismissed() {
            if(getAppSetting().isCan_change_inventory())
                if(!isLayaway)
                    InventoryTools.updateInventoryFromSelectedItemList(getHelper());
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

//                    Double availablePoints = Double.parseDouble(ProductsAdapterHelper.getSelectedCustomer().getAvailable_points());
//                    Double pointsInPeso = 0d;
//
//                    if(salesPromotion != null && salesPromotion.getSettings() != null)
//                        pointsInPeso = PointsTools.pointsToAmount(salesPromotion.getSettings(), availablePoints);

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
                                builder.payment_type_name(paymentType.getName());
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

                    if(isButtonTapped)
                        return;
                    isButtonTapped = true;

                    TransactionDialog transactionDialog = new TransactionDialog(C_Checkout.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                    transactionDialog.setTitle(ConcessioModule.INVOICE);
                    transactionDialog.setAmount("P"+NumberTools.separateInCommas(checkoutFragment.getTotalPaymentMade()));
                    transactionDialog.setAmountLabel("Amount");
                    transactionDialog.setCustomerName(ProductsAdapterHelper.getSelectedCustomer().getName());
                    transactionDialog.setTransactionDialogListener(transactionDialogListener);
                    transactionDialog.setCanceledOnTouchOutside(false);
                    transactionDialog.setCancelable(false);

                    Invoice invoice = generateInvoice();

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

                    customer = ProductsAdapterHelper.getSelectedCustomer();

                    invoice.setStatus("S");
                    if(!isLayaway) {
                        offlineData = new SwableTools.Transaction(getHelper())
                                .toSend()
                                .forBranch(ProductsAdapterHelper.getSelectedBranch())
                                .fromModule(ConcessioModule.INVOICE)
                                .object(invoice)
                                .queue();
                    } else {
                        Branch branch = Branch.fetchById(getHelper(), Branch.class, offlineData.getBranch_id());
                        invoice.setBranch(branch);
                        invoice.updateTo(getHelper());
                        offlineData = new SwableTools.Transaction(getHelper())
                                .toSend()
                                .forBranch(branch)
                                .fromModule(ConcessioModule.INVOICE)
                                .layawayOfflineData(offlineData)
                                .queue();
                    }

                    Double availablePoints = Double.parseDouble(customer.getAvailable_points());
                    Double pointsUsed = 0d;

                    if(salesPromotion != null && salesPromotion.getSettings() != null)
                        pointsUsed = PointsTools.amountToPoints(salesPromotion.getSettings(),
                                //getNewPointsInAmountUsed(invoice, checkoutFragment.getComputation().getPoints())
                                getPointsInAmountUsed());

                    Log.e("C_Checkout", "SEND " + availablePoints + " ~ " + pointsUsed);

                    customer.setAvailable_points(String.valueOf(NumberTools.formatDouble(availablePoints - pointsUsed,
                            ProductsAdapterHelper.getDecimalPlace())));
                    customer.updateTo(getHelper());

                    ProductsAdapterHelper.setSelectedCustomer(customer);

//                    printSimulator(invoice);
                    // Print
                    if(getAppSetting().isCan_print() && getModuleSetting(ConcessioModule.INVOICE).isCan_print()) {
                        if(!EpsonPrinterTools.targetPrinter(C_Checkout.this).equals(""))
                            printTransaction(invoice, "*Salesman Copy*", "*Customer Copy*", "*Office Copy*");
                        if(!StarIOPrinterTools.getTargetPrinter(C_Checkout.this).equals(""))
                            printTransactionStar(invoice, "*Salesman Copy*", "*Customer Copy*", "*Office Copy*");
                    }
                    // Print

                    Log.e("INVOICE ~ Full", invoice.toJSONString());
                }
            }
            else if(v.getId() == R.id.btn2) { /** PARTIAL **/
                if(isButtonTapped)
                    return;
                isButtonTapped = true;

                DialogTools.showConfirmationDialog(C_Checkout.this, "Partial Payment", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO Render layaway invoice
                        if(isButtonTapped)
                            return;
                        isButtonTapped = true;

                        TransactionDialog transactionDialog = new TransactionDialog(C_Checkout.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                        transactionDialog.setTitle(ConcessioModule.INVOICE_PARTIAL);
                        transactionDialog.setStatusResource(R.drawable.ic_alert_red);
                        transactionDialog.setAmount("P" + NumberTools.separateInCommas(checkoutFragment.getRemainingBalance()));
                        transactionDialog.setAmountLabel("Remaining Balance");
                        transactionDialog.setCustomerName(ProductsAdapterHelper.getSelectedCustomer().getName());
                        transactionDialog.setTransactionDialogListener(transactionDialogListener);
                        transactionDialog.setCanceledOnTouchOutside(false);
                        transactionDialog.setCancelable(false);

                        Invoice invoice = generateInvoice();

                        transactionDialog.setInStock("Transaction Ref No. " + invoice.getReference());

                        // Transaction Date
                        SimpleDateFormat fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        fromDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("cccc, MMM. dd, yyyy, h:mma");
                        simpleDateFormat.setTimeZone(TimeZone.getDefault());
                        try {
                            Date date = fromDate.parse(invoice.getInvoice_date().split("T")[0] + " " + invoice.getInvoice_date().split("T")[1].replace("Z", ""));
                            transactionDialog.setDatetime(simpleDateFormat.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        // Transaction Date

                        transactionDialog.show();

                        customer = ProductsAdapterHelper.getSelectedCustomer();
                        invoice.setStatus("L");
                        if (!isLayaway) {
                            offlineData = new SwableTools.Transaction(getHelper())
                                    .toSend()
                                    .forBranch(ProductsAdapterHelper.getSelectedBranch())
                                    .fromModule(ConcessioModule.INVOICE)
                                    .object(invoice)
                                    .queue();
                        } else {
                            Branch branch = Branch.fetchById(getHelper(), Branch.class, offlineData.getBranch_id());
                            invoice.setBranch(branch);
                            invoice.updateTo(getHelper());
                            offlineData = new SwableTools.Transaction(getHelper())
                                    .toSend()
                                    .forBranch(branch)
                                    .fromModule(ConcessioModule.INVOICE)
                                    .layawayOfflineData(offlineData)
                                    .queue();
                        }

                        Double availablePoints = Double.parseDouble(customer.getAvailable_points());
                        Double pointsUsed = 0d;

                        if (salesPromotion != null && salesPromotion.getSettings() != null)
                            pointsUsed = PointsTools.amountToPoints(salesPromotion.getSettings(),
                                    //getNewPointsInAmountUsed(invoice, checkoutFragment.getComputation().getPoints())
                                    getPointsInAmountUsed());

                        Log.e("C_Checkout", "PARTIAL " + availablePoints + " ~ " + pointsUsed);

                        customer.setAvailable_points(String.valueOf(NumberTools.formatDouble(availablePoints - pointsUsed,
                                ProductsAdapterHelper.getDecimalPlace())));
                        customer.updateTo(getHelper());

                        ProductsAdapterHelper.setSelectedCustomer(customer);
                        if (getAppSetting().isCan_print() && getModuleSetting(ConcessioModule.INVOICE).isCan_print()) {
                            if (!EpsonPrinterTools.targetPrinter(C_Checkout.this).equals(""))
                                printTransaction(invoice, "*Salesman Copy*", "*Customer Copy*", "*Office Copy*");
                            if (!StarIOPrinterTools.getTargetPrinter(C_Checkout.this).equals(""))
                                printTransactionStar(invoice, "*Salesman Copy*", "*Customer Copy*", "*Office Copy*");
                        }

                        Gson gson = new Gson();
                        Log.e("INVOICE ~ Partial", gson.toJson(invoice));
                    }
                }, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isButtonTapped = false;
                    }
                }, new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        isButtonTapped = false;
                    }
                }, R.style.AppCompatDialogStyle_Light);
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
            List<PaymentType> paymentTypes = getHelper().fetchIntId(PaymentType.class).queryBuilder().orderBy("id", true)
                    .where()
                        .eq("status", "A").and()
                        .not().like("name", "credit memo").and()
                        .not().like("name", "rs slip")
                    .query();
//            for (PaymentType paymentType : paymentTypes) {
//                Log.e("id", paymentType.getId() + "---" + paymentType.getName());
//            }
            simpleSplitPaymentAdapter = new SimpleSplitPaymentAdapter(this, getHelper(), ListingType
                    .COLORED_PAYMENTS, null, paymentTypes);
            simpleSplitPaymentAdapter.setPaymentUpdateListener(new BaseSplitPaymentAdapter.OnPaymentUpdateListener() {
                @Override
                public void onAddPayment(InvoicePayment invoicePayment) {
                    tvBalance.setText(NumberTools.separateInCommas(checkoutFragment.getRemainingBalance(true)));

                    if(invoicePayment.getPayment_type_name().toLowerCase().equals("rewards")) {
                        availablePoints = availablePoints-PointsTools.amountToPoints(salesPromotion.getSettings(), invoicePayment.getTender());
                        pointsInPeso = pointsInPeso-invoicePayment.getTender();

                        simpleSplitPaymentAdapter.setAvailablePoints(availablePoints);
                        simpleSplitPaymentAdapter.setPointsInPeso(pointsInPeso);
                    }

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
                public void onUpdatePayment(int location, double prevValue, InvoicePayment invoicePayment) {
                    tvBalance.setText(NumberTools.separateInCommas(checkoutFragment.getRemainingBalance(true)));

                    Log.e("Payment", "prevValue="+prevValue + " || newValue="+invoicePayment.getTender());

                    if(invoicePayment.getPayment_type_name().toLowerCase().equals("rewards")) {
                        availablePoints = availablePoints+PointsTools.amountToPoints(salesPromotion.getSettings(), prevValue);
                        pointsInPeso = pointsInPeso+prevValue;

                        availablePoints = availablePoints-PointsTools.amountToPoints(salesPromotion.getSettings(), invoicePayment.getTender());
                        pointsInPeso = pointsInPeso-invoicePayment.getTender();

                        simpleSplitPaymentAdapter.setAvailablePoints(availablePoints);
                        simpleSplitPaymentAdapter.setPointsInPeso(pointsInPeso);
                    }

                    if (simpleSplitPaymentAdapter.isFullyPaid()) {
                        tvLabelBalance.setText("Change");
                        tvBalance.setTextColor(getResources().getColor(R.color.payment_color));
                        btn1.setText("SUBMIT");
                        btn2.setVisibility(View.GONE);
                    } else {
                        tvLabelBalance.setText("Balance");
                        tvBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        btn1.setText("PAY");
                        if(getModuleSetting(ConcessioModule.INVOICE).isHas_partial())
                            btn2.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onDeletePayment(int location, double prevValue) {
                    tvBalance.setText(NumberTools.separateInCommas(checkoutFragment.getRemainingBalance(true)));

                    if(prevValue > -1) {
                        availablePoints = availablePoints+PointsTools.amountToPoints(salesPromotion.getSettings(), prevValue);
                        pointsInPeso = pointsInPeso+prevValue;

                        simpleSplitPaymentAdapter.setAvailablePoints(availablePoints);
                        simpleSplitPaymentAdapter.setPointsInPeso(pointsInPeso);
                    }

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

//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
//        itemTouchHelper.attachToRecyclerView(rvPayments);
//        ((SimpleCheckoutFragment) checkoutFragment).setItemTouchHelper(itemTouchHelper);
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

            availablePoints = Double.parseDouble(ProductsAdapterHelper.getSelectedCustomer().getAvailable_points());
            pointsInPeso = 0d;

            if(salesPromotion != null && salesPromotion.getSettings() != null)
                pointsInPeso = PointsTools.pointsToAmount(salesPromotion.getSettings(), availablePoints);

            simpleSplitPaymentAdapter.setAvailablePoints(availablePoints);
            simpleSplitPaymentAdapter.setPointsInPeso(pointsInPeso);
            simpleSplitPaymentAdapter.setSalesPromotion(salesPromotion);

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

    private void printSimulator(final Invoice invoice) {
        Branch branch = null;
        try {
            branch = getBranches().get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        StringBuilder printText = new StringBuilder();

        try {
            // ---------- HEADER
            printText.append(branch.getName()+"\n");
            printText.append(branch.generateAddress()+"\n\n");

            printText.append("ORDER SLIP\n\n");
            printText.append("Salesman: "+getSession().getUser().getName()+"\n");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
            if(offlineData != null) {
                printText.append("Ref #: "+offlineData.getReference_no()+"\n");
                printText.append("Date: " + simpleDateFormat.format(offlineData.getDateCreated())+"\n");
            }
            else {
                printText.append("Ref #: "+invoice.getReference()+"\n");
                printText.append("Date: " + simpleDateFormat.format(Calendar.getInstance().getTime())+"\n");
            }
            // ---------- HEADER

            double totalQuantity = 0.0;
            printText.append("ORDERS\n");
            printText.append("================================\n");
            printText.append("Quantity                  Amount\n");
            printText.append("================================\n");

            for (InvoiceLine invoiceLine : invoice.getSalesInvoiceLines()) {
                Product product = Product.fetchById(getHelper(), Product.class, invoiceLine.getProduct_id());
                printText.append(product.getName()+"\n");

                if(invoiceLine.getUnit_id() != null) {
                    totalQuantity += invoiceLine.getUnit_quantity();
                    printText.append("  " + invoiceLine.getUnit_quantity() + "   *" + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(invoiceLine.getRetail_price())+"\n");
                    printText.append(NumberTools.separateInCommas(invoiceLine.getSubtotal())+"\n");
                }
                else {
                    totalQuantity += invoiceLine.getQuantity();

                    printText.append("  " + invoiceLine.getQuantity() + "   ~" + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(invoiceLine.getRetail_price())+"\n");
                    printText.append(NumberTools.separateInCommas(invoiceLine.getSubtotal())+"\n");
                }
            }
            printText.append("--------------------------------\n");

            InvoiceTools.PaymentsComputation paymentsComputation = checkoutFragment.getComputation();
            //paymentsComputation.addAllInvoiceLines(invoice.getInvoiceLines());
            //paymentsComputation.addAllPayments(invoice.getPayments());

            printText.append(EpsonPrinterTools.spacer("Total Quantity: ", NumberTools.separateInCommas(totalQuantity), 32)+"\n");
            printText.append(EpsonPrinterTools.spacer("Gross Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayable(false).doubleValue(), 2)), 32)+"\n");

            if(paymentsComputation.getCustomerDiscount().size() > 0) {
                printText.append(EpsonPrinterTools.spacer("LESS Customer Discount: ", invoice.getExtras().getCustomer_discount_text_summary(), 32) + "\n");

                for (Double cusDisc : paymentsComputation.getCustomerDiscount())
                    printText.append("(" + NumberTools.separateInCommas(cusDisc) + ")\n");
            }
//            if(!paymentsComputation.getTotalCompanyDiscount().equals(BigDecimal.ZERO)) {
//                printText.append(EpsonPrinterTools.spacer("LESS Company Discount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalCompanyDiscount().doubleValue(), 2))+")", 32) + "\n");
//            }
//            if(!paymentsComputation.getTotalProductDiscount().equals(BigDecimal.ZERO)) {
//                printText.append(EpsonPrinterTools.spacer("LESS Product Discount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalProductDiscount().doubleValue(), 2))+")", 32) + "\n");
//            }

            printText.append(EpsonPrinterTools.spacer("Net Order Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayableNoReturns(true).doubleValue(), 2)), 32)+"\n\n");

            invoice.getReturnInvoiceLines();
            if(invoice.getBoInvoiceLines().size() > 0) {
                totalQuantity = 0.0;

                printText.append("BAD ORDERS\n");
                printText.append("================================\n");
                printText.append("Quantity                  Amount\n");
                printText.append("================================\n");

                for (InvoiceLine invoiceLine : invoice.getBoInvoiceLines()) {
                    Product product = Product.fetchById(getHelper(), Product.class, invoiceLine.getProduct_id());
                    printText.append(product.getName() + "\n");

                    if (invoiceLine.getUnit_id() != null) {
                        totalQuantity += invoiceLine.getUnit_quantity();
                        printText.append("  " + Math.abs(invoiceLine.getUnit_quantity()) + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\n");
                        printText.append(NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\n");
                        printText.append("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\n");
                    }
                    else {
                        totalQuantity += invoiceLine.getQuantity();
                        printText.append("  " + Math.abs(invoiceLine.getQuantity()) + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\n");
                        printText.append(NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\n");
                        printText.append("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\n");
                    }
                }

                printText.append("--------------------------------\n");
                printText.append(EpsonPrinterTools.spacer("Total Quantity: ", NumberTools.separateInCommas(NumberTools.formatDouble(Math.abs(totalQuantity), 2)), 32)+"\n");
                printText.append(EpsonPrinterTools.spacer("LESS Net BO Amount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(Math.abs(paymentsComputation.getReturnsPayments().get(0).getAmount()),2)), 32)+")\n\n");
            }
            if(invoice.getRgsInvoiceLines().size() > 0) {
                totalQuantity = 0.0;

                printText.append("RGS\n");
                printText.append("================================\n");
                printText.append("Quantity                  Amount\n");
                printText.append("================================\n");

                for (InvoiceLine invoiceLine : invoice.getRgsInvoiceLines()) {
                    Product product = Product.fetchById(getHelper(), Product.class, invoiceLine.getProduct_id());
                    printText.append(product.getName() + "\n");

                    if (invoiceLine.getUnit_id() != null) {
                        totalQuantity += invoiceLine.getUnit_quantity();

                        printText.append("  " + Math.abs(invoiceLine.getUnit_quantity()) + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\n");
                        printText.append(NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\n");
                        printText.append("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\n");
                    }
                    else {
                        totalQuantity += invoiceLine.getQuantity();

                        printText.append("  " + Math.abs(invoiceLine.getQuantity()) + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\n");
                        printText.append(NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\n");
                        printText.append("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\n");
                    }
                }

                printText.append("--------------------------------\n");
                printText.append(EpsonPrinterTools.spacer("Total Quantity: ", NumberTools.separateInCommas(Math.abs(totalQuantity)), 32)+"\n");

                if(paymentsComputation.getReturnsPayments().size() > 2)
                    printText.append(EpsonPrinterTools.spacer("LESS Net RGS Amount: ", "("+NumberTools.separateInCommas(Math.abs(paymentsComputation.getReturnsPayments().get(2).getAmount())), 32)+")\n\n");
                else
                    printText.append(EpsonPrinterTools.spacer("LESS Net RGS Amount: ", "("+NumberTools.separateInCommas(Math.abs(paymentsComputation.getReturnsPayments().get(0).getAmount())), 32)+")\n\n");
            }

            printText.append(EpsonPrinterTools.spacer("Amount Due: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayable(true).doubleValue(), 2)), 32)+"\n\n");
            printText.append("PAYMENTS\n");
            printText.append("================================\n");
            printText.append("Payments                  Amount\n");

            for(InvoicePayment invoicePayment : invoice.getPayments()) {
                PaymentType paymentType = PaymentType.fetchById(getHelper(), PaymentType.class, invoicePayment.getPayment_type_id());
                if(!paymentType.getName().trim().equals("Credit Memo") && !paymentType.getName().trim().equals("RS Slip")) {
                    printText.append(EpsonPrinterTools.spacer(paymentType.getName(), DateTimeTools.convertToDate(invoicePayment.getExtras().getPayment_date(), "yyyy-MM-dd", "MMM dd, yyyy")+"       ", 32) + "\n");
                    printText.append(NumberTools.separateInCommas(invoicePayment.getTender())+"\n");
                }
            }

            printText.append(EpsonPrinterTools.spacer("Paid Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPaymentMade().doubleValue(), 2)), 32)+"\n");
            printText.append("--------------------------------\n");

            if(paymentsComputation.getRemaining().doubleValue() < 0) {
                printText.append(EpsonPrinterTools.spacer("Balance: ", "0.00", 32) + "\n\n");
                printText.append(EpsonPrinterTools.spacer("Change: ", NumberTools.separateInCommas(Math.abs(NumberTools.formatDouble(paymentsComputation.getRemaining().doubleValue(), 2))), 32) + "\n\n");
            }
            else
                printText.append(EpsonPrinterTools.spacer("Balance: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getRemaining().doubleValue(), 2)), 32) + "\n\n");

            SimpleDateFormat nowFormat = new SimpleDateFormat("yyyy-MM-dd");
            printText.append("Available Points("+nowFormat.format(Calendar.getInstance().getTime())+"):\n");
            printText.append(NumberTools.separateInCommas(ProductsAdapterHelper.getSelectedCustomer().getAvailable_points())+"\n");

            printText.append("\n\nCustomer Name: "+ProductsAdapterHelper.getSelectedCustomer().generateFullName()+"\n");
            printText.append("Customer Code: "+ProductsAdapterHelper.getSelectedCustomer().getCode()+"\n");
            printText.append("Address: "+ProductsAdapterHelper.getSelectedCustomer().generateAddress()+"\n");
            printText.append("Signature:______________________\n");

            if(ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms() != null)
                printText.append("Terms: "+(ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms().getName() == null
                        ? "None"
                        : ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms().getName())+"\n\n");
            else
                printText.append("\n");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Log.e("Print", printText.toString()+"<<<");
    }

    private void printTransactionStar(final Invoice invoice, final String... labels) {
        if(!BluetoothTools.isEnabled())
            return;
        if(!StarIOPrinterTools.isPrinterOnline(this, StarIOPrinterTools.getTargetPrinter(this), "portable"))
            return;
        Branch branch = null;
        try {
            branch = getBranches().get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<byte[]> data = new ArrayList<>();

        for(int i = 0;i < labels.length;i++) {
            StringBuilder printText = new StringBuilder();
            try {
                // ---------- HEADER
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)0,
                data.add((branch.getName()+"\r\n").getBytes());
                data.add((branch.generateAddress()+"\r\n\r\n").getBytes());

                data.add(("ORDER SLIP\r\n\r\n").getBytes());
                data.add(("Salesman: "+getSession().getUser().getName()+"\r\n").getBytes());
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                if(offlineData != null) {
                    data.add(("Ref #: "+offlineData.getReference_no()+"\r\n").getBytes());
                    data.add(("Date: " + simpleDateFormat.format(offlineData.getDateCreated())+"\r\n").getBytes());
                }
                else {
                    data.add(("Ref #: "+invoice.getReference()+"\r\n").getBytes());
                    data.add(("Date: " + simpleDateFormat.format(Calendar.getInstance().getTime()) + "\r\n").getBytes());
                }
                // ---------- HEADER

                double totalQuantity = 0.0;
                data.add("ORDERS\r\n".getBytes());
                data.add("================================".getBytes());
                data.add("Quantity                  Amount".getBytes());
                data.add("================================".getBytes());

                int totalInvoiceLines = invoice.getSalesInvoiceLines().size()+invoice.getRgsInvoiceLines().size()+invoice.getBoInvoiceLines().size()+invoice.getPayments().size();

                double numberOfPages = Math.ceil((double)totalInvoiceLines/Configurations.MAX_ITEMS_FOR_PRINTING), items = 0;
                int page = 1;

                for (InvoiceLine invoiceLine : invoice.getSalesInvoiceLines()) {
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                    Product product = Product.fetchById(getHelper(), Product.class, invoiceLine.getProduct_id());
                    data.add((product.getName() + "\r\n").getBytes());
                    if(invoiceLine.getUnit_id() != null) {
                        totalQuantity += invoiceLine.getUnit_quantity();
                        data.add(("  " + invoiceLine.getUnit_quantity() + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(invoiceLine.getRetail_price())+"\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                        data.add((NumberTools.separateInCommas(invoiceLine.getSubtotal())+"\r\n").getBytes());
                    }
                    else {
                        totalQuantity += invoiceLine.getQuantity();
                        data.add(("  " + invoiceLine.getQuantity() + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(invoiceLine.getRetail_price())+"\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                        data.add((NumberTools.separateInCommas(invoiceLine.getSubtotal())+"\r\n").getBytes());
                    }
                    items++;

                    if(numberOfPages > 1.0 && page < (int)numberOfPages && items == Configurations.MAX_ITEMS_FOR_PRINTING) {
                        data.add(("\r\n\r\n\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                        data.add(("*Page "+page+"*\r\n\r\n").getBytes());
                        data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                        page++;
                        items = 0;

                        if(!StarIOPrinterTools.print(this, StarIOPrinterTools.getTargetPrinter(this), "portable", StarIOPaperSize.p2INCH, data))
                            break;
                        data.clear();
                    }
                }
                data.add("--------------------------------".getBytes());
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left

                InvoiceTools.PaymentsComputation paymentsComputation = checkoutFragment.getComputation();
                //paymentsComputation.addAllInvoiceLines(invoice.getInvoiceLines());
                //paymentsComputation.addAllPayments(invoice.getPayments());

                data.add((EpsonPrinterTools.spacer("Total Quantity: ", NumberTools.separateInCommas(totalQuantity), 32)+"\r\n").getBytes());
                data.add((EpsonPrinterTools.spacer("Gross Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayable(false).doubleValue(), 2)), 32)+"\r\n").getBytes());

                if(paymentsComputation.getCustomerDiscount().size() > 0) {
                    data.add((EpsonPrinterTools.spacer("LESS Customer Discount: ", invoice.getExtras().getCustomer_discount_text_summary(), 32) + "\r\n").getBytes());
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                    for (Double cusDisc : paymentsComputation.getCustomerDiscount())
                        data.add(("(" + NumberTools.separateInCommas(cusDisc) + ")\r\n").getBytes());
                }
//                if(!paymentsComputation.getTotalCompanyDiscount().equals(BigDecimal.ZERO)) {
//                    data.add((EpsonPrinterTools.spacer("LESS Company Discount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalCompanyDiscount().doubleValue(), 2))+")", 32) + "\r\n").getBytes());
//                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
//                }
//                if(!paymentsComputation.getTotalProductDiscount().equals(BigDecimal.ZERO)) {
//                    data.add((EpsonPrinterTools.spacer("LESS Product Discount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalProductDiscount().doubleValue(), 2))+")", 32) + "\r\n").getBytes());
//                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
//                }

                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                data.add((EpsonPrinterTools.spacer("Net Order Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayableNoReturns(true).doubleValue(), 2)), 32)+"\r\n\r\n").getBytes());

                invoice.getReturnInvoiceLines();
                if(invoice.getBoInvoiceLines().size() > 0) {
                    totalQuantity = 0.0;
                    data.add("BAD ORDERS\r\n".getBytes());
                    data.add("================================".getBytes());
                    data.add("Quantity                  Amount".getBytes());
                    data.add("================================".getBytes());
                    for (InvoiceLine invoiceLine : invoice.getBoInvoiceLines()) {
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                        Product product = Product.fetchById(getHelper(), Product.class, invoiceLine.getProduct_id());
                        data.add((product.getName() + "\r\n").getBytes());
                        if (invoiceLine.getUnit_id() != null) {
                            totalQuantity += invoiceLine.getUnit_quantity();
                            data.add(("  " + Math.abs(invoiceLine.getUnit_quantity()) + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                            data.add((NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                            data.add(("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\r\n").getBytes());
                        }
                        else {
                            totalQuantity += invoiceLine.getQuantity();
                            data.add(("  " + Math.abs(invoiceLine.getQuantity()) + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                            data.add((NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                            data.add(("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\r\n").getBytes());
                        }

                        items++;

                        if(numberOfPages > 1.0 && page < (int)numberOfPages && items == Configurations.MAX_ITEMS_FOR_PRINTING) {
                            data.add(("\r\n\r\n\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                            data.add(("*Page "+page+"*\r\n\r\n").getBytes());
                            data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                            page++;
                            items = 0;

                            if(!StarIOPrinterTools.print(this, StarIOPrinterTools.getTargetPrinter(this), "portable", StarIOPaperSize.p2INCH, data))
                                break;
                            data.clear();
                        }
                    }
                    data.add("--------------------------------".getBytes());
                    data.add((EpsonPrinterTools.spacer("Total Quantity: ", NumberTools.separateInCommas(NumberTools.formatDouble(Math.abs(totalQuantity), 2)), 32)+"\r\n").getBytes());
                    data.add((EpsonPrinterTools.spacer("LESS Net BO Amount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(Math.abs(paymentsComputation.getReturnsPayments().get(0).getAmount()),2)), 32)+")\r\n\r\n").getBytes());
                }
                if(invoice.getRgsInvoiceLines().size() > 0) {
                    totalQuantity = 0.0;
                    data.add("RGS\r\n".getBytes());
                    data.add("================================".getBytes());
                    data.add("Quantity                  Amount".getBytes());
                    data.add("================================".getBytes());
                    for (InvoiceLine invoiceLine : invoice.getRgsInvoiceLines()) {
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                        Product product = Product.fetchById(getHelper(), Product.class, invoiceLine.getProduct_id());
                        data.add((product.getName() + "\r\n").getBytes());
                        if (invoiceLine.getUnit_id() != null) {
                            totalQuantity += invoiceLine.getUnit_quantity();
                            data.add(("  " + Math.abs(invoiceLine.getUnit_quantity()) + "   " + invoiceLine.getUnit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                            data.add((NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                            data.add(("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\r\n").getBytes());
                        }
                        else {
                            totalQuantity += invoiceLine.getQuantity();
                            data.add(("  " + Math.abs(invoiceLine.getQuantity()) + "   " + product.getBase_unit_name() + " x " + NumberTools.separateInCommas(Math.abs(invoiceLine.getRetail_price())) + "\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                            data.add((NumberTools.separateInCommas(Math.abs(Double.valueOf(invoiceLine.getSubtotal()))) + "\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                            data.add(("Reason: " + invoiceLine.getExtras().getInvoice_purpose_name() + "\r\n").getBytes());
                        }

                        items++;

                        if(numberOfPages > 1.0 && page < (int)numberOfPages && items == Configurations.MAX_ITEMS_FOR_PRINTING) {
                            data.add(("\r\n\r\n\r\n").getBytes());
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                            data.add(("*Page "+page+"*\r\n\r\n").getBytes());
                            data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                            page++;
                            items = 0;

                            if(!StarIOPrinterTools.print(this, StarIOPrinterTools.getTargetPrinter(this), "portable", StarIOPaperSize.p2INCH, data))
                                break;
                            data.clear();
                        }
                    }
                    data.add("--------------------------------".getBytes());
                    data.add((EpsonPrinterTools.spacer("Total Quantity: ", NumberTools.separateInCommas(Math.abs(totalQuantity)), 32)+"\r\n").getBytes());
                    Log.e("InvoicePayment", paymentsComputation.getReturnsPayments().size()+" returns payment");
                    if(paymentsComputation.getReturnsPayments().size() > 2)
                        data.add((EpsonPrinterTools.spacer("LESS Net RGS Amount: ", "("+NumberTools.separateInCommas(Math.abs(paymentsComputation.getReturnsPayments().get(2).getAmount())), 32)+")\r\n\r\n").getBytes());
                    else
                        data.add((EpsonPrinterTools.spacer("LESS Net RGS Amount: ", "("+NumberTools.separateInCommas(Math.abs(paymentsComputation.getReturnsPayments().get(0).getAmount())), 32)+")\r\n\r\n").getBytes());
                }

                data.add((EpsonPrinterTools.spacer("Amount Due: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayable(true).doubleValue(), 2)), 32)+"\r\n\r\n").getBytes());

                data.add("PAYMENTS\r\n".getBytes());
                data.add("================================".getBytes());
                data.add("Payments                  Amount".getBytes());
                for(InvoicePayment invoicePayment : invoice.getPayments()) {
                    PaymentType paymentType = PaymentType.fetchById(getHelper(), PaymentType.class, invoicePayment.getPayment_type_id());
                    if(paymentType != null && !paymentType.getName().trim().equals("Credit Memo") && !paymentType.getName().trim().equals("RS Slip")) {
                        data.add((EpsonPrinterTools.spacer(paymentType.getName(), DateTimeTools.convertToDate(invoicePayment.getExtras().getPayment_date(), "yyyy-MM-dd", "MMM dd, yyyy"), 32) + "\r\n").getBytes());
                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                        data.add((NumberTools.separateInCommas(invoicePayment.getTender()) + "\r\n").getBytes());

                        items++;

                        if (numberOfPages > 1.0 && page < (int) numberOfPages && items == Configurations.MAX_ITEMS_FOR_PRINTING) {
                            data.add(("\r\n\r\n\r\n").getBytes());
                            data.add(new byte[]{0x1b, 0x1d, 0x61, 0x01}); // Center
                            data.add(("*Page " + page + "*\r\n\r\n").getBytes());
                            data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                            page++;
                            items = 0;

                            if (!StarIOPrinterTools.print(this, StarIOPrinterTools.getTargetPrinter(this), "portable", StarIOPaperSize.p2INCH, data))
                                break;
                            data.clear();
                        }
                    }
                }
                data.add((EpsonPrinterTools.spacer("Paid Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPaymentMade().doubleValue(), 2)), 32)+"\r\n").getBytes());
                data.add("--------------------------------".getBytes());
                if(paymentsComputation.getRemaining().doubleValue() < 0) {
                    data.add((EpsonPrinterTools.spacer("Balance: ", "0.00", 32) + "\r\n\r\n").getBytes());
                    data.add((EpsonPrinterTools.spacer("Change: ", NumberTools.separateInCommas(Math.abs(NumberTools.formatDouble(paymentsComputation.getRemaining().doubleValue(), 2))), 32) + "\r\n\r\n").getBytes());
                }
                else
                    data.add((EpsonPrinterTools.spacer("Balance: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getRemaining().doubleValue(), 2)), 32) + "\r\n\r\n").getBytes());
                SimpleDateFormat nowFormat = new SimpleDateFormat("yyyy-MM-dd");
                data.add(("Available Points("+nowFormat.format(Calendar.getInstance().getTime())+"):\r\n").getBytes());
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                data.add((NumberTools.separateInCommas(ProductsAdapterHelper.getSelectedCustomer().getAvailable_points())+"\r\n").getBytes());

                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                data.add(("\r\n\r\nCustomer Name: "+ProductsAdapterHelper.getSelectedCustomer().generateFullName()+"\r\n").getBytes());
                data.add(("Customer Code: "+ProductsAdapterHelper.getSelectedCustomer().getCode()+"\r\n").getBytes());
                data.add(("Address: "+ProductsAdapterHelper.getSelectedCustomer().generateAddress()+"\r\n").getBytes());
                data.add("Signature:______________________\r\n".getBytes());
                if(ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms() != null)
                    data.add(("Terms: "+(ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms().getName() == null
                            ? "None"
                            : ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms().getName())+"\r\n\r\n").getBytes());
                else
                    data.add("\r\n".getBytes());

                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                data.add(labels[i].getBytes());
                if(i < labels.length-1) {
                    data.add("\r\n\r\n\r\n".getBytes());
                    data.add("- - - - - - CUT HERE - - - - - -\r\n\r\n".getBytes());
                }
                else
                    data.add("\r\n\r\n\r\n".getBytes());

                if(!StarIOPrinterTools.print(this, StarIOPrinterTools.getTargetPrinter(this), "portable", StarIOPaperSize.p2INCH, data))
                    break;

                data.clear();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

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
                    Branch branch = null;
                    try {
                        branch = getBranches().get(0);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
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
//                            printer.addText("Salesman: " + getSession().getUser().getName() + "\n");
                            printText.delete(0, printText.length());
                            printer.addTextAlign(Printer.ALIGN_LEFT);
                            printer.addText(EpsonPrinterTools.tabber("Salesman: ", getSession().getUser().getName(), 32) + "\n");

                            // --------------- CHECK THIS LATER
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

                            InvoiceTools.PaymentsComputation paymentsComputation = checkoutFragment.getComputation();
                            //paymentsComputation.addAllInvoiceLines(invoice.getInvoiceLines());
                            //paymentsComputation.addAllPayments(invoice.getPayments());

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
