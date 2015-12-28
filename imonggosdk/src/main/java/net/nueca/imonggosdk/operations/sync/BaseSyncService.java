package net.nueca.imonggosdk.operations.sync;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.nueca.imonggosdk.enums.DailySalesEnums;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.invoice.Discount;
import net.nueca.imonggosdk.objects.routeplan.RoutePlan;
import net.nueca.imonggosdk.objects.TaxRate;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.branchentities.BranchProduct;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.DailySales;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.associatives.ProductTaxRateAssoc;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.document.DocumentType;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Jn on 7/14/2015.
 * imonggosdk(2015)
 */
public abstract class BaseSyncService extends ImonggoService {
    public static final String PARAMS_SYNC_ALL_MODULES = "sync_all_modules";
    public static final String PARAMS_TABLES_TO_SYNC = "tables_to_sync"; // array tables/API
    public static final String PARAMS_INITIAL_SYNC = "initial_sync";
    public static final String PARAMS_SERVER = "mServer";
    public static final String TAG = "BaseSyncService";
    protected static int max_size_per_page = 50;
    protected boolean syncAllModules;
    protected boolean initialSync;
    protected int page = 1;
    protected int count = 0;
    protected int numberOfPages = 1;
    protected int branchIndex = 0;
    protected int mCustomIndex = 0;
    protected int mModulesIndex = 0;
    protected int responseCode = 200;
    protected int[] branches;
    protected Table[] mModulesToSync;
    protected Table mCurrentTableSyncing;
    protected Server mServer;
    protected List<BranchUserAssoc> branchUserAssoc;
    protected List<? extends BaseTable> listOfIds;
    protected String from = "", to = "";
    protected String document_type;
    protected String intransit_status;
    protected LastUpdatedAt lastUpdatedAt;
    protected LastUpdatedAt newLastUpdatedAt;
    protected IBinder mLocalBinder = new LocalBinder();
    protected VolleyRequestListener mVolleyRequestListener = null;
    protected SyncModulesListener mSyncModulesListener = null;

    protected Gson gson = new GsonBuilder().serializeNulls().create();

    /**
     * Empty Constructor
     */
    public BaseSyncService() {

    }

    /**
     * OnStartCommand runs after starService() on is called
     *
     * @param intent  Intent
     * @param flags   Additional data about this start request
     * @param startId A unique integer representing this specific request to start
     * @return START_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        Bundle bundle = intent.getExtras();
        syncAllModules = bundle.getBoolean(PARAMS_SYNC_ALL_MODULES, true);
        mServer = Server.values()[bundle.getInt(PARAMS_SERVER, Server.IMONGGO.ordinal())];
        initialSync = bundle.getBoolean(PARAMS_INITIAL_SYNC, false);

        if (!syncAllModules) { // if custom modules where selected to be download
            initializeTablesToSync(bundle.getIntArray(PARAMS_TABLES_TO_SYNC));
        } else { // Sync All Modules
            mModulesIndex = 0;
            mModulesToSync = new Table[]{
                    Table.USERS,
                    Table.BRANCH_USERS,
                    Table.TAX_SETTINGS,
                    Table.PRODUCTS,
                    Table.CUSTOMERS,
                    Table.UNITS,
                    Table.DOCUMENTS,
                    Table.DOCUMENT_TYPES,
                    Table.DOCUMENT_PURPOSES
            };
            mCurrentTableSyncing = mModulesToSync[mModulesIndex];
        }

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * @param syncAllModules boolean of choice if you want to sync all modules
     */
    public void setSyncAllModules(boolean syncAllModules) {
        this.syncAllModules = syncAllModules;
    }

    public void initializeTablesToSync(int[] forSyncing) {
        if (forSyncing != null) {
            Log.e("initializeTablesToSync", "--" + forSyncing.length);

            mModulesIndex = 0;
            mModulesToSync = new Table[forSyncing.length];
            for (int i = 0; i < mModulesToSync.length; i++) {
                mModulesToSync[i] = Table.values()[forSyncing[i]];
            }
            mCurrentTableSyncing = mModulesToSync[mModulesIndex];
        }
    }

    public boolean isExisting(Object o, Table table) throws SQLException {
        return isExisting(o, 0, table, null);
    }

    public boolean isExisting(int id, Table table) throws SQLException {
        return isExisting(null, id, table, null);
    }

    public boolean checkDailySales(Object o, Table table, DailySalesEnums dailySalesEnums) throws SQLException {
        return isExisting(o, 0, table, dailySalesEnums);
    }

    /**
     * Check if the table item is already existing in the database
     *
     * @param o
     * @param table
     * @return
     * @throws SQLException
     */
    public boolean isExisting(Object o, int id, Table table, DailySalesEnums dailySalesEnums) throws SQLException {


        switch (table) {
            case USERS: {
                User user = (User) o;
                return getHelper().fetchObjects(User.class).queryBuilder().where().eq("id", user.getId()).queryForFirst() != null;
            }
            case PRODUCTS: {
                Product product = (Product) o;
                return getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", product.getId()).queryForFirst() != null;
            }
            case UNITS: {
                Unit unit = (Unit) o;
                return getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("id", unit.getId()).queryForFirst() != null;
            }
            case BRANCHES: {
                Branch branch = (Branch) o;
                return getHelper().fetchObjects(Branch.class).queryBuilder().where().eq("id", branch.getId()).queryForFirst() != null;
            }
            case BRANCH_TAGS: {
                BranchTag branchTag = (BranchTag) o;
                return getHelper().fetchObjects(BranchTag.class).queryBuilder().where().eq("id", branchTag.getId()).queryForFirst() != null;
            }
            case CUSTOMERS: {
                Customer customer = (Customer) o;
                return getHelper().fetchObjects(Customer.class).queryBuilder().where().eq("id", customer.getId()).queryForFirst() != null;
            }
            case INVENTORIES: {
                Inventory inventory = (Inventory) o;
                return getHelper().fetchObjects(Inventory.class).queryBuilder().where().eq("id", inventory.getId()).queryForFirst() != null;
            }
            case TAX_SETTINGS: {
                TaxSetting taxSetting = (TaxSetting) o;
                return getHelper().fetchObjects(TaxSetting.class).queryBuilder().where().eq("id", taxSetting.getId()).queryForFirst() != null;
            }
            case TAX_RATES: {
                return getHelper().fetchObjects(TaxRate.class).queryBuilder().where().eq("tax_rate_id", id).queryForFirst() != null;
            }
            case PRODUCT_TAX_RATES: {
                ProductTaxRateAssoc productTaxRateAssoc = (ProductTaxRateAssoc) o;
                return getHelper().fetchObjects(ProductTaxRateAssoc.class).queryBuilder().where().eq("id", productTaxRateAssoc.getId()).queryForFirst() != null;
            }
            case DOCUMENT_PURPOSES: {
                DocumentPurpose documentPurpose = (DocumentPurpose) o;
                return getHelper().fetchObjects(DocumentPurpose.class).queryBuilder().where().eq("id", documentPurpose.getId()).queryForFirst() != null;
            }
            case DOCUMENT_TYPES: {
                DocumentType documentType = (DocumentType) o;
                return getHelper().fetchObjects(DocumentType.class).queryBuilder().where().eq("id", documentType.getId()).queryForFirst() != null;
            }
            case PRODUCT_TAGS: {
                ProductTag productTag = (ProductTag) o;
                return getHelper().fetchObjects(ProductTag.class).queryBuilder().where().eq("id", productTag.getId()).queryForFirst() != null;
            }
            case DOCUMENTS: {
                Document document = (Document) o;
                return getHelper().fetchObjects(Document.class).queryBuilder().where().eq("id", document.getId()).queryForFirst() != null;
            }
            case INVOICES: {
                Invoice invoice = (Invoice) o;
                return getHelper().fetchObjects(Invoice.class).queryBuilder().where().eq("id", invoice.getId()).queryForFirst() != null;
            }
            case INVOICE_PURPOSES: {
                InvoicePurpose invoicePurpose = (InvoicePurpose) o;
                return getHelper().fetchObjects(InvoicePurpose.class).queryBuilder().where().eq("id", invoicePurpose.getId()).queryForFirst() != null;
            }
            case PRICE_LISTS: {
                PriceList priceList = (PriceList) o;
                return getHelper().fetchObjects(InvoicePurpose.class).queryBuilder().where().eq("id", priceList.getId()).queryForFirst() != null;
            }
            case CUSTOMER_GROUPS: {
                CustomerGroup customerGroup = (CustomerGroup) o;
                return getHelper().fetchObjects(CustomerGroup.class).queryBuilder().where().eq("id", customerGroup.getId()).queryForFirst() != null;
            }
            case CUSTOMER_CATEGORIES: {
                CustomerCategory customerCategory = (CustomerCategory) o;
                return getHelper().fetchObjects(CustomerCategory.class).queryBuilder().where().eq("id", customerCategory.getId()).queryForFirst() != null;
            }
            case PAYMENT_TERMS: {
                PaymentTerms paymentTerms = (PaymentTerms) o;
                return getHelper().fetchObjects(PaymentTerms.class).queryBuilder().where().eq("id", paymentTerms.getId()).queryForFirst() != null;
            }
            case PAYMENT_TYPES: {
                PaymentType paymentType = (PaymentType) o;
                return getHelper().fetchObjects(PaymentType.class).queryBuilder().where().eq("id", paymentType.getId()).queryForFirst() != null;
            }
            case ROUTE_PLANS: {
                RoutePlan routePlan = (RoutePlan) o;
                return getHelper().fetchObjects(RoutePlan.class).queryBuilder().where().eq("id", routePlan.getId()).queryForFirst() != null;
            }
            case BRANCH_PRODUCTS: {
                BranchProduct branchProduct = (BranchProduct) o;
                return getHelper().fetchObjects(BranchProduct.class).queryBuilder().where().eq("product_id", branchProduct.getProduct()).and().eq("branch_id", branchProduct.getBranch()).queryForFirst() != null;
            }
            case SALES_PROMOTIONS_DISCOUNT: {
                Discount discount = (Discount) o;
                return getHelper().fetchObjects(Discount.class).queryBuilder().where().eq("id", discount.getId()) != null;
            }
            case PRICE_LISTS_DETAILS:
                Price price = (Price) o;
                return getHelper().fetchObjects(Price.class).queryBuilder().where().eq("id", price.getId()).queryForFirst() != null;
            case DAILY_SALES: {
                DailySales dailySales = (DailySales) o;
                if (dailySalesEnums == DailySalesEnums.DATE_OF_DAILY_SALES) {
                    return getHelper().fetchObjects(DailySales.class).queryBuilder().where().eq("date_of_sales", dailySales.getDate_of_sales()).queryForFirst() != null;
                } else if (dailySalesEnums == DailySalesEnums.DATE_REQUESTED) {

                    DailySales dailySalesDB = getHelper().fetchObjects(DailySales.class).queryBuilder().where().eq("date_of_sales", dailySales.getDate_of_sales()).queryForFirst();

                    if (dailySalesDB != null) {
                        Log.e(TAG, "daily sales db: " + dailySalesDB.toString());

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Calendar calendar1 = Calendar.getInstance();
                        Calendar calendar2 = Calendar.getInstance();

                        try {
                            String dateparse1 = dailySales.getDate_requested_at();
                            String dateparse2 = dailySalesDB.getDate_requested_at();

                            java.util.Date date1 = dateFormat.parse(dateparse1);
                            java.util.Date date2 = dateFormat.parse(dateparse2);

                            calendar1.setTime(date1);
                            calendar2.setTime(date2);

                            int comparison_result = calendar1.compareTo(calendar2);
                            Log.e(TAG, "Compare Result : " + comparison_result);
                            Log.e(TAG, dateparse2 + " < " + dateparse1);

                            if (comparison_result == 1) {
                                return true;
                            } else if (comparison_result == 0 || comparison_result == -1) {
                                return false;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "dailysalesDB is null");
                    }
                    return false;
                }
            }
        }
        return false;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i("onBind", "is called");
        return mLocalBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("onUnbind", "is called");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i("onRebind", "this is called");
    }

    @Override
    public void onDestroy() {

        // Tell the user we stopped.
        Log.e(TAG, "Sync Service has stopped");
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public BaseSyncService getService() {
            return BaseSyncService.this;
        }
    }

    public Table getCurrentTableSyncing() {
        return mCurrentTableSyncing;
    }


    /**
     * removes modules from array for Re-Sync
     */
    public void prepareModulesToReSync() {

        int length = mModulesToSync.length;
        int newlength = 0;

        //Log.e(TAG, "Tables To Sync:");

        newlength = length - mModulesIndex;
        Table[] temp = new Table[newlength];

        // Log.e(TAG, "Current Index: " + mModulesIndex + " Length: " + length + " new length: " + newlength);

        if (newlength != 0) {
            int x = 0;
            for (int i = mModulesIndex; i < length; i++) {
                temp[x] = Table.values()[mModulesToSync[i].ordinal()];
                x++;
            }
            mModulesToSync = temp;
            //  Log.e(TAG, "Temp length is " + temp.length);
        }

        // reset variable
        page = 0;
        numberOfPages = 0;
        count = 0;
        branchIndex = 0;
        mModulesIndex = 0;

        try {
            getHelper().deleteAll(mCurrentTableSyncing.getTableClass());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Table[] getModulesToSync() {
        return mModulesToSync;
    }

    public void setSyncModulesListener(SyncModulesListener syncModulesListener) {
        this.mSyncModulesListener = syncModulesListener;
    }

    public Branch getBranchWithID(int id) throws SQLException {
        return getHelper().fetchIntId(Branch.class).queryForId(id);

    }

    public Product getProductWithID(int id) throws SQLException {
        return getHelper().fetchIntId(Product.class).queryForId(id);

    }

    public Unit getUnitWithID(int id) throws SQLException {
        return getHelper().fetchIntId(Unit.class).queryForId(id);

    }

    public VolleyRequestListener getVolleyRequestListener() {
        return this.mVolleyRequestListener;
    }
}
