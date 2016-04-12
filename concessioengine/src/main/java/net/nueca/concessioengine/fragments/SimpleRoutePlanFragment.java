package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleRoutePlanRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.objects.Day;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.routeplan.RoutePlan;
import net.nueca.imonggosdk.objects.routeplan.RoutePlanDetail;

import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by rhymart on 12/1/15.
 */
public class SimpleRoutePlanFragment extends BaseCustomersFragment {

    public interface RoutePlanListener {
        void itemClicked(Customer customer);
    }

    private RoutePlanListener routePlanListener;

    private Toolbar tbActionBar;
    private Spinner spDays;
    private TextView tvNoRoutes;

    private SimpleRoutePlanRecyclerViewAdapter simpleRoutePlanRecyclerViewAdapter;
    private ArrayAdapter<Day> daysAdapter;
    private SetupActionBar setupActionBar;

    private ArrayList<Day> days = new ArrayList<Day>(){{
        add(new Day("Sunday", "SU", 1));
        add(new Day("Monday", "M", 2));
        add(new Day("Tuesday", "TU", 3));
        add(new Day("Wednesday", "W", 4));
        add(new Day("Thursday", "TH", 5));
        add(new Day("Friday", "F", 6));
        add(new Day("Saturday", "SA", 7));
    }};
    private ArrayList<Customer> routes = new ArrayList<>();

    private int currentDayOfWeek = 0;
    private int todayPosition = 0;
    private boolean canShowAllCustomers = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ProductsAdapterHelper.setDbHelper(getHelper());

        simpleRoutePlanRecyclerViewAdapter = new SimpleRoutePlanRecyclerViewAdapter(getActivity(), routes);
        if(canShowAllCustomers)
            days.add(0, new Day("All", 0));
        daysAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_dark, days);
        daysAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_route_plan_fragment_rv, container, false);

        rvCustomers = (RecyclerView) view.findViewById(R.id.rvRoutePlan);
        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        spDays = (Spinner) view.findViewById(R.id.spDays);
        tvNoRoutes = (TextView) view.findViewById(R.id.tvNoRoutes);

        simpleRoutePlanRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvCustomers);
        simpleRoutePlanRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                Gson gson = new Gson();
                Log.e("><><><><><><><><><><><", gson.toJson(simpleRoutePlanRecyclerViewAdapter.getList()));
                if (routePlanListener != null)
                    routePlanListener.itemClicked(simpleRoutePlanRecyclerViewAdapter.getItem(position)); // pass the customer
            }
        });
        rvCustomers.setAdapter(simpleRoutePlanRecyclerViewAdapter);
        rvCustomers.addOnScrollListener(rvScrollListener);
        setListScrollListener(new ListScrollListener() {
            @Override
            public void onScrolling() { }

            @Override
            public void onScrollStopped() { }

            @Override
            public int getTotalItemCount() {
                return simpleRoutePlanRecyclerViewAdapter.getLinearLayoutManager().getItemCount();
            }

            @Override
            public int getFirstVisibleItem() {
                return simpleRoutePlanRecyclerViewAdapter.getLinearLayoutManager().findFirstVisibleItemPosition();
            }
        });

        spDays.setAdapter(daysAdapter);
        spDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean shouldShow = false;
                currentDayOfWeek = days.get(position).getDayOfWeek();
                if(currentDayOfWeek == 0) {
                    shouldShow = simpleRoutePlanRecyclerViewAdapter.updateList(getCustomers());
                    rvCustomers.addOnScrollListener(rvScrollListener);
                }
                else {
                    shouldShow = simpleRoutePlanRecyclerViewAdapter.updateList(renderRoutePlan(currentDayOfWeek));
                    rvCustomers.removeOnScrollListener(rvScrollListener);
                }
                Log.e("shouldShow", shouldShow+"");
                toggleNoItems(todayPosition == position ? "No customers today." : "No customers this "+days.get(position).getFullname()+".", shouldShow);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        todayPosition = days.indexOf(new Day(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)));
        spDays.setSelection(todayPosition);

        return view;
    }

    public void updateListWhenSearch(String searchKey) {
        setSearchKey(searchKey);
        if(currentDayOfWeek == 0)
            toggleNoItems("No results for \"" + searchKey + "\".", simpleRoutePlanRecyclerViewAdapter.updateList(getCustomers()));
        else
            toggleNoItems("No results for \"" + searchKey + "\".", simpleRoutePlanRecyclerViewAdapter.updateList(renderRoutePlan(currentDayOfWeek)));
    }


    protected void toggleNoItems(String msg, boolean show) {
        rvCustomers.setVisibility(show ? View.VISIBLE : View.GONE);
        tvNoRoutes.setVisibility(show ? View.GONE : View.VISIBLE);
        tvNoRoutes.setText(msg);
    }

    public void refresh() {
        simpleRoutePlanRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void whenListEndReached(List<Customer> customers) {
        simpleRoutePlanRecyclerViewAdapter.addAll(customers);
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                simpleRoutePlanRecyclerViewAdapter.notifyDataSetChanged();
            }
        };
        handler.sendEmptyMessageDelayed(0, 200);
    }

    private List<Customer> renderRoutePlan(int dayOfWeek) {
        List<Customer> routes = new ArrayList<>();
        Day day = days.get(days.indexOf(new Day(dayOfWeek)));
        try {
            RoutePlan routePlan = getHelper().fetchIntId(RoutePlan.class).queryBuilder().where().isNull("status").and().eq("user_id", getSession().getUser()).queryForFirst();
//            Log.e("RoutePlan", "has route plan?");
            if(routePlan == null)
                return routes;
//            Log.e("RoutePlan", "has route plan");

            List<RoutePlanDetail> routePlanDetails = getHelper().fetchForeignCollection(routePlan.getRoutePlanDetails().closeableIterator());
//            Log.e("RoutePlan", "has route plan details?"+routePlanDetails.size()+" --- "+routePlan.getRoutePlanDetails().size());

            Log.e("Week of the Year", Calendar.getInstance().get(Calendar.DAY_OF_YEAR)+" day");
//            Log.e("Week", Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)+" wk");
            boolean isOdd = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH) % 2 == 1;
            for(RoutePlanDetail routePlanDetail : routePlanDetails) {
                Log.e("RoutePlan", "route plan details="+routePlanDetail.getFrequency()+"---");
                if(!day.getShortname().equals(routePlanDetail.getRoute_day()))
                    continue;
                Log.e("RoutePlanSearch", routePlanDetail.getCustomer()+"");
                if(searchKey != null && (!searchKey.trim().isEmpty() && routePlanDetail.getCustomer()!= null && !routePlanDetail.getCustomer().getSearchKey().toLowerCase().contains(searchKey))) {
                    Log.e("searchKey", searchKey+"----");
                    continue;
                }

                if(routePlanDetail.getFrequency().equals("BM2") && isOdd)
                    continue;
                routes.add(routePlanDetail.getCustomer());
                Log.e("frequency", routePlanDetail.getFrequency());
                Log.e("route day", routePlanDetail.getRoute_day());
                Log.e("sequence", routePlanDetail.getSequence()+"");
                Log.e("RoutePlan", "route plan details="+routePlanDetail.getFrequency()+"---");
                if(routePlanDetail.getCustomer() == null)
                    Log.e("Customer", "is null");
                else {
                    Log.e("Customer", "is not null");
                    Log.e("Customer", routePlanDetail.getCustomer().getName() + " -- " + routes.size());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return routes;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    public void setRoutePlanListener(RoutePlanListener routePlanListener) {
        this.routePlanListener = routePlanListener;
    }

    public void setCanShowAllCustomers(boolean canShowAllCustomers) {
        this.canShowAllCustomers = canShowAllCustomers;
    }
}
