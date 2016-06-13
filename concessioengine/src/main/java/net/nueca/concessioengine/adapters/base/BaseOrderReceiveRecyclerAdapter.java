package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.view.ViewGroup;

import net.nueca.concessioengine.objects.ReceivedMultiItem;
import net.nueca.concessioengine.objects.ReceivedProductItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;

import java.util.ArrayList;

/**
 * Created by gama on 31/05/2016.
 */
public abstract class BaseOrderReceiveRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder>
        extends BaseRecyclerAdapter<T, ReceivedMultiItem> {

    private ImonggoDBHelper2 dbHelper;

    public BaseOrderReceiveRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper) {
        super(context, new ArrayList<ReceivedMultiItem>());
        setHelper(dbHelper);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isHeader()? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public ImonggoDBHelper2 getHelper() {
        return dbHelper;
    }

    public void setHelper(ImonggoDBHelper2 dbHelper) {
        this.dbHelper = dbHelper;
    }
}
