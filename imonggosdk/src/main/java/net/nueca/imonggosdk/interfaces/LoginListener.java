package net.nueca.imonggosdk.interfaces;

import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Session;

import java.sql.SQLException;

/**
 * Created by Jn on 6/9/2015.
 * imonggosdk (c)2015
 */
public interface LoginListener {
    void onStartLogin();
    void onLoginSuccess(Session session);
    void onPositiveButtonPressed();
    void onNegativeButtonPressed();
    void onRetryButtonPressed(Table table) throws SQLException;
    void onStopLogin();
}
