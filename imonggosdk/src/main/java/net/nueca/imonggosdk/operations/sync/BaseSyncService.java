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
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.Customer;
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
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.document.DocumentType;

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
    protected IBinder mLocalBinder = new LocalBinder();
    protected SyncModulesListener mSyncModulesListener = null;
    protected VolleyRequestListener mVolleyRequestListener = null;
    protected Server mServer;
    protected int page = 1;
    protected int count = 0;
    protected int numberOfPages = 1;
    protected LastUpdatedAt lastUpdatedAt;
    protected LastUpdatedAt newLastUpdatedAt;
    protected String from = "", to = "";
    protected Gson gson = new GsonBuilder().serializeNulls().create();
    protected Table mCurrentTableSyncing;
    protected List<BranchUserAssoc> branchUserAssoc;
    protected Table[] mModulesToSync;
    protected int[] branches;
    protected int branchIndex = 0;
    protected int mModulesIndex = 0;
    protected boolean syncAllModules;
    protected boolean initialSync;
    protected String document_type;
    protected String intransit_status;
    protected int responseCode = 200;
    private int NOTIFICATION_ID = 200;

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
                return getHelper().getUsers().queryBuilder().where().eq("id", user.getId()).queryForFirst() != null;
            }
            case PRODUCTS: {
                Product product = (Product) o;
                return getHelper().getProducts().queryBuilder().where().eq("id", product.getId()).queryForFirst() != null;
            }
            case UNITS: {
                Unit unit = (Unit) o;
                return getHelper().getUnits().queryBuilder().where().eq("id", unit.getId()).queryForFirst() != null;
            }
            case BRANCHES: {
                Branch branch = (Branch) o;
                return getHelper().getBranches().queryBuilder().where().eq("id", branch.getId()).queryForFirst() != null;
            }
            case BRANCH_PRICES: {
                BranchPrice branchPrice = (BranchPrice) o;
                return getHelper().getBranchPrices().queryBuilder().where().eq("id", branchPrice.getId()).queryForFirst() != null;
            }
            case BRANCH_TAGS: {
                BranchTag branchTag = (BranchTag) o;
                return getHelper().getBranchTags().queryBuilder().where().eq("id", branchTag.getId()).queryForFirst() != null;
            }
            case CUSTOMERS: {
                Customer customer = (Customer) o;
                return getHelper().getCustomers().queryBuilder().where().eq("id", customer.getId()).queryForFirst() != null;
            }
            case INVENTORIES: {
                Inventory inventory = (Inventory) o;
                return getHelper().getInventories().queryBuilder().where().eq("id", inventory.getId()).queryForFirst() != null;
            }
            case TAX_SETTINGS: {
                TaxSetting taxSetting = (TaxSetting) o;
                return getHelper().getTaxSettings().queryBuilder().where().eq("id", taxSetting.getId()).queryForFirst() != null;
            }
            case TAX_RATES: {
                return getHelper().getTaxRates().queryBuilder().where().eq("tax_rate_id", id).queryForFirst() != null;
            }
            case PRODUCT_TAX_RATES: {
                ProductTaxRateAssoc productTaxRateAssoc = (ProductTaxRateAssoc) o;
                return getHelper().getProductTaxRateAssocs().queryBuilder().where().eq("id", productTaxRateAssoc.getId()).queryForFirst() != null;
            }
            case DOCUMENT_PURPOSES: {
                DocumentPurpose documentPurpose = (DocumentPurpose) o;
                return getHelper().getDocumentPurposes().queryBuilder().where().eq("id", documentPurpose.getId()).queryForFirst() != null;
            }
            case DOCUMENT_TYPES: {
                DocumentType documentType = (DocumentType) o;
                return getHelper().getDocumentTypes().queryBuilder().where().eq("id", documentType.getId()).queryForFirst() != null;
            }
            case PRODUCT_TAGS: {
                ProductTag productTag = (ProductTag) o;
                return getHelper().getProductTags().queryBuilder().where().eq("id", productTag.getId()).queryForFirst() != null;
            }
            case DOCUMENTS: {
                Document document = (Document) o;
                return getHelper().getDocuments().queryBuilder().where().eq("id", document.getId()).queryForFirst() != null;
            }
            case DAILY_SALES: {

                DailySales dailySales = (DailySales) o;

                if (dailySalesEnums == DailySalesEnums.DATE_OF_DAILY_SALES) {
                    return getHelper().getDailySales().queryBuilder().where().eq("date_of_sales", dailySales.getDate_of_sales()).queryForFirst() != null;
                } else if (dailySalesEnums == DailySalesEnums.DATE_REQUESTED) {

                    DailySales dailySalesDB = getHelper().getDailySales().queryBuilder().where().eq("date_of_sales", dailySales.getDate_of_sales()).queryForFirst();

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


    public void setSyncModulesListener(SyncModulesListener syncModulesListener) {
        this.mSyncModulesListener = syncModulesListener;
    }

    public VolleyRequestListener getVolleyRequestListener() {
        return this.mVolleyRequestListener;
    }
}
