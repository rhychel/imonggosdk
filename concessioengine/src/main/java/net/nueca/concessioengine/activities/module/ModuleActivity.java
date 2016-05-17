package net.nueca.concessioengine.activities.module;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.tools.AnimationTools;
import net.nueca.concessioengine.tools.DocumentTools;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.concessioengine.tools.OrderTools;
import net.nueca.concessioengine.tools.appsettings.Constants;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.objects.AccountSettings;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.ModuleSettingTools;
import net.nueca.imonggosdk.tools.SettingTools;
import net.nueca.imonggosdk.tools.StringUtilsEx;
import net.nueca.imonggosdk.tools.TimerTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by rhymart on 6/3/15.
 * imonggosdk (c)2015
 */
public abstract class ModuleActivity extends ImonggoAppCompatActivity {

    public interface HistoryDetailsListener {
        void onVoidTransaction();
        void onDuplicateTransaction();
    }

    public static final String CONCESSIO_MODULE = "concessio_module";
    public static final String FROM_CUSTOMERS_LIST = "from_customers_list";
    public static final String FOR_CUSTOMER_DETAIL = "for_customer_detail";
    public static final String RETURN_ITEMS = "return_items";
    public static final String INIT_PRODUCT_ADAPTER_HELPER = "initialize_pahelper";
    public static final String INIT_SELECTED_CUSTOMER = "initialize_selected_customer";
    public static final String FOR_HISTORY_DETAIL = "for_history_detail";
    public static final String HISTORY_ITEM_FILTERS = "history_item_filters";

    public static final String REFERENCE = "reference";
    public static final String IS_LAYAWAY = "is_layaway";
    public static final String FOR_DUPLICATING = "is_duplicating";
    public static final String CATEGORY = "category";

    protected ConcessioModule concessioModule = ConcessioModule.STOCK_REQUEST;
    protected boolean isFromCustomersList = false;
    protected boolean isMultiInput = false;
    protected boolean clearTransactions = true;
    protected boolean isReturnItems = false;
    protected boolean initSelectedCustomer = true;
    protected boolean isForHistoryDetail = false;
    protected boolean isManualReceive = false;
    private ModuleSetting moduleSetting;
    protected Customer customer;

    protected String reference;
    protected boolean isLayaway = false;
    protected boolean isDuplicating = false;
    protected boolean isButtonTapped = false;

    protected int previousFragmentCount = 0;
    protected HistoryDetailsListener historyDetailsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        concessioModule = ConcessioModule.values()[getIntent().getIntExtra(CONCESSIO_MODULE, ConcessioModule.STOCK_REQUEST.ordinal())];
        isFromCustomersList = getIntent().getBooleanExtra(FROM_CUSTOMERS_LIST, false);
        if(getIntent().hasExtra(FOR_CUSTOMER_DETAIL))
            customer = retrieveCustomer(getIntent().getIntExtra(FOR_CUSTOMER_DETAIL, 0));
        if(getIntent().hasExtra(RETURN_ITEMS))
            isReturnItems = getIntent().getBooleanExtra(RETURN_ITEMS, false);
        if(getIntent().hasExtra(FOR_HISTORY_DETAIL))
            isForHistoryDetail = getIntent().getBooleanExtra(FOR_HISTORY_DETAIL, false);

        if(getIntent().hasExtra(REFERENCE))
            reference = getIntent().getStringExtra(REFERENCE);
        if(getIntent().hasExtra(IS_LAYAWAY))
            isLayaway = getIntent().getBooleanExtra(IS_LAYAWAY, false);
        if(getIntent().hasExtra(FOR_DUPLICATING))
            isDuplicating = getIntent().getBooleanExtra(FOR_DUPLICATING, false);

        clearTransactions = getIntent().getBooleanExtra(INIT_PRODUCT_ADAPTER_HELPER, false);
        initSelectedCustomer = getIntent().getBooleanExtra(INIT_SELECTED_CUSTOMER, true);
    }

    protected Customer retrieveCustomer(int customerId) {
        try {
            return getHelper().fetchIntId(Customer.class).queryBuilder().where().eq("id", customerId).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
//        if(clearTransactions)
//            ProductsAdapterHelper.clearSelectedProductItemList();
        if(ProductsAdapterHelper.isDuplicating)
            ProductsAdapterHelper.isDuplicating = false;
        super.onDestroy();
    }

    public List<ConcessioModule> getTransactionTypes() {
        return getTransactionTypes(true);
    }
    protected SearchViewEx mSearch;

    protected void initializeSearchViewEx(SearchView.OnQueryTextListener queryTextListenerCompat) {
        if(mSearch != null) {
            mSearch.setSearchViewExListener(new SearchViewEx.SearchViewExListener() {
                @Override
                public void whenBackPressed() {
                    if (!mSearch.isIconified())
                        mSearch.setIconified(true);
                }
            });
            mSearch.setIconifiedByDefault(true);
            mSearch.setOnQueryTextListener(queryTextListenerCompat);
//            SearchViewCompat.setOnQueryTextListener(mSearch, queryTextListenerCompat);
        }
    }

    @Override
    public void onBackPressed() {
        if(mSearch != null) {
            if(!mSearch.isIconified()) {
                closeSearchField(mSearch);
                Log.e("onBackPressed", "closed the search field");
            }
            else
                super.onBackPressed();
        }
        else
            super.onBackPressed();
        Log.e("onBackPressed", "is backed :)");
    }

    /**
     * TODO REVISE According to the universal Concessio Settings
     * Get the transaction types the account can access.
     *
     * @param includeAll Include an 'All' filter.
     * @return List of transaction types
     */
    public List<ConcessioModule> getTransactionTypes(boolean includeAll) {
        List<ConcessioModule> transactionTypes = new ArrayList<>();
        if(includeAll)
            transactionTypes.add(ConcessioModule.ALL);

        List<ModuleSetting> moduleSettings = getActiveModuleSetting(HISTORY_ITEM_FILTERS, false);

        for(ModuleSetting moduleSetting : moduleSettings) {
            if(moduleSetting.is_enabled() && !moduleSetting.is_view())
                transactionTypes.add(moduleSetting.getModuleType().setLabel(moduleSetting.getLabel()));
        }

        return transactionTypes;
    }

    public List<Branch> getBranches() throws SQLException {
        return getBranches(Constants.WAREHOUSE_ONLY);
    }


    /**
     * Generate the user's branches.
     * @return
     */
    public List<Branch> getBranches(boolean warehouseOnly) throws SQLException {
        return Branch.allUserBranches(this, getHelper(), getUser(), warehouseOnly);
    }

    /**
     * Search branches wih tag.
     * @param tag
     * @return
     */
    public List<Branch> getBranchesByTag(String tag) {
        List<Branch> branches = new ArrayList<>();
        try {
            List<BranchTag> branchTags = getHelper().fetchObjects(BranchTag.class).queryBuilder().where().in("branch_id", getBranches()).and().like("tag", "#" + tag).query();
            for(BranchTag branchTag : branchTags) {
                branches.add(branchTag.getBranch());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return branches;
    }

    /**
     * Returns the warehouse branch if any.
     * @return
     */
    public Branch getWarehouse() {
        try {
            return getHelper().fetchObjects(Branch.class).queryBuilder().where().eq("site_type", "warehouse").queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generate the list of product categories.
     * @param includeAll
     * @return
     */
    public List<String> getProductCategories(boolean includeAll) {
        List<String> categories = new ArrayList<>();

        try {
            List<ProductTag> productTags = getHelper().fetchObjects(ProductTag.class).queryBuilder().distinct().selectColumns("tag").orderByRaw("tag COLLATE NOCASE ASC").where().like("tag", "#%").query();
            for(ProductTag productTag : productTags) {
                Log.e("ProductTag", productTag.getTag());
                if(productTag.getTag().matches("^#[\\w\\-\\'\\+ ]*")) {
                    String category = StringUtilsEx.ucwords(productTag.getTag().replace("#", ""));
                    categories.add(category.toUpperCase());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(includeAll)
            categories.add(0, "All");

        Log.e("categories", categories.size() + " size");

        return categories;
    }

    /**
     * Close-iconify SearchView.
     * @param searchView
     */
    protected void closeSearchField(SearchViewEx searchView) {
        searchView.setQuery("", false);
        searchView.setIconified(true);
//        SearchViewCompat.setQuery(searchView, "", false);
//        SearchViewCompat.setIconified(searchView, true);
    }

    protected List<Product> processOfflineData(OfflineData offlineData) throws SQLException {
        switch (offlineData.getType()) {
            case OfflineData.ORDER:
                return OrderTools.generateSelectedItemList(getHelper(), offlineData.getObjectFromData(Order.class));
            case OfflineData.DOCUMENT:
                return DocumentTools.generateSelectedItemList(getHelper(), offlineData.getObjectFromData(Document.class),
                        getModuleSetting(offlineData.getConcessioModule()).getQuantityInput().is_multiinput());
            case OfflineData.INVOICE:
                try {
                    Customer customer = offlineData.getObjectFromData(Invoice.class).getCustomer();
                    ProductsAdapterHelper.setSelectedCustomer(customer);
                    List<CustomerGroup> customerGroups = customer.getCustomerGroups(getHelper());
                    if(customerGroups.size() > 0)
                        ProductsAdapterHelper.setSelectedCustomerGroup(customerGroups.get(0));
                    ProductsAdapterHelper.setSelectedBranch(getBranches().get(0));

                    SelectedProductItemList selecteds =
                            InvoiceTools.generateSelectedProductItemList(getHelper(), offlineData, false, false);
                    SelectedProductItemList returns =
                            InvoiceTools.generateSelectedProductItemList(getHelper(), offlineData, true, false);

                    ProductsAdapterHelper.getSelectedProductItems().addAll(selecteds);
                    ProductsAdapterHelper.getSelectedReturnProductItems().addAll(returns);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }

        return null;
    }

    /**
     * Used for the back stack feature of fragments
     * @return
     */
    protected boolean isBackPressed() {
        boolean isBackPressed = getSupportFragmentManager().getBackStackEntryCount() < previousFragmentCount;
        previousFragmentCount = getSupportFragmentManager().getBackStackEntryCount();
        return isBackPressed;
    }

    protected void toggleNext(ViewGroup linearLayout, TextView tvItems) {
        int size = 0;
        if(isReturnItems)
            size = ProductsAdapterHelper.getSelectedReturnProductItems().size();
        else
            size = ProductsAdapterHelper.getSelectedProductItems().size();
        toggleNext(linearLayout, tvItems, size);
    }

    protected void toggleNext(ViewGroup linearLayout, TextView tvItems, int size) {
        tvItems.setText(getResources().getQuantityString(R.plurals.items, size, size));
        AnimationTools.toggleShowHide(linearLayout, size == 0, 300);
    }


    protected void initializeDuplicateButton(Button btn, final String referenceNumber) {
        btn.setVisibility(View.VISIBLE);
        btn.setText("DUPLICATE");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogTools.showConfirmationDialog(ModuleActivity.this, "Duplicate " + referenceNumber, "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(historyDetailsListener != null)
                            historyDetailsListener.onDuplicateTransaction();
                    }
                }, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                }, R.style.AppCompatDialogStyle_Light);
            }
        });
    }

    protected void initializeVoidButton(Button btn, final String referenceNumber) {
        btn.setText("VOID");
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogTools.showConfirmationDialog(ModuleActivity.this, "Void " + referenceNumber, "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(historyDetailsListener != null)
                            historyDetailsListener.onVoidTransaction();
                    }
                }, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                }, R.style.AppCompatDialogStyle_Light);
            }
        });
    }


}
