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
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.exception.SyncException;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.interfaces.SyncModulesListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.LastUpdatedAt;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Settings;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.invoice.InvoiceLine;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
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

    private Toolbar tbActionBar;
    private Spinner spBranches;
    private RecyclerView rvModules;
    private RecyclerView.LayoutManager layoutManager;
    final APIDownloader apiDownloader = new APIDownloader(C_Dashboard.this, false);
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


        List<Settings> setting = Settings.fetchWithConditionInt(getHelper(), Settings.class, new DBTable.ConditionsWindow<Settings, Integer>() {
            @Override
            public Where<Settings, Integer> renderConditions(Where<Settings, Integer> where) throws SQLException {
                return where.eq("name", Configurations.SETTINGS_NAME.get(SettingsName.FORMAT_NO_OF_DECIMALS));
            }
        });
        if(setting.size() > 0)
            Log.e("Setting", setting.get(0).getValue());
        else {
            try {
                Log.e("Setting", getHelper().fetchObjects(Settings.class).countOf()+"");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            getHelper().deleteAll(OfflineData.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /*
        try {
            Customer customer = getHelper().fetchObjects(Customer.class).queryBuilder().where().eq("id",201925).queryForFirst();
            builder.customer(customer);
            new SwableTools.Transaction(getHelper())
                    .toSend()
                    .fromModule(ConcessioModule.INVOICE)
                    .forBranch(getSession().getCurrent_branch_id())
                    .object(builder.build())
                    .queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        try {
            Log.e("DEBUG",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            for(OfflineData o : getHelper().fetchObjects(OfflineData.class).queryForAll() ) {
                Log.e("OfflineData "+o.getId(), " >> " + o.getStatusLog());
            }
            Invoice.Builder builder = new Invoice.Builder();
            builder.invoice_date("2016-02-02T14:58:16Z");
            builder.reference("14-1000");
            builder.salesman_id(1204);
            builder.status("L");
            builder.addInvoiceLine(
                    new InvoiceLine.Builder()
                            .quantity(1.0)
                            .retail_price(100.0)
                            .product_id(197447)
                            .unit_name("Pc(s)")
                            .line_no(1)
                            .subtotal("100.0")
                            .build()
            );
            builder.addInvoiceLine(
                    new InvoiceLine.Builder()
                            .quantity(-1.0)
                            .retail_price(20.0)
                            .product_id(197449)
                            .unit_name("Pc(s)")
                            .line_no(2)
                            .subtotal("-20.0")
                            .build()
            );
            builder.addPayment(
                    new InvoicePayment.Builder()
                            .amount(50.0)
                            .tender(50.0)
                            .payment_type_id(1)
                            .build()
            );
            builder.extras(
                    new Extras.Builder()
                            .total_company_discount("0.0")
                            .total_unit_retail_price("80.0")
                            .payment_term_id(28)
                            .total_selling_price("80.0")
                            .total_customer_discount("0.0")
                            .customer_discount_text_summary("")
                            .build()
            );
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Invoice invoice = builder.build();
            List<InvoicePayment> payments = invoice.getNewBatchPayment();
            Log.e("$$$$ before createBatch", invoice.toJSONString());
            Log.e("NEW Payments " + (payments.size() > 0? payments.get(0).getPaymentBatchNo() : "null") + " " +
                    invoice.getCurrentPaymentBatchNo(), gson.toJson(payments));
            invoice.createNewPaymentBatch();
            Log.e("$$$$ after createBatch", invoice.toJSONString());
            payments = invoice.getNewBatchPayment();
            Log.e("NEW Payments " + (payments.size() > 0? payments.get(0).getPaymentBatchNo() : "null") + " " +
                    invoice.getCurrentPaymentBatchNo(), gson.toJson(payments));
            invoice.addPayment(new InvoicePayment.Builder()
                    .amount(10.0)
                    .tender(10.0)
                    .payment_type_id(1)
                    .build());
            invoice.createNewPaymentBatch();
            Log.e("$$$$ after add Payment", invoice.toJSONString());
            payments = invoice.getNewBatchPayment();
            Log.e("NEW Payments " + payments.get(0).getPaymentBatchNo() + " " + invoice.getCurrentPaymentBatchNo(), gson.toJson(payments));

            Log.e("DEBUG","<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
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

        if(apiDownloader != null) {
            apiDownloader.onUnbindSyncService();
        }
        super.onDestroy();
    }
}
