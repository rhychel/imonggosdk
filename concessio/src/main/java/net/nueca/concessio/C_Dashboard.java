package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.activities.DashboardActivity;
import net.nueca.concessioengine.activities.SettingsActivity;
import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.DashboardRecyclerAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.dialogs.ProgressListDialog;
import net.nueca.concessioengine.dialogs.UpdaterChooserDialog;
import net.nueca.concessioengine.objects.DashboardTile;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.exception.SyncException;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.SalesPushSettings;
import net.nueca.imonggosdk.objects.Settings;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.objects.routeplan.RoutePlanDetail;
import net.nueca.imonggosdk.operations.update.APIDownloader;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.Configurations;
import net.nueca.imonggosdk.tools.LastUpdateAtTools;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 11/23/15.
 */
public class C_Dashboard extends DashboardActivity implements OnItemClickListener {

    private static String TAG = "C_Dashboard";
    private Toolbar tbActionBar;
    private Spinner spBranches;
    private RecyclerView rvModules;
    private RecyclerView.LayoutManager layoutManager;
    final APIDownloader apiDownloader = new APIDownloader(C_Dashboard.this, false);
    private ArrayAdapter<Branch> branchesAdapter;
    private DashboardRecyclerAdapter dashboardRecyclerAdapter;

    private ArrayList<DashboardTile> dashboardTiles = new ArrayList<DashboardTile>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_dashboard);

        Log.e("ClassName", Customer.class.getSimpleName());
//
//        try {
//            getHelper().deleteAll(OfflineData.class);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        /*try {
            List<OfflineData> offlineDatas = getHelper().fetchObjectsList(OfflineData.class);
            for(OfflineData offlineData : offlineDatas) {
                if(offlineData.getOfflineDataTransactionType().equals(OfflineDataType.UPDATE_CUSTOMER))
                    offlineData.deleteTo(getHelper());
            }
            getHelper().deleteAll(OfflineData.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        setNextActivityClass(C_Module.class);

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        rvModules = (RecyclerView) findViewById(R.id.rvModules);
        spBranches = (Spinner) findViewById(R.id.spBranches);
        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_app_logo);

        branchesAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, getBranches());
        branchesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        spBranches.setAdapter(branchesAdapter);

        rvModules.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this, 2);
        rvModules.setLayoutManager(layoutManager);

        if (getModuleSetting(ConcessioModule.CUSTOMERS).isHas_route_plan()) { // REBISCO
            for (ModuleSetting moduleSetting : getActiveModuleSetting(null, true)) {
                if (moduleSetting.getModuleType() == ConcessioModule.INVOICE)
                    dashboardTiles.add(new DashboardTile(ConcessioModule.ROUTE_PLAN, moduleSetting.getLabel()));
                else
                    dashboardTiles.add(new DashboardTile(moduleSetting.getModuleType(), moduleSetting.getLabel()));

            }
            dashboardTiles.add(5, new DashboardTile(ConcessioModule.LAYAWAY, "Layaway", R.drawable.ic_layaway));
        } else { // OTHERS
            for (ModuleSetting moduleSetting : getActiveModuleSetting(null, true)) {
                if (moduleSetting.getModuleType() == ConcessioModule.INVOICE)
                    dashboardTiles.add(new DashboardTile(ConcessioModule.CUSTOMERS, moduleSetting.getLabel(), true, ConcessioModule.INVOICE.getLogo()));
                else
                    dashboardTiles.add(new DashboardTile(moduleSetting.getModuleType(), moduleSetting.getLabel()));

                Log.e("ModuleSettings", moduleSetting.getModule_type() + "=" + moduleSetting.getDisplay_sequence());
            }
        }
        dashboardTiles.add(new DashboardTile(ConcessioModule.HISTORY, "History"));

        dashboardRecyclerAdapter = new DashboardRecyclerAdapter(this, dashboardTiles);
        dashboardRecyclerAdapter.setOnItemClickListener(this);
        rvModules.setAdapter(dashboardRecyclerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        try {
//            Log.e("SESSION", "isNULL? " + (getSession() == null));
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        //if(!SwableTools.isImonggoSwableRunning(this))
        SwableTools.startSwable(this);
        Log.e("SWABLE", "START");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.c_dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mUpdateApp: {
                UpdaterChooserDialog updaterChooserDialog = new UpdaterChooserDialog(this, R.style.AppCompatDialogStyle_Light_NoTitle);
                updaterChooserDialog.setTableToUpdate(getAppSetting().modulesToUpdate(getHelper(), getAppSetting().isShow_only_sellable_products()));
                updaterChooserDialog.setOnTablesSelected(new UpdaterChooserDialog.OnTablesSelected() {
                    @Override
                    public void startUpdate(int[] tables, List<Table> tableList) {
                        final ProgressListDialog progressListDialog = new ProgressListDialog(C_Dashboard.this, tableList);
                        progressListDialog.setCanceledOnTouchOutside(false);
                        progressListDialog.setCancelable(false);

                        apiDownloader.setSyncServer(Server.IRETAILCLOUD_NET);
                        apiDownloader.setSyncModulesListener(new SyncModulesListener() {
                            @Override
                            public void onStartDownload(Table table) {
                                Log.e("apiDownloader", "starting" + table.getStringName());
                                progressListDialog.initDownload(table);
                            }

                            @Override
                            public void onDownloadProgress(Table table, int page, int max) {
                                Log.e("apiDownloader", "progressing" + table + " page=" + page + " | max=" + max);
                                progressListDialog.initDownload(table);
                                progressListDialog.updateProgress(page, max);
                            }

                            @Override
                            public void onEndDownload(Table table) {
                                Log.e("apiDownloader", "end" + table.getStringName());
                                progressListDialog.finishedDownload();
                            }

                            @Override
                            public void onFinishDownload() {
                                Log.e("apiDownloader", "done!");
                                progressListDialog.dismiss();
                                Toast.makeText(C_Dashboard.this, "Update completed!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onErrorDownload(Table table, String message) {
                                Log.e("apiDownloader", "error" + table.getStringName());
                                progressListDialog.errorDownload();
                            }

                            @Override
                            public void onPrepareDialog() {
                                progressListDialog.show();
                            }

                            @Override
                            public void onDismissDialog() {

                            }
                        });

                        progressListDialog.setProgressListener(new ProgressListDialog.ProgressListener() {
                            @Override
                            public void retryDownload() {
                                Log.e("apiDownload", "retry");
                                try {
                                    apiDownloader.retrySync();
                                } catch (SyncException | SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        apiDownloader.forUpdating();

                        try {
                            apiDownloader.addModulesToUpdate(tables);
                            apiDownloader.execute(C_Dashboard.this);
                        } catch (SyncException e) {
                            e.printStackTrace();
                        }
                    }

                });
                updaterChooserDialog.show();
                Log.e("updateApp", "tapped");
            }
            break;
            case R.id.mUnlink: {
                try {
                    AccountTools.unlinkAccount(this, getHelper(), new AccountListener() {
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
            break;
            case R.id.mSettings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Bundle addExtras(DashboardTile dashboardTile) {
        Bundle bundle = new Bundle();
        if (dashboardTile.getConcessioModule() == ConcessioModule.CUSTOMERS) {
            bundle.putBoolean(C_Module.FROM_CUSTOMERS_LIST, !dashboardTile.isProxy());
        }

        bundle.putBoolean(ModuleActivity.INIT_PRODUCT_ADAPTER_HELPER, true);
        return bundle;
    }

    @Override
    public void onItemClicked(View view, int position) {
        moduleSelected(view);
    }

    @Override
    protected void onDestroy() {
        if (!SwableTools.isImonggoSwableRunning(this))
            SwableTools.stopSwable(this);

        if (apiDownloader != null) {
            apiDownloader.onUnbindSyncService();
        }
        super.onDestroy();
    }
}
