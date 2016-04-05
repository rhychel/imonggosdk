package net.nueca.concessio;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import net.nueca.concessioengine.activities.login.BaseLoginActivity;
import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.operations.http.HTTPRequests;
import net.nueca.imonggosdk.operations.sync.SyncModules;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.SettingTools;

import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

/**
 * Created by rhymart on 8/20/15.
 * imonggosdk2 (c)2015
 */
public class C_Login extends LoginActivity {

    @Override
    protected void initLoginEquipments() {
        Fabric.with(this, new Crashlytics());
        super.initLoginEquipments();
        setServer(Server.REBISCO);
        setAutoUpdateApp(false);
        /**
         *"payment_types"
         */
        setRequireConcessioSettings(true);
        setRequireObjectConcessioSettings(true);
    }

    @Override
    protected void updateAppData(SyncModules syncmodules) {
        super.updateAppData(syncmodules);
        int[] modulesToDownload = generateModules();
        setModulesToSync(modulesToDownload);
        syncmodules.initializeTablesToSync(modulesToDownload);
        updateApp();

        Log.e(TAG, "updateAppData called");
    }

    @Override
    protected void successLogin() {
        super.successLogin();
        int []modulesToDownload = generateModules();
        if(modulesToDownload.length == 0) {
            DialogTools.showDialog(this, "Ooops!", "Kindly contact admin to setup your Concessio.", "Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    unlinkAccount();
                }
            }, R.style.AppCompatDialogStyle_Light);
            return;
        }
        setModulesToSync(modulesToDownload);
        getSyncModules().initializeTablesToSync(modulesToDownload);
    }

    @Override
    protected void showNextActivityAfterLogin() {
        finish();
        Intent intent = new Intent(this, (SettingTools.defaultBranch(this).equals("") ? C_Welcome.class : C_Dashboard.class));//C_Dashboard
        startActivity(intent);
    }

    private int[] generateModules() {
        Log.e("generateModules", "here");
        getSyncModules().setSyncAllModules(false);

        ModuleSetting app = ModuleSetting.fetchById(getHelper(), ModuleSetting.class, "app");
        if(app == null) {
            Log.e("App", "nothing is defined");
            return new int[0];
        }
        else
            Log.e("App", app.getSequences().size()+"---");

        return app.modulesToDownload(getHelper(), app.isShow_only_sellable_products());
                /*return new int[]{Table.USERS_ME.ordinal(),
                Table.BRANCH_USERS.ordinal(), Table.SETTINGS.ordinal(),
                Table.UNITS.ordinal(), Table.BRANCH_PRODUCTS.ordinal()};*/
    }



    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();
        setContentView(R.layout.c_login);

        Log.e("Unlinked", AccountTools.isUnlinked(this)+"---");
        initializeApp();

        BaseLoginActivity.TEST_ACCOUNT = true;

        setupLayoutEquipments((EditText)findViewById(R.id.etAccountId),
                (EditText)findViewById(R.id.etEmail),
                (EditText)findViewById(R.id.etPassword),
                (Button)findViewById(R.id.btnLogin));
    }

    private void initializeApp() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(HTTPRequests.sendGETServers(this, new VolleyRequestListener() {
            @Override
            public void onStart(Table table, RequestType requestType) {
                net.nueca.imonggosdk.dialogs.DialogTools.showIndeterminateProgressDialog(C_Login.this, null, "Preparing your app...", false, R.style.AppCompatDialogStyle_Light_NoTitle);
            }

            @Override
            public void onSuccess(Table table, RequestType requestType, Object response) {
                Toast.makeText(C_Login.this, "Success!", Toast.LENGTH_SHORT).show();
                net.nueca.imonggosdk.dialogs.DialogTools.hideIndeterminateProgressDialog();
                Log.e("JSON", response.toString());
                SettingTools.updateSettings(C_Login.this, SettingsName.SERVERS, ((JSONObject) response).toString());
            }

            @Override
            public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                net.nueca.imonggosdk.dialogs.DialogTools.hideIndeterminateProgressDialog();
                DialogTools.showConfirmationDialog(C_Login.this, "Ooops!", "Cannot prepare your app. Kindly check your internet connection.", "Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initializeApp();
                    }
                }, "Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, R.style.AppCompatDialogStyle_Light);
            }

            @Override
            public void onRequestError() {
                net.nueca.imonggosdk.dialogs.DialogTools.hideIndeterminateProgressDialog();
                DialogTools.showConfirmationDialog(C_Login.this, "Ooops!", "Cannot prepare your app. Kindly check your internet connection.", "Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initializeApp();
                    }
                }, "Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, R.style.AppCompatDialogStyle_Light);
            }
        }));
    }
}