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
import net.nueca.imonggosdk.tools.SyncServiceHelper;

public abstract class BaseUpdater implements SyncModulesListener {

    public static String TAG = "BaseUpdater";
    protected SyncModulesListener mSyncModulesListener;
    protected SyncServiceOperationListener mSyncServiceOperation;
    protected SyncModules mSyncModules;
    protected ServiceConnection mConnection;
    protected Intent mServiceIntent;
    protected int mServer = Server.IMONGGO.ordinal();
    protected int[] mModules;

    protected Boolean isInitialSync = true;
    protected Boolean isSyncAllModules = true;
    protected Boolean mServiceBounded = false;

    protected Class<?> cls = SyncModules.class;
    protected Context context;

    protected Boolean startUpdate() {

        if (mSyncModulesListener == null) {
            Log.e(TAG, "Can't start sync, Listener is null");
            return false;
        }

        return true;
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


    public void startSyncService() {

        if (mSyncServiceOperation != null) {
            mSyncServiceOperation.onStartSyncService();
        }

    }

}
