package net.nueca.concessioengine.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleCustomerListAdapter;
import net.nueca.concessioengine.adapters.SimpleCustomerRecyclerViewAdapter;
import net.nueca.imonggosdk.objects.Customer;

import java.util.List;

/**
 * Created by gama on 8/11/15.
 */
public class SimpleCustomersFragment extends BaseCustomersFragment {
    private SimpleCustomerListAdapter simpleCustomerListAdapter;
    private SimpleCustomerRecyclerViewAdapter simpleCustomerRecyclerViewAdapter;

    private TextView tvNoCustomers;
    private boolean isMultiSelect = false;
    private Integer color;
    private boolean useRecyclerView = true;

    public void setMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public void setUseRecyclerView(boolean useRecyclerView) {
        this.useRecyclerView = useRecyclerView;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(useRecyclerView?
                        R.layout.simple_customers_fragment_rv : R.layout.simple_customers_fragment_lv,
                        container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        tvNoCustomers = (TextView) view.findViewById(R.id.tvNoCustomers);

        if(useRecyclerView) {
            rvCustomers = (RecyclerView) view.findViewById(R.id.rvCustomers);
            simpleCustomerRecyclerViewAdapter = new SimpleCustomerRecyclerViewAdapter(getActivity(), getCustomers(),
                    isMultiSelect, "#22000000");

            if (color != null) {
                simpleCustomerRecyclerViewAdapter.setCircleColor(color);
                tbActionBar.setBackgroundColor(color);
            }
            simpleCustomerRecyclerViewAdapter.initializeRecyclerView(getActivity(),rvCustomers);
            rvCustomers.setAdapter(simpleCustomerRecyclerViewAdapter);
            rvCustomers.addOnScrollListener(rvScrollListener);

            toggleNoItems("No customers in the list.", (simpleCustomerRecyclerViewAdapter.getItemCount() > 0));
        }
        else {
            lvCustomers = (ListView) view.findViewById(R.id.lvCustomers);
            simpleCustomerListAdapter = new SimpleCustomerListAdapter(getActivity(), getCustomers(), isMultiSelect,
                    "#22000000");

            if (color != null) {
                simpleCustomerListAdapter.setCircleColor(color);
                tbActionBar.setBackgroundColor(color);
            }

            lvCustomers.setAdapter(simpleCustomerListAdapter);
            lvCustomers.setOnItemClickListener(simpleCustomerListAdapter.getOnItemClickListener());
            lvCustomers.setOnScrollListener(lvScrollListener);

            toggleNoItems("No customers in the list.", (simpleCustomerListAdapter.getCount() > 0));
        }

        return view;
    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {
        tvNoCustomers.setVisibility(show ? View.GONE : View.VISIBLE);
        tvNoCustomers.setText(msg);

        if(useRecyclerView)
            rvCustomers.setVisibility(show ? View.VISIBLE : View.GONE);
        else
            lvCustomers.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void refreshList() {
        if(useRecyclerView)
            simpleCustomerRecyclerViewAdapter.notifyDataSetChanged();
        else
            simpleCustomerListAdapter.notifyDataSetChanged();
    }

    public void updateListWhenSearch(String searchKey) {
        setSearchKey(searchKey);
        offset = 0l;

        if(useRecyclerView)
            toggleNoItems("No results for \"" + searchKey + "\"" + ".",
                    simpleCustomerRecyclerViewAdapter.updateList(getCustomers()));
        else
            toggleNoItems("No results for \"" + searchKey + "\"" + ".",
                    simpleCustomerListAdapter.updateList(getCustomers()));
    }

    public SimpleCustomerListAdapter getListAdapter() {
        return simpleCustomerListAdapter;
    }
    public SimpleCustomerRecyclerViewAdapter getRecyclerAdapter() {
        return simpleCustomerRecyclerViewAdapter;
    }

    public List<Customer> getSelectedCustomers() {
        if(useRecyclerView)
            return simpleCustomerRecyclerViewAdapter.getSelectedCustomers();
        else
            return simpleCustomerListAdapter.getSelectedCustomers();
    }
}
