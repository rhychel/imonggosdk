package net.nueca.imonggosdk.interfaces;

import net.nueca.imonggosdk.enums.Table;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public interface SyncModulesListener {
    void onStartDownload(Table table);
    void onDownloadProgress(int page);
    void onEndDownload(Table table);
    void onFinishDownload();
    void onErrorDownload(String message);
}
