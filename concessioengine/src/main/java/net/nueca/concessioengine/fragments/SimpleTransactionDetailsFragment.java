package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.objects.Product;

import java.util.List;

/**
 * Created by rhymart on 8/5/15.
 * imonggosdk2 (c)2015
 */
public class SimpleTransactionDetailsFragment extends BaseProductsFragment {

    public static final String TRANSACTION_ID = "transaction_id";

    private ListView lvTransactions;
    private SimpleProductListAdapter simpleProductListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_listview, container, false);

        lvTransactions = (ListView) view.findViewById(R.id.lvTransactions);
        simpleProductListAdapter = new SimpleProductListAdapter(getActivity(), getHelper(), getProducts());
        lvTransactions.setAdapter(simpleProductListAdapter);

        return view;
    }

    @Override
    protected void showQuantityDialog(int position, Product product, SelectedProductItem selectedProductItem) {

    }

    @Override
    protected void showProductDetails(Product product) {

    }

    @Override
    protected void whenListEndReached(List<Product> productList) {

    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {
        // NO USE
    }

}
