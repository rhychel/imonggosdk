package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleCustomerDetailsRecyclerViewAdapter;
import net.nueca.concessioengine.enums.CustomerDetail;
import net.nueca.concessioengine.adapters.tools.DividerItemDecoration;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerCategory;
import net.nueca.imonggosdk.objects.invoice.PaymentTerms;
import net.nueca.imonggosdk.objects.routeplan.RoutePlanDetail;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

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

        renderCustomerDetails(false);

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

    public void renderCustomerDetails(boolean refreshList) {
        customerDetails.clear();
        customerDetails.add(CustomerDetail.CODE.setValue(customer.getCode().isEmpty() ? "None" : customer.getCode()));
        customerDetails.add(CustomerDetail.MOBILE_NO.setValue(customer.getMobile().isEmpty() ? "None" : customer.getMobile()));
        customerDetails.add(CustomerDetail.TEL_NO.setValue(customer.getTelephone().isEmpty() ? "None" : customer.getTelephone()));
        customerDetails.add(CustomerDetail.COMPANY_NAME.setValue(customer.getCompany_name().isEmpty() ? "None" : customer.getCompany_name()));
        customerDetails.add(CustomerDetail.ADDRESS.setValue(customer.generateAddress().isEmpty() ? "None" : customer.generateAddress()));
        if(customer.getExtras() != null && customer.getExtras().getCustomerCategory() != null)
            customerDetails.add(CustomerDetail.CUSTOMER_TYPE.setValue(customer.getExtras().getCustomerCategory().getName()));
        else
            customerDetails.add(CustomerDetail.CUSTOMER_TYPE.setValue("None"));

        if(customer.getPaymentTerms() != null)
            customerDetails.add(CustomerDetail.TERMS.setValue(customer.getPaymentTerms().getName()));
        else
            customerDetails.add(CustomerDetail.TERMS.setValue("None"));

//        try {
////            CustomerCategory customerCategory = getHelper().fetchIntId(CustomerCategory.class).queryBuilder().where().eq("id", customer.getCustomerCategory()).queryForFirst();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        if(getModuleSetting(ConcessioModule.CUSTOMERS).isHas_route_plan()) {
            try {
                List<RoutePlanDetail> routePlanDetails = getHelper().fetchForeignCollection(customer.getRoutePlanDetails().closeableIterator());
                String route = "None";
                HashMap<String, String> days = new HashMap<String, String>(){{
                    put("SU", "Sunday");
                    put("M", "Monday");
                    put("TU", "Tuesday");
                    put("W", "Wednesday");
                    put("TH", "Thursday");
                    put("F", "Friday");
                    put("SA", "Saturday");
                }};
                for(RoutePlanDetail routePlanDetail : routePlanDetails) {
                    if(routePlanDetail.getRoutePlan().getStatus() == null) {
                        route = days.get(routePlanDetail.getRoute_day())+", "+routePlanDetail.getFrequency();
                        break;
                    }
                }
                customerDetails.add(CustomerDetail.SALES_ROUTE.setValue(route));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        customerDetails.add(CustomerDetail.DISCOUNT.setValue(customer.getDiscount_text().isEmpty() ? "None" : customer.getDiscount_text()));
        customerDetails.add(CustomerDetail.AVAILABLE_POINTS.setValue(customer.getAvailable_points()));

        if(!customer.getLastPurchase().isEmpty()) {
            SimpleDateFormat fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fromDate.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy, cccc h:mma");
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            try {
                Date date = fromDate.parse(customer.getLastPurchase().split("T")[0] + " " + customer.getLastPurchase().split("T")[1].replace("Z", ""));

                customerDetails.add(CustomerDetail.LAST_PURCHASE_DETAILS.setValue(simpleDateFormat.format(date)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else
            customerDetails.add(CustomerDetail.LAST_PURCHASE_DETAILS.setValue("None"));
        if(refreshList) {
            if(setupActionBar != null)
                setupActionBar.setupActionBar(tbActionBar);
            simpleCustomerDetailsRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void toggleNoItems(String msg, boolean show) { }

    @Override
    protected void whenListEndReached(List<Customer> customers) { }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
