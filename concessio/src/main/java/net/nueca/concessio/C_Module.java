package net.nueca.concessio;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.nueca.concessioengine.activities.AddEditCustomerActivity;
import net.nueca.concessioengine.activities.module.ModuleActivity;
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
import net.nueca.concessioengine.tools.AnimationTools;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.concessioengine.views.SimplePulloutToolbarExt;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.swable.ImonggoSwableServiceConnection;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.TimerTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_module);

        if (clearTransactions) {
            ProductsAdapterHelper.clearSelectedProductItemList(initSelectedCustomer);
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
                SwableTools.bindSwable(this, imonggoSwableServiceConnection);

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
                        Log.e("Customer Details", "Clicked!");
                        Intent intent = new Intent(C_Module.this, C_Module.class);
                        intent.putExtra(ModuleActivity.INIT_PRODUCT_ADAPTER_HELPER, true);
                        intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, customer.getId());
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.CUSTOMER_DETAILS.ordinal());
                        startActivity(intent);
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
            case STOCK_REQUEST: { // TODO for Petron

                changeToReview = true;
                initializeProducts();
                simpleProductsFragment.setHelper(getHelper());
                simpleProductsFragment.setListingType(ListingType.SALES);
                simpleProductsFragment.setHasUnits(false);
                simpleProductsFragment.setHasBrand(false);
                simpleProductsFragment.setProductCategories(new ArrayList<String>() {{
                    add("All");
                }});
                simpleProductsFragment.setShowCategoryOnStart(false);
                simpleProductsFragment.setProductsFragmentListener(this);
                simpleProductsFragment.setHasSubtotal(false);
                simpleProductsFragment.setUseRecyclerView(true);

                initializeFinalize();
                finalizeFragment.setListingType(ListingType.SALES);
                finalizeFragment.setHasCategories(false);
                finalizeFragment.setHasDeliveryDate(false);
                finalizeFragment.setHasUnits(false);
                finalizeFragment.setHasSubtotal(true);
                finalizeFragment.setUseSalesProductAdapter(false);

                prepareFooter();


                btn1.setOnClickListener(nextClickedListener);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.flContent, simpleProductsFragment)
                        .commit();


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
                initializeProducts();
                simpleProductsFragment.setProductCategories(getProductCategories(true));
                simpleProductsFragment.setMultipleInput(true);
                simpleProductsFragment.setMultiInputListener(multiInputListener);
                simpleProductsFragment.setListingType(ListingType.SALES);
                simpleProductsFragment.setDisplayOnly(getModuleSetting(concessioModule).is_view());

                llFooter.setVisibility(View.VISIBLE);
                btn1.setText("PRINT INVENTORY");
                btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogTools.showDialog(C_Module.this, "Print Inventory", "This should be printing...", "Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }, R.style.AppCompatDialogStyle_Light);
                    }
                });
                tvItems.setVisibility(View.INVISIBLE);

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
                simpleInventoryFragment.setListingType(ListingType.SALES);
                simpleInventoryFragment.setSetupActionBar(this);
                simpleInventoryFragment.setHasUnits(true);
                simpleInventoryFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing().isLock_category()));
                simpleInventoryFragment.setShowCategoryOnStart(getModuleSetting(concessioModule).getProductListing().isShow_categories_on_start());
                simpleInventoryFragment.setHasSubtotal(false);
                simpleInventoryFragment.setProductsFragmentListener(this);
                // if there's branch product
                simpleInventoryFragment.setBranch(getBranches().get(0));

                initializeFinalize();
                finalizeFragment.setListingType(ListingType.SALES);
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
            case RELEASE_BRANCH: { // TODO for Petron
                changeToReview = true;
                simplePulloutRequestDialog = new SimplePulloutRequestDialog(this, getHelper());
                simplePulloutRequestDialog.setTitle("Choose a reason");
                if (getModuleSetting(concessioModule).isRequire_document_reason())
                    simplePulloutRequestDialog.show();

                simplePulloutFragment = new SimplePulloutFragment();
                simplePulloutFragment.setHelper(getHelper());
                simplePulloutFragment.setSetupActionBar(this);
                simplePulloutFragment.setHasUnits(true);
                simplePulloutFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing().isLock_category()));
                simplePulloutFragment.setShowCategoryOnStart(getModuleSetting(concessioModule).getProductListing().isShow_categories_on_start());

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
                                simpleProductsFragment.setListingType(ListingType.SALES);
                                simpleProductsFragment.setHasUnits(true);
                                // !getModuleSetting().getProductListing().isLock_category()
                                simpleProductsFragment.setProductCategories(getProductCategories(false));
                                simpleProductsFragment.setShowCategoryOnStart(getModuleSetting(concessioModule).getProductListing().isShow_categories_on_start());
                                simpleProductsFragment.setProductsFragmentListener(C_Module.this);

                                initializeFinalize();
                                finalizeFragment.setHasCategories(false);
                                finalizeFragment.setHasBrand(false);
                                finalizeFragment.setHasDeliveryDate(false);
                                finalizeFragment.setHasUnits(true);
                                finalizeFragment.setListingType(ListingType.SALES);

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
                            simpleCustomersFragment.setHasSelected(false);
                            simpleCustomersFragment.onViewCreated(null, null);
                            showsCustomer = true;
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
        SwableTools.unbindSwable(this, imonggoSwableServiceConnection);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (concessioModule == ConcessioModule.PHYSICAL_COUNT) {
            simpleProductsFragment.refreshList();
            if (getSupportFragmentManager().findFragmentByTag("finalize") != null)
                finalizeFragment.refreshList();
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
        if (getModuleSetting(concessioModule) != null) {
            if (getModuleSetting(concessioModule).isRequire_document_reason()) {
                if (concessioModule == ConcessioModule.RELEASE_BRANCH) {
                    if (simplePulloutToolbarExt != null)
                        simplePulloutToolbarExt.attachAfter(this, toolbar);
                } else {
                    if (simplePulloutToolbarExt != null)
                        simplePulloutToolbarExt.detach();
                }
            }
        }
        if (refreshCustomerList) {
            setResult(REFRESH);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e("onCreateOptionsMenu", hasMenu+" || "+concessioModule.toString());
        if (hasMenu) {
            if(concessioModule == ConcessioModule.CUSTOMERS) {
                getMenuInflater().inflate(R.menu.simple_customers_menu, menu);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setTitle("Customers");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                if(isFromCustomersList)
                    getMenuInflater().inflate(R.menu.simple_edit_menu, menu);
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
                        else if(concessioModule == ConcessioModule.HISTORY)
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
            Intent intent = new Intent(this, AddEditCustomerActivity.class);
            intent.putExtra(CUSTOMER_ID, customer.getId());
            startActivityForResult(intent, EDIT_CUSTOMER);
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
                        startActivity(intent);
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
                finish();
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
                if (concessioModule == ConcessioModule.RELEASE_BRANCH) {
                    if (simplePulloutToolbarExt == null)
                        simplePulloutToolbarExt = new SimplePulloutToolbarExt();
                    simplePulloutToolbarExt.attachAfter(this, this.toolbar);
                    simplePulloutToolbarExt.setOnClickListener(new SimplePulloutToolbarExt.OnToolbarClickedListener() {
                        @Override
                        public void onClick() {
                            simplePulloutRequestDialog.show();
                        }
                    });
                } else {
                    if (simplePulloutToolbarExt != null)
                        simplePulloutToolbarExt.detach();
                }
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
            startActivity(intent);
        }
    };

    /**
     * This is used by the SimpleProductsFragment
     */
    @Override
    public void whenItemsSelectedUpdated() {
        toggleNext(llFooter, tvItems);
    }

    private void prepareFooter() {
        llFooter.setVisibility(View.VISIBLE);
        llFooter.setTranslationY(1000f);
        tvItems.setVisibility(View.VISIBLE);
    }

    private View.OnClickListener nextClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (btn1.getText().toString().equals("SEND")) {
                if (getBranches().size() == 1) {
                    final Branch branch = getBranches().get(0);
                    DialogTools.showConfirmationDialog(C_Module.this, "Send", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Gson gson = new GsonBuilder().serializeNulls().create();

                            TimerTools.start("generateDocument");
                            Document document = generateDocument(C_Module.this, branch.getId(), DocumentTypeCode.identify(concessioModule));
                            TimerTools.duration("generateDocument -- end", true);

                            if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                                document.setDocument_purpose_name(ProductsAdapterHelper.getReason().getName());
                                Extras extras = new Extras();
                                extras.setCustomer_id(ProductsAdapterHelper.getSelectedCustomer().getId());
                                document.setExtras(extras);
                            }

                            if (concessioModule == ConcessioModule.STOCK_REQUEST) {

                                try {
                                    int branch_id = getSession().getCurrent_branch_id();
                                    Branch branch = getHelper().fetchObjects(Branch.class).queryBuilder()
                                            .where().eq("id", branch_id).queryForFirst();

                                    Order order = generateOrder(getApplicationContext(), branch_id);

                                    new SwableTools.Transaction(getHelper())
                                            .toSend()
                                            .forBranch(branch)
                                            .fromModule(ConcessioModule.STOCK_REQUEST)
                                            .object(order)
                                            .queue();

                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }


                            try {
                                JSONObject jsonObject = new JSONObject(gson.toJson(document));
                                Log.e("jsonObject", jsonObject.toString());

                                TimerTools.start("updateInventoryFromSelectedItemList");
                                updateInventoryFromSelectedItemList(concessioModule == ConcessioModule.RECEIVE_SUPPLIER);
                                TimerTools.duration("updateInventoryFromSelectedItemList --- end", true);
//                                List<Inventory> inventoryList = getHelper().fetchObjectsList(Inventory.class);
//                                for (Inventory inventory : inventoryList) {
//                                    Log.e("Inventory", inventory.getProduct().getName() + " = " + inventory.getQuantity());
//                                }

                                TimerTools.start("SwableTools");
                                final OfflineData offlineData = new SwableTools.Transaction(getHelper())
                                        .toSend()
                                        .forBranch(branch)
                                        .object(document)
                                        .fromModule(concessioModule)
                                        .queue();
                                TimerTools.duration("SwableTools.queue --- end", true);

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("cccc, MMM. dd, yyyy, K:mma");
                                TransactionDialog transactionDialog = new TransactionDialog(C_Module.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                                transactionDialog.setTitle(concessioModule);
                                transactionDialog.setInStock(simpleDateFormat.format(offlineData.getDateCreated()));
                                transactionDialog.setTransactionDialogListener(new TransactionDialog.TransactionDialogListener() {
                                    @Override
                                    public void whenDismissed() {
                                        ProductsAdapterHelper.clearSelectedProductItemList(true);
                                        ProductsAdapterHelper.clearSelectedReturnProductItemList();

                                        if(ProductsAdapterHelper.isDuplicating) {
                                            Intent intent = new Intent();
                                            intent.putExtra(FOR_HISTORY_DETAIL, offlineData.getId());
                                            setResult(SUCCESS, intent);
                                            finish();
                                            return;
                                        }

                                        if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                                            llFooter.setVisibility(View.GONE);
                                            simpleCustomersFragment.setHasSelected(false);
                                            simpleCustomersFragment.onViewCreated(null, null);
                                            hasMenu = true;
                                            showsCustomer = true;

                                            getSupportFragmentManager().popBackStackImmediate(); // finalize
                                            getSupportFragmentManager().popBackStackImmediate(); // product list
                                            previousFragmentCount = 0;
                                            Log.e("simple", "called customer fragment");
//                                            getSupportFragmentManager()
//                                                    .beginTransaction()
//                                                    .replace(R.id.flContent, simpleCustomersFragment)
//                                                    .commit();
                                        } else {
                                            // TODO
                                            onBackPressed();
                                        }

                                        if (concessioModule == ConcessioModule.STOCK_REQUEST) {
                                            simpleProductsFragment.refreshList();
                                            finalizeFragment.refreshList();
                                        }
                                    }
                                });
                                transactionDialog.show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, "No", R.style.AppCompatDialogStyle_Light);
                } else {
                    DialogTools.showSelectionDialog(C_Module.this,
                            new ArrayAdapter<>(C_Module.this, R.layout.simple_listitem_single_choice, getBranches()),
                            "Yes", new DialogTools.OnItemSelected<Branch>() {
                                @Override
                                public void itemChosen(final Branch branch) {
                                    final Branch warehouse = getWarehouse();
                                    if (warehouse == null && getModuleSetting(concessioModule).isRequire_warehouse())
                                        DialogTools.showDialog(C_Module.this, "Ooops!", "You have no warehouse. Kindly contact your admin.", R.style.AppCompatDialogStyle_Light);
                                    else {
                                        DialogTools.showConfirmationDialog(C_Module.this, "Send", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Gson gson = new GsonBuilder().serializeNulls().create();
                                                Document document = generateDocument(C_Module.this, branch.getId(), DocumentTypeCode.identify(concessioModule));
                                                if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                                                    document.setDocument_purpose_name(ProductsAdapterHelper.getReason().getName());
//                                                  document.setDocument_purpose_id(ProductsAdapterHelper.getReason().getId());

                                                    Extras extras = new Extras();
                                                    extras.setCustomer_id(ProductsAdapterHelper.getSelectedCustomer().getId());
                                                    document.setExtras(extras);
                                                }
                                                try {
                                                    JSONObject jsonObject = new JSONObject(gson.toJson(document));
                                                    Log.e("jsonObject", jsonObject.toString());

                                                    updateInventoryFromSelectedItemList(concessioModule == ConcessioModule.RECEIVE_SUPPLIER);

                                                    final OfflineData offlineData = new SwableTools.Transaction(getHelper())
                                                            .toSend()
                                                            .forBranch(branch)
                                                            .object(document)
                                                            .fromModule(concessioModule)
                                                            .queue();

                                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("cccc, MMM. dd, yyyy, K:mma");
                                                    TransactionDialog transactionDialog = new TransactionDialog(C_Module.this, R.style.AppCompatDialogStyle_Light_NoTitle);
                                                    transactionDialog.setTitle(concessioModule);
                                                    transactionDialog.setInStock(simpleDateFormat.format(offlineData.getDateCreated()));
                                                    transactionDialog.setTransactionDialogListener(new TransactionDialog.TransactionDialogListener() {
                                                        @Override
                                                        public void whenDismissed() {
                                                            ProductsAdapterHelper.clearSelectedProductItemList(true);
                                                            ProductsAdapterHelper.clearSelectedReturnProductItemList();

                                                            if(ProductsAdapterHelper.isDuplicating) {
                                                                Intent intent = new Intent();
                                                                intent.putExtra(FOR_HISTORY_DETAIL, offlineData.getId());
                                                                setResult(SUCCESS, intent);
                                                                finish();
                                                                return;
                                                            }

                                                            if (concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
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
                                                                onBackPressed();
                                                            }
                                                        }
                                                    });
                                                    transactionDialog.show();

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, "No", R.style.AppCompatDialogStyle_Light);
                                    }
                                }
                            }, "No", R.style.AppCompatDialogStyle_Light);
                }
            } else {
                if (ProductsAdapterHelper.getSelectedProductItems().isEmpty())
                    DialogTools.showDialog(C_Module.this, "Ooops!", "You have no selected items. Kindly select first products.");
                else if (isReturnItems)
                    onBackPressed();
                else if (concessioModule == ConcessioModule.INVOICE) {
                    Intent intent = new Intent(C_Module.this, C_Finalize.class);
                    intent.putExtra(CONCESSIO_MODULE, concessioModule.ordinal());
                    startActivityForResult(intent, REVIEW_SALES);
                } else {
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


}
