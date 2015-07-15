package net.nueca.concessioengine.activities.login;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.operations.login.BaseLogin;
import net.nueca.imonggosdk.operations.sync.TestService;
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
public abstract class BaseLoginActivity extends ImonggoAppCompatActivity implements AccountListener {

    private Boolean isUnlinked;
    private Boolean isLoggingIn;
    private Boolean isLoggedIn;
    private Boolean isUsingCustomLayout;
    private Session mSession = null;
    private String mDefaultBranch;
    private Server mServer;
    private int[] mModules;
    private EditText accountIdEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button btnSignIn;
    private Button btnUnlinkDevice;
    private CustomDialog customDialog;
    private CustomDialogFrameLayout customDialogFrameLayout;

    protected BaseLogin mBaseLogin;

    //----
    private TestService mTestService;
    private Boolean mBounded;

    /**
     * If you want to initialize your own logic. method before login checker.
     */
    protected abstract void initActivity();

    /**
     * If you want to add some logic before fetching data
     */
    protected abstract void updateAppData();

    /**
     * This is called before updating the modules of the app
     * you can set the list of modules to be downloaded here
     */
    protected abstract void updateModules();

    /**
     * If you want to add some logic i select branches
     */
    protected abstract void onCreateSelectBranchLayout();

    protected abstract void beforeLogin();

    protected abstract void stopLogin();

    protected abstract void loginSuccess();

    protected abstract void onCreateLoginLayout();

    @Override
    protected void onStart(){
        super.onStart();
        // Binds service to Activity
        bindService();
    }


    @Override
    protected void onResume() {
        super.onResume();
        bindService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initEquipments();
        initActivity();
        loginChecker();
        createLoginLayout();
        autoUpdateChecker();
    }

    /**
     * Method which will initialize everything
     */
    private void initEquipments() {
        try {
            isUnlinked = AccountTools.isUnlinked(this);
            isLoggedIn = AccountTools.isLoggedIn(getHelper());
            isLoggingIn = AccountTools.isLoggedIn(getHelper());
            mDefaultBranch = SettingTools.defaultBranch(this);
            mBounded = false;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set default server
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
                setLoggingIn(false);
                setLoggedIn(false);
            }
            // Account is Linked
            if (!AccountTools.isUnlinked(this)) {
                // if user is logout
                if (!AccountTools.isLoggedIn(getHelper())) {
                    setUnlinked(false);
                    setLoggingIn(false);
                    setLoggedIn(false);
                }
                // if User is Logged In
                if (AccountTools.isLoggedIn(getHelper())) {
                    setUnlinked(false);

                    // user is logged in set up data
                    mSession = getSession();
                    //player.me raquezha
                    if (!mSession.getApiAuthentication().equals("")) { // User is authenticated
                        setLoggingIn(true);
                        setLoggedIn(true);

                        // TODO: do this after fetching data
                        /* // check if sessions email exist in user's database
                        if (getHelper().getUsers().queryBuilder().where().eq("email", mSession.getEmail()).query().size() == 0) {
                            Log.i("loginChecker", "sessions email dont match dont match user's email");
                            LoggingTools.showToast(this, getString(R.string.LOGIN_USER_DONT_EXIST));
                            // TODO: Offline Data in unlinkAccount
                            // AccountTools.unlinkAccount(this, getHelper(), this, DELETEALL?);
                            unlinkAccount();
                            return;
                        }*/
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void createLoginLayout() {
        Log.i("Jn-BaseLoginActivity", "createLoginLayout");

        // if user is logout
        if (!isLoggedIn()) {
            // show login layout
            setLoginLayout();
        } else {

            // check if user has default branch
            haveDefaultBranch();

            Log.i("CreateLoginLayout", "Error cannot create login layout when user " +
                    "is logged in and account is link");
        }
    }

    /**
     * Checks if AutoUpdate is on. If True Update the data, else skip to welcome screen
     */
    private void autoUpdateChecker() {
        Log.i("autoUpdateChecker", "update app data");
        updateAppData();
        // Account is Linked User is logged in
        if (!isUnlinked() && isLoggedIn()) {
            if (isAutoUpdate()) {
                Log.i("updateData", "auto update is on.");
                // Fetch data
                updateData();
            } else {
                Log.i("updateData", "auto update is off.");
            }

            if (!haveDefaultBranch()) {
                // TODO: show select branches screen

                setLayoutSelectBranch();
                onCreateSelectBranchLayout();
            } else {
                // TODO: show welcome screen
            }
        }
    }

    private void updateData() {
        updateModules();
        // TODO: fetching logic

        if (getModules() == null) {
            Log.i("Update Data", "No Custom Modules to download, fetching all modules");

        } else {
            Log.i("Update Data", "Custom Modules length is : " + getModules().length + " ... fetching data");

        }
    }

    public Boolean haveDefaultBranch() {
        // if default branch is not null
        if (getDefaultBranch().equals("")) {
            return false;
        } else {
            return true;
        }
    }

    protected void setLayoutSelectBranch() {
        setContentView(R.layout.concessioengine_select_branches);

        // TODO: remove this
        Button test_unlink_button = (Button) findViewById(R.id.test_button_unlink);
        Button test_alertDialog_button = (Button) findViewById(R.id.test_button_alertDialog);

        test_unlink_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggingTools.showToast(getApplicationContext(), "Unlink Account..");
                unlinkAccount();

                setLoginLayout();
            }
        });

        test_alertDialog_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] modules = {Table.BRANCHES.ordinal(), Table.PRODUCTS.ordinal(), Table.CUSTOMERS.ordinal()};

                if(mTestService != null) {
                    Toast.makeText(BaseLoginActivity.this, mTestService.getTime() + "", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BaseLoginActivity.this, "null", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * Sets the Layout for BaseLogin
     */
    private void setLoginLayout() {
        onCreateLoginLayout();

        // if you are not using custom layout
        if (!isUsingCustomLayout()) {
            setContentView(R.layout.concessioengine_login);

            setupLayoutEquipments((EditText) findViewById(R.id.text_account_id),
                    (EditText) findViewById(R.id.text_email),
                    (EditText) findViewById(R.id.text_password),
                    (Button) findViewById(R.id.btn_signin), (Button) findViewById(R.id.btn_unlink));
        }
    }

    /**
     * Sets up EditText, Buttons and Listeners
     *
     * @param editTextAccountId Account ID EditText
     * @param editTextEmail     Email EditText
     * @param editTextPassword  Password EditText
     * @param btnSignIn         Sign In Button
     * @param btnLogout         Logout Button
     */
    protected void setupLayoutEquipments(EditText editTextAccountId, EditText
            editTextEmail, EditText editTextPassword, Button btnSignIn, Button btnLogout) {
        this.accountIdEditText = editTextAccountId;
        this.emailEditText = editTextEmail;
        this.passwordEditText = editTextPassword;

        this.accountIdEditText.setText("retailpos");
        this.emailEditText.setText("retailpos@test.com");
        this.passwordEditText.setText("retailpos");

        this.btnSignIn = btnSignIn;
        this.btnUnlinkDevice = btnLogout;

        // Button SignIn Listener
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLogin();
            }
        });

        // Button LogOut Listener
        btnUnlinkDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("btnUnlinkListener", "Unlink Account");
                unlinkAccountCustom();
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
                    } );

           /* Toast.makeText(getBaseContext(),
                    "No network connection", Toast.LENGTH_SHORT).show();*/
        } else {
            Boolean cancelLogin = false;
            // Set error to null
            accountIdEditText.setError(null);
            emailEditText.setError(null);
            passwordEditText.setError(null);
            // Get the String
            String accountId = accountIdEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            // the focus
            View focusView = null;

            // ACCOUNT
            if (TextUtils.isEmpty(accountId)) {
                accountIdEditText.setError(getString(R.string.LOGIN_FIELD_REQUIRED));
                focusView = accountIdEditText;
                cancelLogin = true;
            } else if (TextUtils.isEmpty(email)) { // EMAIL
                emailEditText.setError(getString(R.string.LOGIN_FIELD_REQUIRED));
                focusView = emailEditText;
                cancelLogin = true;
            } else if (!LoginTools.isValidEmail(email)) {
                emailEditText.setError(getString(R.string.LOGIN_INVALID_EMAIL));
                focusView = emailEditText;
                cancelLogin = true;
            }

            // PASSWORD
            if (!TextUtils.isEmpty(password) && !LoginTools.isValidPassword(password)) {
                passwordEditText.setError(getString(R.string.LOGIN_INVALID_PASSWORD));
                focusView = passwordEditText;
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
                Log.i("Jn-BaseLoginActivity", "Loggin in...");
                startLogin(getApplicationContext(), accountId, email, password, getServer());
            }
        }
    }

    private void setLoginListeners() {
        mBaseLogin.setLoginListener(new LoginListener() {
            @Override
            public void onStartLogin() {
                Log.i("Jn-BaseLoginActivity", "onStartLogin");
                beforeLogin();
                setLoggingIn(true);
            }

            @Override
            public void onLoginSuccess(Session session) {
                Log.i("Jn-BaseLoginActivity", "onLoginSuccess");
                loginSuccess();

                mSession = session;

                // hide progress dialog
                DialogTools.hideIndeterminateProgressDialog();

                updateData();

                setLoggedIn(true);
                setLoggingIn(false);
                setUnlinked(false);

                // hide login form
                showLoginForm(false);

                List<String> moduleName = new ArrayList<>();

                moduleName.add("Branches");
                moduleName.add("Users");
                moduleName.add("Products");
                moduleName.add("Settings");

                if (getModules() != null) {
                    Log.i("_modules size: ", getModules().length + "");

                    customDialogFrameLayout = new CustomDialogFrameLayout(BaseLoginActivity.this, moduleName);

                    customDialog = new CustomDialog(BaseLoginActivity.this, R.style.AppCompatDialogStyle);
                    customDialog.setTitle(getString(R.string.FETCHING_MODULE_TITLE));
                    customDialog.setContentView(customDialogFrameLayout);
                    customDialog.show();

                    List<Integer> progressList = new ArrayList<>();

                    for(int i=0; i< moduleName.size(); i++) {
                        progressList.add(i, i * 2);
                    }

                    customDialogFrameLayout.getCustomModuleAdapter().updateProgressBar(moduleName.indexOf("Settings"), 98);
                    customDialogFrameLayout.getCustomModuleAdapter().updateProgressBar(moduleName.indexOf("Branches"), 20);

                } else {
                    DialogTools.showBasicWithTitle(BaseLoginActivity.this,
                            getString(R.string.FETCH_NO_MODULE_SELECTED_TITLE),
                            getString(R.string.FETCH_NO_MODULE_SELECTED),
                            getString(R.string.FETCH_NO_MODULE_POSTIVE_BUTTON), "", false, new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    showLoginForm(true);
                                    unlinkAccount();
                                }
                            } );
                }
            }

            @Override
            public void onStopLogin() {
                Log.i("Jn-BaseLoginActivity", "onStopLogin");
                stopLogin();

                // hide progress dialog
                DialogTools.hideIndeterminateProgressDialog();

                if (mSession != null) {
                    if (AccountTools.isUnlinked(BaseLoginActivity.this)) {
                        mSession.deleteTo(getHelper());
                    }
                } else {
                    try {
                        getHelper().getSessions().deleteBuilder().delete();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                setLoggedIn(false);
                setLoggingIn(false);
                setUnlinked(true);
            }
        });
    }

    private void showLoginForm(Boolean hide) {

        try {
            if (AccountTools.isLoggedIn(getHelper())) {

                if (!hide) {
                    btnUnlinkDevice.setVisibility(View.VISIBLE);
                    btnSignIn.setVisibility(View.GONE);

                    accountIdEditText.setVisibility(View.GONE);
                    passwordEditText.setVisibility(View.GONE);
                    emailEditText.setVisibility(View.GONE);
                } else {
                    btnUnlinkDevice.setVisibility(View.GONE);
                    btnSignIn.setVisibility(View.VISIBLE);
                    accountIdEditText.setVisibility(View.VISIBLE);
                    passwordEditText.setVisibility(View.VISIBLE);
                    emailEditText.setVisibility(View.VISIBLE);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sets the BaseLogin Credentials of the user and starts login
     *
     * @param context   a context
     * @param accountId must not be empty
     * @param email     must not be empty and matches the correct form 'email@me.com'
     * @param password  must not be empty and length must be greater or equal to five (5)
     * @param server    server
     */
    protected void startLogin(Context context, String accountId, String email, String
            password, Server server) {
        try {
            setLoggingIn(true);
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
        setLoggingIn(false);
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
                setLoggingIn(false);
                setLoggingIn(false);
                setDefaultBranch("");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void unlinkAccountCustom() {
        try {

            if (!isUnlinked()) {
                AccountTools.unlinkAccount(this, getHelper(), this);

                setUnlinked(true);
                setLoggedIn(false);
                setLoggingIn(false);
                setLoggingIn(false);
                setDefaultBranch("");

                btnUnlinkDevice.setVisibility(View.GONE);
                btnSignIn.setVisibility(View.VISIBLE);
                accountIdEditText.setVisibility(View.VISIBLE);
                passwordEditText.setVisibility(View.VISIBLE);
                emailEditText.setVisibility(View.VISIBLE);
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

    public Boolean isLoggingIn() {
        return isLoggingIn;
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
    }

    private void setLoggedIn(Boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    private void setLoggingIn(Boolean isLoggingIn) {
        this.isLoggingIn = isLoggingIn;
    }

    private void setUnlinked(Boolean isUnlinked) {
        this.isUnlinked = isUnlinked;
        AccountTools.updateUnlinked(this, isUnlinked);
    }

    public void setServer(Server server) {
        this.mServer = server;
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

    private void bindService(){
        mBounded = bindService(new Intent(BaseLoginActivity.this, TestService.class), mConnection, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);
        if(mBounded) {
            LoggingTools.showToast(BaseLoginActivity.this, "Service started");
        } else {
            LoggingTools.showToast(BaseLoginActivity.this, "Service not started");
        }
    }

    private void doUnbindService(){
        if(mBounded) {
            unbindService(getServiceConnection());
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(BaseLoginActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();

            TestService.LocalBinder mLocalBinder = (TestService.LocalBinder) service;
            mBounded = true;
            mTestService = mLocalBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mTestService = null;
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

        doUnbindService();

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

        super.onDestroy();
    }
}