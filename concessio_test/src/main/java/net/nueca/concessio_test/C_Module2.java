package net.nueca.concessio_test;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.MultiInputSelectedItemFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.interfaces.MultiInputListener;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;


/**
 * Created by Jn on 6/4/15.
 * imonggosdk (c)2015
 */
public class C_Module2 extends ModuleActivity {

    public SimpleProductsFragment simpleProductsFragment;

    private String TAG = "C_Module";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_fragment);

        simpleProductsFragment = new SimpleProductsFragment();
        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setUseRecyclerView(true);
        simpleProductsFragment.setProductCategories(getProductCategories(true));
        simpleProductsFragment.setMultipleInput(false);
        simpleProductsFragment.setMultiInputListener(new MultiInputListener() {
            @Override
            public void showInputScreen(Product product) {
                Intent intent = new Intent(C_Module2.this, TestMultiInput.class);
                intent.putExtra(MultiInputSelectedItemFragment.PRODUCT_ID, product.getId());
                startActivity(intent);
            }
        });

        getSupportFragmentManager().beginTransaction()
                .add(R.id.flContent, simpleProductsFragment)
                .commit();
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
                case R.id.mGo:

                    AccountTools.unlinkAccount(C_Module2.this, getHelper(), null);
                    Intent intent = new Intent(C_Module2.this, C_SampleLogin.class);

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
