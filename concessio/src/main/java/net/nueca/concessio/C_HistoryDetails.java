package net.nueca.concessio;

import android.os.Bundle;
import android.util.Log;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.adapters.tools.TransactionsAdapterHelper;
import net.nueca.concessioengine.fragments.SimpleTransactionDetailsFragment;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 8/5/15.
 * imonggosdk2 (c)2015
 */
public class C_HistoryDetails extends ImonggoAppCompatActivity {

    private OfflineData offlineData;
    private SelectedProductItemList selectedProductItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_fragment);

        selectedProductItems = ProductsAdapterHelper.getSelectedProductItems();

        ProductsAdapterHelper.getSelectedProductItems().clear();
        try {
            offlineData = getHelper().getOfflineData().queryBuilder()
                    .where()
                        .eq("id", getIntent().getIntExtra(SimpleTransactionDetailsFragment.TRANSACTION_ID, -1))
                    .queryForFirst();

            Log.e("OfflineData", offlineData.getData());

            List<Product> orderedItems = new ArrayList<>();
            if(offlineData != null) {
                Log.e("Data", offlineData.getData());
                orderedItems = TransactionsAdapterHelper.generateTransactionItems(offlineData, getHelper());
            }

            SimpleTransactionDetailsFragment simpleTransactionDetailsFragment = new SimpleTransactionDetailsFragment();
            simpleTransactionDetailsFragment.setHelper(getHelper());
            simpleTransactionDetailsFragment.setFilterProductsBy(orderedItems);

            getSupportFragmentManager().beginTransaction().add(R.id.flContent, simpleTransactionDetailsFragment).commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ProductsAdapterHelper.getSelectedProductItems().clear();
    }
}
