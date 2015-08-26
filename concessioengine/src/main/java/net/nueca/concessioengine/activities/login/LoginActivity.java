package net.nueca.concessioengine.activities.login;

import android.content.Intent;
import android.os.AsyncTask;
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
import net.nueca.imonggosdk.tools.NetworkTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jn on 7/12/2015.
 * imonggosdk (c)2015
 */
public class LoginActivity extends BaseLoginActivity {

    public static String TAG = "LoginActivity";

    /**
     * Calling required methods for login.
     * you can Override this method as long
     * as you call super.onCreate
     * @param savedInstanceState bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLoginEquipments();
        loginChecker();
        onCreateLoginLayout();
        autoUpdateChecker();
    }

    /**
     * Shows login layout if the user is logged out
     */
    protected void onCreateLoginLayout() {
        if (!isLoggedIn()) {
            startSyncService();
            onCreateLayoutForLogin();
            getHelper().deleteAllDatabaseValues();
        }
    }

    /**
     * if you want to customize login layout
     * you should override this method.
     * and call these function inside.
     *
     * 1. setContentView(...)
     * 2. setLayoutEquipments(...)
     *
     * Note: don't call super.onCreateLoginLayout
     */
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

    /**
     * Checks if AutoUpdate is on.
     * If True Update the data,
     * else skip to welcome screen
     */
    private void autoUpdateChecker() {
        // Account is Linked User is logged in
        if (!isUnlinked() && isLoggedIn()) {
            if (isAutoUpdate() && NetworkTools.isInternetAvailable(LoginActivity.this)) {
                updateAppData();
                new StartSyncServiceAsyncTask().execute();
            } else {
                showNextActivity();
            }
        }
    }

    /**
     * Initialize default settings of the app
     */
    @Override
    protected void initLoginEquipments() {
        try {
            Intent intent = new Intent(LoginActivity.this, SyncModules.class);
            intent.putExtra(SyncModules.PARAMS_SYNC_ALL_MODULES, true);
            intent.putExtra(SyncModules.PARAMS_SERVER, Server.IMONGGO.ordinal());
            intent.putExtra(SyncModules.PARAMS_INITIAL_SYNC, true);
            intent.putExtra(SyncModules.PARAMS_SYNC_ALL_MODULES, true);
            setSyncServiceIntent(intent);
            setSyncAllModules(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setTag("BaseLoginActivity");
            setSyncServiceBinded(isSyncServiceRunning(SyncModules.class));
            setModules(null);
            setRequireConcessioSettings(false);
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

    /**
     * This methods checks if a user is currently logged in
     * then sets the data needed before syncing starts.
     */
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
                    if(!isSyncFinished()) { // Sync is not finished, unlinking account
                        getSyncServiceIntent().putExtra(SyncModules.PARAMS_INITIAL_SYNC, true);
                        setLoggedIn(false);
                        setUnlinked(true);
                        unlinkAccount();
                    } else {
                        setUnlinked(false);
                        getSyncServiceIntent().putExtra(SyncModules.PARAMS_INITIAL_SYNC, false);
                        setLoginSession(getSession()); // user is logged in set up data

                        if (!getLoginSession().getApiAuthentication().equals("")) { // User is authenticated
                            setLoggedIn(true);
                            // check if sessions email exist in user's database
                            if (getHelper().getUsers().queryBuilder().where().eq("email", getLoginSession().getEmail()).query().size() == 0) {
                                LoggingTools.showToast(this, getString(R.string.LOGIN_USER_DONT_EXIST));
                                setLoggedIn(false);
                                setUnlinked(true);
                                unlinkAccount();
                            } else {
                                getSyncServiceIntent().putExtra(SyncModules.PARAMS_INITIAL_SYNC, false); // Sets Initial sync to false
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
    protected void updateAppData() {

    }

    @Override
    protected void showNextActivity() {

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

    private class StartSyncServiceAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            startSyncService();
            setSyncModules(null);

            while (!isSyncServiceBinded() || getSyncModules() == null) {
                Log.e(TAG, "Service is not yet binded");
            }

            return "Service is now binded";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "This came from doInBackground: " + result);
            try {
                startSyncingImonggoModules();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
