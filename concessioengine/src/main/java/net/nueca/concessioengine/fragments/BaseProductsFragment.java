package net.nueca.concessioengine.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import net.nueca.concessioengine.adapters.SimpleProductListAdapter;
import net.nueca.concessioengine.adapters.SimpleProductRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.tools.DividerItemDecoration;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.exceptions.ProductsFragmentException;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.Product;

/**
 * Created by rhymart on 7/15/15.
 * imonggosdk (c)2015
 */
public abstract class BaseProductsFragment extends ImonggoFragment {

    public interface SetupActionBar {
        void setupActionBar(Toolbar toolbar);
    }

    protected SetupActionBar setupActionBar;
    protected LinearLayoutManager linearLayoutManager;
    protected RecyclerView rvProducts;
    protected ListView lvProducts;
    protected Toolbar tbActionBar;

    protected SimpleProductRecyclerViewAdapter simpleProductRecyclerViewAdapter;
    protected SimpleProductListAdapter simpleProductListAdapter;

    protected abstract void showQuantityDialog(int position, Product product, SelectedProductItem selectedProductItem);
    protected abstract void showProductDetails(Product product);

    protected void initializeRecyclerView(RecyclerView rvProducts) {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        rvProducts.setLayoutManager(linearLayoutManager);
        rvProducts.setHasFixedSize(true);
        rvProducts.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
    }

    public String renderProducts() {
        String jsonSelected = "{}";
        ProductsAdapterHelper.getSelectedProductItems().renderToJson();
        return jsonSelected;
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    @Override
    public ImonggoDBHelper getHelper() {
        if(super.getHelper() == null)
            throw new ProductsFragmentException("dbHelper is null. Use "+this.getClass().getSimpleName()+".setHelper().");
        return super.getHelper();
    }
}
