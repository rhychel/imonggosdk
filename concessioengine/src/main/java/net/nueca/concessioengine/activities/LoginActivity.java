package net.nueca.concessioengine.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import net.nueca.concessioengine.R;
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
import net.nueca.imonggosdk.tools.LoggingTools;
import net.nueca.imonggosdk.tools.LoginTools;

import java.sql.SQLException;

/**
 * Abstract Class LoginActivity
 * created by Jn on 06/16/15
 */
public abstract class LoginActivity extends ImonggoAppCompatActivity implements AccountListener{

    private Session mSession = null;
    private Boolean isUnlinked;
    private Boolean isLoggingIn;
    private Boolean isloginSuccessful;
    protected Login mLogin;

    protected abstract void initActivity(String message);
    protected abstract void beforeLogin();
    protected abstract void stopLogin();
    protected abstract void afterLogin();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Jn-LoginActivity", "onCreate LoginActivity");

        initEquipments();
        initActivity("Message from parent");
        loginChecker();
        createLayout();
    }

    protected void updateData() {
        Log.i("Jn-Login", "updating data parent");
    }

    /**
     * Sets the default login layout.
     *
     * Note: Do not call super.createLayout() in subclass activity
     * when Implementing customlayout.
     */
    protected void createLayout() {
        Log.i("Jn-LoginActivity", "createLayout");
        setContentView(R.layout.concessioengine_login);
    }

    // Method which will initialize everything
    private void initEquipments() {
        isUnlinked = true;
        isloginSuccessful = false;
        isLoggingIn = false;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Methods which checks if someone is loggedIn
    private void loginChecker() {
        try {
            if (AccountTools.isLoggedIn(getHelper())) {
                mSession = getHelper().getSessions().queryForAll().get(0);
                isUnlinked = false;

                //check if user has authentication (loggedIn)
                if (!mSession.getApiAuthentication().equals("")) {
                    isloginSuccessful = true;
                    // check if sessions email exist in user's database
                    if (getHelper().getUsers().queryBuilder().where().eq("email", mSession.getEmail()).query().size() == 0) {
                        LoggingTools.showToast(this, getString(R.string.LOGIN_USER_DONT_EXIST));
                        // TODO: Offline Data in unlinkAccount
                        // AccountTools.unlinkAccount(this, getHelper(), this, DELETEALL?);
                        unlinkAccount();
                        return;
                    }
                    isLoggingIn = true;
                    updateData();
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logoutUser() {
        try {
            AccountTools.logoutUser(this, getHelper(), this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unlinkAccount() {
        AccountTools.setUnlinked(this, true);
        try {
            AccountTools.unlinkAccount(this, getHelper(), this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUnlinkAccount(boolean unlink) {
        AccountTools.setUnlinked(this, unlink);
    }

    /**
     * Sets the Login Credentials of the user and start login
     *
     * @param context   a context
     * @param accountId must not be empty
     * @param email     must not be empty and matches the correct form 'email@me.com'
     * @param password  must not be empty and length must be greater or equal to five (5)
     * @param server server
     */
    protected void startLogin(Context context, String accountId, String email, String password, Server server) {
        try {
            setLoggingIn(true);
            setLoginCredentials(context, accountId, email, password);
            setLoginListeners();
            if(isUnlinked) {
                startLoginAccount(server);
            } else {
                startLoginUser(server);
            }
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    private void setLoginCredentials(Context context, String accountId, String email, String password) throws LoginException {
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

    private void setLoginListeners(){
        mLogin.setLoginListener(new LoginListener() {
            @Override
            public void onStartLogin() {
                Log.i("Jn-LoginActivity", "onStartLogin");
                beforeLogin();

            }
            @Override
            public void onLoginSuccess(Session session) {
                Log.i("Jn-LoginActivity", "onLoginSuccess");
                mSession = session;
                afterLogin();
            }
            @Override
            public void onStopLogin() {
                stopLogin();
                Log.i("Jn-LoginActivity", "onStopLogin");
                setLoggingIn(false);
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
                Log.e("Jn-LoginActivity", "onErrorVolley message: " + response.toString() );
                setLoggingIn(true);
            }

            @Override
            public void onRequestError() {
                Log.e("Jn-LoginActivity", "onRequestError");
                if(mSession != null) {
                    if(AccountTools.isUnlinked(LoginActivity.this)) {
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

    private void startLoginAccount(Server server) throws LoginException {
        if (mLogin != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mLogin.startLoginAccount(server);
        }
    }

    private void startLoginUser(Server server) throws LoginException {
        if(mLogin != null) {
            mLogin.startLoginUser(server);
        }
    }


    public Boolean getIsloginSuccessful() {
        return isloginSuccessful;
    }

    public void setLoginSuccessful(Boolean isloginSuccessful) {
        this.isloginSuccessful = isloginSuccessful;
    }

    public Boolean getIsLoggingIn() {
        return isLoggingIn;
    }

    public void setLoggingIn(Boolean isLoggingIn) {
        this.isLoggingIn = isLoggingIn;
    }

    public Boolean getIsUnlinked() {
        return isUnlinked;
    }

    public void setUnlinked(Boolean isUnlinked) {
        this.isUnlinked = isUnlinked;
    }

    @Override
    public void onStop() {
        stopLogin();
        if (getIsUnlinked() && !getIsloginSuccessful() && getIsLoggingIn()) {
            if (mLogin != null) {
                mLogin.onStop();
            }
        }
        super.onStop();
    }

    @Override
    public void onDestroy(){
        stopLogin();
        if (isUnlinked && !isloginSuccessful) {
            if (mLogin != null) {
                mLogin.onStop();
            }
        }
        super.onDestroy();
    }
}