package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import java.util.List;

/**
 * Created by rhymart on 12/2/15.
 */
public abstract class BaseRoutePlanRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder>
        extends BaseRecyclerAdapter<T, String> {

    public BaseRoutePlanRecyclerAdapter(Context context, List<String> routePlans) {
        super(context);
        setList(routePlans);
    }
}
