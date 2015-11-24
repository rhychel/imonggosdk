package net.nueca.concessio;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.nueca.concessioengine.activities.DashboardActivity;
import net.nueca.concessioengine.adapters.DashboardRecyclerAdapter;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Branch;

import java.util.ArrayList;

/**
 * Created by rhymart on 11/23/15.
 */
public class C_Dashboard extends DashboardActivity {

    private Toolbar tbActionBar;
    private Spinner spBranches;
    private RecyclerView rvModules;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayAdapter<Branch> branchesAdapter;
    private DashboardRecyclerAdapter dashboardRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_dashboard);

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        rvModules = (RecyclerView) findViewById(R.id.rvModules);
        spBranches = (Spinner) findViewById(R.id.spBranches);
        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        branchesAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_light, getBranches());
        branchesAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spBranches.setAdapter(branchesAdapter);

        rvModules.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this, 2);
        rvModules.setLayoutManager(layoutManager);

        dashboardRecyclerAdapter = new DashboardRecyclerAdapter(this, new ArrayList<String>(){{
            add("Booking");
            add("Customers");
            add("Receiving");
            add("Pullout");
            add("MSO");
            add("History");
            add("Pullout Request");
            add("Pullout Confirmation");
            add("Adjustment In");
            add("Layaway");
            add("Physical Count");
        }});
        rvModules.setAdapter(dashboardRecyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.c_dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected Bundle addExtras(ConcessioModule concessioModule) {
        return null;
    }
}
