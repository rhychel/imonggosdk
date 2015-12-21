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
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 12/2/15.
 */
public class SimpleCustomerDetailsFragment extends BaseCustomersFragment {
    private Customer customer;

    private SimpleCustomerDetailsRecyclerViewAdapter simpleCustomerDetailsRecyclerViewAdapter;
    private ArrayList<CustomerDetail> customerDetails = new ArrayList<CustomerDetail>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        customerDetails.add(CustomerDetail.MOBILE_NO.setValue(customer.getMobile()));
        customerDetails.add(CustomerDetail.TEL_NO.setValue(customer.getTelephone()));
        customerDetails.add(CustomerDetail.COMPANY_NAME.setValue(customer.getCompany_name()));
        customerDetails.add(CustomerDetail.ADDRESS.setValue(customer.getFullAddress()));
        try {
            CustomerCategory customerCategory = getHelper().fetchIntId(CustomerCategory.class).queryBuilder().where().eq("id", Integer.valueOf(customer.getExtras().getCategory_id())).queryForFirst();
            if(customerCategory != null)
                customerDetails.add(CustomerDetail.CUSTOMER_TYPE.setValue(customerCategory.getName()));
            if(customer.getPaymentTerms() != null)
                customerDetails.add(CustomerDetail.TERMS.setValue(customer.getPaymentTerms().getName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        customerDetails.add(CustomerDetail.SALES_ROUTE);
        customerDetails.add(CustomerDetail.DISCOUNT);
        customerDetails.add(CustomerDetail.LAST_PURCHASE_DETAILS);

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

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
