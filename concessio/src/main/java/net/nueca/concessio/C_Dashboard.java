package net.nueca.concessio;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.activities.DashboardActivity;
import net.nueca.concessioengine.activities.SettingsActivity;
import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.DashboardRecyclerAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.dialogs.ProgressListDialog;
import net.nueca.concessioengine.dialogs.UpdaterChooserDialog;
import net.nueca.concessioengine.objects.DashboardTile;
import net.nueca.concessioengine.printer.epson.tools.EpsonPrinterTools;
import net.nueca.concessioengine.printer.starmicronics.tools.StarIOPrinterTools;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.exception.SyncException;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.operations.update.APIDownloader;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 11/23/15.
 */
public class C_Dashboard extends DashboardActivity implements OnItemClickListener {

    private static String TAG = "C_Dashboardx";
    private Toolbar tbActionBar;
    private Spinner spBranches;
    private RecyclerView rvModules;
    private RecyclerView.LayoutManager layoutManager;
    final APIDownloader apiDownloader = new APIDownloader(C_Dashboard.this, false);
    private ArrayAdapter<Branch> branchesAdapter;
    private DashboardRecyclerAdapter dashboardRecyclerAdapter;

    private ArrayList<DashboardTile> dashboardTiles = new ArrayList<DashboardTile>();
    private int currentlySelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_dashboard);

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
        spBranches.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (position != currentlySelected) {
                    DialogTools.showConfirmationDialog(C_Dashboard.this, "Change Default Branch", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentlySelected = position;
                            SettingTools.updateSettings(C_Dashboard.this, SettingsName.DEFAULT_BRANCH, String.valueOf(branchesAdapter.getItem(position).getId()));
                            Log.e("Branch selected", branchesAdapter.getItem(position).getName());
                        }
                    }, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            spBranches.setSelection(currentlySelected);
                        }
                    }, R.style.AppCompatDialogStyle_Light);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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

        try {
            Product product = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", 1417).queryForFirst();

            if(product == null) {
                Log.e(TAG, "product is null");
            } else {
                Log.e(TAG, "product is " + product.toString());
                List<BranchProduct> bp = getHelper().fetchObjects(BranchProduct.class).queryBuilder().where().eq("product_id", product).query();

                if(bp == null) {
                    Log.e(TAG, "branch product is null");
                } else {
                    Log.e(TAG, "bp size is " + bp.size());

                    for(BranchProduct b : bp) {
                        Log.e(TAG, "branch product: " + b.getProduct().getName() + " unit: " +  b.getUnit().getName() + " product status: " + b.getProduct().getStatus());

                    }
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SwableTools.isImonggoSwableRunning(this))
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
                DialogTools.showConfirmationDialog(this, "Unlink this account", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            AccountTools.unlinkAccount(C_Dashboard.this, getHelper(), new AccountListener() {
                                @Override
                                public void onLogoutAccount() {
                                }

                                @Override
                                public void onUnlinkAccount() {
                                    EpsonPrinterTools.clearTargetPrinter(C_Dashboard.this);
                                    StarIOPrinterTools.updateTargetPrinter(C_Dashboard.this, "");
                                    SwableTools.stopSwable(C_Dashboard.this);

                                    finish();
                                    Intent intent = new Intent(C_Dashboard.this, C_Login.class);
                                    startActivity(intent);
                                }
                            });
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, R.style.AppCompatDialogStyle_Light);
            }
            break;
            case R.id.mLogout: {
                try {
                    AccountTools.logoutUser(this, getHelper(), new AccountListener() {
                        @Override
                        public void onLogoutAccount() {
                            SwableTools.stopSwable(C_Dashboard.this);

                            finish();
                            Intent intent = new Intent(C_Dashboard.this, C_Login.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onUnlinkAccount() {

                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
//                EpsonPrinterTools.clearTargetPrinter(C_Dashboard.this);
//                StarIOPrinterTools.updateTargetPrinter(C_Dashboard.this, "");
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
        Log.e("onDestroy", "---- unbind");
        if (!SwableTools.isImonggoSwableRunning(this))
            SwableTools.stopSwable(this);

        apiDownloader.onUnbindSyncService();
        Log.e("onDestroy", "---- nothing to unbind");
        super.onDestroy();
    }
}
