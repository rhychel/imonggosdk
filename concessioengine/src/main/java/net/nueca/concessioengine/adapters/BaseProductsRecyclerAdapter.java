package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.lists.ProductsList;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.tools.ProductListTools;

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
    protected ProductsList productsList;

    public BaseProductsRecyclerAdapter(Context context) {
        this.context = context;
    }

    public BaseProductsRecyclerAdapter(Context context, ProductsList productsList) {
        this.productsList = productsList;
        this.context = context;
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

    public void setProductsList(ProductsList productsList) {
        this.productsList = productsList;
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
