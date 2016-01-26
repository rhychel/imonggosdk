package net.nueca.concessioengine.adapters.base;

import android.content.Context;

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

    public boolean removeCustomer(Customer customer) {
        if(selectedCustomer.contains(customer)) {
            selectedCustomer.remove(customer);
            return true;
        }
        return false;
    }

    public int removeCustomers(List<Customer> customersToRemove) {
        int removed = 0;
        for(Customer customer : customersToRemove) {
            if(selectedCustomer.contains(customer)) {
                selectedCustomer.remove(customer);
                removed++;
            }
        }
        return removed;
    }

    public BaseCustomersRecyclerAdapter(Context context, List<Customer> customers, boolean isMultiSelect) {
        super(context);
        setList(customers);
        this.isMultiSelect = isMultiSelect;
    }
}
