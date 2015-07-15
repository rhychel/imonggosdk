package net.nueca.imonggosdk.interfaces;

import net.nueca.imonggosdk.objects.Session;

/**
 * Created by Jn on 6/9/2015.
 * imonggosdk (c)2015
 */
public interface LoginListener {
    public void onStartLogin();
    public void onLoginSuccess(Session session);
    public void onStopLogin();
}
