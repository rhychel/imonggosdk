package net.nueca.imonggosdk.operations.update;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.interfaces.SynServiceAsyncTaskListener;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.interfaces.SyncServiceConnectionListener;
import net.nueca.imonggosdk.interfaces.SyncServiceOperationListener;
import net.nueca.imonggosdk.operations.service.SyncServiceAsyncTask;
import net.nueca.imonggosdk.operations.sync.BaseSyncService;
import net.nueca.imonggosdk.operations.sync.SyncModules;
import net.nueca.imonggosdk.tools.SyncServiceHelper;

/**
 * Created by Jn on 20/01/16.
 */
public class APIDownloader extends BaseUpdater implements SyncServiceConnectionListener, SynServiceAsyncTaskListener, SyncServiceOperationListener {

    public static String TAG = "APIDownloader";

    public APIDownloader() {
    }

    public APIDownloader(Context ctx) {
        this.context = ctx;
    }

    public APIDownloader(Context context, Boolean initialSync) {
        this.isInitialSync = initialSync;
    }

    public void setSyncServer(Server server) {
        this.mServer = server.ordinal();
    }

    public void setSyncServer(int server) {
        this.mServer = server;
    }

    public void forUpdating() {
        this.isInitialSync = false;
    }

    public void setModulesToSync(int[] modules) {
        this.isSyncAllModules = false;

        if(modules != null) {
            this.mModules = modules;
        }
    }

    public void setSyncModulesListener(SyncModulesListener syncModulesListener) {
        this.mSyncModulesListener = syncModulesListener;
    }

    public void execute() {
        execute(context, null);
    }

    public void execute(Context tContext, Class<?> tClass) {
        if(tClass != null) {
            cls = tClass;
            mServiceIntent = new Intent(tContext, tClass);
        } else {
            mServiceIntent = new Intent(tContext, SyncModules.class);
        }

        context = tContext;
        mSyncServiceOperation = this;

        mConnection  = SyncServiceHelper.SyncServiceConnection(this);
        new SyncServiceAsyncTask(this).execute();

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        BaseSyncService.LocalBinder mLocalBinder = (BaseSyncService.LocalBinder) service;

        mSyncModules = (SyncModules) mLocalBinder.getService();

        if(mSyncModules != null) {
            mServiceBounded = true;

            if(mSyncModulesListener != null) {
                mSyncModules.setSyncModulesListener(mSyncModulesListener);
            }

            Log.e(TAG, "Successfully bind Service and Activity");
        } else {
            Log.e(TAG, "Cannot bind Service and Activity");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceBounded = false;
        mSyncModules = null;
    }

    @Override
    public String doInBackground(String... params) {

        startSyncService();
        mSyncModules = null;

        while(!mServiceBounded || mSyncModules == null) {
            Log.e(TAG, "Sync Service is not yet binded");
        }

        return "Sync Service now binded";
    }

    @Override
    public void onPostExecute(String result) {
        // start downloading
        Log.e(TAG, "Starting download");
    }

    @Override
    public void onStartSyncService() {
        mServiceIntent.putExtra(SyncModules.PARAMS_SYNC_ALL_MODULES, isSyncAllModules);
        mServiceIntent.putExtra(SyncModules.PARAMS_SERVER, mServer);
        mServiceIntent.putExtra(SyncModules.PARAMS_INITIAL_SYNC, isInitialSync);

        if(mModules != null) {
            mServiceIntent.putExtra(SyncModules.PARAMS_TABLES_TO_SYNC, mModules);
        }

        if (!SyncServiceHelper.isSyncServiceRunning(cls, context) || mSyncModules == null) {
            mServiceBounded = false;
            context.startService(mServiceIntent);
        }
    }

    @Override
    public void onStopSyncService() {

    }

    @Override
    public void onBindSyncService() {

    }

    @Override
    public void onUnbindSyncService() {

    }
}

