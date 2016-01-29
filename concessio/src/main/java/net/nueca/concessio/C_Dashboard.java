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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import net.nueca.concessioengine.activities.DashboardActivity;
import net.nueca.concessioengine.activities.SettingsActivity;
import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.DashboardRecyclerAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.dialogs.ProgressListDialog;
import net.nueca.concessioengine.dialogs.UpdaterChooserDialog;
import net.nueca.concessioengine.objects.DashboardTile;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.exception.SyncException;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.operations.update.APIDownloader;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 11/23/15.
 */
public class C_Dashboard extends DashboardActivity implements OnItemClickListener {

    private Toolbar tbActionBar;
    private Spinner spBranches;
    private RecyclerView rvModules;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayAdapter<Branch> branchesAdapter;
    private DashboardRecyclerAdapter dashboardRecyclerAdapter;

    private ArrayList<DashboardTile> dashboardTiles = new ArrayList<DashboardTile>(){{
        add(new DashboardTile(ConcessioModule.ROUTE_PLAN, "Sales", R.drawable.ic_booking));
        add(new DashboardTile(ConcessioModule.CUSTOMERS, "Customers", R.drawable.ic_customers));
        add(new DashboardTile(ConcessioModule.RECEIVE_SUPPLIER, "Receiving", R.drawable.ic_receiving));
        add(new DashboardTile(ConcessioModule.RELEASE_SUPPLIER, "Pullout", R.drawable.ic_pullout));
        add(new DashboardTile(ConcessioModule.RELEASE_ADJUSTMENT, "MSO", R.drawable.ic_mso));
        add(new DashboardTile(ConcessioModule.LAYAWAY, "Layaway", R.drawable.ic_layaway));
        add(new DashboardTile(ConcessioModule.PHYSICAL_COUNT, "Physical Count", R.drawable.ic_physical_count));
        add(new DashboardTile(ConcessioModule.HISTORY, "History", R.drawable.ic_history));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_dashboard);

        Log.e("ClassName", Customer.class.getSimpleName());

        try {
            getHelper().deleteAll(OfflineData.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

        dashboardRecyclerAdapter = new DashboardRecyclerAdapter(this, dashboardTiles);
        dashboardRecyclerAdapter.setOnItemClickListener(this);
        rvModules.setAdapter(dashboardRecyclerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.e("SESSION", "isNULL? " + (getSession() == null));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(!SwableTools.isImonggoSwableRunning(this))
            SwableTools.startSwable(this);
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
                updaterChooserDialog.setTableToUpdate(getAppSetting().modulesToUpdate(getHelper()));
//                        Table.BRANCH_USERS, Table.PRODUCTS,
//                        Table.UNITS, Table.BRANCH_PRODUCTS,
//                        Table.CUSTOMER_BY_SALESMAN,
//                        Table.ROUTE_PLANS, // -- details
//                        Table.PRICE_LISTS_FROM_CUSTOMERS, // -- details
//                        Table.SALES_PROMOTIONS_POINTS, // -- details
//                        Table.SALES_PROMOTIONS_SALES_DISCOUNT); // -- details
                updaterChooserDialog.setOnTablesSelected(new UpdaterChooserDialog.OnTablesSelected() {
                    @Override
                    public void startUpdate(int[] tables, List<Table> tableList) {
                        final ProgressListDialog progressListDialog = new ProgressListDialog(C_Dashboard.this, tableList);
                        progressListDialog.setCanceledOnTouchOutside(false);
                        progressListDialog.setCancelable(false);

                        final APIDownloader apiDownloader = new APIDownloader(C_Dashboard.this, false);
                        apiDownloader.setSyncServer(Server.IRETAILCLOUD_NET);
                        apiDownloader.setSyncModulesListener(new SyncModulesListener() {
                            @Override
                            public void onStartDownload(Table table) {
                                Log.e("apiDownloader", "starting"+table.getStringName());
                                progressListDialog.initDownload(table);
                            }

                            @Override
                            public void onDownloadProgress(Table table, int page, int max) {
                                Log.e("apiDownloader", "progressing"+table.getStringName()+" page="+page+" | max="+max);
                                progressListDialog.updateProgress(page, max);
                            }

                            @Override
                            public void onEndDownload(Table table) {
                                Log.e("apiDownloader", "end"+table.getStringName());
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
                                Log.e("apiDownloader", "error"+table.getStringName());
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
            } break;
            case R.id.mUnlink:{
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
            } break;
            case R.id.mSettings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            } break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Bundle addExtras(ConcessioModule concessioModule) {
        Bundle bundle = new Bundle();
        if(concessioModule == ConcessioModule.CUSTOMERS) {
            bundle.putBoolean(C_Module.FROM_CUSTOMERS_LIST, true);
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
        if(!SwableTools.isImonggoSwableRunning(this))
            SwableTools.stopSwable(this);
        super.onDestroy();
    }
}
