package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.util.List;

/**
 * Created by rhymart on 6/3/15.
 * imonggosdk (c)2015
 *
 * 1. Load the layout according to what is set
 * 2. create a simple product list item layout
 * 3. prepare the fields
 */
public abstract class BaseProductsAdapter extends BaseAdapter<Product> {

    private ProductsAdapterHelper productsAdapterHelper = new ProductsAdapterHelper();

    public BaseProductsAdapter(Context context, int resource, List<Product> objects) {
        super(context, resource, objects);
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

    public abstract boolean updateList(List<Product> productList);
}
