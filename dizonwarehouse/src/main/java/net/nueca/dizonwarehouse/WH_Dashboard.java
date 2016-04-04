package net.nueca.dizonwarehouse;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.operations.update.APIDownloader;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 21/03/2016.
 * dizonwarehouse (c)2016
 */
public class WH_Dashboard extends DashboardActivity implements OnItemClickListener {
    private Toolbar tbActionBar;
    private RecyclerView rvModules;
    private Spinner spBranches;

    private DashboardRecyclerAdapter dashboardRecyclerAdapter;

    final APIDownloader apiDownloader = new APIDownloader(WH_Dashboard.this, false);

    private ArrayList<DashboardTile> dashboardTiles = new ArrayList<DashboardTile>();

    private Integer DEFAULT_PADDING = 3;
    private Integer HORIZONTAL_PADDING, VERTICAL_PADDING;
    private Integer CARD_DIMENS;
    private Integer MAX_COL = 2, MAX_ROW;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wh_dashboard);

        setNextActivityClass(WH_Module.class);

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        rvModules = (RecyclerView) findViewById(R.id.rvModules);
        spBranches = (Spinner) findViewById(R.id.spBranches);
        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_app_logo);

        ArrayAdapter<Branch> branchesAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_dark, getBranches());
        branchesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
        spBranches.setAdapter(branchesAdapter);

        rvModules.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, MAX_COL);
        rvModules.setLayoutManager(layoutManager);

        for(ModuleSetting moduleSetting : getActiveModuleSetting(null, true)) {
            /*if(moduleSetting.getModuleType() == ConcessioModule.INVOICE)
                dashboardTiles.add(new DashboardTile(ConcessioModule.CUSTOMERS, moduleSetting.getLabel(), true, ConcessioModule.INVOICE.getLogo()));
            else*/
            if(moduleSetting.getModuleType() == ConcessioModule.RECEIVE_SUPPLIER)
                dashboardTiles.add(new DashboardTile(moduleSetting.getModuleType(), "RECEIVE"));
            else if(moduleSetting.getModuleType() == ConcessioModule.RELEASE_BRANCH)
                dashboardTiles.add(new DashboardTile(moduleSetting.getModuleType(), "DISPATCH", R.drawable.ic_dispatching));
            else
                dashboardTiles.add(new DashboardTile(moduleSetting.getModuleType(), moduleSetting.getLabel()));

            Log.e("ModuleSettings", moduleSetting.getModule_type()+"="+moduleSetting.getDisplay_sequence());
        }

        dashboardRecyclerAdapter = new DashboardRecyclerAdapter(this, dashboardTiles);
        dashboardRecyclerAdapter.setOnItemClickListener(this);
        MAX_ROW = dashboardRecyclerAdapter.getItemCount() / MAX_COL;

        rvModules.addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int itemIndex = parent.getChildLayoutPosition(view);
                if(HORIZONTAL_PADDING == null || VERTICAL_PADDING == null) {
                    int totalWidth = parent.getWidth();
                    int totalHeight = parent.getHeight();
                    CARD_DIMENS = getResources().getDimensionPixelOffset(R.dimen.wh_card_dimens) + DEFAULT_PADDING * 2;

                    MAX_ROW = dashboardRecyclerAdapter.getItemCount() / MAX_COL;

                    HORIZONTAL_PADDING = ((totalWidth / MAX_COL) - CARD_DIMENS);
                    VERTICAL_PADDING = ((totalHeight / MAX_ROW) - CARD_DIMENS) / 2;
                    Log.e("RecyclerView", "init padding");
                }

                int top = DEFAULT_PADDING, left = DEFAULT_PADDING,
                        bot = DEFAULT_PADDING, right = DEFAULT_PADDING;
                if(itemIndex % MAX_COL == 0) left = HORIZONTAL_PADDING;
                if(itemIndex % MAX_COL == MAX_COL - 1) right = HORIZONTAL_PADDING;
                if(itemIndex % MAX_ROW == 0) top = VERTICAL_PADDING;
                if(itemIndex % MAX_ROW == MAX_ROW - 1) bot = VERTICAL_PADDING;

                if(left == right) {
                    left /= 2;
                    right /= 2;
                }
                if(top == bot) {
                    top /= 2;
                    bot /= 2;
                }

                top = Math.max(DEFAULT_PADDING, top);
                bot = Math.max(DEFAULT_PADDING, bot);
                left = Math.max(DEFAULT_PADDING, left);
                right = Math.max(DEFAULT_PADDING, right);

                outRect.set(left, top, right, bot);
            }
        });

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
        getMenuInflater().inflate(R.menu.wh_dashboard, menu);
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
                        final ProgressListDialog progressListDialog = new ProgressListDialog(WH_Dashboard.this, tableList);
                        progressListDialog.setCanceledOnTouchOutside(false);
                        progressListDialog.setCancelable(false);

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
                                Toast.makeText(WH_Dashboard.this, "Update completed!", Toast.LENGTH_LONG).show();
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
                            apiDownloader.execute(WH_Dashboard.this);
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
                            Intent intent = new Intent(WH_Dashboard.this, WH_Login.class);
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
    protected Bundle addExtras(DashboardTile dashboardTile) {
        Bundle bundle = new Bundle();
        if(dashboardTile.getConcessioModule() == ConcessioModule.CUSTOMERS) {
            //bundle.putBoolean(WH_Module.FROM_CUSTOMERS_LIST, !dashboardTile.isProxy());
        }
        bundle.putInt(WH_Module.BRANCH_ID, ((Branch)spBranches.getSelectedItem()).getId());

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

        if(apiDownloader != null) {
            apiDownloader.onUnbindSyncService();
        }
        super.onDestroy();
    }
}
