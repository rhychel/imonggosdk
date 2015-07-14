package net.nueca.concessioengine.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.tools.DialogMaterial;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.exception.LoginException;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.interfaces.LoginListener;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.operations.Login;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.LoginTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;

/**
 * Abstract Class LoginActivity
 * created by Jn on 06/16/15
 */
public abstract class LoginActivity extends ImonggoAppCompatActivity implements AccountListener {

    private Session mSession = null;
    private Boolean isUnlinked;
    private Boolean isLoggingIn;
    private Boolean isLoggedIn;
    private Boolean isAutoUpdate;
    private String mDefaultBranch;
    private Server mServer;
    private MaterialDialog progressDialog;
    private EditText accountIdEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button btnSignIn;
    private Button btnUnlinkDevice;
    protected Login mLogin;

    /**
     * If you want to initialize your own logic. method before login checker.
     */
    protected abstract void initActivity();

    /**
     * If you want to add some logic before fetching data
     */
    protected abstract void updateAppData();

    /**
     * If you want to add some logic i select branches
     */
    protected abstract void showSelectBranches();

    /**
     * If you want to do something before showing the welcome screen
     */
    protected abstract void showDashBoard();

    protected abstract void beforeLogin();

    protected abstract void stopLogin();

    protected abstract void afterLogin();

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
            isAutoUpdate = SettingTools.isAutoUpdate(this);
            mDefaultBranch = SettingTools.defaultBranch(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*********************************************************/
        progressDialog = DialogMaterial.createProgressDialog(LoginActivity.this,
                getString(R.string.LOGIN_PROGRESS_DIALOG_TITLE), "Please Wait...", false);
        progressDialog.hide();
        /*********************************************************/
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

    /**
     * Sets the default login layout.
     * <p/>
     * Note: Do not call super.createLoginLayout() in subclass activity
     * when Implementing custom layout.
     * <p/>
     * How:
     * 1. Override this method and set your custom layout
     * 2. call the function setupLayoutEquipments(...); and it will automatically set the logic
     * 3. that's all
     */
    protected void createLoginLayout() {
        Log.i("Jn-LoginActivity", "createLoginLayout");

        // if user is logout
        if (!isLoggedIn()) {
            // show login layout
            setLoginLayout();
        } else {
            Log.i("CreateLoginLayout", "Error cannot create login layout when user " +
                    "is logged in and account is unlink");
        }
    }

    /**
     * Checks if AutoUpdate is on. If True Update the data, else skip to welcome screen
     */
    private void autoUpdateChecker() {
        // Account is Linked User is logged in
        if (!isUnlinked() && isLoggedIn() & isLoggingIn()) {
            if (isAutoUpdate()) {
                Log.i("updateData", "auto update is on.");
                // Fetch data
                updateAppData();
                // TODO: fetching logic
            }
            checkBranches();
        }
    }

    private void checkBranches() {
        Log.i("mDefaultBranch", mDefaultBranch);
        // if default branch is not equal to ""
        if (getDefaultBranch().equals("")) {
            showSelectBranches();
            // TODO: show select branches screen
            createSelectBranchesLayout();
        }
    }

    protected void createSelectBranchesLayout() {
        setContentView(R.layout.concessioengine_select_branches);
    }

    private void setLoginLayout() {
        setContentView(R.layout.concessioengine_login);

        setupLayoutEquipments((EditText) findViewById(R.id.text_account_id),
                (EditText) findViewById(R.id.text_email),
                (EditText) findViewById(R.id.text_password),
                (Button) findViewById(R.id.btn_signin), (Button) findViewById(R.id.btn_unlink));

        setServer(Server.IRETAILCLOUD_COM);
    }

    protected void setupLayoutEquipments(EditText editTextAccountId, EditText
            editTextEmail, EditText editTextPassword, Button btnSignin, Button btnLogout) {
        this.accountIdEditText = editTextAccountId;
        this.emailEditText = editTextEmail;
        this.passwordEditText = editTextPassword;

        this.accountIdEditText.setText("retailpos");
        this.emailEditText.setText("retailpos@test.com");
        this.passwordEditText.setText("retailpos");


        this.btnSignIn = btnSignin;
        this.btnUnlinkDevice = btnLogout;

        // Button SignIn Listener
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    progressDialog.setTitle("Loggin in");
                    progressDialog.show();
                    // Login Function
                    Log.i("Jn-LoginActivity", "Loggin in...");
                    startLogin(getApplicationContext(), accountId, email, password, getServer());
                }
            }
        });

        // Button LogOut Listener
        btnUnlinkDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("btnUnlinkListener", "Unlink Account");
                unlinkAccount();
            }
        });
    }


    private void setLoginListeners() {
        mLogin.setLoginListener(new LoginListener() {
            @Override
            public void onStartLogin() {
                Log.i("Jn-LoginActivity", "onStartLogin");
                beforeLogin();
                setLoggingIn(true);
            }

            @Override
            public void onLoginSuccess(Session session) {


                Log.i("Jn-LoginActivity", "onLoginSuccess");
                afterLogin();

                mSession = session;
                progressDialog.hide();
                setLoggedIn(true);
                setLoggingIn(false);
                setUnlinked(false);

                try {
                    if (AccountTools.isLoggedIn(getHelper())) {
                        // -- login form
                        btnUnlinkDevice.setVisibility(View.VISIBLE);
                        btnSignIn.setVisibility(View.GONE);

                        accountIdEditText.setVisibility(View.GONE);
                        passwordEditText.setVisibility(View.GONE);
                        emailEditText.setVisibility(View.GONE);
                        // -- login form
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStopLogin() {
                Log.i("Jn-LoginActivity", "onStopLogin");
                stopLogin();

                progressDialog.hide();
                setLoggedIn(false);
                setLoggingIn(false);
                setUnlinked(true);
            }
        });

        mLogin.setVolleyRequestListener(new VolleyRequestListener() {
            @Override
            public void onStart(Table table, RequestType requestType) {
                Log.i("Jn-LoginActivity", "onStartVolley");
            }

            @Override
            public void onSuccess(Table table, RequestType requestType, Object response) {
                Log.i("Jn-LoginActivity", "onSuccessVolley");
            }

            @Override
            public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                Log.e("Jn-LoginActivity", "onErrorVolley message: " + response.toString());
                setLoggingIn(false);
            }

            @Override
            public void onRequestError() {
                Log.e("Jn-LoginActivity", "onRequestError");
                if (mSession != null) {
                    if (AccountTools.isUnlinked(LoginActivity.this)) {
                        mSession.deleteTo(getHelper());
                    }
                } else {
                    try {
                        getHelper().getSessions().deleteBuilder().delete();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                setLoggingIn(false);
            }
        });
    }


    /**
     * Sets the Login Credentials of the user and starts login
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
            progressDialog.setTitle("Unlinking Account");
            progressDialog.show();

            if (!isUnlinked()) {
                AccountTools.unlinkAccount(this, getHelper(), this);

                setUnlinked(true);
                setLoggedIn(false);
                setLoggingIn(false);
                setLoggingIn(false);

                btnUnlinkDevice.setVisibility(View.GONE);
                btnSignIn.setVisibility(View.VISIBLE);
                accountIdEditText.setVisibility(View.VISIBLE);
                passwordEditText.setVisibility(View.VISIBLE);
                emailEditText.setVisibility(View.VISIBLE);
            }

            progressDialog.hide();

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
            mLogin = new Login(context, getHelper(), accountId, email, password);
        }
    }

    private void LogInAccount(Server server) throws LoginException {
        if (mLogin != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mLogin.startLoginAccount(server);
        }
    }

    private void LogInUser(Server server) throws LoginException {
        if (mLogin != null) {
            mLogin.startLoginUser(server);
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
        return isAutoUpdate;
    }

    public String getDefaultBranch() {
        return mDefaultBranch;
    }

    public Server getServer() {return mServer;}

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

    public void setServer(Server server){
        this.mServer = server;
    }

    private void setmDefaultBranch(String branchName) {
        mDefaultBranch = branchName;
    }

    @Override
    public void onStop() {
        stopLogin();
        if (isUnlinked() && !isLoggedIn() && isLoggingIn()) {
            if (mLogin != null) {
                mLogin.onStop();
            }
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopLogin();
        if (isUnlinked && !isLoggedIn) {
            if (mLogin != null) {
                mLogin.onStop();
            }
        }
        super.onDestroy();
    }
}