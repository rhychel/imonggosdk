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

import net.nueca.concessioengine.activities.AddCustomerActivity;
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
import net.nueca.concessioengine.tools.AnimationTools;
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
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.swable.ImonggoSwableServiceConnection;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DialogTools;

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
public class C_Module extends ModuleActivity implements SetupActionBar, BaseProductsFragment.ProductsFragmentListener, SimpleRoutePlanFragment.RoutePlanListener {

    private SimpleProductsFragment simpleProductsFragment, finalizeFragment;
    private Button btn1, btn2;
    private TextView tvItems;
    private LinearLayout llReview, llBalance, llFooter;

    private Toolbar toolbar;
    private boolean hasMenu = true, showsCustomer = false;

    // for transaction details
    private boolean showTransactionDetails = false;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_module);

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        tvItems = (TextView) findViewById(R.id.tvItems);
        llReview = (LinearLayout) findViewById(R.id.llReview);
        llBalance = (LinearLayout) findViewById(R.id.llBalance);
        llFooter = (LinearLayout) findViewById(R.id.llFooter);

        simpleProductsFragment = SimpleProductsFragment.newInstance();
        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);

        llFooter.setVisibility(View.GONE);
        switch (concessioModule) {
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
                simpleTransactionsFragment.setListingType(ListingType.DETAILED_HISTORY);
                simpleTransactionsFragment.setTransactionsListener(new BaseTransactionsFragment.TransactionsListener() {

                    @Override
                    public void showTransactionDetails(OfflineData offlineData) {
                        Log.e("showTransactionDetails", "called");

                        ProductsAdapterHelper.clearSelectedProductItemList();
                        try {
                            simpleTransactionDetailsFragment.setFilterProductsBy(processOfflineData(offlineData));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        referenceNumber = offlineData.getReference_no();
                        showTransactionDetails = true;
                        llFooter.setVisibility(View.VISIBLE);

                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                .add(R.id.flContent, simpleTransactionDetailsFragment, "transaction_details")
                                .addToBackStack("transaction_details")
                                .commit();

                    }
                });

                SwableTools.bindSwable(this, new ImonggoSwableServiceConnection(simpleTransactionsFragment));

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flContent, simpleTransactionsFragment)
                        .commit();
            } break;
            case ROUTE_PLAN: {
                simpleRoutePlanFragment = new SimpleRoutePlanFragment();
                simpleRoutePlanFragment.setHelper(getHelper());
                simpleRoutePlanFragment.setSetupActionBar(this);
                simpleRoutePlanFragment.setRoutePlanListener(this);

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flContent, simpleRoutePlanFragment)
                        .commit();
            } break;
            case CUSTOMER_DETAILS: {
                Log.e("Customer Details", "Yeah");
                simpleCustomerDetailsFragment = new SimpleCustomerDetailsFragment();
                simpleCustomerDetailsFragment.setCustomer(customer);
                simpleCustomerDetailsFragment.setHelper(getHelper());
                simpleCustomerDetailsFragment.setSetupActionBar(this);

                llFooter.setVisibility(View.VISIBLE);
                llReview.setVisibility(View.VISIBLE);
                if(isFromCustomersList)
                    btn1.setText("VIEW HISTORY");
                else {
                    btn1.setText("TRANSACT");
                    btn1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(C_Module.this, C_Module.class);
                            intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.INVOICE.ordinal());
                            startActivity(intent);
                        }
                    });
                    btn2.setText("HISTORY");
                    btn2.setVisibility(View.VISIBLE);
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flContent, simpleCustomerDetailsFragment)
                        .commit();
            } break;
            case CUSTOMERS: {
                simpleCustomersFragment = new SimpleCustomersFragment();
                simpleCustomersFragment.setHelper(getHelper());
                simpleCustomersFragment.setSetupActionBar(this);
                simpleCustomersFragment.setListingType(ListingType.LETTER_HEADER);
                simpleCustomersFragment.setOnCustomerSelectedListener(new SimpleCustomersFragment.OnCustomerSelectedListener() {
                    @Override
                    public void onCustomerSelected(Customer customer) {
                        Intent intent = new Intent(C_Module.this, C_Module.class);
                        intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, customer.getId());
                        intent.putExtra(ModuleActivity.FROM_CUSTOMERS_LIST, isFromCustomersList);
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.CUSTOMER_DETAILS.ordinal());
                        startActivity(intent);
                    }
                });

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flContent, simpleCustomersFragment)
                        .commit();
            } break;
            case STOCK_REQUEST:
            case INVOICE: {
                simpleProductsFragment.setListingType(ListingType.SALES);
                simpleProductsFragment.setHasUnits(true);
                simpleProductsFragment.setProductCategories(getProductCategories(!getModuleSetting().getProductListing().isLock_category()));
                simpleProductsFragment.setShowCategoryOnStart(getModuleSetting().getProductListing().isShow_categories_on_start());
                simpleProductsFragment.setProductsFragmentListener(this);

                initializeFinalize();
                finalizeFragment.setHasCategories(false);
                finalizeFragment.setHasBrand(false);
                finalizeFragment.setHasDeliveryDate(false);
                finalizeFragment.setHasUnits(true);

                prepareFooter();

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.flContent, simpleProductsFragment)
                        .commit();
            }
            break;
            case PHYSICAL_COUNT: {
                simpleProductsFragment.setProductCategories(getProductCategories(true));
                simpleProductsFragment.setMultipleInput(true);
                simpleProductsFragment.setMultiInputListener(multiInputListener);

                initializeFinalize();
                finalizeFragment.setHasCategories(false);
                finalizeFragment.setMultipleInput(true);
                finalizeFragment.setMultiInputListener(multiInputListener);

                getSupportFragmentManager().beginTransaction()
                    .add(R.id.flContent, simpleProductsFragment)
                    .commit();
            }
            break;
            case RECEIVE_BRANCH: {
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
            case RECEIVE_SUPPLIER: // Adjustment In -- Rebisco receiving
            case RELEASE_SUPPLIER: { // Adjustment Out -- Rebisco pullout
                if(getModuleSetting() != null)
                    Log.e("moduleSetting", "Yeah");
                simpleInventoryFragment = new SimpleInventoryFragment();
                simpleInventoryFragment.setHelper(getHelper());
                simpleInventoryFragment.setListingType(ListingType.SALES);
                simpleInventoryFragment.setSetupActionBar(this);
                simpleInventoryFragment.setHasUnits(true);
                simpleInventoryFragment.setProductCategories(getProductCategories(!getModuleSetting().getProductListing().isLock_category()));
                simpleInventoryFragment.setShowCategoryOnStart(getModuleSetting().getProductListing().isShow_categories_on_start());
                simpleInventoryFragment.setHasSubtotal(false);
                simpleInventoryFragment.setProductsFragmentListener(this);

                initializeFinalize();
                finalizeFragment.setListingType(ListingType.SALES);
                finalizeFragment.setHasSubtotal(false);
                finalizeFragment.setHasCategories(false);
                finalizeFragment.setHasBrand(false);
                finalizeFragment.setHasDeliveryDate(false);
                finalizeFragment.setHasUnits(true);

                prepareFooter();

                btn1.setOnClickListener(nextClickedListener);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, simpleInventoryFragment)
                        .commit();
            } break;
            case RELEASE_BRANCH: {
                simplePulloutRequestDialog = new SimplePulloutRequestDialog(this, getHelper());
                simplePulloutRequestDialog.setTitle("Choose a reason");
                if(getModuleSetting().isRequire_document_reason())
                    simplePulloutRequestDialog.show();

                simplePulloutFragment = new SimplePulloutFragment();
                simplePulloutFragment.setHelper(getHelper());
                simplePulloutFragment.setSetupActionBar(this);
                simplePulloutFragment.setHasUnits(true);
                simplePulloutFragment.setProductCategories(getProductCategories(!getModuleSetting().getProductListing().isLock_category()));
                simplePulloutFragment.setShowCategoryOnStart(getModuleSetting().getProductListing().isShow_categories_on_start());

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, simplePulloutFragment)
                        .commit();
            } break;
            case RELEASE_ADJUSTMENT: {
                showsCustomer = true;
                simpleCustomersFragment = new SimpleCustomersFragment();
                simpleCustomersFragment.setHelper(getHelper());
                simpleCustomersFragment.setSetupActionBar(this);
                simpleCustomersFragment.setListingType(ListingType.LETTER_HEADER);
                simpleCustomersFragment.setOnCustomerSelectedListener(new SimpleCustomersFragment.OnCustomerSelectedListener() {
                    @Override
                    public void onCustomerSelected(final Customer customer) {
                        SimplePulloutRequestDialog simplePulloutRequestDialog = new SimplePulloutRequestDialog(C_Module.this, getHelper(), R.style.AppCompatDialogStyle_Light_NoTitle);
                        simplePulloutRequestDialog.setDTitle("MSO");
                        simplePulloutRequestDialog.setShouldShowBranchSelection(false);
                        simplePulloutRequestDialog.setListener(new SimplePulloutRequestDialog.PulloutRequestDialogListener() {
                            @Override
                            public void onSave(DocumentPurpose reason, Branch source, Branch destination) {
                                Log.e("Reason", reason.getName());
                                ProductsAdapterHelper.setSelectedCustomer(customer);
                                ProductsAdapterHelper.setReason(reason);
                                simpleProductsFragment.setHasSubtotal(false);
                                simpleProductsFragment.setReason(reason);
                                simpleProductsFragment.setListingType(ListingType.SALES);
                                simpleProductsFragment.setHasUnits(true);
                                simpleProductsFragment.setProductCategories(getProductCategories(!getModuleSetting().getProductListing().isLock_category()));
                                simpleProductsFragment.setShowCategoryOnStart(getModuleSetting().getProductListing().isShow_categories_on_start());
                                simpleProductsFragment.setProductsFragmentListener(C_Module.this);

                                initializeFinalize();
                                finalizeFragment.setHasCategories(false);
                                finalizeFragment.setHasBrand(false);
                                finalizeFragment.setHasDeliveryDate(false);
                                finalizeFragment.setHasUnits(true);
                                finalizeFragment.setListingType(ListingType.SALES);
                                finalizeFragment.setReason(reason);

                                prepareFooter();

                                btn1.setOnClickListener(nextClickedListener);

                                showsCustomer = false;

                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.flContent, simpleProductsFragment)
                                        .commit();
                            }

                            @Override
                            public void onCancel() { }
                        });
                        simplePulloutRequestDialog.show();
                    }
                });

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flContent, simpleCustomersFragment)
                        .commit();
            } break;
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                hasMenu = false;
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hasMenu = true;
                        onBackPressed();
                        getSupportActionBar().invalidateOptionsMenu();
                    }
                });
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                if(concessioModule != ConcessioModule.HISTORY)
                    getSupportActionBar().setTitle("Review");
                else {
                    if(showTransactionDetails) {
                        tvItems.setVisibility(View.VISIBLE);
                        btn2.setVisibility(View.VISIBLE);
                        int size = simpleTransactionDetailsFragment.numberOfItems();
                        tvItems.setText(getResources().getQuantityString(R.plurals.items, size, size));
                        btn1.setText("VOID");
                        btn2.setText("DUPLICATE");
                        getSupportActionBar().setTitle(referenceNumber);
                    }
                    else {
                        llFooter.setVisibility(View.GONE);
                        hasMenu = true;
                        getSupportActionBar().setDisplayShowTitleEnabled(false);
                    }
                }
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().invalidateOptionsMenu();
            }
        });

//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.flContent, simpleProductsFragment)
//                .commit();
    }

    private void initializeFinalize() {
        finalizeFragment = SimpleProductsFragment.newInstance();
        finalizeFragment.setHelper(getHelper());
        finalizeFragment.setSetupActionBar(this);
        finalizeFragment.setIsFinalize(true);
        finalizeFragment.setProductsFragmentListener(this);
    }

    @Override
    protected void onDestroy() {
        SwableTools.unbindSwable(this, new ImonggoSwableServiceConnection(simpleTransactionsFragment));
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(concessioModule == ConcessioModule.HISTORY)
            llFooter.setVisibility(View.GONE);
        btn1.setText("REVIEW");
        if(concessioModule == ConcessioModule.RECEIVE_SUPPLIER || concessioModule == ConcessioModule.RELEASE_SUPPLIER)
            simpleInventoryFragment.refreshList();
        if(getModuleSetting() != null) {
            if (getModuleSetting().isRequire_document_reason()) {
                if (concessioModule == ConcessioModule.RELEASE_BRANCH) {
                    if (simplePulloutToolbarExt != null)
                        simplePulloutToolbarExt.attachAfter(this, toolbar);
                } else {
                    if (simplePulloutToolbarExt != null)
                        simplePulloutToolbarExt.detach();
                }
            }
        }
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
                else
                    mSearch.setQueryHint("Search product");
                initializeSearchViewEx(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String newText) {
                        if(concessioModule == ConcessioModule.RECEIVE_SUPPLIER || concessioModule ==  ConcessioModule.RELEASE_SUPPLIER)
                            simpleInventoryFragment.updateListWhenSearch(newText);
                        else if(concessioModule == ConcessioModule.CUSTOMERS || showsCustomer)
                            simpleCustomersFragment.updateListWhenSearch(newText);
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
                        else
                            simpleProductsFragment.updateListWhenSearch(newText);
                        return true;
                    }

                });
            }
            else {
                getMenuInflater().inflate(R.menu.simple_search_menu, menu);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mAddCustomer) {
            Intent intent = new Intent(this, AddCustomerActivity.class);
            startActivityForResult(intent, ADD_CUSTOMER);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_CUSTOMER) {
            if(resultCode == SUCCESS) {
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
                handler.sendEmptyMessageDelayed(0, 300);
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
        if(getModuleSetting() != null) {
            if (getModuleSetting().isRequire_document_reason()) {
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
        if(concessioModule == ConcessioModule.ROUTE_PLAN) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if(concessioModule == ConcessioModule.CUSTOMER_DETAILS) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(customer.getName());
        }
        if(concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
            Log.e("release adjustment", "YEAH");
            if(!simpleCustomersFragment.isHasSelected()) {
                Log.e("release adjustment", "isSelected");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setTitle("MSO");
                getSupportActionBar().invalidateOptionsMenu();
            }
        }
    }

    private MultiInputListener multiInputListener = new MultiInputListener() {
        @Override
        public void showInputScreen(Product product) {
            Intent intent = new Intent(C_Module.this, C_MultiInput.class);
            intent.putExtra(MultiInputSelectedItemFragment.PRODUCT_ID, product.getId());
            startActivity(intent);
        }
    };

    @Override
    public void whenItemsSelectedUpdated() {
        int size = ProductsAdapterHelper.getSelectedProductItems().size();
        tvItems.setText(getResources().getQuantityString(R.plurals.items, size, size));
        AnimationTools.toggleShowHide(llFooter, false, 300);
    }

    @Override
    public void itemClicked(Customer customer) {
        Log.e("Customer Details", "Clicked!");
        Intent intent = new Intent(this, C_Module.class);
        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.CUSTOMER_DETAILS.ordinal());
        startActivity(intent);
    }

    private void prepareFooter() {
        llFooter.setVisibility(View.VISIBLE);
        llFooter.setTranslationY(1000f);
        tvItems.setVisibility(View.VISIBLE);
    }

    private View.OnClickListener nextClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(btn1.getText().toString().equals("SEND")) {
                DialogTools.showSelectionDialog(C_Module.this,
                        new ArrayAdapter<>(C_Module.this, R.layout.simple_listitem_single_choice, getBranches()),
                        "Yes", new DialogTools.OnItemSelected<Branch>() {
                            @Override
                            public void itemChosen(final Branch branch) {
                                final Branch warehouse = getWarehouse();
                                if (warehouse == null && getModuleSetting().isRequire_warehouse())
                                    DialogTools.showDialog(C_Module.this, "Ooops!", "You have no warehouse. Kindly contact your admin.", R.style.AppCompatDialogStyle_Light);
                                else {
                                    DialogTools.showConfirmationDialog(C_Module.this, "Send", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Gson gson = new GsonBuilder().serializeNulls().create();
                                            Document document = generateDocument(C_Module.this, branch.getId(), DocumentTypeCode.identify(concessioModule));
                                            if(concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                                                document.setDocument_purpose_name(ProductsAdapterHelper.getReason().getName());
//                                                document.setDocument_purpose_id(ProductsAdapterHelper.getReason().getId());

                                                Extras extras = new Extras();
                                                extras.setCustomer_id(ProductsAdapterHelper.getSelectedCustomer().getId());
                                                document.setExtras(extras);
                                            }
                                            try {
                                                JSONObject jsonObject = new JSONObject(gson.toJson(document));
                                                Log.e("jsonObject", jsonObject.toString());

                                                updateInventoryFromSelectedItemList(concessioModule == ConcessioModule.RECEIVE_SUPPLIER);
                                                List<Inventory> inventoryList = getHelper().fetchObjectsList(Inventory.class);
                                                for(Inventory inventory : inventoryList) {
                                                    Log.e("Inventory", inventory.getProduct().getName()+" = "+inventory.getQuantity());
                                                }

                                                OfflineData offlineData = new SwableTools.Transaction(getHelper())
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
                                                        ProductsAdapterHelper.clearSelectedProductItemList();
                                                        onBackPressed();
                                                        if(concessioModule == ConcessioModule.RELEASE_ADJUSTMENT) {
                                                            llFooter.setVisibility(View.GONE);
                                                            simpleCustomersFragment.setHasSelected(false);
                                                            simpleCustomersFragment.onViewCreated(null, null);
                                                            hasMenu = true;
                                                            showsCustomer = true;

                                                            Log.e("simple", "called customer fragment");
                                                            getSupportFragmentManager()
                                                                    .beginTransaction()
                                                                    .replace(R.id.flContent, simpleCustomersFragment)
                                                                    .commit();
                                                        }
                                                    }
                                                });
                                                transactionDialog.show();

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, "No", R.style.AppCompatDialogStyle_Light);
                                }
                            }
                        }, "No", R.style.AppCompatDialogStyle_Light);
            }
            else {
                if (ProductsAdapterHelper.getSelectedProductItems().isEmpty())
                    DialogTools.showDialog(C_Module.this, "Ooops!", "You have no selected items. Kindly select first products.");
                else {
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
