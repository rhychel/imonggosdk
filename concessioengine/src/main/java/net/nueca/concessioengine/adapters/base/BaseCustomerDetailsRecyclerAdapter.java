package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.concessioengine.enums.CustomerDetail;

import java.util.List;

/**
 * Created by rhymart on 12/2/15.
 */
public abstract class BaseCustomerDetailsRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder>
        extends BaseRecyclerAdapter<T, CustomerDetail> {

    public BaseCustomerDetailsRecyclerAdapter(Context context, List<CustomerDetail> customerDetails) {
        super(context);
        setList(customerDetails);
    }
}
