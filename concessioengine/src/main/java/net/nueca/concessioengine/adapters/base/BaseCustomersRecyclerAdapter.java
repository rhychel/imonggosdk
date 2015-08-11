package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.imonggosdk.objects.Customer;

import java.util.List;

/**
 * Created by gama on 8/10/15.
 */
public abstract class BaseCustomersRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder> extends
        BaseRecyclerAdapter<T, Customer> {
    public BaseCustomersRecyclerAdapter(Context context) {
        super(context);
    }

    public BaseCustomersRecyclerAdapter(Context context, List<Customer> customers) {
        super(context);
        setList(customers);
    }
}
