package net.nueca.concessioengine.fragments;

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

/**
 * Created by gama on 8/11/15.
 */
public class SimpleCustomersFragment extends BaseCustomersFragment {
    private SimpleCustomerListAdapter simpleCustomerListAdapter;

    private TextView tvNoCustomers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_customers_fragment_lv, container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        tvNoCustomers = (TextView) view.findViewById(R.id.tvNoCustomers);

        lvCustomers = (ListView) view.findViewById(R.id.lvCustomers);
        simpleCustomerListAdapter = new SimpleCustomerListAdapter(getActivity(), getCustomers(), "#22000000");

        lvCustomers.setAdapter(simpleCustomerListAdapter);
        lvCustomers.setOnItemClickListener(simpleCustomerListAdapter.getOnItemClickListener());

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
}
