package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.objects.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 8/3/15.
 */
public abstract class BaseCustomersAdapter extends BaseAdapter<Customer> {
    private int layoutResource;
    protected List<Customer> selectedCustomer = new ArrayList<>();
    protected boolean isMultiSelect = false;

    public BaseCustomersAdapter(Context context, int resource, List<Customer> objects, boolean isMultiSelect) {
        super(context, resource, objects);
        this.layoutResource = resource;
        this.isMultiSelect = isMultiSelect;
    }

    public int getLayoutResource() { return layoutResource; }

    public List<Customer> getSelectedCustomers() {
        return selectedCustomer;
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Customer tapped = getItem(position);
            if(selectedCustomer == null)
                selectedCustomer = new ArrayList<>();

            if(selectedCustomer.contains(tapped)) {
                selectedCustomer.remove(tapped);
            }
            else {
                if(isMultiSelect) {
                    selectedCustomer.add(tapped);
                }
                else {
                    selectedCustomer.clear();
                    selectedCustomer.add(tapped);
                }
            }
            notifyDataSetChanged();
        }
    };
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }
}
