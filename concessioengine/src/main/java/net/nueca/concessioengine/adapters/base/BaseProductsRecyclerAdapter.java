package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.util.List;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public abstract class BaseProductsRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder> extends BaseRecyclerAdapter<T, Product> {

    private ProductsAdapterHelper productsAdapterHelper = new ProductsAdapterHelper();

    public BaseProductsRecyclerAdapter(Context context) {
        super(context);
    }

    public BaseProductsRecyclerAdapter(Context context, List<Product> productsList) {
        super(context);
        setList(productsList);
    }

    public BaseProductsRecyclerAdapter(Context context, ImonggoDBHelper dbHelper, List<Product> productsList) {
        super(context);
        setList(productsList);
        setDbHelper(dbHelper);
    }

    public void clearSelectedItems() {
        productsAdapterHelper.getSelectedProductItems().clear();
        ProductListTools.restartLineNo();
    }

    public SelectedProductItemList getSelectedProductItems() {
        return productsAdapterHelper.getSelectedProductItems();
    }

    protected ProductsAdapterHelper getAdapterHelper() {
        return productsAdapterHelper;
    }

    public void setDbHelper(ImonggoDBHelper dbHelper) {
        ProductsAdapterHelper.setDbHelper(dbHelper);
    }

}
