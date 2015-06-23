
package net.nueca.imonggosdk.operations;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.nueca.imonggosdk.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.exception.LoginException;
import net.nueca.imonggosdk.interfaces.LoginListener;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.LoggingTools;
import net.nueca.imonggosdk.tools.LoginTools;
import net.nueca.imonggosdk.tools.NetworkTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Login.java
 * imonggosdk (c)2015
 *
 * @author Jn Cld
 * @since 6/8/2015
 */
public class Login {

    private static final String LOGIN_TAG = "login_tag";

    private String mAccountId;
    private String mEmail;
    private String mPassword;
    private Context mContext;
    private Session mSession;

    private Boolean mConcessioSettings = false;

    private ImonggoDBHelper mDBHelper;
    private RequestQueue mRequestQueue;
    private LoginListener mLoginListener;
    private VolleyRequestListener mVolleyRequestListener;

    /**
     * Default Constructor
     */
    public Login() {
        mSession = null;
        mAccountId = "";
        mEmail = "";
        mPassword = "";
    }

    /**
     * Creates login credentials with the given accountId, email, password
     * and setup Volley Request Queue
     *
     * @param context   A context
     * @param dbHelper  Database Helper for OrmLite
     * @param accountId Account Id for every user
     * @param email     Unique email address of the user
     * @param password  Password of the user
     * @throws net.nueca.imonggosdk.exception.LoginException if accountId, email and password is null or invalid
     */
    public Login(Context context, ImonggoDBHelper dbHelper, String accountId, String email,
                 String password) throws LoginException {
        this.mRequestQueue = Volley.newRequestQueue(context);
        this.mContext = context;
        this.mDBHelper = dbHelper;
        this.mAccountId = accountId;
        this.mEmail = email;
        this.mPassword = password;

        try {
            if (AccountTools.isLoggedIn(dbHelper)) {
                mSession = dbHelper.getSessions().queryForAll().get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns User Account Id
     *
     * @return mAccountId
     */
    public String getAccountId() {
        return mAccountId;
    }

    /**
     * Sets the Account Id
     *
     * @param accountId of the User.
     * @throws net.nueca.imonggosdk.exception.LoginException If the mAccountId is null
     */
    public void setAccountId(String accountId) throws LoginException {
        if (TextUtils.isEmpty(accountId)) {
            throw new LoginException(mContext.getString(R.string.LOGIN_FIELD_REQUIRED));
        } else {
            this.mAccountId = accountId;
        }
    }

    /**
     * Returns User's Email Address
     *
     * @return mEmail
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Sets the Email Address
     *
     * @param email email of the user
     * @throws net.nueca.imonggosdk.exception.LoginException if the Email is null and invalid
     */
    public void setEmail(String email) throws LoginException {

        if (TextUtils.isEmpty(email)) {
            throw new LoginException(mContext.getString(R.string.LOGIN_FIELD_REQUIRED));
        } else if (!LoginTools.isValidEmail(email)) {
            throw new LoginException(mContext.getString(R.string.LOGIN_INVALID_EMAIL));
        } else {
            this.mEmail = email;
        }
    }

    /**
     * Returns Password of the User
     *
     * @return mPassword
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * Sets the Password
     *
     * @param mPassword password of the user
     * @throws net.nueca.imonggosdk.exception.LoginException if the password is null and invalid it
     */
    public void setPassword(String mPassword) throws LoginException {
        if (TextUtils.isEmpty(mPassword)) {
            throw new LoginException(mContext.getString(R.string.LOGIN_FIELD_REQUIRED));
        } else if (!LoginTools.isValidPassword(mPassword)) {
            throw new LoginException(mContext.getString(R.string.LOGIN_INVALID_PASSWORD));
        } else {
            this.mPassword = mPassword;
        }
    }

    /**
     * Sets the context
     *
     * @param context
     */
    public void setContext(Context context) {
        if (context != null) {
            this.mContext = context;
        }
    }

    /**
     * Starts request to get Account URL
     *
     * @param server
     */
    public void startLoginAccount(Server server) {
        if (mLoginListener != null) {
            mLoginListener.onStartLogin();
        }
        requestForAccountUrl(server);
        mRequestQueue.start();
    }

    /**
     * Requests for Account URL and saves it to database for later use
     *
     * @param server
     */
    private void requestForAccountUrl(final Server server) {
        StringRequest stringRequestURL = new StringRequest(Request.Method.GET,
                LoginTools.getAPIUrl(mContext, server, mAccountId), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Create Session and save the string response (URL)
                mSession = new Session();
                mSession.setAccountId(mAccountId);
                mSession.setEmail(mEmail);
                mSession.setPassword(mPassword);
                mSession.setAccountUrl(response);

                // Insert Session to Database
                mSession.insertTo(mDBHelper);

                // Update Volley Listener
                if (mVolleyRequestListener != null) {
                    mVolleyRequestListener.onSuccess(Table.SESSIONS,
                            RequestType.API_CONTENT, response);
                }

                // show Toast Message
                Log.i("Jn-Login","Account URL Request Successful");

                // Login User | Request for token
                startLoginUser(server);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // Execute Listener onStopLogin
                if (mLoginListener != null) {
                    mLoginListener.onStopLogin();
                }
                // if account id is invalid
                if (volleyError.networkResponse != null) {
                    LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_FAILED_ACCOUNT_ID));
                } else { // if URL is invalid, or not connected to internet.
                    // OFFLINE
                    if (!NetworkTools.isInternetAvailable(mContext)) {
                        LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_NETWORK_ERROR));
                    } else { // AUTHENTICATION ERROR
                        LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_AUTHENTICATION_ERROR));
                    }
                }
                // Execute Volley Request Listener onRequestError
                if (mVolleyRequestListener != null) {
                    mVolleyRequestListener.onRequestError();
                }
            }
        });
        stringRequestURL.setTag(LOGIN_TAG);
        mRequestQueue.add(stringRequestURL);

        Log.i("Jn-Login", "Requesting for Account URL");
    }

    /**
     * Starts request for User Token
     *
     * @param server
     */
    public void startLoginUser(Server server) {
        requestForApiToken(server);
        mRequestQueue.start();
    }

    /**
     * Request for User API Token and saves it to database
     * also request for POS Device ID
     *
     * @param server
     */
    private void requestForApiToken(final Server server) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(JsonObjectRequest.Method.GET,
                ImonggoTools.buildAPITokenUrl(mContext, getSession().getAccountUrl(), Table.TOKENS,
                        getEmail(), getPassword()), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                if (response.toString().trim().equals("")) {
                    // If Listener is not null update the listener onStopLogin
                    if (mLoginListener != null) {
                        mLoginListener.onStopLogin();
                    }
                    // Show Toast
                    LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_INVALID_CREDENTIALS) + ", " + mContext.getString(R.string.LOGIN_INVALID_EMAIL_PASSWORD));
                    // if Account Unlinked delete the session
                    if (AccountTools.isUnlinked(mContext)) {
                        mSession.deleteTo(mDBHelper);
                    }
                    return;
                }
                // if Account is Unlinked
                if (!AccountTools.isUnlinked(mContext)) {
                    Session tempSession = new Session();
                    tempSession.setAccountId(getSession().getAccountId());
                    tempSession.setAccountUrl(getSession().getAccountUrl());
                    tempSession.setEmail(getSession().getEmail());
                    tempSession.setPassword(getSession().getPassword());
                    tempSession.setDevice_id(getSession().getDevice_id());

                    getSession().deleteTo(mDBHelper);

                    try {
                        mSession = new Session();
                        mSession.setAccountId(tempSession.getAccountId());
                        mSession.setAccountUrl(tempSession.getAccountUrl());
                        mSession.setEmail(tempSession.getEmail());
                        mSession.setApiToken(response.getString("api_token"));
                        mSession.setApiAuthentication(ImonggoTools.buildAPIAuthentication(response.getString("api_token")));
                        mSession.setPassword(tempSession.getPassword());
                        mSession.setDevice_id(tempSession.getDevice_id());

                        // Insert Session to Database
                        mSession.insertTo(mDBHelper);

                        Log.i("Jn-Login", "session inserted to database");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else { // if Account is linked
                    try {
                        // set the response token
                        mSession.setApiToken(response.getString("api_token"));
                        mSession.setApiAuthentication(ImonggoTools.buildAPIAuthentication(response.getString("api_token")));

                        // update the session in the database
                        mSession.updateTo(mDBHelper);
                        Log.i("Jn-Login", "session updated in the database");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Get POS DEVICES ID

                // if session don't have device id
                if (mSession.getDevice_id() == 0) {

                    requestForPOSDeviceID(server);
                    mRequestQueue.start();

                    if (mLoginListener != null) {
                        mLoginListener.onLoginSuccess(mSession);
                        Log.i("Jn-Login","API Token Request Successful");
                    }
                    if (mVolleyRequestListener != null) {
                        mVolleyRequestListener.onSuccess(Table.TOKENS, RequestType.LOGIN, response);
                    }

                } else {
                    if (mLoginListener != null) {
                        mLoginListener.onLoginSuccess(mSession);
                        Log.i("Jn-Login", "API Token Request Successful");
                    }
                    if (mVolleyRequestListener != null) {
                        mVolleyRequestListener.onSuccess(Table.TOKENS, RequestType.LOGIN, response);
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Jn-Login", "onErrorResponse : " + error.getMessage());

                // Update the Login Listener
                if (mLoginListener != null) {
                    mLoginListener.onStopLogin();
                }

                // if we have network connection but have error
                if (error.networkResponse != null) {

                    // if no Account has been linked)
                    if (AccountTools.isUnlinked(mContext)) {
                        mSession.deleteTo(mDBHelper);
                    }

                    // Show Toast Message
                    LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_INVALID_EMAIL_PASSWORD));

                    // Update the Volley Request Listener
                    if (mVolleyRequestListener != null) {
                        mVolleyRequestListener.onError(Table.TOKENS, true,
                                new String(error.networkResponse.data), error.networkResponse.statusCode);
                    }
                } else { // invalid url or not connected to a network
                    if (!NetworkTools.isInternetAvailable(mContext)) {
                        LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_NETWORK_ERROR));
                    } else {
                        LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_AUTHENTICATION_ERROR));
                    }
                }
            }
        });

        jsonObjectRequest.setTag(LOGIN_TAG);
        mRequestQueue.add(jsonObjectRequest);

        Log.i("Jn-Login", "Requesting for Token");
    }

    public void requestForPOSDeviceID(Server server){
        ImonggoOperations.sendPOSDevice(mContext, mRequestQueue, mSession, new VolleyRequestListener() {
            @Override
            public void onStart(Table table, RequestType requestType) {
                Log.i("Jn_Requesting for", "POS Device ID");
            }

            @Override
            public void onSuccess(Table table, RequestType requestType, Object response) {
                try {
                    Log.i("Jn-Login","POS DEVICE ID Request Successful");

                    // Get the response and save it to mSession object
                    JSONObject pos_device = (JSONObject) response;
                    int id = pos_device.getInt("id");
                    mSession.setDevice_id(id);

                    // Update the database
                    mSession.updateTo(mDBHelper);

                    // if using concessio Settings
                    if (mConcessioSettings) {
                        Log.i("Jn-Login", "Using Concession Settings");
                        // CODE
                    } else { // not using Concession Settings
                        Log.i("Jn-Login", "Not Using Concession Settings");

                        // Update the Listener
                        if (mLoginListener != null) {
                            mLoginListener.onLoginSuccess(mSession);
                        }

                        if (mVolleyRequestListener != null) {
                            mVolleyRequestListener.onSuccess(Table.TOKENS, RequestType.POST, response);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                if (mLoginListener != null) {
                    mLoginListener.onStopLogin();
                }
            }

            @Override
            public void onRequestError() {
                if (mLoginListener != null) {
                    mLoginListener.onStopLogin();
                }
            }
        }, server);
    }

    /**
     * Returns current session
     *
     * @return mSession
     */
    public Session getSession() {
        return mSession;
    }

    /**
     * Returns VolleyRequestListener instance
     *
     * @return mVolleyRequestListener
     */
    public VolleyRequestListener getVolleyRequestListener() {
        return mVolleyRequestListener;
    }

    /**
     * Sets the VolleyRequestListener
     *
     * @param mVolleyRequestListener request listener instance
     */
    public void setVolleyRequestListener(VolleyRequestListener mVolleyRequestListener) {
        this.mVolleyRequestListener = mVolleyRequestListener;
    }

    /**
     * Returns ConcessioSettings, TRUE if using concessio settings, FALSE otherwise
     *
     * @return mConcessioSettings
     */
    public Boolean getConcessioSettings() {
        return mConcessioSettings;
    }

    /**
     * Set TRUE to use Concessio Setting, FALSE otherwise
     *
     * @param mConcessioSettings true or false
     */
    public void setConcessioSettings(Boolean mConcessioSettings) {
        this.mConcessioSettings = mConcessioSettings;
    }

    /**
     * Returns Loginlistener instance
     *
     * @return mLoginListener
     */
    public LoginListener getLoginListener() {
        return mLoginListener;
    }

    /**
     * Sets LoginListener
     *
     * @param mLoginListener listener for Login
     */
    public void setLoginListener(LoginListener mLoginListener) {
        this.mLoginListener = mLoginListener;
    }

    public void onStop() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(LOGIN_TAG);
            mDBHelper.deleteAllDatabaseValues();
        }
    }
}