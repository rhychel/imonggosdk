package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseCustomersAdapter;
import net.nueca.imonggosdk.objects.customer.Customer;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by gama on 8/3/15.
 * imonggosdk (c)2015
 */
public class SimpleCustomerListAdapter extends BaseCustomersAdapter {
    private int highlightColor;
    private int circleColor = Color.WHITE;

    public SimpleCustomerListAdapter(Context context, List<Customer> objects, boolean isMultiSelect, Integer
                                     highlightColor) {
        super(context, R.layout.simple_customer_listitem, objects, isMultiSelect);
        if(highlightColor != null)
            this.highlightColor = highlightColor;
        else
            this.highlightColor = Color.parseColor("#22000000");
    }

    public void setCircleColor(int color) {
        circleColor = color;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(getLayoutResource(), parent, false);
            viewHolder = new ViewHolder();

            viewHolder.llCustomerItem = (LinearLayout) convertView.findViewById(R.id.llCustomerItem);
            viewHolder.tvFirstLetter = (TextView) convertView.findViewById(R.id.tvFirstLetter);
            viewHolder.tvCustomerName = (TextView) convertView.findViewById(R.id.tvCustomerName);
            viewHolder.tvAlternateId = (TextView) convertView.findViewById(R.id.tvAlternateId);
            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Customer customer = getItem(position);

        String name;
        if(customer.getName() != null && customer.getName().length() > 0)
            name = customer.getName();
        else
            name = customer.getFirst_name() + " " + customer.getLast_name();
        name = name.trim();

        if(name.length() > 0 && StringUtils.isAlphanumeric(name.substring(0, 1)))
            viewHolder.tvFirstLetter.setText(StringUtils.upperCase(name).charAt(0) + "");
        else
            viewHolder.tvFirstLetter.setText("@");

        viewHolder.tvCustomerName.setText(name);
        viewHolder.tvAlternateId.setText(customer.getAlternate_code());
        viewHolder.tvAddress.setText(customer.getFullAddress());

        if(customer.getAlternate_code() != null && !customer.getAlternate_code().isEmpty())
            viewHolder.tvAlternateId.setVisibility(View.VISIBLE);
        else
            viewHolder.tvAlternateId.setVisibility(View.GONE);

        if(customer.getFullAddress() != null && !customer.getFullAddress().isEmpty())
            viewHolder.tvAddress.setVisibility(View.VISIBLE);
        else
            viewHolder.tvAddress.setVisibility(View.GONE);

        StateListDrawable bgShape = (StateListDrawable)viewHolder.tvFirstLetter.getBackground();
        bgShape.setColorFilter(circleColor, PorterDuff.Mode.SRC);

        if(selectedCustomer.contains(customer))
            viewHolder.llCustomerItem.setBackgroundColor(highlightColor);
        else
            viewHolder.llCustomerItem.setBackgroundColor(Color.TRANSPARENT);

        return convertView;
    }

    public boolean updateList(List<Customer> customers) {
        clear();
        addAll(customers);
        notifyDataSetChanged();
        return getCount() > 0;
    }

    public class ViewHolder {
        public TextView tvFirstLetter;
        public TextView tvCustomerName, tvAlternateId, tvAddress;
        public LinearLayout llCustomerItem;
    }
}
