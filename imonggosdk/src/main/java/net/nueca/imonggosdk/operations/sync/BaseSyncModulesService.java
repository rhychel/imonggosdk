package net.nueca.imonggosdk.operations.sync;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.User;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public abstract class BaseSyncModulesService extends ImonggoService {

    public static final String PARAMS_SYNC_ALL_MODULES = "sync_all_modules";
    public static final String PARAMS_IS_SECURED = "is_secured";
    public static final String PARAMS_INITIAL_SYNC = "initial_sync";
    public static final String PARAMS_IS_ACTIVE_ONLY = "is_active_only";
    public static final String PARAMS_TABLES_TO_SYNC = "tables_to_sync"; // array tables/API
    public static final String PARAMS_SERVER = "server";

    public static boolean isRequesting = true;
    private User user;
    protected Server server;

    protected int page = 1, responseCode = 200, count = 0, numberOfPages = 1;

    protected boolean syncAllModules = true,
            isSecured = false,
            initialSync = false,
            isActiveOnly = true;

    protected Table[] tablesToSync;
    protected int tablesIndex = 0;
    protected int[] branches;
    protected int branchIndex = 0;

    protected Table tableSyncing;

    protected ImonggoDBHelper dbHelper;
    private RequestQueue queue;
    private Session session;
    protected LastUpdatedAt lastUpdatedAt, newLastUpdatedAt;
    protected String from = "", to = "";

    protected Gson gson = new GsonBuilder().serializeNulls().create();

    protected IBinder syncModulesLocalBinder = new SyncModulesLocalBinder();
    protected SyncModulesListener syncModulesListener = null;

    public abstract void startSync();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        Bundle bundle = intent.getExtras();
        syncAllModules = bundle.getBoolean(PARAMS_SYNC_ALL_MODULES, true);
        isSecured = bundle.getBoolean(PARAMS_IS_SECURED, false);
        initialSync = bundle.getBoolean(PARAMS_INITIAL_SYNC, false);
        isActiveOnly = bundle.getBoolean(PARAMS_IS_ACTIVE_ONLY, true);
        server = Server.values()[bundle.getInt(PARAMS_SERVER, Server.IMONGGO.ordinal())];

        if(syncAllModules) {
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
            tablesIndex = 0;
            tablesToSync = new Table[]{Table.USERS, Table.TAX_RATES,
                                        Table.PRODUCTS, Table.INVENTORIES,
                                        Table.CUSTOMERS, Table.DOCUMENTS,
                                        Table.DOCUMENT_TYPES};
            /*
            TODO , Table.SALES_PROMOTIONS
             */
            tableSyncing = tablesToSync[tablesIndex];
        }
        else {
            int[] forSyncing = bundle.getIntArray(PARAMS_TABLES_TO_SYNC);
            if (forSyncing != null) {
                tablesIndex = 0;
                tablesToSync = new Table[forSyncing.length];
                for (int i = 0; i < forSyncing.length; i++) {
                    tablesToSync[i] = Table.values()[forSyncing[i]];
                }

                tableSyncing = tablesToSync[tablesIndex];
            }
        }

        startSync();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(dbHelper != null) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncModulesLocalBinder;
    }

    /**
     * Get the requestqueue
     * @return
     */
    protected RequestQueue getQueue() {
        if(queue == null)
            queue = Volley.newRequestQueue(this);
        return queue;
    }

    /**
     * Get the database helper
     * @return
     */
    protected ImonggoDBHelper getHelper() {
        if(dbHelper == null)
            dbHelper = OpenHelperManager.getHelper(this, ImonggoDBHelper.class);
        return dbHelper;

    }

    /**
     * Get the current session.
     * @return
     */
    protected Session getSession() {
        if(session == null) {
            try {
                session = getHelper().getSessions().queryBuilder().queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return session;
    }

    protected User getUser() {
        if(user == null)
            try {
                user = getHelper().getUsers().queryBuilder().where().eq("email", getSession().getEmail()).queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return user;
    }

    /**
     * Check if the table item is already existing in the database
     * @param o
     * @param table
     * @return
     * @throws SQLException
     */
    protected boolean isExisting(Object o, Table table) throws SQLException {
        switch (table) {
            case USERS: {
                User user = (User)o;
                return getHelper().getUsers().queryBuilder().where().eq("id", user.getId()).queryForFirst() != null;
            }
            case PRODUCTS: {
                Product product = (Product) o;
                return getHelper().getProducts().queryBuilder().where().eq("id", product.getId()).queryForFirst() != null;
            }
        }
        return false;
    }

    protected void initializeFromTo() {
        SimpleDateFormat convertStringToDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");

        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        from = convertStringToDate.format(now.getTime());
        now.add(Calendar.MONTH, -3);
        now.set(Calendar.HOUR_OF_DAY, 23);
        now.set(Calendar.MINUTE, 59);
        now.set(Calendar.SECOND, 59);
        to = convertStringToDate.format(now.getTime());
    }

    public class SyncModulesLocalBinder extends Binder {
        public BaseSyncModulesService getSyncModulesInstance() {
            return BaseSyncModulesService.this;
        }
    }

    public void setSyncModulesListener(SyncModulesListener syncModulesListener) {
        this.syncModulesListener = syncModulesListener;
    }
}