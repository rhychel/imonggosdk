package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.Configurations;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymart on 8/21/15.
 * imonggosdk2 (c)2015
 */
public class C_Dashboard extends ImonggoAppCompatActivity {

    private Button btnOrder, btnCount, btnReceive, btnUnlink;
    private Button btnConcessio;

    private Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_dashboard);

//        if(!SwableTools.isImonggoSwableRunning(this))
//            SwableTools.startSwable(this);

        btnOrder = (Button) findViewById(R.id.btnOrder);
        btnCount = (Button) findViewById(R.id.btnCount);
        btnReceive = (Button) findViewById(R.id.btnReceive);
        btnUnlink = (Button) findViewById(R.id.btnUnlink);
        btnConcessio = (Button) findViewById(R.id.btnConcessio);

        try {
            List<ModuleSetting> moduleSettings = getHelper().fetchObjectsList(ModuleSetting.class);
            for(ModuleSetting moduleSetting : moduleSettings) {
                if(moduleSetting.getLabel() != null)
                    Log.e("moduleSetting[label]", moduleSetting.getLabel());
                else
                    Log.e("moduleSetting[app]", moduleSetting.getModule_type());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        btnOrder.setOnClickListener(onChooseModule);
        btnCount.setOnClickListener(onChooseModule);
        btnReceive.setOnClickListener(onChooseModule);
        btnConcessio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
                /**
                 * 6267fdfb17d6ea90916e8230e82be969f316d5d0
                 * retailpos.iretailcloud.net
                 *
                 * getSession()
                 */
                try {
                    ImonggoOperations.getConcesioAppSettings(C_Dashboard.this, Volley.newRequestQueue(C_Dashboard.this), getSession(), new VolleyRequestListener() {
                        @Override
                        public void onStart(Table table, RequestType requestType) {
                            Log.e("onStart", "Concessio Settings");
                        }

                        @Override
                        public void onSuccess(Table table, RequestType requestType, Object response) {
                            JSONObject jsonObject = (JSONObject) response;
                            Log.e("onSuccess", jsonObject.toString());
                            try {
                                getHelper().deleteAllDatabaseValues();
                                for(String key : Configurations.MODULE_KEYS) {
                                    JSONObject module = jsonObject.getJSONObject(key);
                                    ModuleSetting moduleSetting = gson.fromJson(module.toString(), ModuleSetting.class);
                                    moduleSetting.setModule_type(key);
                                    if(key.equals("app")) {
                                        moduleSetting.insertTo(getHelper());
                                    }
                                    else {
                                        moduleSetting.insertTo(getHelper());
                                    }
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            Log.e("onError", "hasInternet=" + hasInternet + " || responseCode=" + responseCode);
                        }

                        @Override
                        public void onRequestError() {
                            Log.e("onRequestError", "Concessio Settings");
                        }
                    }, Server.IRETAILCLOUD_NET, true, true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        btnUnlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AccountTools.unlinkAccount(C_Dashboard.this, getHelper(), new AccountListener() {
                        @Override
                        public void onLogoutAccount() {
                        }

                        @Override
                        public void onUnlinkAccount() {
                            finish();
                            Intent intent = new Intent(C_Dashboard.this, C_Login.class);
                            startActivity(intent);
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private View.OnClickListener onChooseModule = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(C_Dashboard.this, C_Module.class);
            switch(view.getId()) {
                case R.id.btnOrder: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.ORDERS.ordinal());
                } break;
                case R.id.btnCount: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.PHYSICAL_COUNT.ordinal());
                } break;
                case R.id.btnReceive: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.RECEIVE.ordinal());
                } break;
            }
            startActivity(intent);
        }
    };
}
