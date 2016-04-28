package net.nueca.concessio;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.google.gson.Gson;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.BaseProductsFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.printer.epson.listener.PrintListener;
import net.nueca.concessioengine.printer.epson.tools.EpsonPrinterTools;
import net.nueca.concessioengine.printer.starmicronics.enums.StarIOPaperSize;
import net.nueca.concessioengine.printer.starmicronics.tools.StarIOPrinterTools;
import net.nueca.concessioengine.tools.BluetoothTools;
import net.nueca.concessioengine.tools.DiscountTools;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.Configurations;
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
 * Created by rhymart on 8/22/15.
 * imonggosdk2 (c)2015
 */
public class C_Finalize extends ModuleActivity {

    private Toolbar tbActionBar;
    private TabLayout tlTotal;
    private ViewPager vpReview;

    private TextView tvItems;
    private Button btn1, btn2; // CHECKOUT
    private LinearLayout llTotalAmount, llReview, llBalance;

    private TextView tvBalance, tvTotalAmount;
    private View viewStub;

    private ReviewAdapter reviewAdapter;
    private Invoice offlineInvoice;
    private OfflineData offlineData;
    private InvoiceTools.PaymentsComputation offlinePaymentsComputation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_review_activity);

        Log.e("C_Finalize >>> CUSTOMER", customer.getName());

        clearTransactions = false;

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        tlTotal = (TabLayout) findViewById(R.id.tlTotal);
        vpReview = (ViewPager) findViewById(R.id.vpReview);
        tvItems = (TextView) findViewById(R.id.tvItems);
        btn1 = (Button) findViewById(R.id.btn1);
        llBalance = (LinearLayout) findViewById(R.id.llBalance);
        llReview = (LinearLayout) findViewById(R.id.llReview);
        tvBalance = (TextView) findViewById(R.id.tvBalance);
        viewStub = findViewById(R.id.viewStub);

        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btn1.setText("CHECKOUT");

        llBalance.setVisibility(View.VISIBLE);

        if(isForHistoryDetail && !isLayaway) {
            getSupportActionBar().setTitle(getIntent().getStringExtra(REFERENCE));
            try {
                offlineData = getHelper().fetchObjectsInt(OfflineData.class).queryBuilder()
                        .where().eq("reference_no", getIntent().getStringExtra(REFERENCE)).queryForFirst();
                historyDetailsListener = new HistoryDetailsListener() {
                    @Override
                    public void onVoidTransaction() {
                        // TODO for double checking..

                        new SwableTools.Transaction(getHelper())
                                .toCancel()
                                .withReason("VOID")
                                .object(offlineData)
                                .queue();

                        revertInventoryFromInvoice();

                        onBackPressed();
                    }

                    @Override
                    public void onDuplicateTransaction() {
                        // TODO Implement your duplication!
                        // TODO for double checking..
                        //DialogTools.showDialog(C_Finalize.this, "Ooops!", "Under construction :)", R.style.AppCompatDialogStyle_Light_NoTitle);
                        //return;
//                        ProductsAdapterHelper.clearSelectedReturnProductItemList(); -- REMOVED BEFORE 1.2.7-BETA

                        ProductsAdapterHelper.isDuplicating = true;
                        Intent intent = new Intent(C_Finalize.this, C_Module.class);
                        intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, ProductsAdapterHelper.getSelectedCustomer().getId());
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, offlineData.getConcessioModule().ordinal());
                        startActivityForResult(intent, IS_DUPLICATING);
                    }
                };

                if(offlineData == null) {
                    DialogTools.showDialog(this, "Ooops!", "This data is not found in your local database.", "Go to History", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }, R.style.AppCompatDialogStyle_Light_NoTitle);
                    return;
                }
                else if(!offlineData.isSynced() && !offlineData.isSyncing() && !offlineData.getOfflineDataTransactionType().isVoiding()) {
                    btn2 = (Button) findViewById(R.id.btn2);
                    initializeVoidButton(btn1, getIntent().getStringExtra(REFERENCE));
                    initializeDuplicateButton(btn2, getIntent().getStringExtra(REFERENCE));
                }
                else
                    initializeDuplicateButton(btn1, getIntent().getStringExtra(REFERENCE));

                offlineInvoice = offlineData.getObjectFromData(Invoice.class);
                offlinePaymentsComputation = new InvoiceTools.PaymentsComputation();

                offlinePaymentsComputation.addAllInvoiceLines(offlineInvoice.getInvoiceLines());
                offlinePaymentsComputation.addAllPayments(offlineInvoice.getPayments());

                Gson gson = new Gson();
                Log.e("C_Finalize", "HISTORY " + gson.toJson(offlinePaymentsComputation.getPayments()));

                //paymentsComputation.getTotalPayable(); // Total Amount
                //paymentsComputation.getRemaining(); // Total Balance

                llTotalAmount = (LinearLayout) findViewById(R.id.llTotalAmount);
                tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);

                llTotalAmount.setVisibility(View.VISIBLE);
                tvTotalAmount.setText("P"+ NumberTools.separateInCommas(offlinePaymentsComputation.getTotalPayable(true)));

                if(offlinePaymentsComputation.getRemaining().doubleValue() == 0)
                    llBalance.setVisibility(View.GONE);
                //Log.e("C_Finalize", "onCreate : BALANCE: " + paymentsComputation.getRemaining());
                if(offlinePaymentsComputation.getRemaining().doubleValue() > 0)
                    tvBalance.setText("P"+ NumberTools.separateInCommas(offlinePaymentsComputation.getRemaining()));
                else
                    tvBalance.setText("P0.00");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else if(!isForHistoryDetail && isLayaway) {
            getSupportActionBar().setTitle(getIntent().getStringExtra(REFERENCE));
            try {
                final OfflineData offlineData = getHelper().fetchObjectsInt(OfflineData.class).queryBuilder()
                        .where().eq("reference_no", getIntent().getStringExtra(REFERENCE)).queryForFirst();
                if(offlineData == null) {
                    DialogTools.showDialog(this, "Ooops!", "This data is not found in your local database.", "Go to History", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }, R.style.AppCompatDialogStyle_Light_NoTitle);
                    return;
                }
                else if(!offlineData.isSynced() && !offlineData.isSyncing() && !isLayaway) {
                    btn2 = (Button) findViewById(R.id.btn2);
                    initializeVoidButton(btn1, getIntent().getStringExtra(REFERENCE));
                    initializeDuplicateButton(btn2, getIntent().getStringExtra(REFERENCE));
                }

                InvoiceTools.PaymentsComputation paymentsComputation = new InvoiceTools
                        .PaymentsComputation();

                Invoice invoice = offlineData.getObjectFromData(Invoice.class);
                /*int device_id = getSession().getDevice_id();
                if(Integer.parseInt(invoice.getReference().substring(0,invoice.getReference().indexOf('-'))) != device_id) {
                    List<InvoiceLine> invoiceLines = InvoiceTools.generateInvoiceLines(InvoiceTools.generateSelectedProductItemList(getHelper(),
                            offlineData, false, false));
                    invoiceLines.addAll(InvoiceTools.generateInvoiceLines(InvoiceTools.generateSelectedProductItemList(getHelper(),
                            offlineData, true, false)));
                    invoice.setInvoiceLines(invoiceLines);
                }*/

                paymentsComputation.addAllInvoiceLines(invoice.getInvoiceLines());
                paymentsComputation.addAllPayments(invoice.getPayments());

                //paymentsComputation.getTotalPayable(); // Total Amount
                //paymentsComputation.getRemaining(); // Total Balance

                llTotalAmount = (LinearLayout) findViewById(R.id.llTotalAmount);
                tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);

                llTotalAmount.setVisibility(View.VISIBLE);
                tvTotalAmount.setText("P"+ NumberTools.separateInCommas(paymentsComputation.getTotalPayable(true)));

                if(paymentsComputation.getRemaining().doubleValue() == 0)
                    llBalance.setVisibility(View.GONE);
                //Log.e("C_Finalize", "onCreate : BALANCE: " + paymentsComputation.getRemaining());
                if(paymentsComputation.getRemaining().doubleValue() > 0)
                    tvBalance.setText("P"+ NumberTools.separateInCommas(paymentsComputation.getRemaining()));
                else
                    tvBalance.setText("P0.00");

            } catch (SQLException e) {
                e.printStackTrace();
            }
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(C_Finalize.this, C_Checkout.class);
                    intent.putExtra(REFERENCE, reference);
                    intent.putExtra(IS_LAYAWAY, isLayaway);
                    startActivityForResult(intent, SALES);
                }
            });
        }
        else {
            getSupportActionBar().setTitle("Review");
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((Double)tvBalance.getTag()) < 0) {
                        DialogTools.showDialog(C_Finalize.this, "Oopss!", "Total return amount cannot be greater than to your total sales amount.", R.style.AppCompatDialogStyle_Light);
                        return;
                    }

                    Intent intent = new Intent(C_Finalize.this, C_Checkout.class);
                    intent.putExtra(REFERENCE, reference);
                    intent.putExtra(IS_LAYAWAY, isLayaway);
                    startActivityForResult(intent, SALES);
                }
            });
        }

        viewStub.setVisibility(View.VISIBLE);
        tvItems.setVisibility(View.VISIBLE);

        toggleNext(llReview, tvItems);

        reviewAdapter = new ReviewAdapter(getSupportFragmentManager());
        vpReview.setAdapter(reviewAdapter);

        tlTotal.setupWithViewPager(vpReview);

        tbActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isForHistoryDetail && !isLayaway) {
            Double balance = getBalance();
            tvBalance.setText("P" + NumberTools.separateInCommas(balance));
            tvBalance.setTag(balance);
        }
    }

    private Double getBalance() {
        /*Double sales = DiscountTools.applyMultipleDiscounts(
                new BigDecimal(ProductsAdapterHelper.getSelectedProductItems().getSubtotal()), BigDecimal.ONE,
                ProductsAdapterHelper.getSelectedCustomer() == null ? null :
                        ProductsAdapterHelper.getSelectedCustomer().getDiscount_text(), ",").doubleValue();
        Double balance =
                sales + ProductsAdapterHelper.getSelectedReturnProductItems().getSubtotal();*/

        InvoiceTools.PaymentsComputation paymentsComputation = new InvoiceTools
                .PaymentsComputation();

        paymentsComputation.addAllInvoiceLines(InvoiceTools.generateInvoiceLines(ProductsAdapterHelper.getSelectedProductItems()));
        paymentsComputation.addAllInvoiceLines(InvoiceTools.generateInvoiceLines(ProductsAdapterHelper.getSelectedReturnProductItems()));

        return paymentsComputation.getRemaining().doubleValue();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isForHistoryDetail)
            getMenuInflater().inflate(R.menu.others_menu, menu);

        if(getModuleSetting(ConcessioModule.INVOICE).isHas_returns())
            if(!isForHistoryDetail && !isLayaway)
                getMenuInflater().inflate(R.menu.simple_review_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mReturn: {
                AlertDialog.Builder addAndReturnDialog = new AlertDialog.Builder(this, R.style.AppCompatDialogStyle_Light);
                addAndReturnDialog.setTitle("Add and Return Items?");
                addAndReturnDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Go to returning
                        Intent intent = new Intent(C_Finalize.this, C_Module.class);
                        intent.putExtra(RETURN_ITEMS, true);
                        intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, ProductsAdapterHelper.getSelectedCustomer().getId());
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.INVOICE.ordinal());
                        startActivityForResult(intent, RETURN_ITEMS_SALES);
                    }
                });
                addAndReturnDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                addAndReturnDialog.show();
            } break;
            case R.id.mPrint: {
                if(getAppSetting().isCan_print() && getModuleSetting(ConcessioModule.INVOICE).isCan_print()) {
                    if(!EpsonPrinterTools.targetPrinter(this).equals(""))
                        DialogTools.showConfirmationDialog(this, "Reprint", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                printTransaction(offlineInvoice, offlinePaymentsComputation, "*Salesman Copy*", "*Customer Copy*", "*Office Copy*");
                            }
                        }, "No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }, R.style.AppCompatDialogStyle_Light);
                    if(!StarIOPrinterTools.getTargetPrinter(this).equals(""))
                        DialogTools.showConfirmationDialog(this, "Reprint", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                printTransactionStar(offlineData, offlineInvoice, offlinePaymentsComputation, "*Salesman Copy*", "*Customer Copy*", "*Office Copy*");
                            }
                        }, "No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }, R.style.AppCompatDialogStyle_Light);
                }
            } break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RETURN_ITEMS_SALES) {
            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    reviewAdapter.updateReturns();
                    vpReview.setCurrentItem(1);
                }
            };
            handler.sendEmptyMessageDelayed(0, 100);
        }
        else if(requestCode == IS_DUPLICATING) {
            if(resultCode == SUCCESS) {
                if(data.hasExtra(FOR_HISTORY_DETAIL))
                    setResult(SUCCESS, data);
                else
                    setResult(SUCCESS);
                finish();
            }
        }
        else if(resultCode == SUCCESS) {
            if(data.hasExtra(FOR_HISTORY_DETAIL))
                setResult(SUCCESS, data);
            else
                setResult(SUCCESS);
            finish();
        }
    }

    public class ReviewAdapter extends FragmentPagerAdapter {

        private SimpleProductsFragment returnsProductFragment;

        public ReviewAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            SimpleProductsFragment simpleProductsFragment = SimpleProductsFragment.newInstance();
            simpleProductsFragment.setListingType(ListingType.ADVANCED_SALES);
            simpleProductsFragment.setUseSalesProductAdapter(true);
            if(position == 1)
                returnsProductFragment = simpleProductsFragment;
            simpleProductsFragment.setHelper(getHelper());
            simpleProductsFragment.setHasUnits(true);
            simpleProductsFragment.setHasToolBar(false);
            simpleProductsFragment.setHasCategories(false);
            simpleProductsFragment.setIsFinalize(true);
            simpleProductsFragment.setHasSubtotal(true);
            simpleProductsFragment.setDisplayOnly(isForHistoryDetail || isLayaway);
            simpleProductsFragment.setHasInStock(!(isForHistoryDetail || isLayaway));
            simpleProductsFragment.setConcessioModule(concessioModule);
            simpleProductsFragment.setCustomer(customer);

            CustomerGroup customerGroup = null;
            try {
                if (/*customer != null && */customer.getCustomerGroups(getHelper()).size() > 0)
                    customerGroup = customer.getCustomerGroups(getHelper()).get(0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            simpleProductsFragment.setCustomerGroup(customerGroup);
            simpleProductsFragment.setBranch(getBranches().get(0));

            simpleProductsFragment.setOnSalesFinalize(true);
            simpleProductsFragment.setProductsFragmentListener(new BaseProductsFragment.ProductsFragmentListener() {
                @Override
                public void whenItemsSelectedUpdated() {
                    toggleNext(llReview, tvItems);
                }
            });

            simpleProductsFragment.setProductsFragmentListener(new BaseProductsFragment.ProductsFragmentListener() {
                @Override
                public void whenItemsSelectedUpdated() {
                    /*Gson gson = new Gson();
                    Log.e(">>>>>",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    Log.e("PRODUCTS ADAPTER HELPER", gson.toJson(ProductsAdapterHelper.getSelectedProductItems()));
                    Log.e(">>>>>",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    Log.e("PRODUCTS ADAPTER HELPER", gson.toJson(ProductsAdapterHelper.getSelectedReturnProductItems()));
                    Log.e(">>>>>",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>");*/

                    if(!isForHistoryDetail && !isLayaway) {
                        Double balance = getBalance();
                        tvBalance.setText("P" + NumberTools.separateInCommas(balance));
                        tvBalance.setTag(balance);
                        toggleNext(llReview, tvItems);
                    }
                }
            });
            if(position == 0)// Positive Transactions
                simpleProductsFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
            else {
                simpleProductsFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedReturnProductItems().getSelectedProducts());
                simpleProductsFragment.setReturnItems(true);
            }

            Log.e("SelectedReturnsPI", "getItem is called");
            return simpleProductsFragment;
        }

        public void updateReturns() {
            Log.e("SelectedReturnsPI", ProductsAdapterHelper.getSelectedReturnProductItems().getSelectedProducts().size()+" size");
            returnsProductFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedReturnProductItems().getSelectedProducts());
            returnsProductFragment.forceUpdateProductList();
        }

        @Override
        public int getCount() {
            Log.e("hasReturns", getModuleSetting(ConcessioModule.INVOICE).isHas_returns()+"");
            return getModuleSetting(ConcessioModule.INVOICE).isHas_returns() ? 2 : 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return "TOTAL SALES";
            return "TOTAL RETURNS";
        }
    }

    // ----------------------- PRINTING

    private void printTransactionStar(final OfflineData offlineData, final Invoice invoice, final InvoiceTools.PaymentsComputation paymentsComputation, final String... labels) {
        if(!BluetoothTools.isEnabled())
            return;
        if(!StarIOPrinterTools.isPrinterOnline(this, StarIOPrinterTools.getTargetPrinter(this), "portable"))
            return;
        Branch branch = getBranches().get(0);
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
                else
                    data.add(("Date: " + simpleDateFormat.format(Calendar.getInstance().getTime())+"\r\n").getBytes());
                // ---------- HEADER

                double totalQuantity = 0.0;
                data.add("ORDERS\r\n".getBytes());
                data.add("================================".getBytes());
                data.add("Quantity                  Amount".getBytes());
                data.add("================================".getBytes());

                int totalInvoiceLines = invoice.getSalesInvoiceLines().size()+invoice.getRgsInvoiceLines().size()+invoice.getBoInvoiceLines().size()+invoice.getPayments().size();

                double numberOfPages = Math.ceil((double)totalInvoiceLines/ Configurations.MAX_ITEMS_FOR_PRINTING), items = 0;
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

                //InvoiceTools.PaymentsComputation paymentsComputation = checkoutFragment.getComputation();
                //paymentsComputation.addAllInvoiceLines(invoice.getInvoiceLines());
                //paymentsComputation.addAllPayments(invoice.getPayments());

                data.add((EpsonPrinterTools.spacer("Total Quantity: ", NumberTools.separateInCommas(totalQuantity), 32)+"\r\n").getBytes());
                data.add((EpsonPrinterTools.spacer("Gross Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayableNoDiscount().doubleValue(), 2)), 32)+"\r\n").getBytes());

                if(invoice.getExtras().getCustomer_discount_text_summary() != null) {
                    data.add((EpsonPrinterTools.spacer("LESS Customer Discount: ", invoice.getExtras().getCustomer_discount_text_summary(), 32) + "\r\n").getBytes());
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                    for (Double cusDisc : paymentsComputation.getCustomerDiscount())
                        data.add(("(" + NumberTools.separateInCommas(cusDisc) + ")\r\n").getBytes());
                }
                if(invoice.getExtras().getTotal_company_discount() != null) {
                    data.add((EpsonPrinterTools.spacer("LESS Company Discount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalCompanyDiscount().doubleValue(), 2))+")", 32) + "\r\n").getBytes());
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                }
                if(paymentsComputation.getTotalProductDiscount() != BigDecimal.ZERO) {
                    data.add((EpsonPrinterTools.spacer("LESS Product Discount: ", "("+NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalProductDiscount().doubleValue(), 2))+")", 32) + "\r\n").getBytes());
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                }

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
                    data.add((EpsonPrinterTools.spacer("Net BO Amount: ", NumberTools.separateInCommas(NumberTools.formatDouble(Math.abs(paymentsComputation.getReturnsPayments().get(0).getAmount()),2)), 32)+"\r\n\r\n").getBytes());
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
                    if(paymentsComputation.getReturnsPayments().size() > 1)
                        data.add((EpsonPrinterTools.spacer("Net RGS Amount: ", NumberTools.separateInCommas(Math.abs(paymentsComputation.getReturnsPayments().get(1).getAmount())), 32)+"\r\n\r\n").getBytes());
                    else
                        data.add((EpsonPrinterTools.spacer("Net RGS Amount: ", NumberTools.separateInCommas(Math.abs(paymentsComputation.getReturnsPayments().get(0).getAmount())), 32)+"\r\n\r\n").getBytes());
                }

                data.add((EpsonPrinterTools.spacer("Amount Due: ", NumberTools.separateInCommas(NumberTools.formatDouble(paymentsComputation.getTotalPayable(true).doubleValue(), 2)), 32)+"\r\n\r\n").getBytes());

                data.add("PAYMENTS\r\n".getBytes());
                data.add("================================".getBytes());
                data.add("Payments                  Amount".getBytes());
                for(InvoicePayment invoicePayment : invoice.getPayments()) {
                    PaymentType paymentType = PaymentType.fetchById(getHelper(), PaymentType.class, invoicePayment.getPayment_type_id());
                    if(!paymentType.getName().trim().equals("Credit Memo") && !paymentType.getName().trim().equals("RS Slip"))
                        data.add((EpsonPrinterTools.spacer(paymentType.getName(), NumberTools.separateInCommas(invoicePayment.getTender()), 32)+"\r\n").getBytes());

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
                    data.add(("Terms: "+ProductsAdapterHelper.getSelectedCustomer().getPaymentTerms().getName()+"\r\n\r\n").getBytes());
                else
                    data.add("\r\n".getBytes());

                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                data.add(labels[i].getBytes());
                if(isForHistoryDetail) {
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                    data.add("\r\n** This is a reprint **\r\n".getBytes());
                }
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

    private void printTransaction(final Invoice invoice, final InvoiceTools.PaymentsComputation paymentsComputation, final String... labels) {
        if(!BluetoothTools.isEnabled())
            return;

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

//                            InvoiceTools.PaymentsComputation paymentsComputation = checkoutFragment.getComputation();
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
