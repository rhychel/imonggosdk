package net.nueca.imonggosdk.operations.update;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.exception.SyncException;
import net.nueca.imonggosdk.interfaces.SynServiceAsyncTaskListener;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.interfaces.SyncServiceConnectionListener;
import net.nueca.imonggosdk.interfaces.SyncServiceOperationListener;
import net.nueca.imonggosdk.operations.service.SyncServiceAsyncTask;
import net.nueca.imonggosdk.operations.sync.BaseSyncService;
import net.nueca.imonggosdk.operations.sync.SyncModules;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.SyncServiceHelper;

import java.sql.SQLException;

/**
 * Created by Jn on 20/01/16.
 */
public class APIDownloader extends BaseUpdater implements SyncServiceConnectionListener, SynServiceAsyncTaskListener, SyncServiceOperationListener {

    public static String TAG = "APIDownloader";
    public static Boolean ok = true;

    public APIDownloader() {
        mModules = new int[]{};
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

    public void retrySync() throws SyncException, SQLException {
        if (mSyncModules != null) {
            mSyncModules.retrySync();
        } else {
            throw new SyncException("Sync Module is null");
        }
    }

    public void setModulesToSync(int... modules) throws SyncException {
        if (isInitialSync) {
            this.isSyncAllModules = false;
            AccountTools.setModulesToSync(mContext, mModules);
            if (modules != null) {
                this.mModules = modules;
            } else {
                this.mModules = null;
            }
        } else {
            ok = false;
            throw new SyncException("Can't use this method for updating, use addModulesToUpdate instead.");
        }
    }

    public void addModulesToUpdate(int... modules) throws SyncException {
        if (!isInitialSync) {
            this.isSyncAllModules = false;
            if (modules != null) {
                mModules = modules;
            }
        } else {
            ok = false;
            throw new SyncException("Can't use this method for initial sync, use setModulesToSync instead.");
        }
    }

    public void setSyncModulesListener(SyncModulesListener syncModulesListener) {
        this.mSyncModulesListener = syncModulesListener;
    }

    public void execute(Context context) {
        execute(context, null);
    }

    public void execute(Context tContext, Class<?> tClass) {

        if (tClass != null) {
            cls = tClass;
            Log.e(TAG, "Using the custom class");
        } else {
            Log.e(TAG, "Using the default class");
        }

        mContext = tContext;

        mServiceIntent = new Intent(mContext, cls);
        mSyncServiceOperation = this;

        mConnection = SyncServiceHelper.SyncServiceConnection(this);

        if (mSyncModulesListener == null) {
            ok = false;
        }

        if (ok) {
            new SyncServiceAsyncTask(this).execute();
        } else {
            Log.e(TAG, "can't start download, you haven't set modules to download or anything");

            if (mSyncModulesListener != null) {
                mSyncModulesListener.onErrorDownload(null, "Something wen't wrong please check the logs");
            } else {
                Log.e(TAG, "also SyncModulesListener is null, don't you wanna listen?");
            }
        }
    }


    @Override
    public String doInBackground(String... params) {

        startSyncService();

        while (!mServiceBounded || mSyncModules == null) {
            // Log.e(TAG, "Sync Service is not yet binded");
        }

        return "Sync Service now binded";
    }

    @Override
    public void onPostExecute(String result) {
        // start downloading
        Log.e(TAG, "Starting download");
        try {
            startSync();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        Log.e(TAG, "onServiceConnected");
        BaseSyncService.LocalBinder mLocalBinder = (BaseSyncService.LocalBinder) service;

        mSyncModules = (SyncModules) mLocalBinder.getService();

        if (mSyncModules != null) {
            mServiceBounded = true;

            if (mSyncModulesListener != null) {
                mSyncModules.setSyncModulesListener(mSyncModulesListener);
            }

            Log.e(TAG, "Successfully bind Service and Activity");
        } else {
            mServiceBounded = true;
            Log.e(TAG, "Cannot bind Service and Activity, Service Already Binded");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e(TAG, "onServiceDisconnected");

        mServiceBounded = false;
        mSyncModules = null;
    }

    @Override
    public void onStartSyncService() {
        mServiceIntent.putExtra(SyncModules.PARAMS_SYNC_ALL_MODULES, isSyncAllModules);
        mServiceIntent.putExtra(SyncModules.PARAMS_SERVER, mServer);
        mServiceIntent.putExtra(SyncModules.PARAMS_INITIAL_SYNC, isInitialSync);

        if (mModules != null) {
            mServiceIntent.putExtra(SyncModules.PARAMS_TABLES_TO_SYNC, mModules);
        }
        if (mSyncModules == null || !SyncServiceHelper.isSyncServiceRunning(cls, mContext)) {
            Log.e(TAG, "Starting Service");

            mServiceBounded = false;

            mContext.startService(mServiceIntent);

            if (mSyncModules == null) {
                Log.e(TAG, "Binding services");
                mContext.bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
            } else {
                Log.e(TAG, "Service is already binded");
                mServiceBounded = true;
            }

        } else {
            mServiceBounded = true;
            Log.e(TAG, "Can't Start Service, Service is already running");

        }
    }

    @Override
    public void onStopSyncService() {

    }

    @Override
    public void onUnbindSyncService() {

    }
}

