package net.nueca.concessioengine.activities.login;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import net.nueca.imonggosdk.tools.LoggingTools;
import net.nueca.imonggosdk.tools.LoginTools;
import net.nueca.imonggosdk.tools.NetworkTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Class BaseLoginActivity
 * created by Jn on 06/16/15
 * imonggosdk (c)2015
 */
public abstract class BaseLoginActivity extends ImonggoAppCompatActivity implements AccountListener, SyncModulesListener {

    private Boolean isUnlinked;
    private Boolean isLoggedIn;
    private Boolean isUsingCustomLayout;
    private Session mSession = null;
    private String mDefaultBranch;
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

    protected BaseLogin mBaseLogin;

    private SyncModules mSyncModules;
    private Boolean mBounded;

    /**
     * If you want to initialize your own logic. method before login checker.
     * you should implement this methods: setServer(...) and setModules(...)
     * if not then the default server and modules is set.
     */
    protected abstract void initLoginEquipments();

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

    protected abstract void showNextActivity();


    protected abstract void beforeLogin();

    protected abstract void stopLogin();

    protected abstract void successLogin();

    protected abstract void syncingModulesSuccessful();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate called");
        initEquipments();
        initLoginEquipments();
        loginChecker();
        onCreateLoginLayout();
        autoUpdateChecker();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart called");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume called");
    }

    /**
     * Method which will initialize everything
     */
    private void initEquipments() {
        try {
            TAG = "BaseLoginActivity";
            mBounded = isSyncServiceRunning(SyncModules.class);
            mModules = null;
            isUnlinked = AccountTools.isUnlinked(this);
            isLoggedIn = AccountTools.isLoggedIn(getHelper());
            mDefaultBranch = SettingTools.defaultBranch(this);
            mServiceIntent = new Intent(BaseLoginActivity.this, SyncModules.class);
            mServiceIntent.putExtra(SyncModules.PARAMS_SYNC_ALL_MODULES, true);
            mServiceIntent.putExtra(SyncModules.PARAMS_SERVER, Server.IMONGGO.ordinal());
            mServiceIntent.putExtra(SyncModules.PARAMS_INITIAL_SYNC, true);

            mModulesToDownload = new ArrayList<>();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set default mServer
        setServer(Server.IMONGGO);

        // Set default custom layout to false
        setUsingCustomLayout(false);
    }

    /**
     * Methods which checks if someone is logged in
     */
    private void loginChecker() {
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
                    setUnlinked(false);
                    mServiceIntent.putExtra(SyncModules.PARAMS_INITIAL_SYNC, false);
                    // user is logged in set up data
                    mSession = getSession();

                    if (!mSession.getApiAuthentication().equals("")) { // User is authenticated
                        setLoggedIn(true);

                        // check if sessions email exist in user's database
                        if (getHelper().getUsers().queryBuilder().where().eq("email", mSession.getEmail()).query().size() == 0) {
                            Log.e(TAG, "sessions email dont match dont match user's email");
                            LoggingTools.showToast(this, getString(R.string.LOGIN_USER_DONT_EXIST));

                            // TODO: Offline Data in unlinkAccount
                            unlinkAccount();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void onCreateLoginLayout() {
        Log.e(TAG, "onCreateLoginLayout called");

        // if user is logout
        if (!isLoggedIn()) {

            startSyncService();

            // show login layout
            onCreateLayoutForLogin();
            setLoginLayout();
        }
    }

    /**
     * Checks if AutoUpdate is on. If True Update the data, else skip to welcome screen
     */
    private void autoUpdateChecker() {
        // Account is Linked User is logged in
        if (!isUnlinked() && isLoggedIn()) {
            if (isAutoUpdate()) {
                updateAppData();
            }

            showNextActivity();
        }
    }

    public void startSyncingImonggoModules() {

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
                            case BRANCH_USERS:
                                mModulesToDownload.add("Branches");
                                break;
                            case TAX_SETTINGS:
                                mModulesToDownload.add("Tax Settings");
                                break;
                            case PRODUCTS:
                                mModulesToDownload.add("Products");
                                break;
                            case INVENTORIES:
                                mModulesToDownload.add("Inventories");
                                break;
                            case CUSTOMERS:
                                mModulesToDownload.add("Customers");
                                break;
                            case DOCUMENTS:
                                mModulesToDownload.add("Documents");
                                break;
                            case UNITS:
                                mModulesToDownload.add("Units");
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
        return !getDefaultBranch().equals("");
    }

    /**
     * Sets the Layout for BaseLogin
     */
    private void setLoginLayout() {
        // if you are not using custom layout
        if (!isUsingCustomLayout()) {
            setContentView(R.layout.concessioengine_login);

            setupLayoutEquipments((EditText) findViewById(R.id.etAccountId),
                    (EditText) findViewById(R.id.etEmail),
                    (EditText) findViewById(R.id.etPassword),
                    (Button) findViewById(R.id.btnSignIn));
        }
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

        this.etAccountID.setText("nuecaonly");
        this.etEmail.setText("nuecaonly@test.com");
        this.etPassword.setText("nuecaonly");

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
                mSession = session;
                setLoggedIn(true);
                setUnlinked(false);

                DialogTools.hideIndeterminateProgressDialog();
                startSyncingImonggoModules();
                successLogin();
            }

            @Override
            public void onStopLogin() {
                Log.e(TAG, "onStopLogin called");

                // hide progress dialog
                DialogTools.hideIndeterminateProgressDialog();

                // delete session data
                deleteUserSessionData();

                setLoggedIn(false);
                setUnlinked(true);

                stopLogin();
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
                setDefaultBranch("");
                startSyncService();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setLoginCredentials(Context context, String accountId, String email, String
            password) throws LoginException {
        if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            throw new LoginException(context.getString(R.string.LOGIN_FIELD_REQUIRED));
        } else if (!LoginTools.isValidEmail(email)) {
            throw new LoginException(context.getString(R.string.LOGIN_INVALID_EMAIL));
        } else if (!LoginTools.isValidPassword(password)) {
            throw new LoginException(context.getString(R.string.LOGIN_INVALID_PASSWORD));
        } else {
            mBaseLogin = new BaseLogin(BaseLoginActivity.this, getHelper(), accountId, email, password);
        }
    }

    private void LogInAccount(Server server) throws LoginException {
        if (mBaseLogin != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mBaseLogin.startLoginAccount(server);
        }
    }

    private void LogInUser(Server server) throws LoginException {
        if (mBaseLogin != null) {
            mBaseLogin.startLoginUser(server);
        }
    }

    private void LogOutUser() {
        try {
            AccountTools.logoutUser(this, getHelper(), this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean isLoggedIn() {
        return isLoggedIn;
    }

    public Boolean isUnlinked() {
        return isUnlinked;
    }

    public Boolean isAutoUpdate() {
        return SettingTools.isAutoUpdate(this);
    }

    public String getDefaultBranch() {
        return mDefaultBranch;
    }

    public Server getServer() {
        return mServer;
    }

    private int[] getModules() {
        return mModules;
    }

    public void setModules(int[] mModules) {
        this.mModules = mModules;
        mServiceIntent.putExtra(SyncModules.PARAMS_SYNC_ALL_MODULES, false);
        mServiceIntent.putExtra(SyncModules.PARAMS_TABLES_TO_SYNC, mModules);
    }

    private void setLoggedIn(Boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    private void setUnlinked(Boolean isUnlinked) {
        this.isUnlinked = isUnlinked;
        AccountTools.updateUnlinked(this, isUnlinked);
    }

    public void setServer(Server server) {
        this.mServer = server;
        mServiceIntent.putExtra(SyncModules.PARAMS_SERVER, server.ordinal());
    }

    private void setDefaultBranch(String branchName) {
        mDefaultBranch = branchName;
    }

    public Boolean isUsingCustomLayout() {
        return isUsingCustomLayout;
    }

    public void setUsingCustomLayout(Boolean useCustomLayout) {
        this.isUsingCustomLayout = useCustomLayout;
    }

    public ServiceConnection getServiceConnection() {
        return mConnection;
    }

    public Boolean isSyncServiceBinded() {
        return mBounded;
    }

    public void stopService() {
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

    private void startSyncService() {
        if (!isSyncServiceRunning(SyncModules.class) || mSyncModules == null) {
            mBounded = false;
            startService(mServiceIntent);
            Log.e(TAG, "There is no service running, starting service..");
            bindSyncService();
        } else {
            Log.e(TAG, "Service is already running");
        }
    }

    private void bindSyncService() {
        Log.e(TAG, "Binding service.........");

        if (!mBounded) {
            bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
            Log.e(TAG, "Service binded");
        } else {
            Log.e(TAG, "Service is already binded.");
        }

    }

    private void doUnbindService() {
        if (isSyncServiceBinded()) {
            mBounded = false;
            unbindService(getServiceConnection());
        }
    }

    private boolean isSyncServiceRunning(Class<?> serviceClass) {
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
        Log.e(TAG, "started downloading " + table);
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
                        currentTable = "User Branches";
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
                        currentTable = "Document Type";
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
    }

    @Override
    public void onFinishDownload() {
        LoggingTools.showToast(this, "Finished Downloading Modules");
        syncingModulesSuccessful();
        if (customDialog != null) {
            customDialog.dismiss();
        }

        showNextActivity();
    }

    @Override
    public void onErrorDownload(Table table, String message) {
        Log.e(TAG, "error downloading " + table + " " + message);
    }

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
                Log.e(TAG, "Succesfully bind Service and Activity");
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

    @Override
    public void onStop() {
        stopLogin();
        if (isUnlinked() && !isLoggedIn()) {
            if (mBaseLogin != null) {
                mBaseLogin.onStop();
            }
        }

        DialogTools.hideIndeterminateProgressDialog();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopLogin();
        if (isUnlinked() && !isLoggedIn()) {
            if (mBaseLogin != null) {
                mBaseLogin.onStop();
            }
        }
        DialogTools.hideIndeterminateProgressDialog();
        doUnbindService();


        super.onDestroy();
    }
}