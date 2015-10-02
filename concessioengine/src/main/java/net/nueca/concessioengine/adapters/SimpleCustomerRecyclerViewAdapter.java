package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseCustomersRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.imonggosdk.objects.Customer;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by gama on 8/13/15.
 */
public class SimpleCustomerRecyclerViewAdapter extends
        BaseCustomersRecyclerAdapter<SimpleCustomerRecyclerViewAdapter.ListViewHolder> {
    private int highlightColor;
    private int circleColor = Color.WHITE;

    public SimpleCustomerRecyclerViewAdapter(Context context, List<Customer> customers, boolean isMultiSelect,
                                             Integer highlightColor) {
        super(context, customers, isMultiSelect);
        if(highlightColor != null)
            this.highlightColor = highlightColor;
        else
            this.highlightColor = Color.parseColor("#22000000");
    }

    public void setCircleColor(int color) {
        circleColor = color;
        notifyDataSetChanged();
    }
    public void setHighlightColor(int color) {
        highlightColor = color;
        notifyDataSetChanged();
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_customer_listitem, viewGroup,
                false);
        Log.e("onCreateViewHolder", "called");

        ListViewHolder lvh = new ListViewHolder(v, getItem(i));
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder listViewHolder, int position) {
        Customer customer = getItem(position);
        listViewHolder.setCustomer(customer);

        String name;
        if(customer.getName() != null && customer.getName().length() > 0)
            name = customer.getName();
        else
            name = customer.getFirst_name() + " " + customer.getLast_name();
        name = name.trim();

        if(name.length() > 0 && StringUtils.isAlphanumeric(name.substring(0, 1)))
            listViewHolder.tvFirstLetter.setText(StringUtils.upperCase(name).charAt(0) + "");
        else
            listViewHolder.tvFirstLetter.setText("@");

        listViewHolder.tvCustomerName.setText(name);
        listViewHolder.tvAlternateId.setText(customer.getAlternate_code());
        listViewHolder.tvAddress.setText(customer.getFullAddress());

        if(customer.getAlternate_code() != null && !customer.getAlternate_code().isEmpty())
            listViewHolder.tvAlternateId.setVisibility(View.VISIBLE);
        else
            listViewHolder.tvAlternateId.setVisibility(View.GONE);

        if(customer.getFullAddress() != null && !customer.getFullAddress().isEmpty())
            listViewHolder.tvAddress.setVisibility(View.VISIBLE);
        else
            listViewHolder.tvAddress.setVisibility(View.GONE);

        StateListDrawable bgShape = (StateListDrawable)listViewHolder.tvFirstLetter.getBackground();
        bgShape.setColorFilter(circleColor, PorterDuff.Mode.SRC);

        Log.e("SelectedCustomer", selectedCustomer.size()+"");
        if(selectedCustomer.contains(customer))
            listViewHolder.llCustomerItem.setBackgroundColor(highlightColor);
        else
            listViewHolder.llCustomerItem.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        public TextView tvFirstLetter;
        public TextView tvCustomerName, tvAlternateId, tvAddress;
        public LinearLayout llCustomerItem;
        private Customer customer;

        public ListViewHolder(View itemView, Customer customer) {
            super(itemView);
            llCustomerItem = (LinearLayout) itemView.findViewById(R.id.llCustomerItem);
            tvFirstLetter = (TextView) itemView.findViewById(R.id.tvFirstLetter);
            tvCustomerName = (TextView) itemView.findViewById(R.id.tvCustomerName);
            tvAlternateId = (TextView) itemView.findViewById(R.id.tvAlternateId);
            tvAddress = (TextView) itemView.findViewById(R.id.tvAddress);

            this.customer = customer;

            itemView.setOnClickListener(this);
        }

        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        @Override
        public void onClick(View view) {
            if(isMultiSelect) {
                if(selectedCustomer.contains(customer))
                    selectedCustomer.remove(customer);
                else
                    selectedCustomer.add(customer);
            }
            else {
                if(selectedCustomer.contains(customer))
                    selectedCustomer.remove(customer);
                else {
                    selectedCustomer.clear();
                    selectedCustomer.add(customer);
                }
            }

            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(view, getLayoutPosition());

            notifyDataSetChanged();
        }


        @Override
        public boolean onLongClick(View view) {
            if(onItemLongClickListener != null)
                onItemLongClickListener.onItemLongClicked(view, getLayoutPosition());
            return true;
        }
    }
}
