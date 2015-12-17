package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleCustomerDetailsRecyclerViewAdapter;
import net.nueca.concessioengine.enums.CustomerDetail;
import net.nueca.concessioengine.adapters.tools.DividerItemDecoration;
import net.nueca.imonggosdk.objects.customer.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 12/2/15.
 */
public class SimpleCustomerDetailsFragment extends BaseCustomersFragment {

    private SimpleCustomerDetailsRecyclerViewAdapter simpleCustomerDetailsRecyclerViewAdapter;
    private ArrayList<CustomerDetail> customerDetails = new ArrayList<CustomerDetail>(){{
        add(CustomerDetail.CODE);
        add(CustomerDetail.MOBILE_NO);
        add(CustomerDetail.TEL_NO);
        add(CustomerDetail.COMPANY_NAME);
        add(CustomerDetail.ADDRESS);
        add(CustomerDetail.CUSTOMER_TYPE);
        add(CustomerDetail.TERMS);
        add(CustomerDetail.SALES_ROUTE);
        add(CustomerDetail.DISCOUNT);
        add(CustomerDetail.LAST_PURCHASE_DETAILS);
    }};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        simpleCustomerDetailsRecyclerViewAdapter = new SimpleCustomerDetailsRecyclerViewAdapter(getActivity(), customerDetails);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_customer_details, container, false);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        rvCustomers = (RecyclerView) view.findViewById(R.id.rvCustomerDetails);
        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);

        simpleCustomerDetailsRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvCustomers);
        rvCustomers.setAdapter(simpleCustomerDetailsRecyclerViewAdapter);

        return view;
    }

    @Override
    protected void toggleNoItems(String msg, boolean show) { }

    @Override
    protected void whenListEndReached(List<Customer> customers) { }
}
