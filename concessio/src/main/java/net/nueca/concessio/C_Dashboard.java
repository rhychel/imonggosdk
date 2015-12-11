package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.nueca.concessioengine.activities.DashboardActivity;
import net.nueca.concessioengine.adapters.DashboardRecyclerAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.objects.DashboardTile;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;
import java.util.ArrayList;

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
//        add(new DashboardTile(ConcessioModule.SALES, "Sales", R.drawable.ic_booking));
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

        setNextActivityClass(C_Module.class);

        if(!SwableTools.isImonggoSwableRunning(this))
            SwableTools.startSwable(this);

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        rvModules = (RecyclerView) findViewById(R.id.rvModules);
        spBranches = (Spinner) findViewById(R.id.spBranches);
        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.c_dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Bundle addExtras(ConcessioModule concessioModule) {
        return null;
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
