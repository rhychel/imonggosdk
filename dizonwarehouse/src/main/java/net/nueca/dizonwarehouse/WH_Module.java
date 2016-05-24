package net.nueca.dizonwarehouse;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.fragments.BaseProductsFragment;
import net.nueca.concessioengine.fragments.BaseTransactionsFragment;
import net.nueca.concessioengine.fragments.MultiInputSelectedItemFragment;
import net.nueca.concessioengine.fragments.SimpleInventoryFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.SimpleTransactionDetailsFragment;
import net.nueca.concessioengine.fragments.SimpleTransactionsFragment;
import net.nueca.concessioengine.fragments.interfaces.MultiInputListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.tools.DocumentTools;
import net.nueca.concessioengine.tools.OrderTools;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.dizonwarehouse.dialogs.SearchOrdersDialog;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.NumberTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by gama on 28/03/16.
 * dizonwarehouse (c)2016
 */
public class WH_Module extends ModuleActivity implements SearchOrdersDialog.OnSearchListener, SetupActionBar {
    public static final String BRANCH_ID = "branch_id";
    public static final String IS_REVIEW = "is_review";

    private final String CONTINUE = "CONTINUE";
    private final String SUBMIT = "SUBMIT";

    private final String RECEIVING_ORDER = "Purchase Order";
    private final String DISPATCHING_ORDER = "Sales Order";

    private LinearLayout llFooter, llReview;
    private TextView tvTotalAmount, tvItems, tvLabel, tvValue;
    private ImageButton ibtnEdit;
    private FloatingActionButton fabAdd;

    private Button btnNext;
    private Toolbar tbActionBar;

    private SimpleInventoryFragment simpleInventoryFragment;
    private SimpleProductsFragment reviewFragment;
    
    private SimpleTransactionDetailsFragment simpleTransactionDetailsFragment;
    private SimpleTransactionsFragment simpleTransactionsFragment;
    
    private Branch selectedBranch;
    private Order order;

    private BaseProductsFragment.ProductsFragmentListener selectionListener = new BaseProductsFragment.ProductsFragmentListener() {
        @Override
        public void whenItemsSelectedUpdated() {
            updateFooter();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wh_module);

        if(getIntent().hasExtra(BRANCH_ID)) {
            try {
                selectedBranch = getHelper().fetchObjectsInt(Branch.class).queryBuilder().where()
                        .eq("id", getIntent().getIntExtra(BRANCH_ID, 0)).queryForFirst();

                if(selectedBranch == null)
                    selectedBranch = getHelper().fetchObjectsInt(Branch.class).queryBuilder().where()
                            .eq("id",SettingTools.currentBranchId(this)).queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        llReview = (LinearLayout) findViewById(R.id.llReview);
        llFooter = (LinearLayout) findViewById(R.id.llFooter);
        btnNext = (Button) findViewById(R.id.btn1);
        btnNext.setText(CONTINUE);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        tvItems = (TextView) findViewById(R.id.tvItems);

        switch (concessioModule) {
            case RECEIVE_SUPPLIER:
            case RELEASE_BRANCH:
                isManualReceive = getModuleSetting(concessioModule).getManual().is_enabled();
                llFooter.setVisibility(View.VISIBLE);
                showDialog();

                ProductsAdapterHelper.clearSelectedProductItemList(true);
                simpleInventoryFragment = new SimpleInventoryFragment();
                simpleInventoryFragment.setHelper(getHelper());
                simpleInventoryFragment.setListingType(ListingType.ADVANCED_SALES);
                simpleInventoryFragment.setSetupActionBar(this);
                simpleInventoryFragment.setHasUnits(true);
                simpleInventoryFragment.setHasInStock(false);
                simpleInventoryFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing()
                        .isLock_category()));
                simpleInventoryFragment.setShowCategoryOnStart(getModuleSetting(concessioModule).getProductListing().isShow_categories_on_start());
                simpleInventoryFragment.setHasSubtotal(true);
                simpleInventoryFragment.setProductsFragmentListener(selectionListener);
                simpleInventoryFragment.setHasCategories(true);
                simpleInventoryFragment.setFilterProductsBy(new ArrayList<Product>());

                //Log.e(concessioModule.toString() + " isMultiInput", ""+getModuleSetting(concessioModule).getQuantityInput().is_multiinput());
                simpleInventoryFragment.setMultipleInput(getModuleSetting(concessioModule).getQuantityInput().is_multiinput());
                simpleInventoryFragment.setMultiInputListener(multiInputListener);
                // if there's branch product
                simpleInventoryFragment.setBranch(selectedBranch);

                btnNext.setOnClickListener(nextClickedListener);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, simpleInventoryFragment)
                        .commit();
                break;
            case HISTORY: {
                llFooter.setVisibility(View.GONE);

                simpleTransactionDetailsFragment = new SimpleTransactionDetailsFragment();
                simpleTransactionDetailsFragment.setHelper(getHelper());
                simpleTransactionDetailsFragment.setHasCategories(false);
                simpleTransactionDetailsFragment.setSetupActionBar(this);
                // if there's branch product
                simpleTransactionDetailsFragment.setBranch(getBranches().get(0));
                simpleTransactionDetailsFragment.setHasInStock(false);

                simpleTransactionsFragment = new SimpleTransactionsFragment();
                simpleTransactionsFragment.setHelper(getHelper());
                simpleTransactionsFragment.setSetupActionBar(this);
                simpleTransactionsFragment.setHasFilterByTransactionType(true);
                simpleTransactionsFragment.setTransactionTypes(getTransactionTypes());
                simpleTransactionsFragment.setmActivity(this);
                if(getIntent().hasExtra(HISTORY_ITEM_FILTERS))
                    simpleTransactionsFragment.setFilterModules(ConcessioModule.convertToConcessioModules(getIntent().getIntArrayExtra(HISTORY_ITEM_FILTERS)));
                simpleTransactionsFragment.setListingType(ListingType.DETAILED_HISTORY);
                if(customer != null) {
                    simpleTransactionsFragment.setCustomer(customer);
                }
                simpleTransactionsFragment.setTransactionsListener(new BaseTransactionsFragment.TransactionsListener() {

                    @Override
                    public void showTransactionDetails(OfflineData offlineData) {
                        if(getSupportFragmentManager().findFragmentByTag("transaction_details") != null)
                            return;
    
                        //prepareFooter();
                        ProductsAdapterHelper.clearSelectedProductItemList(true);
                        ProductsAdapterHelper.clearSelectedReturnProductItemList();
                        ProductsAdapterHelper.setDbHelper(getHelper());
                            try {
                                simpleTransactionDetailsFragment.setFilterProductsBy(processOfflineData(offlineData));
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
    
                            //referenceNumber = offlineData.getReference_no();
                            simpleTransactionDetailsFragment.setOfflineData(offlineData);
                            simpleTransactionDetailsFragment.setMultipleInput(getModuleSetting(offlineData.getConcessioModule()).getQuantityInput().is_multiinput());
    
                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                    .replace(R.id.flContent, simpleTransactionDetailsFragment, "transaction_details")
                                    .addToBackStack("transaction_details")
                                    .commitAllowingStateLoss();
                    }
                });

                //imonggoSwableServiceConnection = new ImonggoSwableServiceConnection(simpleTransactionsFragment);

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
                        if(simpleTransactionDetailsFragment.getOfflineData().getConcessioModule() == ConcessioModule.RELEASE_SUPPLIER
                                || simpleTransactionDetailsFragment.getOfflineData().getConcessioModule() == ConcessioModule.RELEASE_ADJUSTMENT) // Pullout || MSO
                            revertInventoryFromDocument(simpleTransactionDetailsFragment.getOfflineData().getObjectFromData(Document.class), true);

                        onBackPressed();
                        OfflineData offlineData = simpleTransactionDetailsFragment.getOfflineData();
                        offlineData.setSynced(false);
                        simpleTransactionsFragment.updateOfflineData(offlineData);
                    }

                    @Override
                    public void onDuplicateTransaction() {
                        ProductsAdapterHelper.isDuplicating = true;
                        if(!ProductsAdapterHelper.hasSelectedProductItems()) {
                            try {
                                processOfflineData(simpleTransactionDetailsFragment.getOfflineData());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        Intent intent = new Intent(WH_Module.this, WH_Module.class);
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, simpleTransactionDetailsFragment.getOfflineData().getConcessioModule().ordinal());
                        if(simpleTransactionDetailsFragment.getOfflineData().getConcessioModule() == ConcessioModule.RELEASE_ADJUSTMENT)
                            intent.putExtra(ModuleActivity.CATEGORY, simpleTransactionDetailsFragment.getOfflineData().getCategory());
                        if(simpleTransactionDetailsFragment.getOfflineData().getConcessioModule() == ConcessioModule.INVOICE) {
                            Invoice invoice = simpleTransactionDetailsFragment.getOfflineData().getObjectFromData(Invoice.class);
                            intent.putExtra(FOR_CUSTOMER_DETAIL, invoice.getCustomer().getId());
                        }
                        startActivityForResult(intent, IS_DUPLICATING);
                    }
                };

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.flContent, simpleTransactionsFragment)
                        .commit();
            }
                break;
            default:
                break;
        }
    }

    private void showDialog() {
        String title = "Enter Order Number";
        if(concessioModule == ConcessioModule.RECEIVE_SUPPLIER)
            title = "Enter " + RECEIVING_ORDER + " Number";
        else if(concessioModule == ConcessioModule.RELEASE_BRANCH)
            title = "Enter " + DISPATCHING_ORDER + " Number";

        try {
            SearchOrdersDialog dialog = new SearchOrdersDialog(this, getHelper(), getSession(), R.style.WarehouseTheme_InputDialog)
                    .setTitle(title).setOnSearchListener(this);
            dialog.setCancelable(false);
            dialog.setBranch_id(selectedBranch.getId());
            if(concessioModule == ConcessioModule.RECEIVE_SUPPLIER)
                dialog.setOrder_type(Order.ORDERTYPE_PURCHASE_ORDER);
            else if(concessioModule == ConcessioModule.RELEASE_BRANCH)
                dialog.setOrder_type(Order.ORDERTYPE_STOCK_REQUEST);
            dialog.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFound(Order foundOrder) {
        order = foundOrder;
        reference = order.getReference();

        try {
            simpleInventoryFragment.setFilterProductsBy(OrderTools.generateSelectedItemList(getHelper(),order,true));
            simpleInventoryFragment.forceUpdateProductList();
            simpleInventoryFragment.refreshList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        refreshTitle();
    }

    @Override
    public void onCancel() {
        onBackPressed();
    }

    private void updateFooter() {
        tvTotalAmount.setText("P"+NumberTools.separateInCommas(ProductsAdapterHelper.getSelectedProductItems().getSubtotal()));
        int count = ProductsAdapterHelper.getSelectedProductItems().size();
        tvItems.setText(count + " Item" + (count != 1? "s" : ""));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(btnNext.getText().equals(SUBMIT))
            btnNext.setText(CONTINUE);
        setupActionBar(tbActionBar);
    }

    private View.OnClickListener nextClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!isReview()) {
                btnNext.setText(SUBMIT);

                reviewFragment = SimpleProductsFragment.newInstance();
                reviewFragment.setHelper(getHelper());
                reviewFragment.setSetupActionBar(WH_Module.this);
                reviewFragment.setIsFinalize(true);
                reviewFragment.setProductsFragmentListener(new BaseProductsFragment.ProductsFragmentListener() {
                    @Override
                    public void whenItemsSelectedUpdated() {

                    }
                });
                reviewFragment.setReturnItems(isReturnItems);

                reviewFragment.setListingType(ListingType.ADVANCED_SALES); //changed to show the individual price of every unit-- Sales
                reviewFragment.setUseSalesProductAdapter(true);//added to show the individual price of every unit
                reviewFragment.setHasSubtotal(true);
                reviewFragment.setHasCategories(false);
                reviewFragment.setHasBrand(false);
                reviewFragment.setHasDeliveryDate(false);
                reviewFragment.setHasUnits(true);
                reviewFragment.setHasInStock(false);
                // if there's branch product
                reviewFragment.setBranch(selectedBranch);
                reviewFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
                reviewFragment.setMultipleInput(getModuleSetting(concessioModule).getQuantityInput().is_multiinput());
                reviewFragment.setMultiInputListener(multiInputListener);
                reviewFragment.setCanDeleteItems(concessioModule != ConcessioModule.RECEIVE_SUPPLIER);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, reviewFragment, "reviewFragment")
                        .addToBackStack("review")
                        .commit();
            }
            else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(WH_Module.this, R.style.AppCompatDialogStyle_Light);
                dialog.setTitle("Send this Document?");
                dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Document document = null;
                            if (concessioModule == ConcessioModule.RECEIVE_SUPPLIER)
                                document = DocumentTools.generateDocument(WH_Module.this, getSession().getDevice_id(),
                                        DocumentTypeCode.RECEIVE_SUPPLIER);
                            else if (concessioModule == ConcessioModule.RELEASE_BRANCH)
                                document = DocumentTools.generateDocument(WH_Module.this, getSession().getDevice_id(),
                                        order.getBranch_id(),
                                        DocumentTypeCode.RELEASE_BRANCH);

                            if(document != null) {
                                new SwableTools.Transaction(getHelper())
                                        .toSend()
                                        .fromModule(concessioModule)
                                        .object(document)
                                        .forBranch(selectedBranch)
                                        .queue();
                                setResult(SUCCESS);
                                finish();

                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
            }
        }
    };

    private MultiInputListener multiInputListener = new MultiInputListener() {
        @Override
        public void showInputScreen(Product product) {
            Intent intent = new Intent(WH_Module.this, WH_MultiInput.class);
            intent.putExtra(MultiInputSelectedItemFragment.PRODUCT_ID, product.getId());
            intent.putExtra(MultiInputSelectedItemFragment.IS_MANUAL_RECEIVE, isManualReceive);
            intent.putExtra(ModuleActivity.CONCESSIO_MODULE, concessioModule.ordinal());
            startActivityForResult(intent, FROM_MULTIINPUT);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wh_module_right, menu);
        menu.findItem(R.id.mDelete).setVisible(false);
        menu.findItem(R.id.mSearch).setVisible(!isReview());
        SearchViewEx searchViewEx = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
        searchViewEx.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                simpleInventoryFragment.updateListWhenSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                simpleInventoryFragment.updateListWhenSearch(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void refreshTitle() {
        switch (concessioModule) {
            case RECEIVE_SUPPLIER:
                tvLabel.setText("PO Number: ");
                tvValue.setText(reference == null? "" : reference);
                if(!isReview())
                    setTitle("PO Number: " + (reference == null? "" : reference));
                else
                    setTitle("Review");
                break;
            case RELEASE_BRANCH:
                tvLabel.setText("SO Number: ");
                tvValue.setText(reference == null? "" : reference);
                if(!isReview())
                    setTitle("SO Number: " + (reference == null? "" : reference));
                else
                    setTitle("Review");
                break;
            default:
                //tvLabel.setText("Ref No: ");
                //tvValue.setText(reference == null? "" : reference);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        tbActionBar = toolbar;
        tbActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvLabel = (TextView) findViewById(R.id.tvLabel);
        tvValue = (TextView) findViewById(R.id.tvValue);

        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        if(fabAdd != null) {
            fabAdd.setVisibility(isReview() && concessioModule != ConcessioModule.RECEIVE_SUPPLIER?
                    View.VISIBLE : View.GONE);
        }

        ibtnEdit = (ImageButton) findViewById(R.id.ibtnEdit);
        if(ibtnEdit != null)
            ibtnEdit.setVisibility(View.GONE);

        View toolbarExt = findViewById(R.id.toolbar_extension);
        if((concessioModule == ConcessioModule.RECEIVE_SUPPLIER || concessioModule == ConcessioModule.RELEASE_BRANCH)
                && toolbarExt != null && !isReview())
            toolbarExt.setVisibility(View.VISIBLE);

        refreshTitle();

        getSupportActionBar().setDisplayShowTitleEnabled(isReview());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FROM_MULTIINPUT) {
            if(resultCode == SUCCESS) {
                simpleInventoryFragment.refreshList();
                if(reviewFragment != null && isReview())
                    reviewFragment.refreshList();
            }
        }
        else {
            if(resultCode == SUCCESS)
                finish();
        }
    }

    protected boolean isReview() {
        return btnNext.getText().toString().equals(SUBMIT);
    }
}
