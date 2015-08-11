package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.objects.Customer;

import java.util.List;

/**
 * Created by gama on 8/3/15.
 */
public abstract class BaseCustomersAdapter extends BaseAdapter<Customer> {
    private int layoutResource;
    protected Customer selectedCustomer = null;
    protected View selectedView = null;

    public BaseCustomersAdapter(Context context, int resource, List<Customer> objects) {
        super(context, resource, objects);
        this.layoutResource = resource;
    }

    public int getLayoutResource() { return layoutResource; }

    protected void setAsSelected(View convertView, boolean isSelected) {
        if(isSelected) {
            selectedView = convertView;
        }
    }

    public Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(selectedCustomer != null && selectedCustomer.equals(getItem(position))) {
                setAsSelected(view, false);
                selectedCustomer = null;
                return;
            }

            if(selectedCustomer != null)
                setAsSelected(selectedView, false);
            setAsSelected(view, true);
            selectedCustomer = getItem(position);
        }
    };
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }
}
