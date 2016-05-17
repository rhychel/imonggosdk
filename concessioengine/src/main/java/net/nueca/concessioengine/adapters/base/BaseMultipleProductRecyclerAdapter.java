package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.concessioengine.objects.MultiItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;

import java.util.List;

/**
 * Created by rhymartmanchus on 12/05/2016.
 */
public abstract class BaseMultipleProductRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder> extends BaseRecyclerAdapter<T, MultiItem> {

    protected boolean isReturnItems = false;
    public BaseMultipleProductRecyclerAdapter(Context context) {
        super(context);
    }

    public BaseMultipleProductRecyclerAdapter(Context context, List<MultiItem> list) {
        super(context, list);
    }

    public BaseMultipleProductRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper, List<MultiItem> list) {
        super(context, list);
        setDbHelper(dbHelper);
    }

    public void setDbHelper(ImonggoDBHelper2 dbHelper) {
        ProductsAdapterHelper.setDbHelper(dbHelper);
    }

    public ImonggoDBHelper2 getHelper() {
        return ProductsAdapterHelper.getDbHelper();
    }

    public SelectedProductItemList getSelectedProductItems() {
        if(isReturnItems)
            return ProductsAdapterHelper.getSelectedReturnProductItems();
        return ProductsAdapterHelper.getSelectedProductItems();
    }

}
