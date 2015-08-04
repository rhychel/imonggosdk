package net.nueca.concessioengine.adapters.customer;

import android.content.Context;
import android.widget.ArrayAdapter;

import net.nueca.imonggosdk.objects.Customer;

import java.util.List;

/**
 * Created by gama on 8/3/15.
 */
public abstract class BaseCustomerAdapter extends ArrayAdapter<Customer> {
    private int layoutResource;

    public BaseCustomerAdapter(Context context, int resource, List<Customer> objects) {
        super(context, resource, objects);
        this.layoutResource = resource;
    }

    public int getLayoutResource() { return layoutResource; }
}
