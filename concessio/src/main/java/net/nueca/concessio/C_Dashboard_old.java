package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.nueca.concessioengine.activities.DashboardActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymart on 8/21/15.
 * imonggosdk2 (c)2015
 */
public class C_Dashboard_old extends DashboardActivity {

    private Button btnOrder, btnCount, btnReceive, btnUnlink;
    private Button btnInventory, btnPullout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_dashboard_old);

        if(!SwableTools.isImonggoSwableRunning(this))
            SwableTools.startSwable(this);

        btnOrder = (Button) findViewById(R.id.btnOrder);
        btnCount = (Button) findViewById(R.id.btnCount);
        btnReceive = (Button) findViewById(R.id.btnReceive);
        btnUnlink = (Button) findViewById(R.id.btnUnlink);
        btnInventory = (Button) findViewById(R.id.btnInventory);
        btnPullout = (Button) findViewById(R.id.btnPullout);

        try {
            List<ModuleSetting> moduleSettings = getHelper().fetchObjectsList(ModuleSetting.class);
            for(ModuleSetting moduleSetting : moduleSettings) {
                if(moduleSetting.getLabel() != null) {
                    Log.e("moduleSetting[label]", moduleSetting.getLabel());
                    if(moduleSetting.is_enabled()) {
                        switch (moduleSetting.getModuleType()) {
                            case ORDERS: {
                                btnOrder.setVisibility(View.VISIBLE);
                            }
                            break;
                            case PHYSICAL_COUNT: {
                                btnCount.setVisibility(View.VISIBLE);
                            }
                            break;
                            case RECEIVE: {
                                btnReceive.setVisibility(View.VISIBLE);
                            }
                            break;
                            case INVENTORY: {
                                btnInventory.setVisibility(View.VISIBLE);
                            }
                            break;
                            case PULLOUT_REQUEST: {
                                btnPullout.setVisibility(View.VISIBLE);
                            }
                            break;
                        }
                    }
                }
                else
                    Log.e("moduleSetting[app]", moduleSetting.getModule_type());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        btnOrder.setTag(ConcessioModule.ORDERS);
        btnCount.setTag(ConcessioModule.PHYSICAL_COUNT);
        btnReceive.setTag(ConcessioModule.RECEIVE);
        btnInventory.setTag(ConcessioModule.INVENTORY);
        btnPullout.setTag(ConcessioModule.PULLOUT_REQUEST);
        setNextActivityClass(C_Module.class);

        btnUnlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AccountTools.unlinkAccount(C_Dashboard_old.this, getHelper(), new AccountListener() {
                        @Override
                        public void onLogoutAccount() {
                        }

                        @Override
                        public void onUnlinkAccount() {
                            finish();
                            Intent intent = new Intent(C_Dashboard_old.this, C_Login.class);
                            startActivity(intent);
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected Bundle addExtras(ConcessioModule concessioModule) {
        return null;
    }
}
