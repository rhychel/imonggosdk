package net.nueca.concessio;

import android.os.Bundle;
import android.util.Log;

import net.nueca.concessioengine.activities.LoginActivity;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

public class C_Login2 extends LoginActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if(AccountTools.isLoggedIn(getHelper()) && !AccountTools.isUnlinked(this))
                Log.e("Account", "I'm logged in!");
            else
                Log.e("Account", "I'm not logged in!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void initActivity() {

    }

    @Override
    protected void beforeFetchingData() {

    }

    @Override
    protected void beforeLogin() {

    }

    @Override
    protected void stopLogin() {

    }

    @Override
    protected void afterLogin() {

    }

    @Override
    public void onLogoutAccount() {

    }

    @Override
    public void onUnlinkAccount() {

    }
}
