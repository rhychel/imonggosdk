package net.nueca.concessioengine.activities.login;

import android.os.Bundle;
import android.util.Log;

import net.nueca.imonggosdk.dialogs.DialogTools;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

/**
 * Created by Jn on 7/12/2015.
 * imonggosdk(2015)
 */
public class LoginActivity extends BaseLoginActivity {

    public static String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (AccountTools.isLoggedIn(getHelper()) && !AccountTools.isUnlinked(this)) {
                Log.e(TAG, "I'm logged in!");
                Log.e(TAG, "POS Device ID: " + getSession().getDevice_id() + "");
                Log.e(TAG, "Server: " + getSession().getServer() + "");
            } else
                Log.e(TAG, "I'm not logged in!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        DialogTools.hideIndeterminateProgressDialog();
    }

    @Override
    protected void initLoginEquipments() {
        // set the mServer choice here
        setServer(Server.IRETAILCLOUD_NET);
        Log.e(TAG, "Server is " + getServer().toString());

        // set the Modules to download
        int[] modules = {Table.USERS.ordinal(), Table.PRODUCTS.ordinal(), Table.UNITS.ordinal()};
        setModules(modules);
    }

    @Override
    protected void onCreateLayoutForLogin() {

    }

    @Override
    protected void updateAppData() {

        startSyncingImonggoModules();
    }

    @Override
    protected void showNextActivity() {

    }

    @Override
    protected void beforeLogin() {

    }

    @Override
    protected void stopLogin() {


    }

    @Override
    protected void successLogin() {

    }

    @Override
    protected void syncingModulesSuccessful() {
        Log.e(TAG, "Syncing Modules Successful");
    }


    @Override
    public void onLogoutAccount() {

    }

    @Override
    public void onUnlinkAccount() {

    }

    @Override
    public void onStartDownload(Table table) {

    }
}
