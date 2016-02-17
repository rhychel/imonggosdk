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
import net.nueca.concessioengine.tools.InvoiceTools;
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
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.ModuleSettingTools;
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

    protected ConcessioModule concessioModule = ConcessioModule.STOCK_REQUEST;
    protected boolean isFromCustomersList = false;
    protected boolean isMultiInput = false;
    protected boolean clearTransactions = true;
    protected boolean isReturnItems = false;
    protected boolean initSelectedCustomer = true;
    protected boolean isForHistoryDetail = false;
    private ModuleSetting moduleSetting;
    protected Customer customer;

    protected String reference;
    protected boolean isLayaway = false;
    protected boolean isDuplicating = false;

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
            if(!mSearch.isIconified())
                closeSearchField(mSearch);
            else
                super.onBackPressed();
        }
        else
            super.onBackPressed();
    }

    // TODO Search the document
    public List<Document> getDocument(int branchId, String referenceNumber) {
        List<Document> documents = new ArrayList<>();
        return documents;
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

        List<ModuleSetting> moduleSettings = getActiveModuleSetting(HISTORY_ITEM_FILTERS);

        for(ModuleSetting moduleSetting : moduleSettings) {
            if(moduleSetting.is_enabled())
                transactionTypes.add(moduleSetting.getModuleType().setLabel(moduleSetting.getLabel()));
        }

        return transactionTypes;
    }

    /**
     * Generate the user's branches.
     * @return
     */
    public List<Branch> getBranches() {
        List<Branch> assignedBranches = new ArrayList<>();
        try {
            List<BranchUserAssoc> branchUserAssocs = getHelper().fetchObjects(BranchUserAssoc.class).queryBuilder().where().eq("user_id", getUser()).query();
            for(BranchUserAssoc branchUser : branchUserAssocs) {
                if(branchUser.getBranch().getId() == getUser().getHome_branch_id())
                    assignedBranches.add(0, branchUser.getBranch());
                else
                    assignedBranches.add(branchUser.getBranch());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignedBranches;
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
                    categories.add(category);
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

    /**
     *
     * @param context
     * @param servingBranchId --- Pass the serving branch id of the order. Preferably, this should be the warehouse branch id
     * @return
     */
    public Order generateOrder(Context context, int servingBranchId) {
        Order.Builder order = new Order.Builder();
        for(int i = 0;i < ProductsAdapterHelper.getSelectedProductItems().size();i++) {
            SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().get(i);
            Values value = selectedProductItem.getValues().get(0);

            OrderLine orderLine = new OrderLine.Builder()
                    .line_no(value.getLine_no())
                    .product_id(selectedProductItem.getProduct().getId())
                    .quantity(Double.valueOf(value.getActualQuantity()))
                    .build();
            if(value.isValidUnit()) {
                orderLine.setUnit_id(value.getUnit().getId());
                orderLine.setUnit_name(value.getUnit_name());
                orderLine.setUnit_content_quantity(value.getUnit_content_quantity());
                orderLine.setUnit_quantity(Double.valueOf(value.getUnit_quantity()));
                orderLine.setUnit_retail_price(value.getUnit_retail_price());
            }
            order.addOrderLine(orderLine);
        }
        order.order_type_code("stock_request");
        try {
            order.serving_branch_id(servingBranchId);
            order.generateReference(context, getSession().getDevice_id());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order.build();
    }

    /**
     * Simple generateDocument to create a Document object for PHYSICAL_COUNT.
     * @param context
     * @return
     */
    public Document generateDocument(Context context) {
        return generateDocument(context, -1, DocumentTypeCode.PHYSICAL_COUNT);
    }

    /**
     * Generate a Document object for sending.
     * @param context
     * @param targetBranchId
     * @param documentTypeCode
     * @return
     */
    public Document generateDocument(Context context, int targetBranchId, DocumentTypeCode documentTypeCode) {
        Document.Builder pcount = new Document.Builder();
//        TimerTools.duration("generateDocument -- first loop", true);
        for(int i = 0;i < ProductsAdapterHelper.getSelectedProductItems().size();i++) {
            SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().get(i);
//            TimerTools.duration("generateDocument -- second loop", true);
            for(Values value : selectedProductItem.getValues()) {
                DocumentLine.Builder builder = new DocumentLine.Builder()
                        .line_no(value.getLine_no())
                        .product_id(selectedProductItem.getProduct().getId())
                        .quantity(Double.valueOf(value.getActualQuantity()));
                if(value.getExtendedAttributes() != null) {
                    ExtendedAttributes extendedAttributes = value.getExtendedAttributes();
                    // if pcount
//                    extendedAttributes.setBatch_no("0");
                    builder.extras(extendedAttributes.convertForDocumentLine());
                }

                DocumentLine documentLine = builder.build();
                if(value.isValidUnit()) {
                    documentLine.setUnit_id(value.getUnit().getId());
                    documentLine.setUnit_name(value.getUnit_name());
                    documentLine.setUnit_content_quantity(value.getUnit_content_quantity());
                    documentLine.setUnit_quantity(Double.valueOf(value.getUnit_quantity()));
                    documentLine.setUnit_retail_price(value.getUnit_retail_price());
                }

                pcount.addDocumentLine(documentLine);
            }
//            TimerTools.duration("generateDocument -- second loop, end", true);
        }
//        TimerTools.duration("generateDocument -- first loop, end", true);
        pcount.customer(ProductsAdapterHelper.getSelectedCustomer()); // can be null
        pcount.document_type_code(documentTypeCode);
        if(targetBranchId > -1)
            pcount.target_branch_id(targetBranchId);
        try {
            pcount.generateReference(context, getSession().getDevice_id());
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        TimerTools.duration("generateDocument BUILD", true);
        return pcount.build();
    }

    /**
     *
     * @return the number of inventory objects updated
     */
    public int updateInventoryFromSelectedItemList(boolean shouldAdd) {
        int updated = 0;
        BatchList<Inventory> newInventories = new BatchList<>(DatabaseOperation.INSERT, getHelper());
        BatchList<Inventory> updateInventories = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
        TimerTools.duration("updateInventoryFromSelectedItemList, first loop", true);
        for(SelectedProductItem selectedProductItem : ProductsAdapterHelper.getSelectedProductItems()) {
            if(selectedProductItem.getInventory() != null) {
                Inventory updateInventory = selectedProductItem.getInventory();
                updateInventory.setQuantity(Double.valueOf(selectedProductItem.updatedInventory(shouldAdd)));
//                updateInventory.updateTo(getHelper());
                updateInventories.add(updateInventory);
                updated++;
            }
            else {
                Inventory newInventory = new Inventory();
                newInventory.setProduct(selectedProductItem.getProduct());
                newInventory.setQuantity(Double.valueOf(selectedProductItem.updatedInventory(shouldAdd)));
                newInventories.add(newInventory);
//                newInventory.insertTo(getHelper());
//                Product product = selectedProductItem.getProduct();
//                product.setInventory(newInventory);
//                product.updateTo(getHelper());
                updated++;
            }
        }
        TimerTools.duration("updateInventoryFromSelectedItemList, first loop, end", true);
        newInventories.doOperation(Inventory.class);
        updateInventories.doOperation(Inventory.class);
        return updated;
    }

    protected int revertInventoryFromDocument(Document document, boolean shouldAdd) {
        int updated = 0;
        List<DocumentLine> documentLines = document.getDocument_lines();
        BatchList<Inventory> inventories = new BatchList<>(DatabaseOperation.UPDATE, getHelper());
        for(DocumentLine documentLine : documentLines) {
            try {
                Log.e("revertInventoryFromDoc", shouldAdd+"");
                Inventory inventory = getHelper().fetchObjectsInt(Inventory.class).queryBuilder().where().eq("product_id", documentLine.getProduct_id()).queryForFirst();
                inventory.operationQuantity(documentLine.getQuantity(), shouldAdd);
//                inventory.updateTo(getHelper());
                inventories.add(inventory);
                updated++;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        inventories.doOperation(Inventory.class);
        return updated;
    }

    protected List<Product> processOfflineData(OfflineData offlineData) throws SQLException {
        List<Product> productList = new ArrayList<>();
        if(offlineData.getType() == OfflineData.ORDER) {
            Order order = offlineData.getObjectFromData(Order.class);
            List<OrderLine> orderLines = order.getOrder_lines();
            for(OrderLine orderLine : orderLines) {
                Product product = getHelper().fetchIntId(Product.class).queryForId(orderLine.getProduct_id());
                if(productList.indexOf(product) == -1)
                    productList.add(product);

                SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().initializeItem(product);
                String quantity = "0";
                Unit unit = null;
                if(orderLine.getUnit_id() != null)
                    unit = getHelper().fetchIntId(Unit.class).queryForId(orderLine.getUnit_id());
                if(unit != null)
                    quantity = orderLine.getUnit_quantity().toString();
                else {
                    unit = new Unit();
                    unit.setId(-1);
                    unit.setName(product.getBase_unit_name());
                    quantity = String.valueOf(orderLine.getQuantity());
                }
                Values values = new Values(unit, quantity);
                values.setLine_no(orderLine.getLine_no());
                selectedProductItem.addValues(values);
                ProductsAdapterHelper.getSelectedProductItems().add(selectedProductItem);
            }
            return productList;
        }
        else if(offlineData.getType() == OfflineData.DOCUMENT) {
            Document document = offlineData.getObjectFromData(Document.class);
            List<DocumentLine> documentLines = document.getDocument_lines();
            for(DocumentLine documentLine : documentLines) {
                Product product = getHelper().fetchIntId(Product.class).queryForId(documentLine.getProduct_id());
                if(productList.indexOf(product) == -1)
                    productList.add(product);

                SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().initializeItem(product);
                String quantity = "0";
                Unit unit = null;
                if(documentLine.getUnit_id() != null)
                    unit = getHelper().fetchIntId(Unit.class).queryForId(documentLine.getUnit_id());
                if(unit != null)
                    quantity = documentLine.getUnit_quantity().toString();
                else {
                    unit = new Unit();
                    unit.setId(-1);
                    unit.setName(product.getBase_unit_name());
                    quantity = String.valueOf(documentLine.getQuantity());
                }
                Values values = new Values(unit, quantity);
                values.setLine_no(documentLine.getLine_no());
                selectedProductItem.addValues(values);
                ProductsAdapterHelper.getSelectedProductItems().add(selectedProductItem);
            }
            Log.e("SelectedProductItems", ProductsAdapterHelper.getSelectedProductItems().size()+"");
            return productList;
        }
        else if(offlineData.getType() == OfflineData.INVOICE) {

        }
        return productList;
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
