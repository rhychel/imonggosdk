package net.nueca.concessio_test;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.nueca.concessioengine.fragments.AddCustomersFragment;
import net.nueca.concessioengine.fragments.SimpleCustomersFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;


public class C_Customers extends ImonggoAppCompatActivity implements SetupActionBar {

    private SimpleCustomersFragment mSimpleCustomersFragment;
    private String TAG = "C_Customers";
    private String CurrentView = "Customers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initComponents();
        Log.e(TAG, "Customers Activity");
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
        } else if(CurrentView.equals("Add Customers")) {
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
                AddCustomersFragment addCustomersFragment = AddCustomersFragment.newInstance();
                addCustomersFragment.setSetupActionBar(this);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flContent, addCustomersFragment)
                        .addToBackStack("Add Customer")
                        .commit();

                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(CurrentView.equals("Add Customers"))
            CurrentView = "Customers";
    }
}
