package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.concessioengine.enums.ListingType;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.OfflineData;

import java.util.List;

/**
 * Created by rhymart on 8/7/15.
 * imonggosdk2 (c)2015
 */
public abstract class BaseTransactionsRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder> extends BaseRecyclerAdapter<T, OfflineData>{

    protected ImonggoDBHelper2 dbHelper;

    public BaseTransactionsRecyclerAdapter(Context context) {
        super(context);
    }

    public BaseTransactionsRecyclerAdapter(Context context, List<OfflineData> offlineDataList) {
        super(context);
        setList(offlineDataList);
    }

    public BaseTransactionsRecyclerAdapter(Context context, List<OfflineData> list, ListingType listingType) {
        super(context, list);
        this.listingType = listingType;
    }

    public void setDbHelper(ImonggoDBHelper2 dbHelper) {
        this.dbHelper = dbHelper;
    }
}
