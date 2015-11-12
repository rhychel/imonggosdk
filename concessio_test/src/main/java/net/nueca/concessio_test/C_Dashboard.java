package net.nueca.concessio_test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.dialogs.SimplePulloutRequestDialog;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymart on 8/21/15.
 * imonggosdk2 (c)2015
 */
public class C_Dashboard extends ImonggoAppCompatActivity {

    private Button btnSales, btnOrder, btnCount, btnReceive, btnPullout, btnUnlink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_dashboard);

        if(!SwableTools.isImonggoSwableRunning(this))
            SwableTools.startSwable(this);

        btnSales = (Button) findViewById(R.id.btnSales);
        btnOrder = (Button) findViewById(R.id.btnOrder);
        btnCount = (Button) findViewById(R.id.btnCount);
        btnReceive = (Button) findViewById(R.id.btnReceive);
        btnPullout = (Button) findViewById(R.id.btnPullout);
        btnUnlink = (Button) findViewById(R.id.btnUnlink);

        btnSales.setOnClickListener(onChooseModule);
        btnOrder.setOnClickListener(onChooseModule);
        btnCount.setOnClickListener(onChooseModule);
        btnReceive.setOnClickListener(onChooseModule);
        btnPullout.setOnClickListener(onChooseModule);
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
                case R.id.btnSales: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.SALES.ordinal());
                } break;
                case R.id.btnOrder: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.ORDERS.ordinal());
                } break;
                case R.id.btnCount: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.PHYSICAL_COUNT.ordinal());
                } break;
                case R.id.btnReceive: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.RECEIVE.ordinal());
                } break;
                case R.id.btnPullout: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.PULLOUT.ordinal());
                } break;
            }
            startActivity(intent);
        }
    };
}
