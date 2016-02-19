package net.nueca.concessio_test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.toolbox.Volley;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.dialogs.SimplePulloutRequestDialog;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.RequestType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.interfaces.VolleyRequestListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.operations.http.ImonggoOperations;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.LoggingTools;

import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymart on 8/21/15.
 * imonggosdk2 (c)2015
 */
public class C_Dashboard extends ImonggoAppCompatActivity {

    private static String TAG = "C_Dashboard";
    private Button btnSales, btnOrder, btnCount, btnReceive, btnPullout, btnUnlink;
    private Button btnConcessio;

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
        btnConcessio = (Button) findViewById(R.id.btnConcessio);

        btnSales.setOnClickListener(onChooseModule);
        btnOrder.setOnClickListener(onChooseModule);
        btnCount.setOnClickListener(onChooseModule);
        btnReceive.setOnClickListener(onChooseModule);
        btnPullout.setOnClickListener(onChooseModule);
        btnConcessio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        }

                        @Override
                        public void onError(Table table, boolean hasInternet, Object response, int responseCode) {
                            Log.e("onDiscoveryError", "hasInternet="+hasInternet+" || responseCode="+responseCode);
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


        List<Product> products = Product.fetchAll(getHelper(), Product.class);

        if(products != null) {
            for(Product p: products) {
                Log.e(TAG, "Product: " + p.getName() + " status: " + p.getStatus());

                if(p.getExtras()==  null){
                    Log.e(TAG, "Extras is null");
                } else {
                    Log.e(TAG, "Product Extras: " + p.getExtras().toString());
                }
            }
        } else {
            Log.e(TAG, "Products is null");
        }

        List<BranchProduct> branchProducts = BranchProduct.fetchAll(getHelper(), BranchProduct.class);

        if(products != null) {
            for(BranchProduct bp: branchProducts) {
                Log.e(TAG,"Branch Product: " +  bp.getName() + " status: " + bp.getProduct().getStatus());
                if(bp.getProduct().getExtras() ==  null){
                    Log.e(TAG, "Extras is null");
                } else {
                    Log.e(TAG, "Product Extras: " + bp.getProduct().getExtras().toString());
                }
            }
        } else {
            Log.e(TAG, "Branch Products is null");
        }

    }

    private View.OnClickListener onChooseModule = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(C_Dashboard.this, C_Module.class);
            switch(view.getId()) {
                case R.id.btnSales: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.INVOICE.ordinal());
                } break;
                case R.id.btnOrder: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.STOCK_REQUEST.ordinal());
                } break;
                case R.id.btnCount: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.PHYSICAL_COUNT.ordinal());
                } break;
                case R.id.btnReceive: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.RECEIVE_BRANCH.ordinal());
                } break;
                case R.id.btnPullout: {
                    intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.RELEASE_BRANCH.ordinal());
                } break;
            }
            startActivity(intent);
        }
    };
}
