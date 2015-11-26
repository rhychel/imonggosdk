package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.concessioengine.adapters.enums.ListingType;
import net.nueca.imonggosdk.objects.customer.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 8/10/15.
 */
public abstract class BaseCustomersRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder> extends
        BaseRecyclerAdapter<T, Customer> {

    protected List<Customer> selectedCustomer = new ArrayList<>();
    protected boolean isMultiSelect = false;

    public List<Customer> getSelectedCustomers() {
        return selectedCustomer;
    }

    public BaseCustomersRecyclerAdapter(Context context, List<Customer> customers, boolean isMultiSelect) {
        super(context);
        setList(customers);
        this.isMultiSelect = isMultiSelect;
    }
}
