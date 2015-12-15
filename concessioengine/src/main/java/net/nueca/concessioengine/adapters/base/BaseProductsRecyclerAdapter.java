package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public abstract class BaseProductsRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder> extends BaseRecyclerAdapter<T, Product> {

    private ProductsAdapterHelper productsAdapterHelper = new ProductsAdapterHelper();
    protected boolean hasSubtotal = false;

    public BaseProductsRecyclerAdapter(Context context) {
        super(context);
    }

    public BaseProductsRecyclerAdapter(Context context, List<Product> productsList) {
        super(context);
        setList(productsList);
    }

    public BaseProductsRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper, List<Product> productsList) {
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

    public void setHasSubtotal(boolean hasSubtotal) {
        this.hasSubtotal = hasSubtotal;
    }

    public void setDbHelper(ImonggoDBHelper2 dbHelper) {
        ProductsAdapterHelper.setDbHelper(dbHelper);
    }

    @Override
    public Product getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public boolean updateList(List<Product> products) {
        return super.updateList(products);
    }

    @Override
    public void addAll(List<Product> products) {
        super.addAll(products);
    }

    @Override
    public void add(Product product) {
        super.add(product);
    }

    @Override
    public void setList(List<Product> objectList) {
        super.setList(objectList);
    }
}
