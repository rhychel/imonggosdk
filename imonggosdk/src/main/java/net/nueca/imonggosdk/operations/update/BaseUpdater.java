package net.nueca.imonggosdk.operations.update;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.interfaces.SyncServiceOperationListener;
import net.nueca.imonggosdk.operations.sync.SyncModules;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.LoggingTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseUpdater implements SyncModulesListener {

    public static String TAG = "BaseUpdater";
    protected SyncModulesListener mSyncModulesListener;
    protected SyncServiceOperationListener mSyncServiceOperation;
    protected SyncModules mSyncModules;
    protected ServiceConnection mConnection;
    protected Intent mServiceIntent;
    protected int mServer = Server.IMONGGO.ordinal();
    protected int[] mModules = null;

    protected Boolean isInitialSync = true;
    protected Boolean isSyncAllModules = true;
    protected Boolean mServiceBounded = false;

    protected Class<?> cls = SyncModules.class;
    protected Context mContext;

    protected void startSync() throws SQLException {

        if (mServiceBounded) {
            setUpTablesToDownload();
            onPrepareDialog();

            if (mSyncModules != null) {
                mSyncModules.initializeTablesToSync(mModules);
                mSyncModules.setSyncAllModules(isSyncAllModules);
                mSyncModules.setSyncModulesListener(mSyncModulesListener);
                mSyncModules.startFetchingModules();
            } else {
                mSyncModulesListener.onDismissDialog();
                startSyncService();
            }
        } else {
            startSyncService();
            mSyncModulesListener.onErrorDownload(null, "Cannot Start Update, Service not binded");
        }

    }

    @Override
    public void onStartDownload(Table table) {
        mSyncModulesListener.onStartDownload(table);
    }

    @Override
    public void onDownloadProgress(Table table, int page, int max) {
        if (mSyncModulesListener != null) {
            mSyncModulesListener.onDownloadProgress(table, page, max);
        }
    }

    @Override
    public void onEndDownload(Table table) {
        if (mSyncModulesListener != null) {
            mSyncModulesListener.onEndDownload(table);
        }
    }

    @Override
    public void onFinishDownload() {
        if (mSyncModulesListener != null) {
            mSyncModulesListener.onFinishDownload();
        }
    }

    @Override
    public void onErrorDownload(Table table, String message) {
        if (mSyncModulesListener != null) {
            mSyncModulesListener.onErrorDownload(table, message);
        }
    }

    @Override
    public void onPrepareDialog() {
        if (mSyncModulesListener != null) {
            mSyncModulesListener.onPrepareDialog();
        }
    }

    @Override
    public void onDismissDialog() {

    }


    public void startSyncService() {

        if (mSyncServiceOperation != null) {
            mSyncServiceOperation.onStartSyncService();
        }

    }

    private void setUpTablesToDownload() {
        if (mModules.length == 0) {
            Log.e(TAG, "mModules is null getting in shared pref");
            // Get Tables in SharedPref
            mModules = AccountTools.getModulesSyncing(mContext);

            // If still null then put set it manually
            if (mModules.length == 0) {
                Log.e(TAG, "mModules is null manual setting ");
                mModules = new int[]{
                        Table.USERS_ME.ordinal(),
                        Table.BRANCH_USERS.ordinal(),
                        Table.SETTINGS.ordinal()
                };
            }
        } else {
            Log.e(TAG, "modules is not null, size: " + mModules.length );
        }

        for (int module : mModules) {
            Log.e(TAG, Table.values()[module] + "");
            Table table = Table.values()[module];
            if (table.isNoNAPI()) {
                LoggingTools.showToast(mContext.getApplicationContext(), "You have added unsupported module. please check your code");
                Log.i(TAG, "You have added unsupported module. please check your code. " + table);
            }
        }
    }
}
