package net.nueca.concessio_test;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.fragments.AddCustomersFragment;
import net.nueca.concessioengine.fragments.SimpleCustomersFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.objects.associatives.CustomerCustomerGroupAssoc;
import net.nueca.imonggosdk.objects.branchentities.BranchProduct;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.invoice.Discount;
import net.nueca.imonggosdk.objects.routeplan.RoutePlan;
import net.nueca.imonggosdk.objects.routeplan.RoutePlanDetail;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;
import java.util.List;

public class C_Customers extends ImonggoAppCompatActivity implements SetupActionBar {

    private SimpleCustomersFragment mSimpleCustomersFragment;
    private AddCustomersFragment addCustomersFragment;
    private String TAG = "C_Customers";
    private String CurrentView = "Customers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initComponents();
        Log.e(TAG, "Customers Activity");

        try {
            List<RoutePlan> routePlan = getHelper().fetchObjectsList(RoutePlan.class);

            for (RoutePlan rp : routePlan) {
                Log.e(TAG, "Route Plan: " + String.valueOf(rp.getId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        try {
            List<RoutePlanDetail> routePlan = getHelper().fetchObjectsList(RoutePlanDetail.class);

            for (RoutePlanDetail rp : routePlan) {
                Log.e(TAG, "RoutePlanDetail: " + String.valueOf(rp.getId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            List<CustomerGroup> discounts = getHelper().fetchObjectsList(CustomerGroup.class);

            if(discounts.size() == 0) {
                Log.e(TAG, "CustomerGroup Size is 0 " );
            }
            for (CustomerGroup rp : discounts) {
                Log.e(TAG, "CustomerGroup: " + String.valueOf(rp.getId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



        try {
            List<CustomerCustomerGroupAssoc> discounts = getHelper().fetchObjectsList(CustomerCustomerGroupAssoc.class);

            if(discounts.size() == 0) {
                Log.e(TAG, "CustomerCustomerGroup Size is 0 " );
            }
            for (CustomerCustomerGroupAssoc rp : discounts) {
                Log.e(TAG, "CustomerCustomerGroup: " + String.valueOf(rp.getId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void initComponents() {
        setContentView(R.layout.c_customers);
        mSimpleCustomersFragment = new SimpleCustomersFragment();
        mSimpleCustomersFragment.setHelper(getHelper());
        mSimpleCustomersFragment.setSetupActionBar(this);
        mSimpleCustomersFragment.setListingType(ListingType.LETTER_HEADER);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flContent, mSimpleCustomersFragment)
                .addToBackStack("customers")
                .commit();

        try {
            List<BranchProduct> branchProducts = getHelper().fetchObjectsList(BranchProduct.class);

            Log.e(TAG, "Branch Products size: " + branchProducts.size());

            for(BranchProduct bp : branchProducts) {
                Log.e(TAG, ">>" + bp.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(TAG, "OnCreateOptionsMenu");

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (CurrentView.equals("Customers")) {
            getMenuInflater().inflate(R.menu.simple_customers_menu, menu);
            getSupportActionBar().setTitle("Customers");
            menu.findItem(R.id.mSearch).setVisible(false);
        } else if (CurrentView.equals("Add Customers")) {
            getSupportActionBar().setTitle("Add Customers");
            getMenuInflater().inflate(R.menu.simple_add_customers_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case net.nueca.concessioengine.R.id.mAddCustomer:
                CurrentView = "Add Customers";

                addCustomersFragment = AddCustomersFragment.newInstance();
                addCustomersFragment.setSetupActionBar(this);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flContent, addCustomersFragment)
                        .addToBackStack("Add Customer")
                        .commit();
                break;

            case net.nueca.concessioengine.R.id.mSaveCustomer:
                Log.e(TAG, "Add Customer");

                if (addCustomersFragment != null) {

                    if (addCustomersFragment.validateCustomerInput()) {
                        // get the data
                        addCustomersFragment.getCustomerData();
                        onBackPressed();
                    }

                } else {
                    Log.e(TAG, "Fragment is Null!");
                }

            case net.nueca.concessioengine.R.id.mUnlink:
                unlinkDevice();
                break;
            default:
                break;
        }

        return true;
    }

    private void unlinkDevice() {
        Log.e(TAG, "Unlink Device");
        try {
            AccountTools.unlinkAccount(C_Customers.this, getHelper());
            finish();
            startActivity(new Intent(C_Customers.this, C_Login.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (CurrentView.equals("Add Customers"))
            CurrentView = "Customers";
    }
}
