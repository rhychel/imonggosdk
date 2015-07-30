package net.nueca.concessio;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;

/**
 * Created by rhymart on 6/4/15.
 * imonggosdk (c)2015
 */
public class C_Module extends ModuleActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_fragment);

        SimpleProductsFragment simpleProductsFragment = new SimpleProductsFragment();
        simpleProductsFragment.setHelper(getHelper());
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
