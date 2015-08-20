package net.nueca.imonggosdk.operations.sync;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.nueca.imonggosdk.R;
import net.nueca.imonggosdk.activities.ImonggoActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.BranchTag;
import net.nueca.imonggosdk.objects.Customer;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.TaxSetting;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.ProductTaxRateAssoc;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;
import net.nueca.imonggosdk.objects.document.DocumentType;

import java.sql.SQLException;

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
    protected int responseCode = 200;
    protected int count = 0;
    protected int numberOfPages = 1;

    protected LastUpdatedAt lastUpdatedAt;
    protected LastUpdatedAt newLastUpdatedAt;
    protected String from = "", to = "";
    protected Gson gson = new GsonBuilder().serializeNulls().create();

    protected Table mCurrentTableSyncing;

    protected Table[] mModulesToSync;
    protected int[] branches;
    protected int branchIndex = 0;
    protected int mModulesIndex = 0;

    protected boolean syncAllModules;
    protected boolean initialSync;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    private int NOTIFICATION_ID = 200;

    public BaseSyncService() {

    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this);
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

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
            int[] forSyncing = bundle.getIntArray(PARAMS_TABLES_TO_SYNC);

            if (forSyncing != null) {
                mModulesIndex = 0;
                mModulesToSync = new Table[forSyncing.length];
                for (int i = 0; i < mModulesToSync.length; i++) {
                    mModulesToSync[i] = Table.values()[forSyncing[i]];
                }

                mCurrentTableSyncing = mModulesToSync[mModulesIndex];
            }
        } else { // Sync All Modules
            /**
             * Application Settings //==> During login
             * Users -- LAST_UPDATED_AT
             * User Branches -- COUNT
             * Tax Settings -- LAST_UPDATED_AT
             * Products -- LAST_UPDATED_AT, COUNT
             * Inventory -- LAST_UPDATED_AT, COUNT
             * Customers -- LAST_UPDATED_AT, COUNT
             * Documents -- LAST_UPDATED_AT, COUNT
             *
             * Document Types -- #CONSTANT
             * Document Purposes -- LAST_UPDATED_AT, COUNT
             * Sales Promotion
             */
            mModulesIndex = 0;
            mModulesToSync = new Table[]{
                    Table.USERS, Table.TAX_RATES,
                    Table.BRANCH_USERS, Table.PRODUCTS,
                    Table.INVENTORIES, Table.CUSTOMERS,
                    Table.DOCUMENTS, Table.DOCUMENT_TYPES,
                    };
            mCurrentTableSyncing = mModulesToSync[mModulesIndex];
        }

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public boolean isExisting(Object o, Table table) throws SQLException {
        return isExisting(o, 0, table);
    }
    public boolean isExisting(int id, Table table) throws SQLException {
        return isExisting(null, id, table);
    }

    /**
     * Check if the table item is already existing in the database
     *
     * @param o
     * @param table
     * @return
     * @throws SQLException
     */
    public boolean isExisting(Object o, int id, Table table) throws SQLException {
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
                DocumentPurpose documentPurpose= (DocumentPurpose) o;
                return getHelper().getDocumentPurposes().queryBuilder().where().eq("id", documentPurpose.getId()).queryForFirst() != null;
            }
            case DOCUMENT_TYPES: {
                DocumentType documentType= (DocumentType) o;
                return getHelper().getDocumentTypes().queryBuilder().where().eq("id", documentType.getId()).queryForFirst() != null;
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
        // Cancel the persistent notification.
        mNotificationManager.cancel(NOTIFICATION_ID);

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

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // Set the icon, scrolling text and timestamp
        mNotificationBuilder
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentTitle("Test Service")
                .setContentText("Service is starting");

        Intent resultIntent = new Intent(this, ImonggoActivity.class);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mNotificationBuilder.setContentIntent(resultPendingIntent);
        // Send the notification.
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    public void setSyncModulesListener(SyncModulesListener syncModulesListener) {
        this.mSyncModulesListener = syncModulesListener;
    }

    public VolleyRequestListener getVolleyRequestListener() {
        return this.mVolleyRequestListener;
    }
}
