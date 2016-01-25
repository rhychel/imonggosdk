package net.nueca.imonggosdk.interfaces;

import net.nueca.imonggosdk.enums.Table;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public interface SyncModulesListener {
    void onStartDownload(Table table);
    void onDownloadProgress(Table table, int page, int max);
    void onEndDownload(Table table);
    void onFinishDownload();
    void onErrorDownload(Table table, String message);
    void onPrepareDialog();
    void onDismissDialog();
}
