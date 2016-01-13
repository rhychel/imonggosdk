package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.imonggosdk.objects.customer.Customer;

import java.util.List;

/**
 * Created by rhymart on 12/2/15.
 */
public abstract class BaseRoutePlanRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder>
        extends BaseRecyclerAdapter<T, Customer> {

    public BaseRoutePlanRecyclerAdapter(Context context, List<Customer> routePlans) {
        super(context);
        setList(routePlans);
    }
}
