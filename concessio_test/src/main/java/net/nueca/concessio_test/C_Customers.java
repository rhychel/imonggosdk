package net.nueca.concessio_test;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.nueca.concessioengine.fragments.AddCustomersFragment;
import net.nueca.concessioengine.fragments.SimpleCustomersFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;
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
            List<CustomerGroup> customerGroup = getHelper().fetchObjectsList(CustomerGroup.class);
            List<PriceList> priceLists = getHelper().fetchObjectsList(PriceList.class);

            for(PriceList pl : priceLists) {
                if(pl != null)
                Log.e(TAG, "price_list: " + pl.toString());
            }

            /*
            for (CustomerGroup cg : customerGroup) {
                Log.e(TAG, "[\n\t{");
                Log.e(TAG, "\t\tutc_created_at: " + cg.getUtc_created_at());
                Log.e(TAG, "\t\tname: " + cg.getName());
                Log.e(TAG, "\t\tutc_updated_at: " + cg.getUtc_updated_at());
                Log.e(TAG, "\t\tcode: " + cg.getCode());
                Log.e(TAG, "\t\tdiscount_text: " + cg.getDiscount_text());
                Log.e(TAG, "\t\tid: " + cg.getId());
*//*                if (cg.getPriceList() != null)
                    Log.e(TAG, "\t\tprice_list_id: " + cg.getPriceList().getId());
                else
                    Log.e(TAG, "price_list_id: " )*//*
                Log.e(TAG, "\t},");
            }*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        setContentView(R.layout.c_customers);
        mSimpleCustomersFragment = new SimpleCustomersFragment();
        mSimpleCustomersFragment.setHelper(getHelper());
        mSimpleCustomersFragment.setSetupActionBar(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flContent, mSimpleCustomersFragment)
                .addToBackStack("customers")
                .commit();
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

            case net.nueca.concessioengine.R.id.mAddCustomerOkay:
                Log.e(TAG, "Add Customer");

                if (addCustomersFragment != null) {

                    if (addCustomersFragment.validateCustomerInput()) {
                        addCustomersFragment.getCustomerData();
                    }

                } else {
                    Log.e(TAG, "Fragment is Null!");
                }

               /* onBackPressed();*/
                break;
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
