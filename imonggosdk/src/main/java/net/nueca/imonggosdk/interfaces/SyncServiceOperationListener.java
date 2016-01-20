package net.nueca.imonggosdk.interfaces;

/**
 * Created by Jn on 20/01/16.
 */
public interface SyncServiceOperationListener {
    void onStartSyncService();
    void onStopSyncService();
    void onBindSyncService();
    void onUnbindSyncService();
}
