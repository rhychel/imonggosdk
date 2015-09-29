package net.nueca.concessio;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;

import net.nueca.concessioengine.activities.login.BaseLoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

public class C_Login extends BaseLoginActivity {

    @Override
    protected void initActivity() {
        // set the server choice here
        // This is a test edit for the Google Cloud Repository
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