package net.nueca.imonggosdk.interfaces;

import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;

/**
 * Created by rhymart on 7/14/14.
 * NuecaLibrary (c)2014
 */
public interface VolleyRequestListener {
    void onStart(Table table, RequestType requestType);
    void onSuccess(Table table, RequestType requestType, Object response);
    void onError(Table table, boolean hasInternet, Object response, int responseCode);
    void onRequestError();
}
