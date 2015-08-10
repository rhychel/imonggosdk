package net.nueca.concessioengine.adapters.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.objects.Customer;

import java.util.List;

/**
 * Created by gama on 8/3/15.
 * imonggosdk (c)2015
 */
public class SimpleCustomerAdapter extends BaseCustomerAdapter implements AdapterView.OnItemClickListener {
    public SimpleCustomerAdapter(Context context, List<Customer> objects) {
        super(context, R.layout.simple_customer_listitem, objects);
    }

    private Customer selectedCustomer = null;
    private View selectedView = null;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(getLayoutResource(), parent, false);
            viewHolder = new ViewHolder();

            viewHolder.llCustomerItem = (LinearLayout) convertView.findViewById(R.id.llCustomerItem);
            viewHolder.ivStatus = (ImageView) convertView.findViewById(R.id.ivStatus);
            viewHolder.tvCustomerName = (TextView) convertView.findViewById(R.id.tvCustomerName);
            viewHolder.tvAlternateId = (TextView) convertView.findViewById(R.id.tvAlternateId);
            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Customer customer = getItem(position);

        viewHolder.tvCustomerName.setText(customer.getName());
        viewHolder.tvAlternateId.setText(customer.getAlternate_code());
        viewHolder.tvAddress.setText(customer.getFullAddress());

        setAsSelected(convertView, selectedCustomer != null && selectedCustomer.equals(getItem(position)));

        return convertView;
    }

    private void setAsSelected(View view, boolean isSelected) {
        if(isSelected) {
            selectedView = view;
            selectedView.findViewById(R.id.ivStatus).setVisibility(View.VISIBLE);
        }
        else {
            view.findViewById(R.id.ivStatus).setVisibility(View.INVISIBLE);
        }
    }

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

    public class ViewHolder {
        public ImageView ivStatus;
        public TextView tvCustomerName, tvAlternateId, tvAddress;
        public LinearLayout llCustomerItem;
    }
}
