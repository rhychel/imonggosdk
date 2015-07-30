package net.nueca.concessioengine.activities.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.LoggingTools;

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
    protected void initLoginEquipments() {
        // set the mServer choice here
        setServer(Server.IRETAILCLOUD_NET);
        Log.e(TAG, "Server is " + getServer().toString());


        // set the Modules to download
        int[] modules = {Table.USERS.ordinal(), Table.PRODUCTS.ordinal()};
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


    private void showSelectBranchLayout() {
        setContentView(R.layout.concessioengine_select_branches);

        // TODO: remove this
        Button test_unlink_button = (Button) findViewById(R.id.btnTestUnlink);
        Button test_fetch_button = (Button) findViewById(R.id.btnTestFunction);

        test_unlink_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggingTools.showToast(getApplicationContext(), "Unlink Account..");

                setContentView(R.layout.concessioengine_login);

                setupLayoutEquipments((EditText) findViewById(R.id.etAccountId),
                        (EditText) findViewById(R.id.etEmail),
                        (EditText) findViewById(R.id.etPassword),
                        (Button) findViewById(R.id.btnSignIn));

                unlinkAccount();
            }
        });

        test_fetch_button.setVisibility(View.GONE);
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
