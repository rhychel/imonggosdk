package net.nueca.imonggosdk.interfaces;

import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Table;

/**
 * Created by rhymart on 7/14/14.
 * NuecaLibrary (c)2014
 */
public interface VolleyRequestListener {
    public void onStart(Table table, RequestType requestType);
    public void onSuccess(Table table, RequestType requestType, Object response);
    public void onError(Table table, boolean hasInternet, Object response, int responseCode);
    public void onRequestError();
}
