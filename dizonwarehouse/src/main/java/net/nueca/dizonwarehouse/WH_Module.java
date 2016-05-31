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

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;

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
import net.nueca.concessioengine.printer.epson.listener.PrintListener;
import net.nueca.concessioengine.printer.epson.tools.EpsonPrinterTools;
import net.nueca.concessioengine.tools.DocumentTools;
import net.nueca.concessioengine.tools.OrderTools;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.dizonwarehouse.dialogs.SearchOrdersDialog;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.dizonwarehouse.services.SwableTools;
import net.nueca.imonggosdk.tools.NumberTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by gama on 28/03/16.
 * dizonwarehouse (c)2016
 */
public class WH_Module extends ModuleActivity implements SearchOrdersDialog.OnSearchListener, SetupActionBar {
    public static final String BRANCH_ID = "branch_id";

    private final String CONTINUE = "CONTINUE";
    private final String SUBMIT = "SUBMIT";
    private final String VOID = "VOID";
    private final String DUPLICATE = "DUPLICATE";

    private final String RECEIVING_ORDER = "Purchase Order";
    private final String DISPATCHING_ORDER = "Sales Order";

    private boolean isAddProduct;

    private LinearLayout llFooter, llReview, llTotalAmount;
    private TextView tvTotalAmount, tvItems, tvLabel, tvValue;
    private ImageButton ibtnEdit;
    private FloatingActionButton fabAdd;

    private Button btnPrimary, btnSecondary;
    private Toolbar tbActionBar;

    private SimpleInventoryFragment simpleInventoryFragment;
    private SimpleProductsFragment reviewFragment, additionalProductsFragment;
    
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
        llTotalAmount = (LinearLayout) findViewById(R.id.llTotalAmount);
        llFooter = (LinearLayout) findViewById(R.id.llFooter);
        btnPrimary = (Button) findViewById(R.id.btn1);
        btnSecondary = (Button) findViewById(R.id.btn2);
        btnPrimary.setText(CONTINUE);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        tvItems = (TextView) findViewById(R.id.tvItems);

        switch (concessioModule) {
            case RECEIVE_SUPPLIER:
            case RELEASE_BRANCH:
                isManualReceive = getModuleSetting(concessioModule).getManual().is_enabled();
                llFooter.setVisibility(View.VISIBLE);

                if(!isAddProduct && !ProductsAdapterHelper.isDuplicating)
                    showDialog();
                if(ProductsAdapterHelper.isDuplicating)
                    refreshTitle();

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

                btnPrimary.setOnClickListener(nextClickedListener);

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
    
                        llFooter.setVisibility(View.VISIBLE);
                        btnPrimary.setText(VOID);

                        if(!offlineData.isCancelled()) {
                            btnPrimary.setEnabled(true);
                            initializeVoidButton(btnPrimary, offlineData.getReferenceNumber());
                        }
                        else
                            btnPrimary.setEnabled(false);

                        ProductsAdapterHelper.clearSelectedProductItemList(true);
                        ProductsAdapterHelper.clearSelectedReturnProductItemList();
                        ProductsAdapterHelper.setDbHelper(getHelper());
                        try {
                            simpleTransactionDetailsFragment.setFilterProductsBy(processOfflineData(offlineData));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        updateFooter();

                        reference = offlineData.getReference_no();
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
        if(isAddProduct) {
            isAddProduct = false;
            btnPrimary.setEnabled(true);
        }
        else {
            if (isReview()) {
                btnPrimary.setText(CONTINUE);
            }
            else if(isTransactionDetail()) {
                llFooter.setVisibility(View.GONE);
                btnPrimary.setText(CONTINUE);
                btnPrimary.setEnabled(true);
            }
        }
        setupActionBar(tbActionBar);
    }

    private View.OnClickListener nextClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!isReview()) {
                btnPrimary.setText(SUBMIT);

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
            else if(isReview()){
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
                                OfflineData offlineData = new SwableTools.Transaction(getHelper())
                                        .toSend()
                                        .fromModule(concessioModule)
                                        .object(document)
                                        .forBranch(selectedBranch)
                                        .queue();
                                setResult(SUCCESS);
                                finish();
                                printTransaction(offlineData,"TEST");
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
            else if(isTransactionDetail()) {

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
        menu.findItem(R.id.mSearch).setVisible(!isReview() || isAddProduct);
        SearchViewEx searchViewEx = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
        searchViewEx.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(concessioModule == ConcessioModule.HISTORY)
                    simpleTransactionsFragment.updateListWhenSearch(query);
                else {
                    if(isAddProduct)
                        additionalProductsFragment.updateListWhenSearch(query);
                    else
                        simpleInventoryFragment.updateListWhenSearch(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(concessioModule == ConcessioModule.HISTORY)
                    simpleTransactionsFragment.updateListWhenSearch(newText);
                else {
                    if (isAddProduct)
                        additionalProductsFragment.updateListWhenSearch(newText);
                    else
                        simpleInventoryFragment.updateListWhenSearch(newText);
                }
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
                setTitle(reference);
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
            fabAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isAddProduct = true;
                    btnPrimary.setEnabled(false);

                    additionalProductsFragment = SimpleProductsFragment.newInstance();
                    additionalProductsFragment.setHelper(getHelper());
                    additionalProductsFragment.setSetupActionBar(WH_Module.this);
                    additionalProductsFragment.setProductsFragmentListener(new BaseProductsFragment.ProductsFragmentListener() {
                        @Override
                        public void whenItemsSelectedUpdated() {

                        }
                    });
                    additionalProductsFragment.setReturnItems(isReturnItems);

                    additionalProductsFragment.setListingType(ListingType.ADVANCED_SALES); //changed to show the individual price of every unit-- Sales
                    additionalProductsFragment.setUseSalesProductAdapter(true);//added to show the individual price of every unit
                    additionalProductsFragment.setHasSubtotal(true);
                    additionalProductsFragment.setHasCategories(true);
                    additionalProductsFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing()
                            .isLock_category()));
                    additionalProductsFragment.setHasBrand(false);
                    additionalProductsFragment.setHasDeliveryDate(false);
                    additionalProductsFragment.setHasUnits(true);
                    additionalProductsFragment.setHasInStock(false);
                    // if there's branch product
                    additionalProductsFragment.setBranch(selectedBranch);
                    additionalProductsFragment.setMultipleInput(getModuleSetting(concessioModule).getQuantityInput().is_multiinput());
                    additionalProductsFragment.setMultiInputListener(multiInputListener);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.flContent, additionalProductsFragment, "additionalProductsFragment")
                            .addToBackStack("addProducts")
                            .commit();
                }
            });
            fabAdd.setVisibility(isReview() && concessioModule != ConcessioModule.RECEIVE_SUPPLIER && !isAddProduct?
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

        getSupportActionBar().setDisplayShowTitleEnabled((isReview() && !isAddProduct) || isTransactionDetail());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FROM_MULTIINPUT) {
            if(resultCode == SUCCESS) {
                if(additionalProductsFragment != null && isAddProduct) {
                    additionalProductsFragment.refreshList();
                    simpleInventoryFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
                    simpleInventoryFragment.forceUpdateProductList();

                    if(reviewFragment != null) {
                        reviewFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
                        reviewFragment.forceUpdateProductList();
                    }
                }

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
        return btnPrimary.getText().toString().equals(SUBMIT);
    }
    protected boolean isTransactionDetail() {
        return btnPrimary.getText().toString().equals(VOID);
    }

    private final int MAX_CHAR = 33;
    private void printTransactionLog(OfflineData offlineData, String... labels) {
        Document document = offlineData.getObjectFromData(Document.class);
        Branch targetBranch = Branch.fetchById(getHelper(), Branch.class, document.getTarget_branch_id());
        Log.e("TARGET BRANCH", targetBranch.getName());
        Branch branch = Branch.fetchById(getHelper(), Branch.class, offlineData.getBranch_id());
        Log.e("BRANCH", branch.getName());

        for(int i = 0;i < labels.length;i++) {
            // ---------- HEADER
            Log.e("",EpsonPrinterTools.centerInRange(branch.getName(),MAX_CHAR));
            Log.e("",EpsonPrinterTools.centerInRange(branch.generateAddress(),MAX_CHAR));

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            Log.e("",simpleDateFormat.format(offlineData.getDateCreated()) + "\n");
            Log.e("",EpsonPrinterTools.spacer("Store Code:", branch.getName(), MAX_CHAR));
            Log.e("",EpsonPrinterTools.spacer("Store     :", branch.getCity(), MAX_CHAR));
            Log.e("",EpsonPrinterTools.spacer("DR No.    :", offlineData.getReference_no(), MAX_CHAR));

            // ---------- HEADER
            double totalQuantity = 0.0;
            double totalAmount = 0.0;
            Log.e("",EpsonPrinterTools.repeat('=', MAX_CHAR));
            Log.e("","Item No.  Description     UOM    ");
            Log.e(""," Original    Shipped    Received ");
            Log.e("","             Selling     Amount  ");
            Log.e("","   Notes                         ");
            Log.e("",EpsonPrinterTools.repeat('=', MAX_CHAR));

            for(DocumentLine documentLine : document.getDocument_lines()) {
                Product product = documentLine.getProduct();
                Log.e("",
                        EpsonPrinterTools.spacer(product.getStock_no() + " ",
                                product.getName() + " ", 2 * (MAX_CHAR / 3), true) +
                                EpsonPrinterTools.centerInRange(documentLine.getUnit_name(), MAX_CHAR/3)
                );
                Log.e("",
                        EpsonPrinterTools.centerInRange("0.00",MAX_CHAR/3) +
                        EpsonPrinterTools.centerInRange(""+NumberTools.separateInCommas(documentLine.getQuantity()),
                                (MAX_CHAR / 3)) +
                        EpsonPrinterTools.repeat('_',MAX_CHAR/3)
                );
                Log.e("",
                        EpsonPrinterTools.spacer("",
                        EpsonPrinterTools.spacer("@ P " + NumberTools.separateInCommas(documentLine.getUnit_retail_price())
                                + " ", "", (MAX_CHAR / 3)) +
                        EpsonPrinterTools.spacer("", "P " + NumberTools.separateInCommas(documentLine.getSubtotal()),
                                (MAX_CHAR / 3) + 3), MAX_CHAR)
                );
                Log.e("","                                 ");
            }

            Log.e("",EpsonPrinterTools.repeat('-',MAX_CHAR));

            try {
                SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MM/dd/yy hh:mm a");
                Log.e("",EpsonPrinterTools.spacer("ENCODED BY: "+ getSession().getUser().getName(),
                        simpleDateFormat2.format(offlineData.getDateCreated()),MAX_CHAR));
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Log.e("",labels[i]);
            if(i < labels.length-1) {
                Log.e("","- - - - - - CUT HERE - - - - - -\n\n");
            }
        }
    }

    private void printTransaction(final OfflineData offlineData, final String... labels) {
        String targetPrinter = EpsonPrinterTools.targetPrinter(getApplicationContext());
        if(targetPrinter != null) {
            EpsonPrinterTools.print(targetPrinter, new PrintListener() {
                @Override
                public Printer initializePrinter() {
                    try {
                        return new Printer(EpsonPrinterTools.getPrinterProperties(WH_Module.this, EpsonPrinterTools.PRINTER_SERIES),
                                EpsonPrinterTools.getPrinterProperties(WH_Module.this, EpsonPrinterTools.PRINTER_LANGUAGE), WH_Module.this);
                    } catch (Epos2Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public Printer onBuildPrintData(Printer printer) {
                    Document document = offlineData.getObjectFromData(Document.class);
                    Branch targetBranch = Branch.fetchById(getHelper(), Branch.class, document.getTarget_branch_id());
                    Log.e("TARGET BRANCH", targetBranch.getName());
                    Branch branch = Branch.fetchById(getHelper(), Branch.class, offlineData.getBranch_id());
                    Log.e("BRANCH", branch.getName());

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
                            printText.delete(0, printText.length());
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                            printer.addText(simpleDateFormat.format(offlineData.getDateCreated()) + "\n");
                            printer.addText(EpsonPrinterTools.spacer("Store Code:", branch.getName(), MAX_CHAR));
                            printer.addText(EpsonPrinterTools.spacer("Store     :", branch.getCity(), MAX_CHAR));
                            printer.addText(EpsonPrinterTools.spacer("DR No.    :", offlineData.getReference_no(), MAX_CHAR));

                            // ---------- HEADER
                            printer.addText(EpsonPrinterTools.repeat('=', MAX_CHAR));
                            printer.addText("Item No.  Description     UOM    ");
                            printer.addText(" Original    Shipped    Received ");
                            printer.addText("             Selling     Amount  ");
                            printer.addText("   Notes                         ");
                            printer.addText(EpsonPrinterTools.repeat('=', MAX_CHAR));

                            printer.addFeedLine(1);

                            for(DocumentLine documentLine : document.getDocument_lines()) {
                                Product product = documentLine.getProduct();
                                printer.addText(
                                        EpsonPrinterTools.spacer(product.getStock_no() + " ",
                                                product.getName() + " ", 2 * (MAX_CHAR / 3), true) +
                                                EpsonPrinterTools.centerInRange(documentLine.getUnit_name(), MAX_CHAR/3)
                                );
                                printer.addText(
                                        EpsonPrinterTools.centerInRange("0.00",MAX_CHAR/3) +
                                                EpsonPrinterTools.centerInRange(""+NumberTools.separateInCommas(documentLine.getQuantity()),
                                                        (MAX_CHAR / 3)) +
                                                EpsonPrinterTools.repeat('_',MAX_CHAR/3)
                                );
                                printer.addText(
                                        EpsonPrinterTools.spacer("",
                                                EpsonPrinterTools.spacer("@ P " + NumberTools.separateInCommas(documentLine.getUnit_retail_price())
                                                        + " ", "", (MAX_CHAR / 3)) +
                                                        EpsonPrinterTools.spacer("", "P " + NumberTools.separateInCommas(documentLine.getSubtotal()),
                                                                (MAX_CHAR / 3) + 3), MAX_CHAR)
                                );
                                printer.addFeedLine(1);
                            }

                            printer.addText(EpsonPrinterTools.repeat('-',MAX_CHAR));

                            try {
                                SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MM/dd/yy hh:mm a");
                                printer.addText(EpsonPrinterTools.spacer("ENCODED BY: "+ getSession().getUser().getName()+", ",
                                        simpleDateFormat2.format(offlineData.getDateCreated()),MAX_CHAR));
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            printer.addTextAlign(Printer.ALIGN_CENTER);
                            printer.addText(labels[i]);
                            if(i < labels.length-1) {
                                printer.addFeedLine(3);
                                printer.addText("- - - - - - CUT HERE - - - - - -\n\n");
                            }
                            else
                                printer.addFeedLine(5);

                        } catch (Epos2Exception e) {
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
