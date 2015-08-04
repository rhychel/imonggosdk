package net.nueca.concessioengine.adapters.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.BaseProductsRecyclerAdapter;
import net.nueca.imonggosdk.objects.Customer;

import java.util.List;

/**
 * Created by gama on 8/3/15.
 */
public class SimpleCustomerAdapter extends BaseCustomerAdapter implements BaseProductsRecyclerAdapter.OnItemClickListener {
    public SimpleCustomerAdapter(Context context, List<Customer> objects) {
        super(context, R.layout.simple_customer_listitem, objects);
    }

    private int selectedCustomer = -1;
    private ImageView selectedCustomer_status = null;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(getLayoutResource(), null);
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

        if(position == selectedCustomer)
            viewHolder.ivStatus.setVisibility(View.VISIBLE);
        else
            viewHolder.ivStatus.setVisibility(View.INVISIBLE);

        viewHolder.tvCustomerName.setText(customer.getName());
        viewHolder.tvAlternateId.setText(customer.getAlternate_code());
        viewHolder.tvAddress.setText(customer.getFullAddress());

        viewHolder.llCustomerItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCustomer = position;
                selectedCustomer_status.setVisibility(View.INVISIBLE);
            }
        });

        return convertView;
    }

    @Override
    public void onItemClicked(View view, int position) {

    }

    public class ViewHolder {
        public ImageView ivStatus;
        public TextView tvCustomerName, tvAlternateId, tvAddress;
        public LinearLayout llCustomerItem;
    }
}
