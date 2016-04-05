package net.nueca.concessioengine.activities.login;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.dialogs.DialogTools;
import net.nueca.imonggosdk.enums.DialogType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.LoginListener;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.operations.sync.SyncModules;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.NetworkTools;

import java.sql.SQLException;

/**
 * Created by Jn on 7/12/2015.
 * imonggosdk (c)2015
 */
public class LoginActivity extends BaseLoginActivity implements LoginListener {

    public static String TAG = "LoginActivity";
    private boolean isUsingDefaultLoginLayout = true;

    public static boolean IS_AUTOUPDATE = false;

    /**
     * Shows login layout if the user is logged out
     * <p/>
     * if you want to customize login layout
     * you should extend this class and
     * override this method and call this
     * functions inside:
     * <p/>
     * 1. setContentView( your custom layout)
     * 2. setLayoutEquipments( fill in the ids in your layout)
     */
    @Override
    protected void onCreateLoginLayout() {
        startSyncService();

        if (isUsingDefaultLoginLayout()) {
            setContentView(R.layout.concessioengine_login);
            setupLayoutEquipments((EditText) findViewById(R.id.etAccountId),
                    (EditText) findViewById(R.id.etEmail),
                    (EditText) findViewById(R.id.etPassword),
                    (Button) findViewById(R.id.btnLogin));

            setIsUsingDefaultLoginLayout(true);
        }

        try {
            getHelper().deleteAllDatabaseValues();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if AutoUpdate is on.
     * If True Update the data,
     * else skip to method showNextActivityAfterLogin()
     */
    @Override
    protected void autoUpdateChecker() {
        // Account is Linked User is logged in
        if (!isUnlinked() && isLoggedIn()) {
            // isAutoUpdate()
            if (LoginActivity.IS_AUTOUPDATE && NetworkTools.isInternetAvailable(LoginActivity.this)) {
                startSyncService();
            } else {
                showNextActivityAfterLogin();
            }
        }
    }

    @Override
    protected void dialogPositiveButtonAction() {

    }

    @Override
    protected void dialogNegativeButtonAction() {

    }

    @Override
    protected void showProgressDialog(DialogType type, String message, String positiveText, String negativeText) {

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
            setSyncServiceIntent(intent);
            setModulesToSync(null);
            setSyncAllModules(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setRequireConcessioSettings(false);
            setUnlinked(AccountTools.isUnlinked(this));
            setLoggedIn(AccountTools.isLoggedIn(getHelper()));
            setIsUsingDefaultLoginLayout(true);
            if(getSession() == null) {
                setUnlinked(true);
            }
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
                    if (!isSyncFinished()) { // Sync is not finished, unlinking account
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
                            if (getHelper().fetchObjects(User.class).queryBuilder().where().eq("email", getLoginSession().getEmail()).query().size() == 0) {
                                //LoggingTools.showToast(this, getString(R.string.LOGIN_USER_DONT_EXIST));
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
    protected void updateAppData(SyncModules syncmodules) {

    }

    @Override
    protected void showNextActivityAfterLogin() {

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
    protected void showCustomDownloadDialog() {
        if (isUsingDefaultCustomDialogForSync()) {
            setIsUsingDefaultLoginLayout(true);
            //createNewCustomDialogFrameLayout(LoginActivity.this, getModulesToSync());
            createNewCustomDialogFrameLayout(getTableToSync(), LoginActivity.this);
            createNewCustomDialog(LoginActivity.this, R.style.LoginTheme_DialogFrameLayout);
//            isAutoUpdate()
            if(IS_AUTOUPDATE && isLoggedIn() && !isUnlinked())
                setCustomDialogTitle(getString(R.string.FETCHING_MODULE_TITLE));
            else
                setCustomDialogTitle(getString(R.string.UPDATING_MODULE_TITLE));
            setCustomDialogContentView(getCustomDialogFrameLayout());
            setCustomDialogCancelable(false);
            showCustomDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DialogTools.hideIndeterminateProgressDialog();
    }

    @Override
    public void onStartLogin() {

    }

    @Override
    public void onLoginSuccess(Session session) {

    }

    @Override
    public void onPositiveButtonPressed() {

    }

    @Override
    public void onNegativeButtonPressed() {

    }

    @Override
    public void onRetryButtonPressed(Table table) {

    }

    @Override
    public void onStopLogin() {

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

    public boolean isUsingDefaultLoginLayout() {
        return isUsingDefaultLoginLayout;
    }

    public void setIsUsingDefaultLoginLayout(boolean isUsingDefaultLoginLayout) {
        this.isUsingDefaultLoginLayout = isUsingDefaultLoginLayout;
    }
}
