package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.imonggosdk.objects.OfflineData;

import java.util.List;

/**
 * Created by rhymart on 8/7/15.
 * imonggosdk2 (c)2015
 */
public abstract class BaseTransactionsRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder> extends BaseRecyclerAdapter<T, OfflineData>{

    public BaseTransactionsRecyclerAdapter(Context context) {
        super(context);
    }

    public BaseTransactionsRecyclerAdapter(Context context, List<OfflineData> offlineDataList) {
        super(context);
        setList(offlineDataList);
    }

}
