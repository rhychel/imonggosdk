package net.nueca.imonggosdk.operations.login;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.nueca.imonggosdk.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.dialogs.DialogTools;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.exception.LoginException;
import net.nueca.imonggosdk.interfaces.LoginListener;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.AccountSettings;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.LoginTools;
import net.nueca.imonggosdk.tools.NetworkTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * BaseLogin.java
 * imonggosdk (c)2015
 *
 * @author Jn Cld
 * @since 6/8/2015
 */
public class BaseLogin {
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

    /**
     * Default Constructor
     */
    public BaseLogin() {
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
    public BaseLogin(Context context, ImonggoDBHelper dbHelper, String accountId, String email,
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
                mSession.setServer(server);

                // Insert Session to Database
                mSession.insertTo(mDBHelper);


                // show Toast Message
                Log.i("Jn-BaseLogin", "Account URL Request Successful");

                // BaseLogin User | Request for token
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
                    DialogTools.showBasicWithTitle(mContext, mContext.getString(R.string.LOGIN_FAILED_TITLE),
                            mContext.getString(R.string.LOGIN_FAILED_ACCOUNT_ID),
                            mContext.getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
                            new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    dialog.dismiss();
                                }
                            });
                    //LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_FAILED_ACCOUNT_ID));
                } else { // if URL is invalid, or not connected to internet.
                    // OFFLINE
                    if (!NetworkTools.isInternetAvailable(mContext)) {
                        DialogTools.showBasicWithTitle(mContext, mContext.getString(R.string.LOGIN_FAILED_TITLE),
                                mContext.getString(R.string.LOGIN_NETWORK_ERROR),
                                mContext.getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
                                new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        dialog.dismiss();
                                    }
                                });
                        //LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_NETWORK_ERROR));
                    } else { // AUTHENTICATION ERROR
                        DialogTools.showBasicWithTitle(mContext, mContext.getString(R.string.LOGIN_FAILED_TITLE),
                                mContext.getString(R.string.LOGIN_AUTHENTICATION_ERROR),
                                mContext.getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
                                new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        dialog.dismiss();
                                    }
                                });
                        //LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_AUTHENTICATION_ERROR));
                    }
                }
            }
        });
        stringRequestURL.setTag(LOGIN_TAG);
        mRequestQueue.add(stringRequestURL);

        Log.i("Jn-BaseLogin.java", "method: requestForAccountUrl() message: Requesting for Account URL");
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
                    DialogTools.showBasicWithTitle(mContext, mContext.getString(R.string.LOGIN_FAILED_TITLE),
                            mContext.getString(R.string.LOGIN_INVALID_CREDENTIALS) + ", " + mContext.getString(R.string.LOGIN_INVALID_EMAIL_PASSWORD),
                            mContext.getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
                            new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    dialog.dismiss();
                                }
                            });
                    // Show Toast
                    //LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_INVALID_CREDENTIALS) + ", " + mContext.getString(R.string.LOGIN_INVALID_EMAIL_PASSWORD));
                    // if Account Unlinked delete the session
                    if (AccountTools.isUnlinked(mContext)) {
                        mSession.deleteTo(mDBHelper);
                    }
                    return;
                }
                // if Account is Linked
                if (!AccountTools.isUnlinked(mContext)) {
                    Session tempSession = new Session();
                    tempSession.setAccountId(getSession().getAccountId());
                    tempSession.setAccountUrl(getSession().getAccountUrl());
                    tempSession.setEmail(getSession().getEmail());
                    tempSession.setPassword(getSession().getPassword());
                    tempSession.setDevice_id(getSession().getDevice_id());
                    tempSession.setServer(getSession().getServer());

                    getSession().deleteTo(mDBHelper);

                    try {
                        mSession = new Session();
                        getSession().setAccountId(tempSession.getAccountId());
                        getSession().setAccountUrl(tempSession.getAccountUrl());
                        getSession().setEmail(tempSession.getEmail());
                        getSession().setApiToken(response.getString("api_token"));
                        getSession().setApiAuthentication(ImonggoTools.buildAPIAuthentication(response.getString("api_token")));
                        getSession().setPassword(tempSession.getPassword());
                        getSession().setDevice_id(tempSession.getDevice_id());

                        // Insert Session to Database
                        mSession.insertTo(mDBHelper);
                        Log.i("Jn-BaseLogin", "API Token Request Successful");
                        Log.i("Jn-BaseLogin", "session inserted to database");
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
                        Log.i("Jn-BaseLogin", "API Token Request Successful");
                        Log.i("Jn-BaseLogin", "session updated in the database");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Get POS DEVICES ID

                // if session don't have device id
                if (mSession.getDevice_id() == 0) {

                    requestForPOSDeviceID(server);
                    mRequestQueue.start();
                } else {
                    if (mLoginListener != null) {
                        mLoginListener.onLoginSuccess(mSession);
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Jn-BaseLogin", "onErrorResponse : " + error.getMessage());

                // Update the BaseLogin Listener
                if (mLoginListener != null) {
                    mLoginListener.onStopLogin();
                }

                // if we have network connection but have error
                if (error.networkResponse != null) {

                    // if no Account has been linked)
                    if (AccountTools.isUnlinked(mContext)) {
                        mSession.deleteTo(mDBHelper);
                    }

                    DialogTools.showBasicWithTitle(mContext, mContext.getString(R.string.LOGIN_FAILED_TITLE),
                            mContext.getString(R.string.LOGIN_INVALID_EMAIL_PASSWORD),
                            mContext.getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
                            new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    dialog.dismiss();
                                }
                            });
                    // Show Toast Message
                    //LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_INVALID_EMAIL_PASSWORD));
                } else { // invalid url or not connected to a network
                    if (!NetworkTools.isInternetAvailable(mContext)) {
                        DialogTools.showBasicWithTitle(mContext, mContext.getString(R.string.LOGIN_FAILED_TITLE),
                                mContext.getString(R.string.LOGIN_NETWORK_ERROR),
                                mContext.getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
                                new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        dialog.dismiss();
                                    }
                                });
                        //LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_NETWORK_ERROR));
                    } else {
                        DialogTools.showBasicWithTitle(mContext, mContext.getString(R.string.LOGIN_FAILED_TITLE),
                                mContext.getString(R.string.LOGIN_AUTHENTICATION_ERROR),
                                mContext.getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
                                new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        dialog.dismiss();
                                    }
                                });
                        //LoggingTools.showToast(mContext, mContext.getString(R.string.LOGIN_AUTHENTICATION_ERROR));
                    }
                }
            }
        });

        jsonObjectRequest.setTag(LOGIN_TAG);
        mRequestQueue.add(jsonObjectRequest);

        Log.i("Jn-BaseLogin", "Requesting for Token");
    }

    public void requestForPOSDeviceID(final Server server) {
        ImonggoOperations.sendPOSDevice(mContext, mRequestQueue, mSession, new VolleyRequestListener() {
            @Override
            public void onStart(Table table, RequestType requestType) {
                Log.i("Jn_Requesting for", "POS Device ID");
            }

            @Override
            public void onSuccess(Table table, RequestType requestType, Object response) {
                try {
                    Log.i("Jn-BaseLogin", "POS DEVICE ID Request Successful");

                    // Get the response and save it to mSession object
                    JSONObject pos_device = (JSONObject) response;


                    int id = Integer.parseInt(pos_device.get("id").toString());
                    Log.i("_response", "response: " + response.toString() + "  --  pos_device: " + id);
                    getSession().setDevice_id(id);

                    // Update the database
                    mSession.updateTo(mDBHelper);

                    // if using concessio Settings
                    if (mConcessioSettings) {
                        Log.i("Jn-BaseLogin", "Using Concession Settings");
                        // CODE ADDED by Rhy
                        ImonggoOperations.getConcesioAppSettings(mContext,
                                getRequestQueue(), getSession(), new VolleyRequestListener() {
                                    @Override
                                    public void onStart(Table table, RequestType requestType) {
                                        Log.e("Rhy-BaseLogin", "Getting the account-settings now...");
                                        DialogTools.updateMessage("Downloading settings...");
                                    }

                                    @Override
                                    public void onSuccess(Table table, RequestType requestType, Object response) {
                                        JSONArray concesio = (JSONArray) response;
                                        Log.e("Rhy-BaseLogin", concesio.toString());
                                        try {
                                            AccountSettings.initializeApplicationSettings(mContext, concesio.getJSONObject(0));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        DialogTools.hideIndeterminateProgressDialog();
                                        if (mLoginListener != null)
                                            mLoginListener.onLoginSuccess(mSession);
                                    }

                                    @Override
                                    public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                                        DialogTools.hideIndeterminateProgressDialog();
                                        Log.e("Rhy-BaseLogin["+responseCode+"]", (response == null) ? "null" : ((String) response));

                                        DialogTools.showBasicWithTitle(mContext,
                                                mContext.getString(R.string.LOGIN_FAILED_TITLE),
                                                mContext.getString(R.string.LOGIN_CONCESSIO_SETTINGS_ERROR),
                                                mContext.getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
                                                new MaterialDialog.ButtonCallback() {
                                                    @Override
                                                    public void onPositive(MaterialDialog dialog) {
                                                        super.onPositive(dialog);
                                                        dialog.dismiss();
                                                    }
                                                });


                                        if (mLoginListener != null) {
                                            mLoginListener.onStopLogin();
                                        }
                                    }

                                    @Override
                                    public void onRequestError() {
                                        DialogTools.hideIndeterminateProgressDialog();

                                        if (mLoginListener != null) {
                                            mLoginListener.onStopLogin();
                                        }
                                    }
                                }, server, true);
                    } else { // not using Concession Settings
                        Log.i("Jn-BaseLogin", "Not Using Concession Settings");
                        // Update the Listener
                        if (mLoginListener != null) {
                            mLoginListener.onLoginSuccess(mSession);
                            Log.i("Jn-BaseLogin", "API Token Request Successful");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Table table, boolean hasInternet, Object response, int responseCode) {

                DialogTools.showBasicWithTitle(mContext, mContext.getString(R.string.LOGIN_FAILED_TITLE),
                        mContext.getString(R.string.LOGIN_NETWORK_ERROR),
                        mContext.getString(R.string.LOGIN_FAILED_POSITIVE_BUTTON), "", false,
                        new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                dialog.dismiss();
                            }
                        });


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
     * @param mLoginListener listener for BaseLogin
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

    /**
     * <i>Added by Rhy<i/>
     *
     * Return the request queue object from this class.
     *
     * @return <i>mRequestQueue</i>
     */
    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}