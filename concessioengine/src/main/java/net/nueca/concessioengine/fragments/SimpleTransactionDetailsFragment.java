package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.adapters.SimpleProductRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.SimpleSalesProductRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.Document;

import java.util.List;

/**
 * Created by rhymart on 8/5/15.
 * imonggosdk2 (c)2015
 */
public class SimpleTransactionDetailsFragment extends BaseProductsFragment {

    public static final String TRANSACTION_ID = "transaction_id";

    private Toolbar tbActionBar;

    private RecyclerView rvProducts;
    private SimpleSalesProductRecyclerAdapter simpleProductRecyclerViewAdapter;
    private OfflineData offlineData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_products_fragment_rv, container, false);

        rvProducts = (RecyclerView) view.findViewById(R.id.rvProducts);
        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        ((Spinner) view.findViewById(R.id.spCategories)).setVisibility(View.GONE);

        simpleProductRecyclerViewAdapter = new SimpleSalesProductRecyclerAdapter(getActivity(), getHelper(), getProducts());
        simpleProductRecyclerViewAdapter.setListingType(ListingType.ADVANCED_SALES);
        simpleProductRecyclerViewAdapter.setBranch(branch);
        simpleProductRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvProducts);
        rvProducts.setAdapter(simpleProductRecyclerViewAdapter);

        if(offlineData.getType() == OfflineData.DOCUMENT) {
            if(offlineData.getConcessioModule() == ConcessioModule.RELEASE_ADJUSTMENT) {
                LinearLayout llReason = (LinearLayout) view.findViewById(R.id.llReason);
                ImageView ivEdit = (ImageView) view.findViewById(R.id.ivEdit);
                TextView tvReason = (TextView) view.findViewById(R.id.tvReason);

                ivEdit.setVisibility(View.GONE);
                tvReason.setText(offlineData.getDocumentReason());
                llReason.setVisibility(View.VISIBLE);
            }
//            if(offlineData.getObjectFromData(Document.class).getCustomer() == null)
//                Log.e("TransactionsDetails", "customer is null");
//            else
//                Log.e("TransactionDetails", offlineData.getObjectFromData(Document.class).getCustomer().generateFullName());
        }

        return view;
    }

    public int numberOfItems() {
        return simpleProductRecyclerViewAdapter.getItemCount();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.others_menu, menu);
    }

    public OfflineData getOfflineData() {
        return offlineData;
    }

    public void setOfflineData(OfflineData offlineData) {
        this.offlineData = offlineData;
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
    protected void toggleNoItems(String msg, boolean show) { }

}
