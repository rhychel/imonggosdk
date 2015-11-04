package nueca.net.salesdashboard.interfaces;

import net.nueca.imonggosdk.enums.RequestType;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public interface SyncDailySalesListener {

    void onStartDownload(RequestType requestType);

    // Today and yesterday will not use this
    // week and month will use this
    void onDownloadProgress(RequestType requestType, int page, int max);

    void onEndDownload(RequestType requestType);

    void onFinishDownload();

    void onErrorDownload(String message);
}
