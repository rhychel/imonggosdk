package net.nueca.concessioengine.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.dialogs.DialogTools;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.operations.sync.SyncModules;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.LoggingTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jn on 7/12/2015.
 * imonggosdk (c)2015
 */
public class LoginActivity extends BaseLoginActivity {

    public static String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void initLoginEquipments() {
        try {
            Intent intent = new Intent(LoginActivity.this, SyncModules.class);
            intent.putExtra(SyncModules.PARAMS_SYNC_ALL_MODULES, true);
            intent.putExtra(SyncModules.PARAMS_SERVER, Server.IMONGGO.ordinal());
            intent.putExtra(SyncModules.PARAMS_INITIAL_SYNC, true);
            setSyncServiceIntent(intent);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setTag("BaseLoginActivity");
            setSyncServiceBinded(isSyncServiceRunning(SyncModules.class));
            setModules(null);
            setUnlinked(AccountTools.isUnlinked(this));
            setLoggedIn(AccountTools.isLoggedIn(getHelper()));
            setServer(Server.IMONGGO);
            Log.e(TAG, "Server is " + getServer().toString());
            List<String> m = new ArrayList<>();
            setModulesToSync(m);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void loginChecker() {
        try {
            // Account is unlinked and user is logout
            if (AccountTools.isUnlinked(this) && !AccountTools.isLoggedIn(getHelper())) {
                setUnlinked(true);
                setLoggedIn(false);
            }
            // Account is Linked
            if (!AccountTools.isUnlinked(this)) {
                // if user is logout
                if (!AccountTools.isLoggedIn(getHelper())) {
                    setUnlinked(false);
                    setLoggedIn(false);
                }
                // if User is Logged In
                if (AccountTools.isLoggedIn(getHelper())) {

                    if(!isSyncFinished()) { //
                        Log.e(TAG, "Sync is not finished, unlinking account");
                        getSyncServiceIntent().putExtra(SyncModules.PARAMS_INITIAL_SYNC, true);
                        setLoggedIn(false);
                        setUnlinked(true);
                        unlinkAccount();
                    } else {
                        Log.e(TAG, "Sync is finished, Owryt!");
                        setUnlinked(false);
                        getSyncServiceIntent().putExtra(SyncModules.PARAMS_INITIAL_SYNC, false);
                        // user is logged in set up data
                        setLoginSession(getSession());

                        if (!getLoginSession().getApiAuthentication().equals("")) { // User is authenticated
                            setLoggedIn(true);


                            // check if sessions email exist in user's database
                            if (getHelper().getUsers().queryBuilder().where().eq("email", getLoginSession().getEmail()).query().size() == 0) {
                                Log.e(TAG, "sessions email don't match don't match user's email");
                                LoggingTools.showToast(this, getString(R.string.LOGIN_USER_DONT_EXIST));
                                setLoggedIn(false);
                                setUnlinked(true);
                                unlinkAccount();
                            } else {
                                Log.e(TAG, "Setting Initial sync to false");
                                getSyncServiceIntent().putExtra(SyncModules.PARAMS_INITIAL_SYNC, false);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreateLayoutForLogin() {
        setContentView(R.layout.concessioengine_login);

        setupLayoutEquipments((EditText) findViewById(R.id.etAccountId),
                (EditText) findViewById(R.id.etEmail),
                (EditText) findViewById(R.id.etPassword),
                (Button) findViewById(R.id.btnSignIn));

        setEditTextAccountID("retailpos");
        setEditTextEmail("retailpos@test.com");
        setEditTextPassword("retailpos");
    }

    @Override
    protected void updateAppData() {

    }

    @Override
    protected void showNextActivity() {
        if (getCustomDialog() != null) {
            getCustomDialog().dismiss();
        }
    }

    @Override
    public void onLogoutAccount() {

    }

    @Override
    public void onUnlinkAccount() {

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
    protected void onPause() {
        super.onPause();
        DialogTools.hideIndeterminateProgressDialog();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLogin();

        if (isUnlinked() && !isLoggedIn()) {
            if (getBaseLogin() != null) {
                getBaseLogin().onStop();
            }
        }

        DialogTools.hideIndeterminateProgressDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isUnlinked() && !isLoggedIn()) {
            if (getBaseLogin() != null) {
                getBaseLogin().onStop();
            }
        }

        DialogTools.hideIndeterminateProgressDialog();

        if (getCustomDialog() != null) {
            getCustomDialog().dismiss();
        }

        doUnbindService();
    }
}
