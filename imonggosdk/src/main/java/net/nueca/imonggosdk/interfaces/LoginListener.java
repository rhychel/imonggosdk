package net.nueca.imonggosdk.interfaces;

import net.nueca.imonggosdk.objects.Session;

/**
 * Created by Jn on 6/9/2015.
 * imonggosdk (c)2015
 */
public interface LoginListener {
    void onStartLogin();
    void onLoginSuccess(Session session);
    void onPositiveButtonPressed();
    void onNegativeButtonPressed();
    void onStopLogin();
}
