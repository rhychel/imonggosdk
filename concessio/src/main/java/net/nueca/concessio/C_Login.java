package net.nueca.concessio;

import android.os.Bundle;
import android.util.Log;
import net.nueca.concessioengine.activities.LoginActivity;
import net.nueca.concessioengine.tools.DialogMaterial;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class C_Login extends LoginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (AccountTools.isLoggedIn(getHelper()) && !AccountTools.isUnlinked(this)) {
                Log.e("Account", "I'm logged in!");
                Log.i("session pos id", getSession().getDevice_id() + "");
                Log.i("session server", getSession().getServer() + "");
            } else
                Log.e("Account", "I'm not logged in!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void initActivity() {

    }

    @Override
    protected void updateAppData() {

    }

    @Override
    protected void showSelectBranches() {

    }

    @Override
    protected void showDashBoard() {

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
