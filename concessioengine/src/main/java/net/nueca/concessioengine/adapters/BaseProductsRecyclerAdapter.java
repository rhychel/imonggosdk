package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.lists.ProductsList;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.util.List;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public abstract class BaseProductsRecyclerAdapter<T extends BaseProductsRecyclerAdapter.ViewHolder> extends RecyclerView.Adapter<T> {

    public interface OnItemClickListener {
        void onItemClicked(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClicked(View view, int position);
    }

    private Context context;
    protected OnItemClickListener onItemClickListener = null;
    protected OnItemLongClickListener onItemLongClickListener = null;

    private ProductsAdapterHelper productsAdapterHelper = new ProductsAdapterHelper();
    protected List<Product> productsList;

    public BaseProductsRecyclerAdapter(Context context) {
        this.context = context;
    }

    public BaseProductsRecyclerAdapter(Context context, List<Product> productsList) {
        this.context = context;
        this.productsList = productsList;
    }

    public BaseProductsRecyclerAdapter(Context context, ImonggoDBHelper dbHelper, List<Product> productsList) {
        this.productsList = productsList;
        this.context = context;
        setDbHelper(dbHelper);
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ViewHolder(View itemView) {
            super(itemView);
        }
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

    public void setProductsList(List<Product> productsList) {
        this.productsList = productsList;
    }

    public List<Product> getProductsList() {
        return productsList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public Context getContext() {
        return context;
    }

    public void setDbHelper(ImonggoDBHelper dbHelper) {
        ProductsAdapterHelper.setDbHelper(dbHelper);
    }

}
