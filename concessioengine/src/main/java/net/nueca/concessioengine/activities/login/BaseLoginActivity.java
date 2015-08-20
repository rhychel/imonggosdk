package net.nueca.concessioengine.activities.login;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.dialogs.CustomDialog;
import net.nueca.concessioengine.dialogs.CustomDialogFrameLayout;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.dialogs.DialogTools;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.exception.LoginException;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.interfaces.LoginListener;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.operations.login.BaseLogin;
import net.nueca.imonggosdk.operations.sync.BaseSyncService;
import net.nueca.imonggosdk.operations.sync.SyncModules;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.LoginTools;
import net.nueca.imonggosdk.tools.NetworkTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.util.List;

/**
 * Abstract Class BaseLoginActivity
 * created by Jn on 06/16/15
 * imonggosdk (c)2015
 */
public abstract class BaseLoginActivity extends ImonggoAppCompatActivity implements AccountListener, SyncModulesListener {

    private BaseLogin mBaseLogin;
    private Boolean isUnlinked;
    private Boolean isLoggedIn;
    private Boolean requireConcessioSettings = false; // added by rhy
    private Session mSession = null;
    private Server mServer;
    private int[] mModules;
    private EditText etAccountID;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSignIn;
    private CustomDialog customDialog;
    private CustomDialogFrameLayout customDialogFrameLayout;
    private Intent mServiceIntent;
    private List<String> mModulesToDownload;
    private String TAG;
    private SyncModules mSyncModules;
    private Boolean mBounded;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "service is connected");

            BaseSyncService.LocalBinder mLocalBinder = (BaseSyncService.LocalBinder) service;

            mSyncModules = (SyncModules) mLocalBinder.getService();

            if (mSyncModules != null) {
                mBounded = true;
                Log.e(TAG, "Successfully bind Service and Activity");
                mSyncModules.setSyncModulesListener(BaseLoginActivity.this);
            } else {
                Log.e(TAG, "Cannot bind Service and Activity");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mSyncModules = null;
        }
    };

    /**
     * If you want to initialize your own logic. method before login checker.
     * you should implement this methods: setServer(...) and setModules(...)
     * if not then the default server and modules is set.
     */
    protected abstract void initLoginEquipments();

    /**
     * Checks if someone is logged in
     */
    protected abstract void loginChecker();

    /**
     * if you want to customize login you use call the method
     * setIsUsingCustomLayout(...)
     * setContentView(...)
     * setLayoutEquipments(..);
     */
    protected abstract void onCreateLayoutForLogin();

    /**
     * If you want to add some logic before fetching data
     */
    protected abstract void updateAppData();

    /**
     * This is where the Login Life Cycle will Stop.
     * You should @Override this method and the activity you want
     * to show next
     */
    protected abstract void showNextActivity();

    protected abstract void beforeLogin();

    protected abstract void stopLogin();

    protected abstract void successLogin();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLoginEquipments();
        loginChecker();
        onCreateLoginLayout();
        autoUpdateChecker();
    }

    protected void onCreateLoginLayout() {
        Log.e(TAG, "onCreateLoginLayout called");

        // if user is logout show login layout
        if (!isLoggedIn()) {
            startSyncService();
            onCreateLayoutForLogin();
            getHelper().deleteAllDatabaseValues();
        }
    }

    /**
     * Checks if AutoUpdate is on. If True Update the data, else skip to welcome screen
     */
    private void autoUpdateChecker() {
        // Account is Linked User is logged in
        if (!isUnlinked() && isLoggedIn()) {
            if (isAutoUpdate() && NetworkTools.isInternetAvailable(BaseLoginActivity.this)) {
                updateAppData();
                new StartSyncServiceAsyncTask().execute();
            } else {
                showNextActivity();
            }
        }
    }

    private class StartSyncServiceAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            startSyncService();
            mSyncModules = null;

            while (!isSyncServiceBinded() || mSyncModules == null) {
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

    public void startSyncingImonggoModules() throws SQLException {
        if (isSyncServiceBinded()) {
            setUpModuleNamesForCustomDialog();
            showSyncModulesCustomDialog();

            Log.e(TAG, "Starting Module Download");
            if (mSyncModules != null) {
                mSyncModules.startFetchingModules();
            } else {
                Log.e(TAG, "Service Modules is null cannot start sync");

                if (customDialog != null) {
                    customDialog.dismiss();
                }
                startSyncService();
                DialogTools.showBasicWithTitle(BaseLoginActivity.this, "Sync Failed",
                        "Sync failed. Login Again ",
                        "Ok", "", false,
                        new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                bindSyncService();
                                dialog.dismiss();
                            }
                        });
            }
        } else {
            Log.e(TAG, "Service is not binded cannot start sync");
            DialogTools.showBasicWithTitle(BaseLoginActivity.this, getString(R.string.LOGIN_FAILED_TITLE),
                    "Cannot start sync service.",
                    "START SERVICE", "", false,
                    new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            startSyncService();
                        }
                    });
        }
    }

    private void setUpModuleNamesForCustomDialog() {
        if (getModules() != null) { // manually set modules to download see /**/
            mModulesToDownload.clear();
            for (int module : getModules()) {

                for (Table table : Table.values()) {
                    if (module == table.ordinal()) {
                        switch (table) {
                            case USERS:
                                mModulesToDownload.add("Users");
                                break;
                            case PRODUCTS:
                                mModulesToDownload.add("Products");
                                break;
                            case UNITS:
                                mModulesToDownload.add("Units");
                                break;
                            case BRANCH_USERS:
                                mModulesToDownload.add("Branches");
                                break;
                            case CUSTOMERS:
                                mModulesToDownload.add("Customers");
                                break;
                            case INVENTORIES:
                                mModulesToDownload.add("Inventories");
                                break;
                            case TAX_SETTINGS:
                                mModulesToDownload.add("Tax Settings");
                                break;
                            case DOCUMENTS:
                                mModulesToDownload.add("Documents");
                                break;
                            case DOCUMENT_TYPES:
                                mModulesToDownload.add("Doc Type");
                                break;
                            case DOCUMENT_PURPOSES:
                                mModulesToDownload.add("Doc Purposes");
                                break;
                            default:
                                Log.e(TAG, "You have added unsupported module");
                                break;
                        }
                    }
                }
            }
        } else { // if you don't set custom modules to download. Sync All
            mModules = new int[]{
                    Table.USERS.ordinal(),
                    Table.BRANCH_USERS.ordinal(),
                    Table.TAX_SETTINGS.ordinal(),
                    Table.PRODUCTS.ordinal(),
                    Table.INVENTORIES.ordinal(),
                    Table.CUSTOMERS.ordinal(),
                    Table.UNITS.ordinal(),
                    Table.DOCUMENTS.ordinal(),
                    Table.DOCUMENT_TYPES.ordinal()
            };

            mModulesToDownload.add("Users");
            mModulesToDownload.add("Branches");
            mModulesToDownload.add("Tax Settings");
            mModulesToDownload.add("Products");
            mModulesToDownload.add("Inventories");
            mModulesToDownload.add("Customers");
            mModulesToDownload.add("Units");
            mModulesToDownload.add("Documents");
            mModulesToDownload.add("Document Types");
        }
    }

    private void showSyncModulesCustomDialog() {

        customDialogFrameLayout = new CustomDialogFrameLayout(BaseLoginActivity.this, mModulesToDownload);

        customDialog = new CustomDialog(BaseLoginActivity.this, R.style.AppCompatDialogStyle);

        customDialog.setTitle(getString(R.string.FETCHING_MODULE_TITLE));
        customDialog.setContentView(customDialogFrameLayout);
        customDialog.setCancelable(false);
        customDialog.show();
    }

    public Boolean haveDefaultBranch() {
        // if default branch is not null
        return !getDefaultBranch(BaseLoginActivity.this).equals("");
    }


    /**
     * Sets up EditText, Buttons and Listeners
     *
     * @param editTextAccountId Account ID EditText
     * @param editTextEmail     Email EditText
     * @param editTextPassword  Password EditText
     * @param btnSignIn         Sign In Button
     */
    protected void setupLayoutEquipments(EditText editTextAccountId, EditText
            editTextEmail, EditText editTextPassword, Button btnSignIn) {

        this.etAccountID = editTextAccountId;
        this.etEmail = editTextEmail;
        this.etPassword = editTextPassword;

        this.btnSignIn = btnSignIn;

        // Button SignIn Listener
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLogin();
            }
        });
    }

    /**
     * Validate BaseLogin form and authenticate.
     */
    private void initLogin() {

        if (!NetworkTools.isInternetAvailable(this)) {

            DialogTools.showBasicWithTitle(BaseLoginActivity.this, getString(R.string.LOGIN_FAILED_TITLE),
                    "No network connection",
                    getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
                    new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            dialog.dismiss();
                        }
                    });
        } else {
            Boolean cancelLogin = false;
            // Set error to null
            etAccountID.setError(null);
            etEmail.setError(null);
            etPassword.setError(null);
            // Get the String
            String accountId = etAccountID.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            // the focus
            View focusView = null;

            // ACCOUNT
            if (TextUtils.isEmpty(accountId)) {
                etAccountID.setError(getString(R.string.LOGIN_FIELD_REQUIRED));
                focusView = etAccountID;
                cancelLogin = true;
            } else if (TextUtils.isEmpty(email)) { // EMAIL
                etEmail.setError(getString(R.string.LOGIN_FIELD_REQUIRED));
                focusView = etEmail;
                cancelLogin = true;
            } else if (!LoginTools.isValidEmail(email)) {
                etEmail.setError(getString(R.string.LOGIN_INVALID_EMAIL));
                focusView = etEmail;
                cancelLogin = true;
            }

            // PASSWORD
            if (!TextUtils.isEmpty(password) && !LoginTools.isValidPassword(password)) {
                etPassword.setError(getString(R.string.LOGIN_INVALID_PASSWORD));
                focusView = etPassword;
                cancelLogin = true;
            }
            if (cancelLogin) {
                // error in login
                focusView.requestFocus();
            } else {
                //show progress dialog
                DialogTools.showIndeterminateProgressDialog(BaseLoginActivity.this,
                        null,
                        getString(R.string.LOGIN_PROGRESS_DIALOG_CONTENT), false);

                // BaseLogin Function
                Log.i(TAG, "Loggin in...");
                startLogin(getApplicationContext(), accountId, email, password, getServer());
            }
        }
    }

    private void setLoginListeners() {
        mBaseLogin.setLoginListener(new LoginListener() {
            @Override
            public void onStartLogin() {
                Log.e(TAG, "onStartLogin called");
                beforeLogin();
            }

            @Override
            public void onLoginSuccess(Session session) {
                Log.e(TAG, "Successfully logged in");
                successLogin();
                mSession = session;
                setLoggedIn(true);
                setUnlinked(false);

                DialogTools.hideIndeterminateProgressDialog();
                try {
                    startSyncingImonggoModules();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStopLogin() {
                Log.e(TAG, "onStopLogin called");
                stopLogin();
                // hide progress dialog
                DialogTools.hideIndeterminateProgressDialog();

                // delete session data
                deleteUserSessionData();

                setLoggedIn(false);
                setUnlinked(true);
            }
        });
    }

    private void deleteUserSessionData() {
        if (getHelper() != null) {
            getHelper().deleteAllDatabaseValues();
        }
    }

    private void showLoginForm(Boolean hide) {
        try {
            if (AccountTools.isLoggedIn(getHelper())) {

                if (!hide) {
                    btnSignIn.setVisibility(View.GONE);
                    etAccountID.setVisibility(View.GONE);
                    etPassword.setVisibility(View.GONE);
                    etEmail.setVisibility(View.GONE);
                } else {
                    btnSignIn.setVisibility(View.VISIBLE);
                    etAccountID.setVisibility(View.VISIBLE);
                    etPassword.setVisibility(View.VISIBLE);
                    etEmail.setVisibility(View.VISIBLE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    /**
     * Sets the BaseLogin Credentials of the user and starts login
     *
     * @param context   a context
     * @param accountId must not be empty
     * @param email     must not be empty and matches the correct form 'email@me.com'
     * @param password  must not be empty and length must be greater or equal to five (5)
     * @param server    mServer
     */
    protected void startLogin(Context context, String accountId, String email, String
            password, Server server) {
        try {
            setLoginCredentials(context, accountId, email, password);
            setLoginListeners();
            if (isUnlinked()) {
                LogInAccount(server);
            } else {
                LogInUser(server);
            }
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logout the current user
     */
    protected void startLogout() {
        setUnlinked(false);
        setLoggedIn(false);
        LogOutUser();
    }

    /**
     * Unlinks account within the device
     */
    protected void unlinkAccount() {
        try {
            if (!isUnlinked()) {
                AccountTools.unlinkAccount(this, getHelper(), this);
                setUnlinked(true);
                setLoggedIn(false);
                setDefaultBranch(BaseLoginActivity.this, "");
                startSyncService();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void setLoginCredentials(Context context, String accountId, String email, String
            password) throws LoginException {
        if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            throw new LoginException(context.getString(R.string.LOGIN_FIELD_REQUIRED));
        } else if (!LoginTools.isValidEmail(email)) {
            throw new LoginException(context.getString(R.string.LOGIN_INVALID_EMAIL));
        } else if (!LoginTools.isValidPassword(password)) {
            throw new LoginException(context.getString(R.string.LOGIN_INVALID_PASSWORD));
        } else {
            mBaseLogin = new BaseLogin(BaseLoginActivity.this, getHelper(), accountId, email, password);
            mBaseLogin.setConcessioSettings(requireConcessioSettings);
        }
    }

    protected void LogInAccount(Server server) throws LoginException {
        if (mBaseLogin != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mBaseLogin.startLoginAccount(server);
        }
    }

    protected void LogInUser(Server server) throws LoginException {
        if (mBaseLogin != null) {
            mBaseLogin.startLoginUser(server);
        }
    }

    protected void LogOutUser() {
        try {
            AccountTools.logoutUser(this, getHelper(), this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void setLoginSession(Session session) {
        this.mSession = session;
    }

    protected Session getLoginSession() {
        return this.mSession;
    }

    protected List<String> getModulesToSync() {
        return this.mModulesToDownload;
    }

    protected void setModulesToSync(List<String> modules) {
        this.mModulesToDownload = modules;
    }

    protected String getTag() {
        return this.TAG;
    }

    protected void setTag(String tag) {
        this.TAG = tag;
    }

    protected BaseLogin getBaseLogin() {
        return mBaseLogin;
    }

    protected CustomDialog getCustomDialog() {
        return this.customDialog;
    }

    public void setRequireConcessioSettings(Boolean requireConcessioSettings) {
        this.requireConcessioSettings = requireConcessioSettings;
    }

    protected String getEditTextAccountID() {
        if (this.etAccountID != null) {
            return this.etAccountID.getText().toString();
        }
        return "";
    }

    protected void setEditTextAccountID(String id) {
        if (this.etAccountID != null) {
            this.etAccountID.setText(id);
        }
    }

    protected String getEditTextEmail() {
        if (this.etEmail != null) {
            return this.etEmail.getText().toString();
        }
        return "";
    }

    protected void setEditTextEmail(String email) {
        if (this.etEmail != null) {
            this.etEmail.setText(email);
        }
    }

    protected String getEditTextPassword() {
        if (this.etPassword != null) {
            return this.etPassword.getText().toString();
        }
        return "";
    }

    protected void setEditTextPassword(String password) {
        if (this.etPassword != null) {
            this.etPassword.setText(password);
        }
    }

    protected Boolean isLoggedIn() {
        return isLoggedIn;
    }

    protected Boolean isUnlinked() {
        return isUnlinked;
    }

    protected Boolean isAutoUpdate() {
        return SettingTools.isAutoUpdate(this);
    }

    protected String getDefaultBranch(Context context) {
        return SettingTools.defaultBranch(context);
    }

    protected void setDefaultBranch(Context context, String branchName) {
        SettingTools.updateSettings(context, SettingsName.DEFAULT_BRANCH, branchName);
    }

    protected Server getServer() {
        return mServer;
    }

    protected void setServer(Server server) {
        this.mServer = server;
        getSyncServiceIntent().putExtra(SyncModules.PARAMS_SERVER, server.ordinal());
    }

    public Boolean isSyncFinished() {
        return SettingTools.isSyncFinished(this);
    }

    public void setSyncFinished(Context context, Boolean choice) {
        SettingTools.updateSettings(context, SettingsName.SYNC_FINISHED, choice);
    }

    protected int[] getModules() {
        return mModules;
    }

    protected void setModules(int... mModules) {
        setModule(mModules);
    }

    protected void setModule(int[] mModules) {
        this.mModules = mModules;
        mServiceIntent.putExtra(SyncModules.PARAMS_SYNC_ALL_MODULES, false);
        mServiceIntent.putExtra(SyncModules.PARAMS_TABLES_TO_SYNC, mModules);
    }

    protected void setLoggedIn(Boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    protected void setUnlinked(Boolean isUnlinked) {
        this.isUnlinked = isUnlinked;
        AccountTools.updateUnlinked(this, isUnlinked);
    }

    protected ServiceConnection getServiceConnection() {
        return mConnection;
    }

    protected void setSyncServiceBinded(Boolean bind) {
        this.mBounded = bind;
    }

    protected Boolean isSyncServiceBinded() {
        return mBounded;
    }

    protected Intent getSyncServiceIntent() {
        return this.mServiceIntent;
    }

    protected void setSyncServiceIntent(Intent intent) {
        this.mServiceIntent = intent;
    }

    protected void stopService() {
        Log.e(TAG, "Stopping sync service");
        doUnbindService();
        if (isSyncServiceRunning(BaseSyncService.class) | mSyncModules != null) {
            stopService(mServiceIntent);
        }

        if (!isSyncServiceRunning(SyncModules.class)) {
            Log.e(TAG, "Sync Services stopped");
        } else {
            Log.e(TAG, "Stopping Sync Service failed, Service is still running.");
        }
    }

    protected void startSyncService() {
        if (!isSyncServiceRunning(SyncModules.class) || mSyncModules == null) {
            mBounded = false;
            startService(mServiceIntent);
            Log.e(TAG, "There is no service running, starting service..");
            bindSyncService();
        } else {
            Log.e(TAG, "Service is already running");
        }
    }

    protected void bindSyncService() {
        Log.e(TAG, "Binding service.........");

        if (!mBounded) {
            bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
            Log.e(TAG, "Service binded");
        } else {
            Log.e(TAG, "Service is already binded.");
        }

    }

    protected void doUnbindService() {
        if (isSyncServiceBinded()) {
            mBounded = false;
            if(mBounded)
                unbindService(getServiceConnection());
        }
    }

    protected boolean isSyncServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStartDownload(Table table) {
        setSyncFinished(BaseLoginActivity.this, false);
        if (!isSyncFinished()) {
            Log.e(TAG, "Sync setting is false");
        } else {
            Log.e(TAG, "Sync setting is true");
        }
    }

    @Override
    public void onDownloadProgress(Table table, int page, int max) {
        Log.e(TAG, "Downloading " + table + " " + page + " out of " + max);
        String currentTable = "";
        for (Table tableN : Table.values()) {
            if (table == tableN) {
                switch (tableN) {
                    case USERS:
                        currentTable = "Users";
                        break;
                    case BRANCHES:
                        currentTable = "Branches";
                        break;
                    case BRANCH_USERS:
                        currentTable = "Branches";
                        break;
                    case TAX_SETTINGS:
                        currentTable = "Tax Settings";
                        break;
                    case PRODUCTS:
                        currentTable = "Products";
                        break;
                    case INVENTORIES:
                        currentTable = "Inventories";
                        break;
                    case CUSTOMERS:
                        currentTable = "Customers";
                        break;
                    case DOCUMENTS:
                        currentTable = "Documents";
                        break;
                    case DOCUMENT_TYPES:
                        currentTable = "Document Types";
                        break;
                    case DOCUMENT_PURPOSES:
                        currentTable = "Document Types";
                        break;
                    case UNITS:
                        currentTable = "Units";
                    default:
                        break;
                }
            }
        }

        int progress = (int) Math.ceil((((double) page / (double) max) * 100.0));

        customDialogFrameLayout.getCustomModuleAdapter().hideCircularProgressBar(mModulesToDownload.indexOf(currentTable));
        customDialogFrameLayout.getCustomModuleAdapter().updateProgressBar(mModulesToDownload.indexOf(currentTable), progress);
    }

    @Override
    public void onEndDownload(Table table) {
        Log.e(TAG, "finished downloading " + table);
        stopService();
        setSyncFinished(BaseLoginActivity.this, true);

        if (isSyncFinished()) {
            Log.e(TAG, "Sync is finished");
        } else {
            Log.e(TAG, "Sync is not finished");
        }
    }

    @Override
    public void onFinishDownload() {
        if (customDialog != null) {
            Log.e(TAG, "Custom dialog is showing");
            customDialog.dismiss();
            customDialog = null;
        }
        showNextActivity();
    }

    @Override
    public void onErrorDownload(Table table, String message) {
        Log.e(TAG, "error downloading " + table + " " + message);
    }


    @Override
    public void onStop() {

        if (customDialog != null) {
            Log.e(TAG, "Custom dialog is showing");
            customDialog.dismiss();
            customDialog = null;
        }

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

        doUnbindService();
    }
}