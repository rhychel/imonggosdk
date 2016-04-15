package net.nueca.concessio;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.activities.AddEditCustomerActivity;
import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.dialogs.SearchDRDialog;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.SimplePulloutRequestDialog;
import net.nueca.concessioengine.dialogs.TransactionDialog;
import net.nueca.concessioengine.fragments.BaseProductsFragment;
import net.nueca.concessioengine.fragments.BaseTransactionsFragment;
import net.nueca.concessioengine.fragments.MultiInputSelectedItemFragment;
import net.nueca.concessioengine.fragments.SimpleCustomerDetailsFragment;
import net.nueca.concessioengine.fragments.SimpleCustomersFragment;
import net.nueca.concessioengine.fragments.SimpleInventoryFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.SimplePulloutFragment;
import net.nueca.concessioengine.fragments.SimpleReceiveFragment;
import net.nueca.concessioengine.fragments.SimpleReceiveReviewFragment;
import net.nueca.concessioengine.fragments.SimpleRoutePlanFragment;
import net.nueca.concessioengine.fragments.SimpleTransactionDetailsFragment;
import net.nueca.concessioengine.fragments.SimpleTransactionsFragment;
import net.nueca.concessioengine.fragments.interfaces.MultiInputListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.lists.ReceivedProductItemList;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.printer.PrinterTask;
import net.nueca.concessioengine.printer.epson.listener.PrintListener;
import net.nueca.concessioengine.printer.epson.tools.EpsonPrinterTools;
import net.nueca.concessioengine.printer.starmicronics.enums.StarIOPaperSize;
import net.nueca.concessioengine.printer.starmicronics.tools.StarIOPrinterTools;
import net.nueca.concessioengine.tools.AnimationTools;
import net.nueca.concessioengine.tools.BluetoothTools;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.concessioengine.tools.PriceTools;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.concessioengine.views.SimplePulloutToolbarExt;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.accountsettings.ProductSorting;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.swable.ImonggoSwableServiceConnection;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DateTimeTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.NumberTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by rhymart on 8/21/15.
 * imonggosdk2 (c)2015
 */
public class C_Module extends ModuleActivity implements SetupActionBar, BaseProductsFragment.ProductsFragmentListener {

    private SimpleProductsFragment simpleProductsFragment, finalizeFragment;
    private Button btn1, btn2;
    private TextView tvItems;
    private LinearLayout llReview, llBalance, llFooter;

    private Toolbar toolbar;
    private boolean hasMenu = true, showsCustomer = false, // -- for the search
            changeToReview = false, refreshCustomerList = false;

    private SearchDRDialog searchDRDialog;

    // for transaction details
    private String referenceNumber = "";

    private SimpleTransactionsFragment simpleTransactionsFragment;
    private SimpleTransactionDetailsFragment simpleTransactionDetailsFragment;

    private SimpleReceiveFragment simpleReceiveFragment;
    private SimpleReceiveReviewFragment simpleReceiveReviewFragment;

    private SimpleInventoryFragment simpleInventoryFragment;

    private SimpleCustomersFragment simpleCustomersFragment;

    private SimpleRoutePlanFragment simpleRoutePlanFragment;
    private SimpleCustomerDetailsFragment simpleCustomerDetailsFragment;

    // For the pullout module
    private SimplePulloutFragment simplePulloutFragment;
    private SimplePulloutToolbarExt simplePulloutToolbarExt;
    private SimplePulloutRequestDialog simplePulloutRequestDialog;

    private ImonggoSwableServiceConnection imonggoSwableServiceConnection;

    private OfflineData offlineData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_module);

        if (clearTransactions) {
            ProductsAdapterHelper.clearSelectedProductItemList(initSelectedCustomer, initSelectedCustomer);
            ProductsAdapterHelper.clearSelectedReturnProductItemList();
        }

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        tvItems = (TextView) findViewById(R.id.tvItems);
        llReview = (LinearLayout) findViewById(R.id.llReview);
        llBalance = (LinearLayout) findViewById(R.id.llBalance);
        llFooter = (LinearLayout) findViewById(R.id.llFooter);

        llFooter.setVisibility(View.GONE);
        switch (concessioModule) {
            case LAYAWAY: {
                simpleTransactionsFragment = new SimpleTransactionsFragment();
                simpleTransactionsFragment.setHelper(getHelper());
                simpleTransactionsFragment.setSetupActionBar(this);
                simpleTransactionsFragment.onlyInvoices(true);
                simpleTransactionsFragment.setTransactionTypes(getTransactionTypes());
                simpleTransactionsFragment.setListingType(ListingType.DETAILED_HISTORY);
                simpleTransactionsFragment.setTransactionsListener(new BaseTransactionsFragment.TransactionsListener() {
                    @Override
                    public void showTransactionDetails(OfflineData offlineData) {
                        prepareFooter();
                        ProductsAdapterHelper.clearSelectedProductItemList(true);
                        ProductsAdapterHelper.clearSelectedReturnProductItemList();
                        ProductsAdapterHelper.setDbHelper(getHelper());

                        if(offlineData.getType() == OfflineData.INVOICE) {
                            try {
                                SelectedProductItemList selecteds =
                                        InvoiceTools.generateSelectedProductItemList(getHelper(), offlineData, false, false);
                                SelectedProductItemList returns =
                                        InvoiceTools.generateSelectedProductItemList(getHelper(), offlineData, true, false);

                                Customer customer = offlineData.getObjectFromData(Invoice.class).getCustomer();
                                ProductsAdapterHelper.setSelectedCustomer(customer);
                                List<CustomerGroup> customerGroups = customer.getCustomerGroups(getHelper());
                                if(customerGroups.size() > 0)
                                    ProductsAdapterHelper.setSelectedCustomerGroup(customerGroups.get(0));

                                ProductsAdapterHelper.getSelectedProductItems().addAll(selecteds);
                                ProductsAdapterHelper.getSelectedReturnProductItems().addAll(returns);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            Intent intent = new Intent(C_Module.this, C_Finalize.class);
                            intent.putExtra(REFERENCE, offlineData.getReference_no());
                            Log.e("INOVOICE SEND", offlineData.getObjectFromData(Invoice.class).toJSONString());
                            intent.putExtra(IS_LAYAWAY, true);
                            startActivityForResult(intent, REVIEW_SALES);
                        }
                    }
                });

                imonggoSwableServiceConnection = new ImonggoSwableServiceConnection(simpleTransactionsFragment);

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flContent, simpleTransactionsFragment)
                        .commit();
            }
            break;
            case HISTORY: {
                simpleTransactionDetailsFragment = new SimpleTransactionDetailsFragment();
                simpleTransactionDetailsFragment.setHelper(getHelper());
                simpleTransactionDetailsFragment.setHasCategories(false);
                simpleTransactionDetailsFragment.setSetupActionBar(this);
                // if there's branch product
                simpleTransactionDetailsFragment.setBranch(getBranches().get(0));

                simpleTransactionsFragment = new SimpleTransactionsFragment();
                simpleTransactionsFragment.setHelper(getHelper());
                simpleTransactionsFragment.setSetupActionBar(this);
                simpleTransactionsFragment.setHasFilterByTransactionType(true);
                simpleTransactionsFragment.setTransactionTypes(getTransactionTypes());
                if(getIntent().hasExtra(HISTORY_ITEM_FILTERS))
                    simpleTransactionsFragment.setFilterModules(ConcessioModule.convertToConcessioModules(getIntent().getIntArrayExtra(HISTORY_ITEM_FILTERS)));
                simpleTransactionsFragment.setListingType(ListingType.DETAILED_HISTORY);
                if(customer != null) {
                    simpleTransactionsFragment.setCustomer(customer);
                }
                simpleTransactionsFragment.setTransactionsListener(new BaseTransactionsFragment.TransactionsListener() {

                    @Override
                    public void showTransactionDetails(OfflineData offlineData) {
                        prepareFooter();
                        ProductsAdapterHelper.clearSelectedProductItemList(true);
                        ProductsAdapterHelper.clearSelectedReturnProductItemList();
                        ProductsAdapterHelper.setDbHelper(getHelper());

                        if(offlineData.getType() == OfflineData.INVOICE) {
                            try {
                                SelectedProductItemList selecteds =
                                        InvoiceTools.generateSelectedProductItemList(getHelper(), offlineData, false, false);
                                SelectedProductItemList returns =
                                        InvoiceTools.generateSelectedProductItemList(getHelper(), offlineData, true, false);

                                Customer customer = offlineData.getObjectFromData(Invoice.class).getCustomer();
                                ProductsAdapterHelper.setSelectedCustomer(customer);
                                List<CustomerGroup> customerGroups = customer.getCustomerGroups(getHelper());
                                if(customerGroups.size() > 0)
                                    ProductsAdapterHelper.setSelectedCustomerGroup(customerGroups.get(0));

                                ProductsAdapterHelper.getSelectedProductItems().addAll(selecteds);
                                ProductsAdapterHelper.getSelectedReturnProductItems().addAll(returns);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            Intent intent = new Intent(C_Module.this, C_Finalize.class);
                            intent.putExtra(REFERENCE, offlineData.getReference_no());
                            intent.putExtra(FOR_HISTORY_DETAIL, true);
                            startActivityForResult(intent, HISTORY_DETAILS);
                        }
                        else if(offlineData.getType() == OfflineData.CUSTOMER) {
                            Intent intent = new Intent(C_Module.this, C_Module.class);
                            intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, offlineData.getObjectFromData(Customer.class).getId());
                            intent.putExtra(ModuleActivity.FOR_HISTORY_DETAIL, true);
                            intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.CUSTOMER_DETAILS.ordinal());
                            startActivityForResult(intent, ALL_CUSTOMERS);
                        }
                        else {
                            try {
                                simpleTransactionDetailsFragment.setFilterProductsBy(processOfflineData(offlineData));
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            referenceNumber = offlineData.getReference_no();
                            simpleTransactionDetailsFragment.setOfflineData(offlineData);

                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                    .add(R.id.flContent, simpleTransactionDetailsFragment, "transaction_details")
                                    .addToBackStack("transaction_details")
                                    .commit();
                        }

                    }
                });

                imonggoSwableServiceConnection = new ImonggoSwableServiceConnection(simpleTransactionsFragment);

                historyDetailsListener = new HistoryDetailsListener() {
                    @Override
                    public void onVoidTransaction() {
                        new SwableTools.Transaction(getHelper())
                                .toCancel()
                                .withReason("VOID")
                                .object(simpleTransactionDetailsFragment.getOfflineData())
                                .queue();

                        // <-- Voiding issue when the transaction is voided for Receive and Pullout -->
                        if(simpleTransactionDetailsFragment.getOfflineData().getConcessioModule() == ConcessioModule.RECEIVE_SUPPLIER) // Receive
                            revertInventoryFromDocument(simpleTransactionDetailsFragment.getOfflineData().getObjectFromData(Document.class), false);
                        if(simpleTransactionDetailsFragment.getOfflineData().getConcessioModule() == ConcessioModule.RELEASE_SUPPLIER) // Pullout
                            revertInventoryFromDocument(simpleTransactionDetailsFragment.getOfflineData().getObjectFromData(Document.class), true);

                        onBackPressed();
                        OfflineData offlineData = simpleTransactionDetailsFragment.getOfflineData();
                        offlineData.setSynced(false);
                        simpleTransactionsFragment.updateOfflineData(offlineData);
                    }

                    @Override
                    public void onDuplicateTransaction() {
                        ProductsAdapterHelper.isDuplicating = true;
                        Intent intent = new Intent(C_Module.this, C_Module.class);
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, simpleTransactionDetailsFragment.getOfflineData().getConcessioModule().ordinal());
                        if(simpleTransactionDetailsFragment.getOfflineData().getConcessioModule() == ConcessioModule.RELEASE_ADJUSTMENT)
                            intent.putExtra(ModuleActivity.CATEGORY, simpleTransactionDetailsFragment.getOfflineData().getCategory());
                        startActivityForResult(intent, IS_DUPLICATING);
                    }
                };

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flContent, simpleTransactionsFragment)
                        .commit();
            }
            break;
            case ROUTE_PLAN: {
                simpleRoutePlanFragment = new SimpleRoutePlanFragment();
                simpleRoutePlanFragment.setHelper(getHelper());
                simpleRoutePlanFragment.setSetupActionBar(this);
                simpleRoutePlanFragment.setCanShowAllCustomers(true);
                simpleRoutePlanFragment.setRoutePlanListener(new SimpleRoutePlanFragment.RoutePlanListener() {
                    @Override
                    public void itemClicked(Customer customer) {
                        Log.e("Customer Details", "Clicked! -- isCustomerNull? " + (customer == null));
                        Intent intent = new Intent(C_Module.this, C_Module.class);
                        intent.putExtra(ModuleActivity.INIT_PRODUCT_ADAPTER_HELPER, true);
                        intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, customer.getId());
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.CUSTOMER_DETAILS.ordinal());
                        startActivityForResult(intent, ROUTE_PLAN);
                    }
                });

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flContent, simpleRoutePlanFragment)
                        .commit();
            }
            break;
            case CUSTOMER_DETAILS: {
                Log.e("Customer Details", "Yeah");
                simpleCustomerDetailsFragment = new SimpleCustomerDetailsFragment();
                simpleCustomerDetailsFragment.setCustomer(customer);
                simpleCustomerDetailsFragment.setHelper(getHelper());
                simpleCustomerDetailsFragment.setSetupActionBar(this);

                llFooter.setVisibility(View.VISIBLE);
                llReview.setVisibility(View.VISIBLE);

                View.OnClickListener showHistory = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(C_Module.this, C_Module.class);
                        intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, customer.getId());
                        intent.putExtra(ModuleActivity.HISTORY_ITEM_FILTERS, new int[]{ConcessioModule.INVOICE.ordinal(), ConcessioModule.RELEASE_ADJUSTMENT.ordinal()});
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.HISTORY.ordinal());
                        startActivity(intent);
                    }
                };
                if(isFromCustomersList) {
                    btn1.setText("VIEW HISTORY");
                    btn1.setOnClickListener(showHistory);
                }
                else if (isForHistoryDetail) {
                    llFooter.setVisibility(View.GONE);
                }
                else {
                    btn1.setText("TRANSACT");
                    btn1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ProductsAdapterHelper.setSelectedCustomer(customer);
                            Intent intent = new Intent(C_Module.this, C_Module.class);
                            intent.putExtra(ModuleActivity.INIT_PRODUCT_ADAPTER_HELPER, true);
                            intent.putExtra(ModuleActivity.INIT_SELECTED_CUSTOMER, false);
                            intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, customer.getId());
                            intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.INVOICE.ordinal());
                            startActivityForResult(intent, SALES);
                        }
                    });
                    btn2.setText("HISTORY");
                    btn2.setOnClickListener(showHistory);
                    btn2.setVisibility(View.VISIBLE);
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flContent, simpleCustomerDetailsFragment)
                        .commit();
            }
            break;
            case CUSTOMERS: {
                simpleCustomersFragment = new SimpleCustomersFragment();
                simpleCustomersFragment.setHelper(getHelper());
                simpleCustomersFragment.setSetupActionBar(this);
                simpleCustomersFragment.setListingType(ListingType.LETTER_HEADER);
                simpleCustomersFragment.setOnCustomerSelectedListener(new SimpleCustomersFragment.OnCustomerSelectedListener() {
                    @Override
                    public void onCustomerSelected(Customer customer, int position) {
                        Intent intent = new Intent(C_Module.this, C_Module.class);
                        intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, customer.getId());
                        intent.putExtra(ModuleActivity.FROM_CUSTOMERS_LIST, isFromCustomersList);
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.CUSTOMER_DETAILS.ordinal());
                        startActivityForResult(intent, ALL_CUSTOMERS);
                    }
                });

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flContent, simpleCustomersFragment)
                        .commit();
            }
            break;
            case STOCK_REQUEST:
            case RECEIVE_BRANCH_PULLOUT: { // TODO for Petron

                changeToReview = true;
                initializeProducts();
                simpleProductsFragment.setListingType(ListingType.SALES);
                simpleProductsFragment.setHasUnits(true);
                simpleProductsFragment.setHasBrand(false);
                simpleProductsFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing().isLock_category()));
                simpleProductsFragment.setShowCategoryOnStart(false);
                simpleProductsFragment.setProductsFragmentListener(this);
                simpleProductsFragment.setHasSubtotal(false);
                simpleProductsFragment.setUseRecyclerView(true);

                initializeFinalize();
                finalizeFragment.setListingType(ListingType.SALES);
                finalizeFragment.setHasCategories(false);
                finalizeFragment.setHasDeliveryDate(false);
                finalizeFragment.setHasUnits(true);
                finalizeFragment.setHasSubtotal(false);
                finalizeFragment.setUseSalesProductAdapter(false);

                prepareFooter();

                btn1.setOnClickListener(nextClickedListener);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.flContent, simpleProductsFragment)
                        .commit();

                if(concessioModule == ConcessioModule.RECEIVE_BRANCH_PULLOUT) {
                    simpleProductsFragment.setConcessioModule(concessioModule);
                    finalizeFragment.setConcessioModule(concessioModule);
                    try {
                        searchDRDialog = new SearchDRDialog(this, getHelper(), getUser(), R.style.AppCompatDialogStyle_Light);
                        searchDRDialog.setTitle("Confirm Pullout");
                        searchDRDialog.setHasBranch(false);
                        searchDRDialog.setConcessioModule(concessioModule);
                        searchDRDialog.setDialogListener(new SearchDRDialog.SearchDRDialogListener() {
                            @Override
                            public boolean onCancel() {
                                finish();
                                return true;
                            }

                            @Override
                            public void onSearch(String deliveryReceiptNo, Branch target_branch, Document document) {
                                if(target_branch == null)
                                    Log.e("target_branch", "is null");

                                ProductsAdapterHelper.setWarehouse_id(document.getTarget_branch_id());
                                ProductsAdapterHelper.setParent_document_id(document.getReturnId());

                                try {
                                    simpleProductsFragment.setFilterProductsBy(processObject(document));
                                    simpleProductsFragment.forceUpdateProductList(simpleProductsFragment.getFilterProductsBy());
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                whenItemsSelectedUpdated();
                            }

                            @Override
                            public void onManualReceive(String deliveryReceiptNo, Branch target_branch) {

                            }
                        });
                        searchDRDialog.show();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
            case INVOICE: {
                CustomerGroup customerGroup = null;

                try {
                    if (customer.getCustomerGroups(getHelper()).size() > 0)
                        customerGroup = customer.getCustomerGroups(getHelper()).get(0);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                changeToReview = true;
                initializeProducts();
                simpleProductsFragment.setHelper(getHelper());
                simpleProductsFragment.setListingType(ListingType.ADVANCED_SALES);
                simpleProductsFragment.setHasUnits(true);
                simpleProductsFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing().isLock_category()));
                simpleProductsFragment.setShowCategoryOnStart(getModuleSetting(concessioModule).getProductListing().isShow_categories_on_start());
                simpleProductsFragment.setProductsFragmentListener(this);
                simpleProductsFragment.setHasSubtotal(true);
                simpleProductsFragment.setUseSalesProductAdapter(true);
                simpleProductsFragment.setCustomer(customer);
                simpleProductsFragment.setHasPromotionalProducts(true);
                simpleProductsFragment.setCustomerGroup(customerGroup);
                simpleProductsFragment.setBranch(getBranches().get(0));
                simpleProductsFragment.setConcessioModule(concessioModule);

                prepareFooter();
                btn1.setOnClickListener(nextClickedListener);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.flContent, simpleProductsFragment)
                        .commit();
            }
            break;
            case PHYSICAL_COUNT: { // TODO Revise for Petron
                changeToReview = true;

                initializeProducts();
                simpleProductsFragment.setDisplayOnly(getModuleSetting(concessioModule).is_view());
                // if there's branch product
                simpleProductsFragment.setBranch(getBranches().get(0));
                simpleProductsFragment.setProductCategories(getProductCategories(!getModuleSetting(ConcessioModule.PHYSICAL_COUNT).getProductListing().isLock_category()));
                simpleProductsFragment.setListingType(ListingType.SALES);
                simpleProductsFragment.setProductsFragmentListener(this);

                Log.e("PHYSICAL_COUNT", "is_view"+getModuleSetting(concessioModule).is_view());
                Log.e("PHYSICAL_COUNT", "is_can_print"+getAppSetting().isCan_print());
                if(getModuleSetting(concessioModule).is_view()) {
                    if(getAppSetting().isCan_print() && getModuleSetting(concessioModule).isCan_print()) {
                        llFooter.setVisibility(View.VISIBLE);
                        btn1.setText("PRINT INVENTORY");
                        btn1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // ---- So let's print...
                                if(!EpsonPrinterTools.targetPrinter(C_Module.this).equals(""))
                                    printTransaction(null, "*Salesman Copy*", "*Office Copy*");
                                if(!StarIOPrinterTools.getTargetPrinter(C_Module.this).equals(""))
                                    printTransactionStar(null, "*Salesman Copy*", "*Office Copy*");
                            }
                        });
                        tvItems.setVisibility(View.INVISIBLE);
                    }
                }
                else {
                    initializeFinalize();
                    finalizeFragment.setListingType(ListingType.SALES);
                    finalizeFragment.setHasCategories(false);
                    finalizeFragment.setUseRecyclerView(true);
                    finalizeFragment.setUseSalesProductAdapter(false);

                    if(getModuleSetting(ConcessioModule.PHYSICAL_COUNT).getQuantityInput().is_multiinput()) {
                        simpleProductsFragment.setMultipleInput(true);
                        simpleProductsFragment.setMultiInputListener(multiInputListener);

                        finalizeFragment.setMultipleInput(true);
                        finalizeFragment.setMultiInputListener(multiInputListener);
                    }
                    else {
                        simpleProductsFragment.setHasUnits(true);
                        simpleProductsFragment.setHasBrand(true);
                        simpleProductsFragment.setHasSubtotal(false);
                        simpleProductsFragment.setUseRecyclerView(true);

                        finalizeFragment.setHasDeliveryDate(false);
                        finalizeFragment.setHasUnits(true);
                        finalizeFragment.setHasBrand(true);
                        finalizeFragment.setHasSubtotal(false);
                    }
                    btn1.setOnClickListener(nextClickedListener);

                    prepareFooter();
                }

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.flContent, simpleProductsFragment)
                        .commit();
            }
            break;
            case RECEIVE_BRANCH: { // TODO for Petron
                changeToReview = true;
                simpleReceiveFragment = new SimpleReceiveFragment();
                simpleReceiveFragment.setHelper(getHelper());
                simpleReceiveFragment.setSetupActionBar(this);
                simpleReceiveFragment.setUseRecyclerView(false);
                simpleReceiveFragment.setFragmentContainer(R.id.flContent);
                simpleReceiveFragment.setProductCategories(getProductCategories(true));
                simpleReceiveFragment.setFABListener(new SimpleReceiveFragment.FloatingActionButtonListener() {
                    @Override
                    public void onClick(ReceivedProductItemList receivedProductItemList, Branch targetBranch,
                                        String reference, Integer parentDocumentID) {
                        SimpleReceiveReviewFragment simpleReceiveReviewFragment = new SimpleReceiveReviewFragment();
                        simpleReceiveReviewFragment.setParentID(parentDocumentID);
                        simpleReceiveReviewFragment.setTargetBranch(targetBranch);
                        simpleReceiveReviewFragment.setDRNo(reference);
                        simpleReceiveReviewFragment.setUseRecyclerView(true);
                        simpleReceiveReviewFragment.setHelper(getHelper());
                        simpleReceiveReviewFragment.setFragmentContainer(R.id.flContent);
                        simpleReceiveReviewFragment.setReceivedProductItemList(receivedProductItemList);
                        simpleReceiveReviewFragment.setIsManual(simpleReceiveFragment.isManual());

                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                        R.anim.slide_in_left, R.anim.slide_out_right)
                                .replace(R.id.flContent, simpleReceiveReviewFragment)
                                .addToBackStack("review_fragment")
                                .commit();
                    }
                });

                prepareFooter();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, simpleReceiveFragment)
                        .commit();
            }
            break;
            case RECEIVE_SUPPLIER: // Rebisco receiving
            case RELEASE_SUPPLIER: { // Rebisco pullout
                changeToReview = true;
                simpleInventoryFragment = new SimpleInventoryFragment();
                simpleInventoryFragment.setHelper(getHelper());
                simpleInventoryFragment.setListingType(ListingType.ADVANCED_SALES); //changed to show the individual price of every unit-- Sales
                simpleInventoryFragment.setUseSalesProductAdapter(true);//added to show the individual price of every unit
                simpleInventoryFragment.setHasUnits(true);
                simpleInventoryFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing().isLock_category()));
                simpleInventoryFragment.setShowCategoryOnStart(getModuleSetting(concessioModule).getProductListing().isShow_categories_on_start());
                simpleInventoryFragment.setSetupActionBar(this);
                simpleInventoryFragment.setHasSubtotal(false);
                simpleInventoryFragment.setProductsFragmentListener(this);
                // if there's branch product
                simpleInventoryFragment.setBranch(getBranches().get(0));

                initializeFinalize();
                finalizeFragment.setListingType(ListingType.ADVANCED_SALES); //changed to show the individual price of every unit-- Sales
                finalizeFragment.setUseSalesProductAdapter(true);//added to show the individual price of every unit
                finalizeFragment.setHasSubtotal(false);
                finalizeFragment.setHasCategories(false);
                finalizeFragment.setHasBrand(false);
                finalizeFragment.setHasDeliveryDate(false);
                finalizeFragment.setHasUnits(true);
                // if there's branch product
                finalizeFragment.setBranch(getBranches().get(0));

                prepareFooter();

                btn1.setOnClickListener(nextClickedListener);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, simpleInventoryFragment)
                        .commit();
            }
            break;
//            case RECEIVE_BRANCH_PULLOUT: {
//
//            }
//            break;
            case RELEASE_BRANCH: { // TODO for Petron
                changeToReview = true;
//                simplePulloutRequestDialog = new SimplePulloutRequestDialog(this, getHelper(), R.style.AppCompatDialogStyle_Light_NoTitle);
//                simplePulloutRequestDialog.setTitle("Choose a reason");
//                simplePulloutRequestDialog.setListener(new SimplePulloutRequestDialog.PulloutRequestDialogListener() {
//                    @Override
//                    public void onSave(DocumentPurpose reason, Branch source, Branch destination) {
//                        ProductsAdapterHelper.setReason(reason);
//                        ProductsAdapterHelper.setSource(source);
//                        ProductsAdapterHelper.setDestination(destination);
//
//                        simplePulloutToolbarExt.renderReason();
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        if (getModuleSetting(concessioModule).isRequire_document_reason())
//                            finish();
//                    }
//                });
//                if (getModuleSetting(concessioModule).isRequire_document_reason())
//                    simplePulloutRequestDialog.show();

                simplePulloutFragment = new SimplePulloutFragment();
                simplePulloutFragment.setListingType(ListingType.SALES);
                simplePulloutFragment.setHasUnits(true);
                simplePulloutFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing().isLock_category()));
                simplePulloutFragment.setShowCategoryOnStart(getModuleSetting(concessioModule).getProductListing().isShow_categories_on_start());
                simplePulloutFragment.setProductsFragmentListener(this);
                simplePulloutFragment.setHasSubtotal(false);
                simplePulloutFragment.setUseRecyclerView(true);
                simplePulloutFragment.setHelper(getHelper());
                simplePulloutFragment.setSetupActionBar(this);
                simplePulloutFragment.setHasUnits(true);
                simplePulloutFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing().isLock_category()));
                simplePulloutFragment.setConcessioModule(concessioModule);

                initializeFinalize();
                finalizeFragment.setListingType(ListingType.SALES);
                finalizeFragment.setHasCategories(false);
                finalizeFragment.setHasUnits(true);
                finalizeFragment.setConcessioModule(concessioModule);

                prepareFooter();
                btn1.setOnClickListener(nextClickedListener);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, simplePulloutFragment)
                        .commit();
            }
            break;
            case RELEASE_ADJUSTMENT: { // ----> MSO of Rebisco
                changeToReview = true;
                showsCustomer = true;
                simpleCustomersFragment = new SimpleCustomersFragment();
                simpleCustomersFragment.setHelper(getHelper());
                simpleCustomersFragment.setSetupActionBar(this);
                simpleCustomersFragment.setListingType(ListingType.LETTER_HEADER);
                simpleCustomersFragment.setOnCustomerSelectedListener(new SimpleCustomersFragment.OnCustomerSelectedListener() {
                    @Override
                    public void onCustomerSelected(final Customer customer, final int position) {
                        SimplePulloutRequestDialog simplePulloutRequestDialog = new SimplePulloutRequestDialog(C_Module.this, getHelper(), R.style.AppCompatDialogStyle_Light_NoTitle);
                        simplePulloutRequestDialog.setDTitle("MSO");
                        simplePulloutRequestDialog.setShouldShowBranchSelection(false);
                        simplePulloutRequestDialog.setListener(new SimplePulloutRequestDialog.PulloutRequestDialogListener() {
                            @Override
                            public void onSave(DocumentPurpose reason, Branch source, Branch destination) {
                                Log.e("Reason", reason.getName());
                                ProductsAdapterHelper.clearSelectedProductItemList(true);
                                ProductsAdapterHelper.clearSelectedReturnProductItemList();
                                ProductsAdapterHelper.setSelectedCustomer(customer);
                                ProductsAdapterHelper.setReason(reason);

                                initializeProducts();
                                simpleProductsFragment.setLockCategory(true);
                                simpleProductsFragment.setHasSubtotal(false);
                                simpleProductsFragment.setListingType(ListingType.ADVANCED_SALES); //changed to show the individual price of every unit-- Sales
                                simpleProductsFragment.setUseSalesProductAdapter(true);//added to show the individual price of every unit
//                                simpleProductsFragment.setListingType(ListingType.SALES);
                                simpleProductsFragment.setHasUnits(true);
                                // !getModuleSetting().getProductListing().isLock_category()
                                simpleProductsFragment.setProductCategories(getProductCategories(false));
                                simpleProductsFragment.setShowCategoryOnStart(getModuleSetting(concessioModule).getProductListing().isShow_categories_on_start());
                                simpleProductsFragment.setProductsFragmentListener(C_Module.this);
                                // if there's branch product
                                simpleProductsFragment.setBranch(getBranches().get(0));
                                simpleProductsFragment.setConcessioModule(concessioModule);
                                if(ProductsAdapterHelper.isDuplicating)
                                    simpleProductsFragment.setCategory(getIntent().getStringExtra(ModuleActivity.CATEGORY));

                                initializeFinalize();
                                finalizeFragment.setHasCategories(false);
                                finalizeFragment.setHasBrand(false);
                                finalizeFragment.setHasDeliveryDate(false);
                                finalizeFragment.setHasUnits(true);
                                finalizeFragment.setListingType(ListingType.ADVANCED_SALES); //changed to show the individual price of every unit-- Sales
                                finalizeFragment.setUseSalesProductAdapter(true);//added to show the individual price of every unit
//                                finalizeFragment.setListingType(ListingType.SALES);
                                // if there's branch product
                                finalizeFragment.setBranch(getBranches().get(0));
                                finalizeFragment.setConcessioModule(concessioModule);

                                prepareFooter();

                                btn1.setOnClickListener(nextClickedListener);

                                showsCustomer = false;

                                simpleCustomersFragment.deselectCustomer(customer, position);
                                getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                        .add(R.id.flContent, simpleProductsFragment, "products_fragment")
                                        .addToBackStack("products_fragment")
                                        .commit();
                            }

                            @Override
                            public void onCancel() {
                                simpleCustomersFragment.deselectCustomer(customer, position);
                            }
                        });
                        simplePulloutRequestDialog.show();
                    }
                });

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flContent, simpleCustomersFragment)
                        .commit();
            }
            break;
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Log.e("onBackStackChanged", "add=" + getSupportFragmentManager().getBackStackEntryCount());
                if (isBackPressed()) {
                    Log.e("onBackStackChanged", "called--hasMenu");
                    if (concessioModule == ConcessioModule.HISTORY) {
                        llFooter.setVisibility(View.GONE);
                        btn1.setVisibility(View.GONE);
                        btn2.setVisibility(View.GONE);
                        tvItems.setVisibility(View.INVISIBLE);
                        hasMenu = true;
                        getSupportActionBar().setDisplayShowTitleEnabled(false);
                    }
                    if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                        hasMenu = true;
                        if (previousFragmentCount == 0) {
                            llFooter.setVisibility(View.GONE);
                            if(simpleCustomersFragment != null) {
                                simpleCustomersFragment.setHasSelected(false);
                                simpleCustomersFragment.onViewCreated(null, null);
                            }
                            showsCustomer = true;
                        }
                        else if(previousFragmentCount == 1) {
                            simpleProductsFragment.refreshList();
                        }
                    }
                } else {
                    hasMenu = false;
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.e("onBackStackChanged", "called--setNavigationOnClickListener");
                            hasMenu = true;
                            onBackPressed();
                            getSupportActionBar().invalidateOptionsMenu();
                        }
                    });
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                    if (concessioModule != ConcessioModule.HISTORY)
                        getSupportActionBar().setTitle("Review");
                    if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                        if (previousFragmentCount == 1) {
                            getSupportActionBar().setDisplayShowTitleEnabled(false);
                            hasMenu = true;
                        }
                    }
                    if (concessioModule == ConcessioModule.HISTORY) {
                        hasMenu = true;
                        tvItems.setVisibility(View.VISIBLE);
                        int size = simpleTransactionDetailsFragment.numberOfItems();
                        tvItems.setText(getResources().getQuantityString(R.plurals.items, size, size));
                        Log.e("Offline Data", simpleTransactionDetailsFragment.getOfflineData().getReference_no());
                        Log.e("Offline Data", simpleTransactionDetailsFragment.getOfflineData().isCancelled() + "");

                        Log.e("Offline Data", simpleTransactionDetailsFragment.getOfflineData().isSynced()+" isSynced");
                        Log.e("Offline Data", simpleTransactionDetailsFragment.getOfflineData().isSyncing()+" isSyncing");
                        Log.e("Offline Data", simpleTransactionDetailsFragment.getOfflineData().isCancelled()+" isCancelled");

                        boolean useBtn2 = true;
                        boolean isVoiding = simpleTransactionDetailsFragment.getOfflineData().isCancelled()
                                || simpleTransactionDetailsFragment.getOfflineData().getOfflineDataTransactionType().isVoiding();
                        if(isVoiding) {
                            btn1.setVisibility(View.GONE);
                            useBtn2 = false;
                        }
                        else
                            initializeVoidButton(btn1, referenceNumber);

                        if(simpleTransactionDetailsFragment.getOfflineData().getConcessioModule() == ConcessioModule.RECEIVE_SUPPLIER ||
                                simpleTransactionDetailsFragment.getOfflineData().getConcessioModule() == ConcessioModule.RELEASE_SUPPLIER) {
                            Log.e("duplicate", "yeah");
                            if(isVoiding)
                                initializeDuplicateButton(btn1, referenceNumber);
                        }
                        else
                            initializeDuplicateButton(useBtn2 ? btn2 : btn1, referenceNumber);

                        Log.e("useBtn2", useBtn2+"");
                        whenItemsSelectedUpdated();
                        getSupportActionBar().setTitle(referenceNumber);
                    }
                }

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().invalidateOptionsMenu();
            }
        });
    }


    private void initializeFinalize() {
        finalizeFragment = SimpleProductsFragment.newInstance();
        finalizeFragment.setHelper(getHelper());
        finalizeFragment.setSetupActionBar(this);
        finalizeFragment.setIsFinalize(true);
        finalizeFragment.setProductsFragmentListener(this);
        finalizeFragment.setReturnItems(isReturnItems);
    }

    private void initializeProducts() {
        simpleProductsFragment = SimpleProductsFragment.newInstance();
        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);
        simpleProductsFragment.setReturnItems(isReturnItems);
        Log.e("IS_RETURN_ITEMS", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + isReturnItems);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(imonggoSwableServiceConnection != null)
            SwableTools.unbindSwable(this, imonggoSwableServiceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (concessioModule == ConcessioModule.PHYSICAL_COUNT) {
//            simpleProductsFragment.refreshList();
//            if (getSupportFragmentManager().findFragmentByTag("finalize") != null)
//                finalizeFragment.refreshList();
//        }
        if(concessioModule == ConcessioModule.HISTORY || concessioModule == ConcessioModule.LAYAWAY) {
            if(imonggoSwableServiceConnection == null) {
                imonggoSwableServiceConnection = new ImonggoSwableServiceConnection(simpleTransactionsFragment);
                SwableTools.bindSwable(this, imonggoSwableServiceConnection);
            }
        }
        if (concessioModule == ConcessioModule.CUSTOMERS)
            simpleCustomersFragment.deselectCustomers();
    }

    @Override
    public void onBackPressed() {
        if (concessioModule == ConcessioModule.HISTORY)
            llFooter.setVisibility(View.GONE);
        if (changeToReview)
            btn1.setText("REVIEW");
        if (concessioModule == ConcessioModule.RECEIVE_SUPPLIER || concessioModule == ConcessioModule.RELEASE_SUPPLIER)
            simpleInventoryFragment.refreshList();
        if (concessioModule == ConcessioModule.STOCK_REQUEST || concessioModule == ConcessioModule.PHYSICAL_COUNT)
            simpleProductsFragment.refreshList();
        if (concessioModule == ConcessioModule.RELEASE_BRANCH)
            simplePulloutFragment.refreshList();
//        if (getModuleSetting(concessioModule) != null) { TODO Remove!
//            if (getModuleSetting(concessioModule).isRequire_document_reason()) {
//                if (concessioModule == ConcessioModule.RELEASE_BRANCH) {
//                    if (simplePulloutToolbarExt != null)
//                        simplePulloutToolbarExt.attachAfter(this, toolbar);
//                } else {
//                    if (simplePulloutToolbarExt != null)
//                        simplePulloutToolbarExt.detach();
//                }
//            }
//        }
        if (refreshCustomerList) {
            setResult(REFRESH);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e("onCreateOptionsMenu", hasMenu+" || "+concessioModule.toString());
        if (hasMenu) {
            if(concessioModule == ConcessioModule.HISTORY && getSupportFragmentManager().getBackStackEntryCount() == 1) {
                if(getModuleSetting(simpleTransactionDetailsFragment.getOfflineData().getConcessioModule()).isCan_print())
                    getMenuInflater().inflate(R.menu.others_menu, menu);
            }
            else if(concessioModule == ConcessioModule.CUSTOMERS) {
                getMenuInflater().inflate(R.menu.simple_customers_menu, menu);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setTitle("Customers");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                if(!getModuleSetting(ConcessioModule.CUSTOMERS).isCan_add())
                    menu.findItem(R.id.mAddCustomer).setVisible(false);

                mSearch = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
                mSearch.setQueryHint("Search customer");
                initializeSearchViewEx(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String newText) {
                        simpleCustomersFragment.updateListWhenSearch(newText);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        simpleCustomersFragment.updateListWhenSearch(newText);
                        return true;
                    }

                });
            }
            else if(concessioModule == ConcessioModule.CUSTOMER_DETAILS) {
                if(isFromCustomersList) {
                    getMenuInflater().inflate(R.menu.simple_edit_menu, menu);

                    if (!getModuleSetting(ConcessioModule.CUSTOMERS).isCan_edit())
                        menu.findItem(R.id.mEditCustomer).setVisible(false);
                }
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            else if(concessioModule != ConcessioModule.ROUTE_PLAN) {
                getMenuInflater().inflate(R.menu.simple_products_menu, menu);
                menu.findItem(R.id.mHistory).setVisible(false);
                menu.findItem(R.id.mLogout).setVisible(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                mSearch = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
                if(showsCustomer)
                    mSearch.setQueryHint("Search customer");
                else if(concessioModule == ConcessioModule.HISTORY)
                    mSearch.setQueryHint("Search transaction");
                else
                    mSearch.setQueryHint("Search product");
                initializeSearchViewEx(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String newText) {
                        if(concessioModule == ConcessioModule.RECEIVE_SUPPLIER || concessioModule ==  ConcessioModule.RELEASE_SUPPLIER)
                            simpleInventoryFragment.updateListWhenSearch(newText);
                        else if(concessioModule == ConcessioModule.CUSTOMERS || showsCustomer)
                            simpleCustomersFragment.updateListWhenSearch(newText);
                        else if(concessioModule == ConcessioModule.HISTORY)
                            simpleTransactionsFragment.updateListWhenSearch(newText);
                        else
                            simpleProductsFragment.updateListWhenSearch(newText);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if(concessioModule == ConcessioModule.RECEIVE_SUPPLIER || concessioModule ==  ConcessioModule.RELEASE_SUPPLIER)
                            simpleInventoryFragment.updateListWhenSearch(newText);
                        else if(concessioModule == ConcessioModule.CUSTOMERS || showsCustomer)
                            simpleCustomersFragment.updateListWhenSearch(newText);
                        else if(concessioModule == ConcessioModule.HISTORY || concessioModule == ConcessioModule.LAYAWAY)
                            simpleTransactionsFragment.updateListWhenSearch(newText);
                        else
                            simpleProductsFragment.updateListWhenSearch(newText);
                        return true;
                    }

                });
            }
            else {
                getMenuInflater().inflate(R.menu.simple_search_menu, menu);
                mSearch = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
                mSearch.setQueryHint("Search customer");
                initializeSearchViewEx(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        simpleRoutePlanFragment.updateListWhenSearch(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        simpleRoutePlanFragment.updateListWhenSearch(newText);
                        return true;
                    }
                });
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mAddCustomer) {
            Intent intent = new Intent(this, AddEditCustomerActivity.class);
            startActivityForResult(intent, ADD_CUSTOMER);
        } else if (item.getItemId() == R.id.mEditCustomer) {
            Log.e("EditCustomer", customer.getId()+"---");
            Intent intent = new Intent(this, AddEditCustomerActivity.class);
            intent.putExtra(CUSTOMER_ID, customer.getId());
            startActivityForResult(intent, EDIT_CUSTOMER);
        }
        else if(item.getItemId() == R.id.mPrint) {
            if(getAppSetting().isCan_print()) {
                if(!EpsonPrinterTools.targetPrinter(C_Module.this).equals(""))
                    printTransaction(simpleTransactionDetailsFragment.getOfflineData(), "*Salesman Copy*", "*Office Copy*");
                if(!StarIOPrinterTools.getTargetPrinter(C_Module.this).equals(""))
                    printTransactionStar(simpleTransactionDetailsFragment.getOfflineData(), "*Salesman Copy*", "*Office Copy*");

//                AsyncTask<Void, Void, Void> startPrint = new AsyncTask<Void, Void, Void>() {
//                    @Override
//                    protected Void doInBackground(Void... params) {
//
//                        return null;
//                    }
//                };
//                startPrint.execute();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CUSTOMER) {
            if (resultCode == SUCCESS) {
                final int customerId = data.getIntExtra(CUSTOMER_ID, -1);
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        simpleCustomersFragment.reinitializeList();

                        Intent intent = new Intent(C_Module.this, C_Module.class);
                        intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, customerId);
                        intent.putExtra(ModuleActivity.FROM_CUSTOMERS_LIST, isFromCustomersList);
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.CUSTOMER_DETAILS.ordinal());
                        startActivityForResult(intent, ALL_CUSTOMERS);
                    }
                };
                handler.sendEmptyMessageDelayed(0, 100);
            }
        } else if (requestCode == EDIT_CUSTOMER) {
            if (resultCode == SUCCESS) {
                int customerId = data.getIntExtra(CUSTOMER_ID, -1);
                customer = retrieveCustomer(customerId);
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        refreshCustomerList = true;
                        simpleCustomerDetailsFragment.setCustomer(customer);
                        simpleCustomerDetailsFragment.renderCustomerDetails(true);
                    }
                };
                handler.sendEmptyMessageDelayed(0, 100);
            }
        } else if (requestCode == REVIEW_SALES) {
            if (resultCode == SUCCESS) {
                if(data.hasExtra(FOR_HISTORY_DETAIL))
                    setResult(SUCCESS, data);
                else
                    setResult(SUCCESS);
                finish();
            } else {
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if(simpleProductsFragment != null)
                            simpleProductsFragment.refreshList();
                    }
                };
                handler.sendEmptyMessageDelayed(0, 100);
            }
        } else if (requestCode == ALL_CUSTOMERS) {
            if (resultCode == REFRESH) {
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        simpleCustomersFragment.reinitializeList();
                    }
                };
                handler.sendEmptyMessageDelayed(0, 100);
            }
        } else if (requestCode == SALES) {
            if (resultCode == SUCCESS) {
                setResult(SUCCESS);
                finish();
            }
        } else if(requestCode == ROUTE_PLAN) {
            if (resultCode == SUCCESS) {
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        simpleRoutePlanFragment.refresh();
                    }
                };
                handler.sendEmptyMessageDelayed(0, 50);
            }
        } else if(requestCode == IS_DUPLICATING) {
            if (resultCode == SUCCESS) {
                Log.e("IS_DUPLICATING", "success");
                onBackPressed();
                OfflineData newOfflineData = OfflineData.fetchById(getHelper(), OfflineData.class, data.getIntExtra(FOR_HISTORY_DETAIL, 0));
                simpleTransactionsFragment.addOfflineData(newOfflineData);
//                setResult(SUCCESS);
//                finish();
            }
        } else if(requestCode == HISTORY_DETAILS) {
            if (resultCode == SUCCESS) {
                Log.e("HISTORY_DETAILS", "success");
//                onBackPressed();
                OfflineData newOfflineData = OfflineData.fetchById(getHelper(), OfflineData.class, data.getIntExtra(FOR_HISTORY_DETAIL, 0));
                simpleTransactionsFragment.addOfflineData(newOfflineData);
            }
        } else if(requestCode == FROM_MULTIINPUT) {
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if(simpleProductsFragment != null)
                        simpleProductsFragment.refreshList();
                    if (getSupportFragmentManager().findFragmentByTag("finalize") != null)
                        finalizeFragment.refreshList();
                }
            };
            handler.sendEmptyMessageDelayed(0, 100);
        }
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        this.toolbar = toolbar;
        if (getModuleSetting(concessioModule) != null) {
            if (getModuleSetting(concessioModule).isRequire_document_reason()) {
//                if (concessioModule == ConcessioModule.RELEASE_BRANCH) { TODO Remove!
//                    if (simplePulloutToolbarExt == null)
//                        simplePulloutToolbarExt = new SimplePulloutToolbarExt();
//                    simplePulloutToolbarExt.attachAfter(this, this.toolbar);
//                    simplePulloutToolbarExt.setOnClickListener(new SimplePulloutToolbarExt.OnToolbarClickedListener() {
//                        @Override
//                        public void onClick() {
//                            simplePulloutRequestDialog.show();
//                        }
//                    });
//                } else {
//                    if (simplePulloutToolbarExt != null)
//                        simplePulloutToolbarExt.detach();
//                }
            }
        }
        if (concessioModule == ConcessioModule.ROUTE_PLAN) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (concessioModule == ConcessioModule.CUSTOMER_DETAILS) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(customer.getName());
        }
        if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
            Log.e("release adjustment", "YEAH");
            if (!simpleCustomersFragment.isHasSelected()) {
                Log.e("release adjustment", "isSelected");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setTitle("MSO");
                getSupportActionBar().invalidateOptionsMenu();
            }
        }
        if (concessioModule == ConcessioModule.RELEASE_BRANCH)
            if(getModuleSetting(concessioModule).isRequire_document_reason() && getSupportFragmentManager().findFragmentByTag("finalize") == null) {
                simplePulloutFragment.showReasonDialog(true, getModuleSetting(concessioModule).isRequire_document_reason());
            }
        if (concessioModule == ConcessioModule.HISTORY)
            whenItemsSelectedUpdated();
        if(concessioModule == ConcessioModule.LAYAWAY) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Layaway");
        }
    }

    /**
     * Not yet in use...
     */
    private MultiInputListener multiInputListener = new MultiInputListener() {
        @Override
        public void showInputScreen(Product product) {
            Intent intent = new Intent(C_Module.this, C_MultiInput.class);
            intent.putExtra(MultiInputSelectedItemFragment.PRODUCT_ID, product.getId());
            startActivityForResult(intent, FROM_MULTIINPUT);
        }
    };

    /**
     * This is used by the SimpleProductsFragment
     */
    @Override
    public void whenItemsSelectedUpdated() {
        Log.e("whenSelectedUpdated", "is called");
        if(!getModuleSetting(concessioModule).is_view())
            toggleNext(llFooter, tvItems);
    }

    private void prepareFooter() {
        Log.e("prepareFooter", "is called");
        llFooter.setVisibility(View.VISIBLE);
        llFooter.setTranslationY(1000f);
        tvItems.setVisibility(View.VISIBLE);
    }

    private View.OnClickListener nextClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (btn1.getText().toString().equals("SEND")) {
                if(isButtonTapped)
                    return;
                isButtonTapped = true;
                if(concessioModule == ConcessioModule.RECEIVE_BRANCH_PULLOUT) {
                    if(getWarehouse() != null) {
                        DialogTools.showConfirmationDialog(C_Module.this, "Send", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(isButtonTapped)
                                    return;

                                isButtonTapped = true;

                                Gson gson = new GsonBuilder().serializeNulls().create();

                                Branch warehouse = Branch.fetchById(getHelper(), Branch.class, ProductsAdapterHelper.getWarehouse_id());
                                Document document = generateDocument(C_Module.this, ProductsAdapterHelper.getWarehouse_id(), DocumentTypeCode.identify(concessioModule));

                                try {
                                    JSONObject jsonObject = new JSONObject(gson.toJson(document));
                                    Log.e("jsonObject", jsonObject.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                offlineData = new SwableTools.Transaction(getHelper())
                                        .toSend()
                                        .forBranch(warehouse)
                                        .object(document)
                                        .fromModule(concessioModule)
                                        .queue();

                                TransactionDialog transactionDialog = new TransactionDialog(C_Module.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                                transactionDialog.setTitle(concessioModule);
                                String dateTime = DateTimeTools.convertFromTo(offlineData.getDateCreated(), "cccc, MMM. dd, yyyy, h:mma", Calendar.getInstance().getTimeZone());
                                transactionDialog.setInStock(dateTime);
                                transactionDialog.setTransactionDialogListener(new TransactionDialog.TransactionDialogListener() {
                                    @Override
                                    public void whenDismissed() {
                                        isButtonTapped = false;
                                        ProductsAdapterHelper.clearSelectedProductItemList(true);
                                        ProductsAdapterHelper.clearSelectedReturnProductItemList();

                                        simpleProductsFragment.refreshList();
                                        finalizeFragment.refreshList();
                                        if (!mSearch.isIconified())
                                            closeSearchField(mSearch);
                                        onBackPressed();

                                        searchDRDialog.show();
                                    }
                                });
                                transactionDialog.show();

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
                    else
                        DialogTools.showDialog(C_Module.this, "Ooops!", "You have no assigned warehouse on your account. Please contact admin.");
                    return;
                }
                if (getBranches().size() == 1) {
                    final Branch branch = getBranches().get(0);
                    DialogTools.showConfirmationDialog(C_Module.this, "Send", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (isButtonTapped)
                                return;

                            isButtonTapped = true;

                            Gson gson = new GsonBuilder().serializeNulls().create();
                            if (concessioModule == ConcessioModule.STOCK_REQUEST) {
                                Order order = generateOrder(getApplicationContext(), branch.getId());

                                try {
                                    JSONObject jsonObject = new JSONObject(gson.toJson(order));
                                    Log.e("jsonObject", jsonObject.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                offlineData = new SwableTools.Transaction(getHelper())
                                        .toSend()
                                        .forBranch(branch)
                                        .fromModule(ConcessioModule.STOCK_REQUEST)
                                        .object(order)
                                        .queue();
                            } else {
                                int warehouseId = 0;
                                if (getBranches(true).size() > 0)
                                    warehouseId = getBranches(true).get(0).getId();
                                Document document = generateDocument(C_Module.this, warehouseId, DocumentTypeCode.identify(concessioModule));

                                try {
                                    JSONObject jsonObject = new JSONObject(gson.toJson(document));
                                    Log.e("jsonObject", jsonObject.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                                    Extras extras = new Extras();
                                    extras.setCustomer_id(ProductsAdapterHelper.getSelectedCustomer().getReturnId());
                                    document.setExtras(extras);
                                }

                                if (getAppSetting().isCan_change_inventory())
                                    updateInventoryFromSelectedItemList(concessioModule == ConcessioModule.RECEIVE_SUPPLIER);

                                offlineData = new SwableTools.Transaction(getHelper())
                                        .toSend()
                                        .forBranch(branch)
                                        .object(document)
                                        .fromModule(concessioModule)
                                        .category(simpleProductsFragment != null ? simpleProductsFragment.getCategory() : "")
                                        .documentReason(ProductsAdapterHelper.getReason() != null ? ProductsAdapterHelper.getReason().getName() : "")
                                        .queue();
                                if (getAppSetting().isCan_print() && getModuleSetting(concessioModule).isCan_print()) {
                                    if (!EpsonPrinterTools.targetPrinter(C_Module.this).equals(""))
                                        printTransaction(offlineData, "*Salesman Copy*", "*Office Copy*");
                                    if (!StarIOPrinterTools.getTargetPrinter(C_Module.this).equals(""))
                                        printTransactionStar(offlineData, "*Salesman Copy*", "*Office Copy*");
                                }
                            }

                            TransactionDialog transactionDialog = new TransactionDialog(C_Module.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                            transactionDialog.setTitle(concessioModule);
                            String dateTime = DateTimeTools.convertFromTo(offlineData.getDateCreated(), "cccc, MMM. dd, yyyy, h:mma", Calendar.getInstance().getTimeZone());
                            transactionDialog.setInStock(dateTime);
                            transactionDialog.setTransactionDialogListener(new TransactionDialog.TransactionDialogListener() {
                                @Override
                                public void whenDismissed() {
                                    isButtonTapped = false;

                                    ProductsAdapterHelper.clearSelectedProductItemList(true);
                                    ProductsAdapterHelper.clearSelectedReturnProductItemList();

                                    if (ProductsAdapterHelper.isDuplicating) {
                                        Intent intent = new Intent();
                                        intent.putExtra(FOR_HISTORY_DETAIL, offlineData.getId());
                                        setResult(SUCCESS, intent);
                                        finish();
                                        return;
                                    }

                                    if (concessioModule == ConcessioModule.STOCK_REQUEST) {
                                        simpleProductsFragment.refreshList();
                                        finalizeFragment.refreshList();
                                        onBackPressed();
                                    } else if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                                        llFooter.setVisibility(View.GONE);
                                        simpleCustomersFragment.setHasSelected(false);
                                        simpleCustomersFragment.onViewCreated(null, null);
                                        hasMenu = true;
                                        showsCustomer = true;

                                        getSupportFragmentManager().popBackStackImmediate(); // finalize
                                        getSupportFragmentManager().popBackStackImmediate(); // product list
                                        previousFragmentCount = 0;
                                        Log.e("simple", "called customer fragment");
                                    } else {
                                        Log.e("whenDismissed", " should backpressed");
                                        // TODO
                                        if (!mSearch.isIconified())
                                            closeSearchField(mSearch);
                                        onBackPressed();

                                        if (concessioModule == ConcessioModule.RELEASE_BRANCH)
                                            simplePulloutFragment.showReasonDialog(true, getModuleSetting(concessioModule).isRequire_document_reason()); // add settings
                                    }
                                }
                            });
                            transactionDialog.show();

                        }
                    }, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isButtonTapped = false;
                        }
                    }, new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Log.e("onShow", isButtonTapped+"<---");
                            isButtonTapped = false;
                        }
                    }, R.style.AppCompatDialogStyle_Light);
                } else {
                    new MaterialDialog.Builder(C_Module.this)
                            .title("Choose branch")
                            .items(getBranches())
                            .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    final Branch branch = getBranches().get(which);

                                    final Branch warehouse = getWarehouse();
                                    if (warehouse == null && getModuleSetting(concessioModule).isRequire_warehouse())
                                        DialogTools.showDialog(C_Module.this, "Ooops!", "You have no warehouse. Kindly contact your admin.", R.style.AppCompatDialogStyle_Light);
                                    else {
                                        DialogTools.showConfirmationDialog(C_Module.this, "Send", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (isButtonTapped)
                                                    return;

                                                isButtonTapped = true;

                                                Gson gson = new GsonBuilder().serializeNulls().create();

                                                if (concessioModule == ConcessioModule.STOCK_REQUEST) {
                                                    Order order = generateOrder(getApplicationContext(), branch.getId());

                                                    try {
                                                        JSONObject jsonObject = new JSONObject(gson.toJson(order));
                                                        Log.e("jsonObject", jsonObject.toString());
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                    offlineData = new SwableTools.Transaction(getHelper())
                                                            .toSend()
                                                            .forBranch(branch)
                                                            .fromModule(ConcessioModule.STOCK_REQUEST)
                                                            .object(order)
                                                            .queue();
                                                } else {
                                                    int warehouseId = 0;
                                                    if (getBranches(true).size() > 0)
                                                        warehouseId = getBranches(true).get(0).getId();

                                                    Document document = generateDocument(C_Module.this, warehouseId, DocumentTypeCode.identify(concessioModule));

                                                    if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
//                                                  document.setDocument_purpose_id(ProductsAdapterHelper.getReason().getId());

                                                        Extras extras = new Extras();
                                                        extras.setCustomer_id(ProductsAdapterHelper.getSelectedCustomer().getReturnId());
                                                        document.setExtras(extras);
                                                    }

                                                    try {
                                                        JSONObject jsonObject = new JSONObject(gson.toJson(document));
                                                        Log.e("jsonObject", jsonObject.toString());
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                    if (getAppSetting().isCan_change_inventory())
                                                        updateInventoryFromSelectedItemList(concessioModule == ConcessioModule.RECEIVE_SUPPLIER);

                                                    offlineData = new SwableTools.Transaction(getHelper())
                                                            .toSend()
                                                            .forBranch(branch)
                                                            .object(document)
                                                            .fromModule(concessioModule)
                                                            .category(simpleProductsFragment != null ? simpleProductsFragment.getCategory() : "")
                                                            .documentReason(ProductsAdapterHelper.getReason() != null ? ProductsAdapterHelper.getReason().getName() : "")
                                                            .queue();

                                                    if (getAppSetting().isCan_print() && getModuleSetting(concessioModule).isCan_print()) {
                                                        if (!EpsonPrinterTools.targetPrinter(C_Module.this).equals(""))
                                                            printTransaction(offlineData, "*Salesman Copy*", "*Office Copy*");
                                                        if (!StarIOPrinterTools.getTargetPrinter(C_Module.this).equals(""))
                                                            printTransactionStar(offlineData, "*Salesman Copy*", "*Office Copy*");
                                                    }
                                                }


                                                TransactionDialog transactionDialog = new TransactionDialog(C_Module.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                                                transactionDialog.setTitle(concessioModule);
                                                String dateTime = DateTimeTools.convertFromTo(offlineData.getDateCreated(), "cccc, MMM. dd, yyyy, h:mma", Calendar.getInstance().getTimeZone());
                                                transactionDialog.setInStock(dateTime);
                                                transactionDialog.setTransactionDialogListener(new TransactionDialog.TransactionDialogListener() {
                                                    @Override
                                                    public void whenDismissed() {
                                                        isButtonTapped = false;

                                                        ProductsAdapterHelper.clearSelectedProductItemList(true);
                                                        ProductsAdapterHelper.clearSelectedReturnProductItemList();

                                                        if (ProductsAdapterHelper.isDuplicating) {
                                                            Intent intent = new Intent();
                                                            intent.putExtra(FOR_HISTORY_DETAIL, offlineData.getId());
                                                            setResult(SUCCESS, intent);
                                                            finish();
                                                            return;
                                                        }

                                                        if (concessioModule == ConcessioModule.STOCK_REQUEST || concessioModule == ConcessioModule.PHYSICAL_COUNT) {
                                                            simpleProductsFragment.refreshList();
                                                            finalizeFragment.refreshList();
                                                            onBackPressed();
                                                        } else if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                                                            llFooter.setVisibility(View.GONE);
                                                            simpleCustomersFragment.setHasSelected(false);
                                                            simpleCustomersFragment.onViewCreated(null, null);


                                                            hasMenu = true;
                                                            showsCustomer = true;

                                                            Log.e("simple", "called customer fragment");

                                                            getSupportFragmentManager().popBackStackImmediate(); // finalize
                                                            getSupportFragmentManager().popBackStackImmediate(); // product list
                                                            previousFragmentCount = 0;
                                                        } else {
                                                            // TODO
                                                            if (!mSearch.isIconified())
                                                                closeSearchField(mSearch);
                                                            onBackPressed();

                                                            if (concessioModule == ConcessioModule.RELEASE_BRANCH)
                                                                simplePulloutFragment.showReasonDialog(true, getModuleSetting(concessioModule).isRequire_document_reason()); // add settings
                                                        }
                                                    }
                                                });
                                                transactionDialog.show();

                                            }
                                        }, "No", R.style.AppCompatDialogStyle_Light, new DialogInterface.OnShowListener() {
                                            @Override
                                            public void onShow(DialogInterface dialog) {
                                                isButtonTapped = false;
                                            }
                                        });
                                    }
                                    return true;
                                }
                            })
                            .positiveText("YES")
                            .positiveColor(ContextCompat.getColor(C_Module.this, R.color.text_orange))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    isButtonTapped = false;
                                }
                            })
                            .negativeText("NO")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    isButtonTapped = false;
                                }
                            })
                            .negativeColor(ContextCompat.getColor(C_Module.this, R.color.text_orange))
                            .widgetColor(ContextCompat.getColor(C_Module.this, R.color.text_orange))
                            .show();
                }
            } else {
                if (ProductsAdapterHelper.getSelectedProductItems().isEmpty())
                    DialogTools.showDialog(C_Module.this, "Ooops!", "You have no selected items. Kindly select first products.");
                else if (isReturnItems)
                    onBackPressed();
                else if (concessioModule == ConcessioModule.INVOICE) {
                    Log.e("INVOICE", "Finalizing....");
                    Intent intent = new Intent(C_Module.this, C_Finalize.class);
                    intent.putExtra(CONCESSIO_MODULE, concessioModule.ordinal());
                    startActivityForResult(intent, REVIEW_SALES);
                } else {
                    Log.e("FROM PROD LIST", "Sending....");
                    btn1.setText("SEND");
                    finalizeFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .add(R.id.flContent, finalizeFragment, "finalize")
                            .addToBackStack("finalizer")
                            .commit();
                }
            }
        }
    };

    private void printTransactionStar(final OfflineData offlineData, final String... labels) {
        if(!BluetoothTools.isEnabled())
            return;
        Branch branch = getBranches().get(0);
        ArrayList<byte[]> data = new ArrayList<>();

        try {
            for(int i = 0;i < labels.length;i++) {
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)0,
                data.add((branch.getName()+"\r\n").getBytes());
                data.add((branch.generateAddress()+"\r\n\r\n").getBytes());

                if(offlineData != null && offlineData.getConcessioModule() == ConcessioModule.RELEASE_ADJUSTMENT)
                    data.add(("MISCELLANEOUS STOCK OUT SLIP\r\n\r\n").getBytes());
                else
                    data.add(("INVENTORY SLIP\r\n\r\n").getBytes());
//                data.add(("Salesman: "+getSession().getUser().getName()+"\r\n").getBytes());
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                data.add((EpsonPrinterTools.tabber("Salesman: ", getSession().getUser().getName(), 32)+"\r\n").getBytes());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                if(offlineData != null) {
                    data.add(("Ref #: "+offlineData.getReference_no()+"\r\n").getBytes());
                    data.add(("Date: " + simpleDateFormat.format(offlineData.getDateCreated())+"\r\n").getBytes());
                    if(offlineData.getConcessioModule() == ConcessioModule.RELEASE_ADJUSTMENT) {
                        data.add(("Company: " + offlineData.getCategory().toUpperCase()+"\r\n").getBytes()); // TODO,
                        data.add((EpsonPrinterTools.tabber("Reason: ", offlineData.getDocumentReason(), 32)+"\r\n").getBytes()); //ProductsAdapterHelper.getReason().getName()
                    }
                }
                else
                    data.add(("Date: " + simpleDateFormat.format(Calendar.getInstance().getTime())+"\r\n").getBytes());


                double totalQuantity = 0.0;
                double totalAmount = 0.0;
                data.add("================================".getBytes());
                data.add("Quantity                  Amount".getBytes());
                data.add("================================".getBytes());

                if (offlineData != null && offlineData.getType() == OfflineData.DOCUMENT &&
                        (concessioModule == ConcessioModule.RECEIVE_SUPPLIER
                                || concessioModule == ConcessioModule.RELEASE_SUPPLIER
                                || concessioModule == ConcessioModule.RELEASE_ADJUSTMENT
                                || concessioModule == ConcessioModule.HISTORY)) {
                    for (final DocumentLine documentLine : offlineData.getObjectFromData(Document.class).getDocument_lines()) {
                        Double retail_price = 0.0;
                        try {
                            final BranchProduct branchProduct = getHelper().fetchForeignCollection(documentLine.getProduct().getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
                                @Override
                                public boolean validate(BranchProduct obj) {
                                    if(documentLine.getUnit_id() == null) {
                                        if(obj.getUnit() == null)
                                            return true;
                                    }
                                    else if(obj.getUnit() != null && documentLine.getUnit_id() == obj.getUnit().getId())
                                        return true;
                                    return false;
                                }
                            }, 0);

                            Unit unit = null;
                            if(branchProduct != null)
                                unit = branchProduct.getUnit();
                            retail_price = PriceTools.identifyRetailPrice(getHelper(), documentLine.getProduct(), branch, null, null, unit);

                            if(retail_price == null)
                                retail_price = documentLine.getRetail_price();
                            Log.e("identified retail_price", retail_price.toString());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                        data.add((documentLine.getProduct().getName() + "\r\n").getBytes());
                        Log.e("documentLine.unit_id", documentLine.getUnit_id()+" --- ");
                        if (documentLine.getUnit_id() != null) {
                            totalQuantity += documentLine.getUnit_quantity();
                            data.add(("  " + documentLine.getUnit_quantity() + "   " + documentLine.getUnit_name() + " x " + NumberTools.separateInCommas(retail_price)+"\r\n").getBytes());
                            Double subtotal = documentLine.getUnit_quantity() * retail_price;
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                            data.add((NumberTools.separateInCommas(subtotal)+"\r\n").getBytes());
                            totalAmount += subtotal;
                        } else {
                            totalQuantity += documentLine.getQuantity();
                            data.add(("  " + documentLine.getQuantity() + "   " + documentLine.getProduct().getBase_unit_name() + " x " + NumberTools.separateInCommas(retail_price) + "\r\n").getBytes());
                            Double subtotal = documentLine.getQuantity() * retail_price;
                            data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Right
                            data.add((NumberTools.separateInCommas(subtotal)+"\r\n").getBytes());
                            totalAmount += subtotal;
                        }
                    }
                }
                else {
                    QueryBuilder<Inventory, Integer> currentInventories = getHelper().fetchObjectsInt(Inventory.class).queryBuilder();
                    currentInventories.selectColumns("id");
                    currentInventories.where().gt("quantity", 0.0);

                    ProductSorting productSorting = getHelper().fetchForeignCollection(getAppSetting().getProductSortings().closeableIterator(), new ImonggoDBHelper2.Conditional<ProductSorting>() {
                        @Override
                        public boolean validate(ProductSorting obj) {
                            if(obj.is_default())
                                return true;
                            return false;
                        }
                    }, 0);
                    List<Product> products = getHelper().fetchObjects(Product.class).queryBuilder()
                            .orderBy(productSorting.getColumn(), true)
                            .where()
                            .isNotNull("inventory_id").and()
                            .in("inventory_id", currentInventories)
                            .query();
                    for(Product product : products) {
                        Double retail_price = 0.0;
                        final Unit unit = Unit.fetchById(getHelper(), Unit.class, product.getExtras().getDefault_selling_unit());
                        try {
                            final BranchProduct branchProduct = getHelper().fetchForeignCollection(product.getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
                                @Override
                                public boolean validate(BranchProduct obj) {
                                    if(unit == null) {
                                        if(obj.getUnit() == null)
                                            return true;
                                    }
                                    else if(obj.getUnit() != null && unit.getId() == obj.getUnit().getId())
                                        return true;
                                    return false;
                                }
                            }, 0);

                            retail_price = PriceTools.identifyRetailPrice(getHelper(), product, branch, null, null, unit);

                            if(retail_price == null)
                                retail_price = branchProduct.getUnit_retail_price();
                            Log.e("identified retail_price", retail_price.toString());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                        data.add((product.getName() + "\r\n").getBytes());

                        totalQuantity += product.getInventory().getQuantity();

                        data.add(("  " + product.getInventory().getQuantity() + "   "
                                + (unit == null ? product.getBase_unit_name() : unit.getName()) + " x "
                                + NumberTools.separateInCommas(retail_price)+"\r\n").getBytes());
                        Double subtotal = product.getInventory().getQuantity() * retail_price;

                        totalAmount += subtotal;

                        data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x02 }); // Left
                        data.add((NumberTools.separateInCommas(subtotal)+"\r\n").getBytes());
                    }
                }

                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x00 }); // Left
                data.add(("--------------------------------").getBytes());
                data.add(("Total Quantity: " + NumberTools.separateInCommas(totalQuantity) + "\r\n").getBytes());
                data.add((EpsonPrinterTools.spacer("Total Order Amount: ", NumberTools.separateInCommas(totalAmount), 32)+"\r\n\r\n").getBytes());
                data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Left
                data.add(labels[i].getBytes());
                if(simpleTransactionDetailsFragment != null) {
                    data.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 }); // Center
                    data.add("\r\n** This is a reprint **\r\n".getBytes());
                }
                if(i < labels.length-1) {
                    data.add(("\r\n\r\n\r\n").getBytes());
                    data.add(("- - - - - - CUT HERE - - - - - -\r\n\r\n").getBytes());
                }
                else
                    data.add(("\r\n\r\n").getBytes());

                if(!StarIOPrinterTools.print(this, StarIOPrinterTools.getTargetPrinter(this), "portable", StarIOPaperSize.p2INCH, data))
                    break;
                data.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void printTransaction(final OfflineData offlineData, final String... labels) {
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
                            printer.addFeedLine(2);
                            printer.addText(printText.toString());
                            printer.addFeedLine(2);
                            if(concessioModule == ConcessioModule.RELEASE_ADJUSTMENT)
                                printer.addText("MISCELLANEOUS STOCK OUT SLIP");
                            else
                                printer.addText("INVENTORY SLIP");
                            printer.addFeedLine(2);
//                            printer.addText("Salesman: " + getSession().getUser().getName() + "\n");
                            printText.delete(0, printText.length());
                            printer.addTextAlign(Printer.ALIGN_LEFT);
                            printer.addText(EpsonPrinterTools.tabber("Salesman: ", getSession().getUser().getName(), 32) + "\n");
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                            if(offlineData != null) {
                                printer.addText("Ref #: " + offlineData.getReference_no() + "\n");
                                printer.addText("Date: " + simpleDateFormat.format(offlineData.getDateCreated()) + "\n");
                                if(concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                                    printer.addText("Company: " + simpleProductsFragment.getCategory().toUpperCase() + "\n");
                                    printer.addText(EpsonPrinterTools.tabber("Reason: ", ProductsAdapterHelper.getReason().getName(), 32) + "\n");
                                }
                            }
                            else
                                printer.addText("Date: " + simpleDateFormat.format(Calendar.getInstance().getTime()) + "\n");

                            // ---------- HEADER
                            double totalQuantity = 0.0;
                            double totalAmount = 0.0;
                            printer.addText("================================");
                            printer.addText("Quantity                  Amount");
                            printer.addText("================================");
                            if (offlineData != null && offlineData.getType() == OfflineData.DOCUMENT &&
                                    (concessioModule == ConcessioModule.RECEIVE_SUPPLIER || concessioModule == ConcessioModule.RELEASE_SUPPLIER || concessioModule == ConcessioModule.RELEASE_ADJUSTMENT)) {
                                for (final DocumentLine documentLine : offlineData.getObjectFromData(Document.class).getDocument_lines()) {
                                    Double retail_price = 0.0;
                                    try {
                                        final BranchProduct branchProduct = getHelper().fetchForeignCollection(documentLine.getProduct().getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
                                            @Override
                                            public boolean validate(BranchProduct obj) {
                                                if(documentLine.getUnit_id() == null) {
                                                    if(obj.getUnit() == null)
                                                        return true;
                                                }
                                                else if(obj.getUnit() != null && documentLine.getUnit_id() == obj.getUnit().getId())
                                                    return true;
                                                return false;
                                            }
                                        }, 0);

                                        Unit unit = null;
                                        if(branchProduct != null)
                                            unit = branchProduct.getUnit();
                                        retail_price = PriceTools.identifyRetailPrice(getHelper(), documentLine.getProduct(), branch, null, null, unit);

                                        if(retail_price == null)
                                            retail_price = documentLine.getRetail_price();
                                        Log.e("identified retail_price", retail_price.toString());
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                    printer.addTextAlign(Printer.ALIGN_LEFT);
                                    printer.addText(documentLine.getProduct().getName() + "\n");
                                    if (documentLine.getUnit_id() != null) {
                                        totalQuantity += documentLine.getUnit_quantity();
                                        printer.addText("  " + documentLine.getUnit_quantity() + "   " + documentLine.getUnit_name() + " x " + NumberTools.separateInCommas(retail_price)+"\n");
                                        Double subtotal = documentLine.getUnit_quantity() * retail_price;
                                        printer.addTextAlign(Printer.ALIGN_RIGHT);
                                        printer.addText(NumberTools.separateInCommas(subtotal)+"\n");
                                        totalAmount += subtotal;
                                    } else {
                                        totalQuantity += documentLine.getQuantity();
                                        printer.addText("  " + documentLine.getQuantity() + "   " + documentLine.getProduct().getBase_unit_name() + " x " + NumberTools.separateInCommas(retail_price) + "\n");
                                        Double subtotal = documentLine.getQuantity() * retail_price;
                                        printer.addTextAlign(Printer.ALIGN_RIGHT);
                                        printer.addText(NumberTools.separateInCommas(subtotal)+"\n");
                                        totalAmount += subtotal;
                                    }
                                }
                            }
                            else {
                                QueryBuilder<Inventory, Integer> currentInventories = getHelper().fetchObjectsInt(Inventory.class).queryBuilder();
                                currentInventories.selectColumns("id");
                                currentInventories.where().gt("quantity", 0.0);

                                ProductSorting productSorting = getHelper().fetchForeignCollection(getAppSetting().getProductSortings().closeableIterator(), new ImonggoDBHelper2.Conditional<ProductSorting>() {
                                    @Override
                                    public boolean validate(ProductSorting obj) {
                                        if(obj.is_default())
                                            return true;
                                        return false;
                                    }
                                }, 0);
                                List<Product> products = getHelper().fetchObjects(Product.class).queryBuilder()
                                        .orderBy(productSorting.getColumn(), true)
                                        .where()
                                            .isNotNull("inventory_id").and()
                                            .in("inventory_id", currentInventories)
                                        .query();
                                for(Product product : products) {
                                    Double retail_price = 0.0;
                                    final Unit unit = Unit.fetchById(getHelper(), Unit.class, product.getExtras().getDefault_selling_unit());
                                    try {
                                        final BranchProduct branchProduct = getHelper().fetchForeignCollection(product.getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
                                            @Override
                                            public boolean validate(BranchProduct obj) {
                                                if(unit == null) {
                                                    if(obj.getUnit() == null)
                                                        return true;
                                                }
                                                else if(obj.getUnit() != null && unit.getId() == obj.getUnit().getId())
                                                    return true;
                                                return false;
                                            }
                                        }, 0);

                                        retail_price = PriceTools.identifyRetailPrice(getHelper(), product, branch, null, null, unit);

                                        if(retail_price == null)
                                            retail_price = branchProduct.getUnit_retail_price();
                                        Log.e("identified retail_price", retail_price.toString());
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                    printer.addTextAlign(Printer.ALIGN_LEFT);
                                    printer.addText(product.getName() + "\n");
                                    printer.addText("  " + product.getInventory().getQuantity() + "   "
                                            + (unit == null ? product.getBase_unit_name() : unit.getName()) + " x "
                                            + NumberTools.separateInCommas(retail_price)+"\n");
                                    Double subtotal = product.getInventory().getQuantity() * retail_price;
                                    printer.addTextAlign(Printer.ALIGN_RIGHT);
                                    printer.addText(NumberTools.separateInCommas(subtotal)+"\n");
                                }
                            }

                            printer.addTextAlign(Printer.ALIGN_LEFT);
                            printer.addText("--------------------------------");
                            printer.addText("Total Quantity: " + NumberTools.separateInCommas(totalQuantity) + "\n");
                            printer.addText(EpsonPrinterTools.spacer("Total Order Amount: ", NumberTools.separateInCommas(totalAmount), 32)+"\n\n");
                            printer.addTextAlign(Printer.ALIGN_CENTER);
                            printer.addText(labels[i]);
                            if(i < labels.length-1) {
                                printer.addFeedLine(3);
                                printer.addText("- - - - - - CUT HERE - - - - - -\n\n");
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
