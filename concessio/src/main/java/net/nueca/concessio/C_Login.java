package net.nueca.concessio;

import android.os.Bundle;
import android.util.Log;

import net.nueca.concessioengine.activities.login.BaseLoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;

public class C_Login extends BaseLoginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (AccountTools.isLoggedIn(getHelper()) && !AccountTools.isUnlinked(this)) {
                Log.e("Account", "I'm logged in!");

            } else
                Log.e("Account", "I'm not logged in!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initActivity() {
        // set the server choice here
        setServer(Server.IRETAILCLOUD_NET);
    }

    @Override
    protected void updateAppData() {
        // update the app code here
    }

    @Override
    protected void updateModules() {
        // set the list of modules here
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

        /*//int[] modules = {}

        List<Table> list = new ArrayList<>();

        list.add(Table.BRANCH_USERS);
        list.add(Table.USERS);

        DialogTools.showCustomDialog(this, list, "Updating", false);*/
    }

    /**
     * Using Custom Layout
     *
     * 1. call setUsingCustomLayout(...);
     * 2. call setContentView(...);
     * 3. call the function setupLayoutEquipments(...); and it will automatically set the logic
     */
    @Override
    protected void onCreateLoginLayout() {

    }

    @Override
    public void onLogoutAccount() {

    }

    @Override
    public void onUnlinkAccount() {

    }
}
