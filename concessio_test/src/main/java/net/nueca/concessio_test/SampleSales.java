package net.nueca.concessio_test;

import android.support.v4.widget.SearchViewCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.imonggosdk.tools.DialogTools;

public class SampleSales extends ModuleActivity implements SetupActionBar, View.OnClickListener {
    private SimpleProductsFragment simpleProductsFragment, finalizeFragment;
    private Toolbar toolbar;
    private Button btnSummary;
    private MenuItem miSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_module);

        btnSummary = (Button) findViewById(R.id.btnSummary);
        btnSummary.setOnClickListener(this);

        simpleProductsFragment = SimpleProductsFragment.newInstance();
        finalizeFragment = SimpleProductsFragment.newInstance();

        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);

        finalizeFragment.setHelper(getHelper());
        finalizeFragment.setSetupActionBar(this);

        simpleProductsFragment.setHasUnits(true);
        simpleProductsFragment.setProductCategories(getProductCategories(true));

        finalizeFragment.setHasCategories(false);
        finalizeFragment.setHasBrand(false);
        finalizeFragment.setHasDeliveryDate(false);
        finalizeFragment.setHasUnits(true);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.flContent, simpleProductsFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(btnSummary != null && btnSummary.getText().toString().equals("Summary")) {
            getMenuInflater().inflate(R.menu.menu_sample_sales, menu);
            mSearch = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
            initializeSearchViewEx(new SearchViewCompat.OnQueryTextListenerCompat() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    simpleProductsFragment.updateListWhenSearch(newText);
                    return true;
                }
            });


            miSearch = menu.findItem(R.id.mSearch);
        }
        miSearch.setVisible(btnSummary.getText().toString().equals("Summary"));
        getSupportActionBar().setDisplayShowTitleEnabled(!btnSummary.getText().toString().equals("Summary"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(!btnSummary.getText().toString().equals("Summary"));
        getSupportActionBar().setHomeButtonEnabled(!btnSummary.getText().toString().equals("Summary"));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        btnSummary.setText("Summary");

        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //getSupportActionBar().setHomeButtonEnabled(false);
        //miSearch.setVisible(true);
        invalidateOptionsMenu();
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.toolbar = toolbar;
    }

    @Override
    public void onClick(View v) {
        if(btnSummary.getText().toString().equals("Summary")) {
            if (ProductsAdapterHelper.getSelectedProductItems().isEmpty())
                DialogTools.showDialog(SampleSales.this, "Ooops!", "You have no selected items. Kindly select first " +
                        "products.");
            else {
                setTitle("Finalize");
                miSearch.setVisible(false);

                btnSummary.setText("Send");
                finalizeFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.flContent, finalizeFragment, "finalize")
                        .addToBackStack("finalizer")
                        .commit();
                //getSupportActionBar().setDisplayShowTitleEnabled(true);
                //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //getSupportActionBar().setHomeButtonEnabled(true);
            }
        } else {
            setTitle("Product Selection");
        }

        invalidateOptionsMenu();
    }
}
