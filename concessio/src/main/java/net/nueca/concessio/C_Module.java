package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;


/**
 * Created by rhymart on 6/4/15.
 * imonggosdk (c)2015
 */
public class C_Module extends ModuleActivity {

    private ListView lvSampleProducts;
    private String TAG = "C_Module";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_list);
        lvSampleProducts = (ListView) findViewById(R.id.lvSampleProducts);

        try {
            SimpleProductListAdapter simpleProductListAdapter = new SimpleProductListAdapter(this, getHelper(), getHelper().getProducts().queryForAll());
            lvSampleProducts.setAdapter(simpleProductListAdapter);

        } catch (SQLException e) {
            e.printStackTrace();
        }

/*        try {
            List<ProductTaxRateAssoc> productTaxRateAssocList = getHelper().getProductTaxRateAssocs().queryForAll();
            List<TaxRate> taxRateList = getHelper().getTaxRates().queryForAll();

            for(ProductTaxRateAssoc productTaxRateAssoc : productTaxRateAssocList) {
                Log.e(TAG, "Product: " + productTaxRateAssoc.getProduct().getName() + " TaxRate: " + productTaxRateAssoc.getTaxRate().getName());
            }

            for(TaxRate taxRate : taxRateList) {
                Log.e(TAG, "Tax Rates: " + taxRate.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.c_module, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.sample:

                    AccountTools.unlinkAccount(C_Module.this, getHelper(), null);
                    Intent intent = new Intent(C_Module.this, C_Login.class);

                    startActivity(intent);
                    finish();

                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProductsAdapterHelper.destroyProductAdapterHelper();
    }
}
