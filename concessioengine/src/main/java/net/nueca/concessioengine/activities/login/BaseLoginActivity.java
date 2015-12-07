package net.nueca.concessioengine.activities.login;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.dialogs.CustomDialog;
import net.nueca.concessioengine.dialogs.CustomDialogFrameLayout;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.dialogs.DialogTools;
import net.nueca.imonggosdk.enums.DialogType;
import net.nueca.imonggosdk.enums.LoginState;
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

    private BaseLogin mBaseLogin = null;
    private Boolean isUnlinked = true;
    private Boolean isLoggedIn = false;
    private Boolean requireConcessioSettings = false;
    private Boolean requireObjectConcessioSettings = false;
    private Session mSession = null;
    private Server mServer = Server.IMONGGO;
    private Boolean isUsingDefaultCustomDialogForSync = true;
    private int[] mModules = null;
    private EditText etAccountID = null;
    private EditText etEmail = null;
    private EditText etPassword = null;
    private Button btnSignIn = null;
    private CustomDialog customDialog = null;
    private CustomDialogFrameLayout customDialogFrameLayout = null;
    private Intent mServiceIntent = null;
    private List<String> mModulesToDownload = null;
    private String TAG = "BaseLoginActivity";
    private SyncModules mSyncModules = null;
    private Boolean mBounded = false;
    private Boolean mUseDefaultDialog = true;
    private LoginState mLoginState = LoginState.LOGIN_DEFAULT;

    /**
     * If you want to initialize your own logic. method before login checker.
     * you should implement this methods: setServer(...) and setModulesToSync(...)
     * if not then the default server and modules is set.
     */
    protected abstract void initLoginEquipments();

    /**
     * Checks if someone is logged in
     */
    protected abstract void loginChecker();

    /**
     * If you want to add some logic before fetching data
     */
    protected abstract void updateAppData();

    /**
     * This is where the Login Life Cycle will Stop.
     * You should @Override this method and the activity you want
     * to show next
     */
    protected abstract void showNextActivityAfterLogin();

    /**
     * Override this method if you want to add
     * your own before logging in start
     */
    protected abstract void beforeLogin();

    /**
     * Override this method if you want to add you code.
     * This code
     */
    protected abstract void stopLogin();

    /**
     * This method is called when after user has successfully
     * downloaded Account_URL, Token and Authentication
     */
    protected abstract void successLogin();

    /**
     * This method is called before downloading any modules
     * this is where you will builds the custom downloading
     * progress dialogs and etc.
     */
    protected abstract void showCustomDownloadDialog();

    /**
     * This is where you will create your login layout
     * setContentView and align the ids in your layout
     * to this class.
     * <p/>
     * if you want to customize login layout
     * you should extend this class and
     * override this method and call this
     * functions inside:
     * <p/>
     * 1. setContentView( your custom layout)
     * 2. setLayoutEquipments( fill in the ids in your layout)
     */
    protected abstract void onCreateLoginLayout();

    /**
     * This is where you should check if existing user is currently logged in
     * logged out or the user has unlinked the credentials in the device
     */
    protected abstract void autoUpdateChecker();

    protected abstract void dialogPositiveButtonAction();

    protected abstract void dialogNegativeButtonAction();

    protected abstract void showProgressDialog(DialogType type, String message, String positiveText, String negativeText);

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "OnStart()");
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "OnResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause()");
    }

    /**
     * Calling required methods for login.
     * you can Override this method as long
     * as you call super.onCreate
     *
     * @param savedInstanceState bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLoginEquipments();
        loginChecker();
        if (!isLoggedIn() || isUnlinked()) {
            onCreateLoginLayout();
        }
        autoUpdateChecker();
    }

    /**
     * For Sync Service only
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
     * This method starts handles the downloads and updates of all modules.
     *
     * @throws SQLException
     */
    public void startSyncingImonggoModules() throws SQLException {
        if (isSyncServiceBinded()) {
            setUpModuleNamesForCustomDialog();
            showCustomDownloadDialog();

            Log.e(TAG, "Starting Module Download");
            if (mSyncModules != null) {
                mSyncModules.startFetchingModules();
            } else {
                Log.e(TAG, "Service Modules is null cannot start sync");

                if (customDialog != null) {
                    customDialog.dismiss();
                }
                startSyncService();
                DialogTools.showBasicWithTitle(BaseLoginActivity.this, "Syncing Failed",
                        "Sync failed. Login again.",
                        "Okay", null, null,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btnSignIn.setEnabled(true);
                            }
                        }, null, null, false, R.style.AppCompatDialogStyle_Light_NoTitle);
//                DialogTools.showBasicWithTitle(BaseLoginActivity.this, "Sync Failed",
//                        "Sync failed. Login Again ",
//                        "Ok", "", false,
//                        new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(MaterialDialog dialog, DialogAction dialogAction) {
//                                bindSyncService();
//                                dialog.dismiss();
//                                dialogPositiveButtonAction();
//                                btnSignIn.setEnabled(true);
//                            }
//                        }, null, null);
            }
        } else {

            startSyncService();
        }
    }

    public Table getCurrentTableToSync() {
        return mSyncModules.getCurrentTableSyncing();
    }

    /**
     * This populates list of module names that
     * the custom dialog needs based on your list.
     */
    private void setUpModuleNamesForCustomDialog() {
        if (getModules() != null) { // manually set modules to download see /**/
            if (mModulesToDownload != null) {
                mModulesToDownload.clear();
            } else {
                mModulesToDownload = new ArrayList<>();
            }

            for (int module : getModules()) {

                Table table = Table.values()[module];

                if (table.isAPI()) {

                    mModulesToDownload.add(table.getStringName());
                } else {
                    LoggingTools.showToast(BaseLoginActivity.this, "You have added unsupported module. please check your code");
                    Log.i(TAG, "You have added unsupported module. please check your code. " + table);
                }
            }
        } else { // if you don't set custom modules to download. Sync All
            mModulesToDownload.add("Users");
            mModulesToDownload.add("Branches");
            mModulesToDownload.add("Tax Settings");
            mModulesToDownload.add("Products");
            mModulesToDownload.add("Customers");
            mModulesToDownload.add("Units");
            mModulesToDownload.add("Documents");
            mModulesToDownload.add("Document Types");
            mModulesToDownload.add("Document Purposes");
        }
    }

    public void createNewCustomDialogFrameLayout(Context context, List<String> moduleName) {
        customDialogFrameLayout = new CustomDialogFrameLayout(context, moduleName);
        customDialogFrameLayout.setLoginListener(mBaseLogin.getLoginListener());
    }

    public CustomDialogFrameLayout getCustomDialogFrameLayout() {
        return customDialogFrameLayout;
    }


    public void setCustomDialogContentView(CustomDialogFrameLayout customDialogContentView) {
        customDialog.setContentView(customDialogContentView);
    }

    public void setCustomDialogTitle(String title) {
        customDialog.setTitle(title);
    }

    public void createNewCustomDialog(Context context) {
        customDialog = new CustomDialog(context);
    }

    public void createNewCustomDialog(Context context, int theme) {
        customDialog = new CustomDialog(context, theme);
    }

    public void setCustomDialogCancelable(Boolean choice) {
        customDialog.setCancelable(choice);
    }

    public void hideCustomDialog() {
        showOrHideCustomDialog(false);
    }

    public void showCustomDialog() {
        showOrHideCustomDialog(true);
    }

    private void showOrHideCustomDialog(boolean choice) {
        if (choice) {
            customDialog.show();
        } else {
            customDialog.hide();
        }
    }

    public void setIsUsingDefaultCustomDialogForSync(boolean choice) {
        isUsingDefaultCustomDialogForSync = choice;
    }

    public boolean isUsingDefaultCustomDialogForSync() {
        return isUsingDefaultCustomDialogForSync;
    }

    /**
     * Checks if the app has default branch.
     *
     * @return true if default branch is saved, false otherwise.
     */
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
            editTextEmail, EditText editTextPassword, final Button btnSignIn) {

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
            DialogTools.showBasicWithTitle(BaseLoginActivity.this, getString(R.string.LOGIN_FAILED_TITLE), "No network connection", getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), null, null,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialogPositiveButtonAction();
                            btnSignIn.setEnabled(true);
                        }
                    }, null, null, false, R.style.AppCompatDialogStyle_Light_NoTitle);
//            DialogTools.showBasicWithTitle(BaseLoginActivity.this, getString(R.string.LOGIN_FAILED_TITLE),
//                    "No network connection",
//                    getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
//                    new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
//                            dialogPositiveButtonAction();
//                            btnSignIn.setEnabled(true);
//                            materialDialog.dismiss();
//                        }
//                    }, null, new Theme);
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
            } else if (!LoginTools.isValidEmail(email)) {
                etEmail.setError(getString(R.string.LOGIN_INVALID_EMAIL));
                focusView = etEmail;
                cancelLogin = true;
            } else if (TextUtils.isEmpty(email)) { // EMAIL
                etEmail.setError(getString(R.string.LOGIN_FIELD_REQUIRED));
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

                if (isUsingDefaultDialog()) {
                    DialogTools.showIndeterminateProgressDialog(BaseLoginActivity.this,
                            null,
                            getString(R.string.LOGIN_PROGRESS_DIALOG_CONTENT), false, R.style.AppCompatDialogStyle_Light_NoTitle);
                } else {
                    showProgressDialog(DialogType.INDETERDMINATE, getString(R.string.LOGIN_PROGRESS_DIALOG_CONTENT), "", "");
                }

                // BaseLogin Function
                btnSignIn.setEnabled(false);
                startLogin(getApplicationContext(), accountId, email, password, getServer());
            }
        }
    }

    private void setLoginListeners() {
        mBaseLogin.setLoginListener(new LoginListener() {
            @Override
            public void onStartLogin() {
                Log.e(TAG, "onStartLogin called");
                mLoginState = LoginState.LOGIN_STARTED;
                beforeLogin();
            }

            @Override
            public void onLoginSuccess(Session session) {
                Log.e(TAG, "Successfully logged in");
                DialogTools.hideIndeterminateProgressDialog();
                mLoginState = LoginState.LOGIN_SUCCESS;
                successLogin();
                mSession = session;
                setLoggedIn(true);
                setUnlinked(false);

                try {
                    startSyncingImonggoModules();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPositiveButtonPressed() {
                dialogPositiveButtonAction();
                DialogTools.hideIndeterminateProgressDialog();
                btnSignIn.setEnabled(true);
            }

            @Override
            public void onNegativeButtonPressed() {
                dialogNegativeButtonAction();
            }

            @Override
            public void onRetryButtonPressed(Table table) throws SQLException {
                Log.e(TAG, table.getStringName());
                if (mLoginState == LoginState.LOGIN_FAILED) {
                    if(!NetworkTools.isInternetAvailable(BaseLoginActivity.this)) {

                        DialogTools.showBasicWithTitle(BaseLoginActivity.this, "Failed to Re-Sync Data",
                                getString(R.string.LOGIN_NETWORK_ERROR),
                                "Okay", null, null,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }, null, null, false, R.style.AppCompatDialogStyle_Light_NoTitle);
//                        DialogTools.showBasicWithTitle(
//                                BaseLoginActivity.this,
//                                "Failed to Re-Sync Data",
//                                getString(R.string.LOGIN_NETWORK_ERROR),
//                                "Okay", null, false,
//                                new MaterialDialog.SingleButtonCallback() {
//                                    @Override
//                                    public void onClick(@NonNull MaterialDialog materialDialog, DialogAction dialogAction) {
//                                        materialDialog.dismiss();
//                                    }
//                                }, null, null);
                    } else {
                        if (table == mSyncModules.getCurrentTableSyncing()) {
                            Log.e(TAG, "retrying sync table " + table);
                            mSyncModules.prepareModulesToReSync();
                            Log.e(TAG, "Number of Modules to re-Sync: " + mSyncModules.getModulesToSync().length);

                            getCustomDialogFrameLayout().getCustomModuleAdapter().hideRetryButton(mModulesToDownload.indexOf(getStringOfCurrentTable(table)));

                            mLoginState = LoginState.LOGIN_DOWNLOADING_DATA;
                            mSyncModules.startFetchingModules();
                        } else {
                            Log.e(TAG, "oops");
                        }
                    }
                } else {
                    Log.e(TAG, "Login State: " + mLoginState);
                }
            }

            @Override
            public void onStopLogin() {
                btnSignIn.setEnabled(true);
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
            try {
                getHelper().deleteAllDatabaseValues();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            mBaseLogin.setmUseObjectForConcessioSettings(requireObjectConcessioSettings);
        }
    }

    protected void LogInAccount(Server server) throws LoginException {
        if (mBaseLogin != null) {
            mBaseLogin.startLoginAccount(server);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

    protected void setLoginSession(Session session) {
        this.mSession = session;
    }

    protected Session getLoginSession() {
        return this.mSession;
    }

    protected List<String> getModulesToSync() {
        return this.mModulesToDownload;
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

    /**
     * setRequireConcessioSettings
     *
     * @param requireConcessioSettings your choice
     */
    public void setRequireConcessioSettings(Boolean requireConcessioSettings) {
        this.requireConcessioSettings = requireConcessioSettings;
    }

    public void setRequireObjectConcessioSettings(Boolean requireObjectConcessioSettings) {
        this.requireObjectConcessioSettings = requireObjectConcessioSettings;
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

    protected void setModulesToSync(int... mModules) {
        setModule(mModules);
    }

    protected void setModule(int[] mModules) {
        this.mModules = mModules;
        if (mModules != null) {
            mServiceIntent.putExtra(SyncModules.PARAMS_SYNC_ALL_MODULES, false);
            mServiceIntent.putExtra(SyncModules.PARAMS_TABLES_TO_SYNC, mModules);
        }
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


    protected void setSyncAllModules(boolean choice) {
        this.mServiceIntent.putExtra(SyncModules.PARAMS_SYNC_ALL_MODULES, choice);
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

    protected SyncModules getSyncModules() {
        return mSyncModules;
    }

    protected void setSyncModules(SyncModules syncModules) {
        mSyncModules = syncModules;
    }

    public Boolean isUsingDefaultDialog() {
        return mUseDefaultDialog;
    }

    public void setIsUsingDefaultDialog(Boolean mUseDefaultDialog) {
        this.mUseDefaultDialog = mUseDefaultDialog;
    }

    protected void stopService() {
        Log.e(TAG, "Stopping sync service");
        doUnbindService();
        if (isSyncServiceRunning(BaseSyncService.class) || mSyncModules != null) {
            stopService(mServiceIntent);
        }

        if (!isSyncServiceRunning(SyncModules.class)) {
            Log.e(TAG, "Sync Services stopped");
        } else {
            Log.e(TAG, "Stopping Sync Service failed, Service is still running.");
        }
    }

    protected void startSyncService() {
        Log.e("startSyncService", "called");
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

        if (!mBounded || mSyncModules == null) {
            bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
            Log.e(TAG, "Service binded");
        } else {
            Log.e(TAG, "Service is already binded.");
        }

    }

    protected void doUnbindService() {
        Log.e("mBounded", "" + mBounded);
        if (isSyncServiceBinded()) {
            mBounded = false;
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

    private String getStringOfCurrentTable(Table table) {
        return table.getStringName();
    }

    @Override
    public void onStartDownload(Table table) {
        setSyncFinished(BaseLoginActivity.this, false);
        Log.e(TAG, "onStartDownload");
        mLoginState = LoginState.LOGIN_DOWNLOADING_DATA;
    }

    @Override
    public void onDownloadProgress(Table table, int page, int max) {
        Log.e(TAG, "Downloading " + table + " " + page + " out of " + max);
        int progress = 0;
        String currentTable = getStringOfCurrentTable(table);

        progress = (int) Math.ceil((((double) page / (double) max) * 100.0));

        Log.e(TAG, table + " progress: " + progress);
        if (isUsingDefaultCustomDialogForSync()) {
            customDialogFrameLayout.getCustomModuleAdapter().hideCircularProgressBar(mModulesToDownload.indexOf(currentTable));
            customDialogFrameLayout.getCustomModuleAdapter().updateProgressBar(mModulesToDownload.indexOf(currentTable), progress);
        }
    }

    @Override
    public void onEndDownload(Table table) {
        stopService();
        setSyncFinished(BaseLoginActivity.this, true);
    }

    @Override
    public void onFinishDownload() {
        if (customDialog != null) {
            customDialog.dismiss();
            customDialog = null;
        }

        DialogTools.hideIndeterminateProgressDialog();
        showNextActivityAfterLogin();
    }

    @Override
    public void onErrorDownload(Table table, String message) {
        Log.e(TAG, "error downloading " + table + " " + message);
        mLoginState = LoginState.LOGIN_FAILED;

        getCustomDialogFrameLayout().getCustomModuleAdapter().showRetryButton(mModulesToDownload.indexOf(getStringOfCurrentTable(table)));

        LoggingTools.showToastLong(BaseLoginActivity.this, "Download failed, Tap " + table + " module to retry");
        stopLogin();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop()");
        if (customDialog != null) {
            customDialog.dismiss();
            customDialog = null;
        }

        super.onStop();


        if (isUnlinked() && !isLoggedIn()) {
            if (getBaseLogin() != null) {
                getBaseLogin().onStop();
            }
        }
        DialogTools.hideIndeterminateProgressDialog();
        stopLogin();
    }

    public void setAutoUpdateApp(Boolean choice) {
        SettingTools.updateSettings(this, SettingsName.AUTO_UPDATE, choice, "");
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy()");

        if (isUnlinked() && !isLoggedIn()) {
            if (getBaseLogin() != null) {
                getBaseLogin().onStop();
            }
        }
        super.onDestroy(); // This should be the last to call after onStop();
        DialogTools.hideIndeterminateProgressDialog();
        doUnbindService();
    }
}