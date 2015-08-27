package net.nueca.concessio_test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.tools.AccountTools;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by rhymart on 6/4/15.
 * imonggosdk (c)2015
 */
public class C_Module2 extends ModuleActivity {

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

        try {
            List<BranchUserAssoc> branchUserAssoc = getHelper().getBranchUserAssocs().queryForAll();
            List<Document> documentList = getHelper().getDocuments().queryForAll();

            for(BranchUserAssoc b : branchUserAssoc) {
                Log.e(TAG, b.getBranch().getId() + "");
            }

            Log.e(TAG, "Document Size: " + documentList.size());
            for(Document document : documentList) {
                Log.e(TAG, document.toString());
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
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
