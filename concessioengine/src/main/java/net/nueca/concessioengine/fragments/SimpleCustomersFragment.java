package net.nueca.concessioengine.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleCustomerListAdapter;
import net.nueca.imonggosdk.objects.Customer;

import java.util.List;

/**
 * Created by gama on 8/11/15.
 */
public class SimpleCustomersFragment extends BaseCustomersFragment {
    private SimpleCustomerListAdapter simpleCustomerListAdapter;

    private TextView tvNoCustomers;
    private boolean isMultiSelect = false;
    private Integer color;

    public void setMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_customers_fragment_lv, container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        tvNoCustomers = (TextView) view.findViewById(R.id.tvNoCustomers);

        lvCustomers = (ListView) view.findViewById(R.id.lvCustomers);
        simpleCustomerListAdapter = new SimpleCustomerListAdapter(getActivity(), getCustomers(), isMultiSelect,
                "#11000000");

        if(color != null) {
            simpleCustomerListAdapter.setCircleColor(color);
            tbActionBar.setBackgroundColor(color);
        }

        lvCustomers.setAdapter(simpleCustomerListAdapter);
        lvCustomers.setOnItemClickListener(simpleCustomerListAdapter.getOnItemClickListener());
        lvCustomers.setOnScrollListener(lvScrollListener);

        toggleNoItems("No customers in the list.", (simpleCustomerListAdapter.getCount() > 0));

        return view;
    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {
        lvCustomers.setVisibility(show ? View.VISIBLE : View.GONE);
        tvNoCustomers.setVisibility(show ? View.GONE : View.VISIBLE);
        tvNoCustomers.setText(msg);
    }

    public void refreshList() {
        simpleCustomerListAdapter.notifyDataSetChanged();
    }

    public void updateListWhenSearch(String searchKey) {
        setSearchKey(searchKey);
        offset = 0l;

        toggleNoItems("No results for \"" + searchKey + "\"" + ".", simpleCustomerListAdapter.updateList(getCustomers()));
    }

    public SimpleCustomerListAdapter getAdapter() {
        return simpleCustomerListAdapter;
    }

    public List<Customer> getSelectedCustomers() {
        return simpleCustomerListAdapter.getSelectedCustomers();
    }
}
