package net.nueca.concessio;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import net.nueca.concessioengine.activities.login.BaseLoginActivity;
import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.concessioengine.printer.epson.tools.EpsonPrinterTools;
import net.nueca.concessioengine.printer.starmicronics.tools.StarIOPrinterTools;
import net.nueca.concessioengine.tools.appsettings.AppTools;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.operations.http.HTTPRequests;
import net.nueca.imonggosdk.operations.sync.SyncModules;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.SettingTools;

import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

/**
 * Created by rhymart on 8/20/15.
 * imonggosdk2 (c)2015
 */
public class C_Login extends LoginActivity {

    private int hiddenTaps = 0;
    private Spinner spServers;

    @Override
    protected void initLoginEquipments() {
        Fabric.with(this, new Crashlytics());
        super.initLoginEquipments();

        setAutoUpdateApp(true); // should have a settings

        setRequireConcessioSettings(true);
        setRequireObjectConcessioSettings(true);
    }

    @Override
    protected void updateAppData(SyncModules syncmodules) {
        super.updateAppData(syncmodules);
        try {
           if(getSession().getServer() == Server.REBISCO_DEV) {
                int[] modulesToDownload = {Table.SETTINGS.ordinal()};
                setModulesToSync(modulesToDownload);
                syncmodules.initializeTablesToSync(modulesToDownload);
            }
            else {
                int[] modulesToDownload = generateModules();
                setModulesToSync(modulesToDownload);
                syncmodules.initializeTablesToSync(modulesToDownload);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    protected void forceUnlinkUser() {
        super.forceUnlinkUser();

        DialogTools.showDialog(this, "Ooopps!", "Your account has been disabled.", "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    AccountTools.unlinkAccount(C_Login.this, getHelper(), new AccountListener() {
                        @Override
                        public void onLogoutAccount() {
                        }

                        @Override
                        public void onUnlinkAccount() {
                            EpsonPrinterTools.clearTargetPrinter(C_Login.this);
                            StarIOPrinterTools.updateTargetPrinter(C_Login.this, "");
                            SwableTools.stopSwable(C_Login.this);
                            SettingTools.updateSettings(C_Login.this, SettingsName.DEFAULT_BRANCH, "");

                            finish();
                            Intent intent = new Intent(C_Login.this, C_Login.class);
                            startActivity(intent);
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, R.style.AppCompatDialogStyle_Light);
    }

    @Override
    protected void showNextActivityAfterLogin() {
        if(!AccountTools.isUserActive(this))
            forceUnlinkUser();
        else {
            finish();
            Intent intent = new Intent(this, (SettingTools.defaultBranch(this).equals("") ? C_Welcome.class : C_Dashboard.class));//C_Dashboard
            startActivity(intent);
        }
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
    }

    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();
        setIsUsingDefaultLoginLayout(false);
        setContentView(R.layout.c_login);

        ((TextView)findViewById(R.id.tvVersion)).setText("Version: "+AppTools.getAppVersionName(this));

        Log.e("Unlinked", AccountTools.isUnlinked(this)+"---");
        initializeApp();

        BaseLoginActivity.TEST_ACCOUNT = false;

        setupLayoutEquipments((EditText)findViewById(R.id.etAccountId),
                (EditText)findViewById(R.id.etEmail),
                (EditText)findViewById(R.id.etPassword),
                (Button)findViewById(R.id.btnLogin));

        spServers = (Spinner) findViewById(R.id.spServers);
        ImageView ivLogo = (ImageView) findViewById(R.id.ivLogo);

        final ArrayList<Server> servers = new ArrayList<Server>(){{
            add(Server.IMONGGO);
            add(Server.IMONGGO_NET);
            add(Server.IRETAILCLOUD_COM);
            add(Server.IRETAILCLOUD_NET);
            add(Server.PETRONDIS_COM);
            add(Server.PETRONDIS_NET);
            add(Server.REBISCO_DEV);
            add(Server.REBISCO_LIVE);
            add(Server.REBISCO_LIVE_NET);
        }};
        ArrayAdapter<Server> serversAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_light, servers);
        serversAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        spServers.setAdapter(serversAdapter);
        spServers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(useCustomServer)
                    setServer(servers.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hiddenTaps == 8)
                    Toast.makeText(C_Login.this, "Almost there!", Toast.LENGTH_SHORT).show();
                if(hiddenTaps == 10) {
                    spServers.setVisibility(View.VISIBLE);
                    useCustomServer = true;
                }
                hiddenTaps++;
            }
        });
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