package net.nueca.concessioengine.activities.login;

import android.os.Bundle;
import android.util.Log;

import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

/**
 * Created by Jn on 7/12/2015.
 * imonggosdk(2015)
 */
public class LoginActivity extends BaseLoginActivity {

    @Override
    protected void initActivity() {
        // set the server choice here
        setServer(Server.IRETAILCLOUD_NET);
    }

    @Override
    protected void updateAppData() {

    }

    @Override
    protected void updateModules() {
        int[] modules = {Table.BRANCHES.ordinal(), Table.PRODUCTS.ordinal(), Table.CUSTOMERS.ordinal() };
        setModules(modules);
    }

    @Override
    protected void onCreateSelectBranchLayout() {

    }

    @Override
    protected void beforeLogin() {

    }

    @Override
    protected void stopLogin() {

    }

    @Override
    protected void loginSuccess() {

    }

    @Override
    protected void onCreateLoginLayout() {

    }

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
    public void onLogoutAccount() {

    }

    @Override
    public void onUnlinkAccount() {

    }
}
