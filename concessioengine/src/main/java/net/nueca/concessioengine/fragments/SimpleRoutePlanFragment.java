package net.nueca.concessioengine.fragments;

import android.os.Bundle;
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

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleRoutePlanRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.objects.Day;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
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
public class SimpleRoutePlanFragment extends ImonggoFragment {

    public interface RoutePlanListener {
        void itemClicked(Customer customer);
    }

    private RoutePlanListener routePlanListener;

    private RecyclerView rvRoutePlan;
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
    private String searchKey = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        simpleRoutePlanRecyclerViewAdapter = new SimpleRoutePlanRecyclerViewAdapter(getActivity(), routes);
        daysAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_dark, days);
        daysAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_route_plan_fragment_rv, container, false);

        rvRoutePlan = (RecyclerView) view.findViewById(R.id.rvRoutePlan);
        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        spDays = (Spinner) view.findViewById(R.id.spDays);
        tvNoRoutes = (TextView) view.findViewById(R.id.tvNoRoutes);

        simpleRoutePlanRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvRoutePlan);
        simpleRoutePlanRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                if (routePlanListener != null)
                    routePlanListener.itemClicked(simpleRoutePlanRecyclerViewAdapter.getItem(position)); // pass the customer
            }
        });
        rvRoutePlan.setAdapter(simpleRoutePlanRecyclerViewAdapter);
        spDays.setAdapter(daysAdapter);
        spDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentDayOfWeek = days.get(position).getDayOfWeek();
//                renderRoutePlan(currentDayOfWeek);
//                simpleRoutePlanRecyclerViewAdapter.notifyDataSetChanged();
                boolean shouldShow = simpleRoutePlanRecyclerViewAdapter.updateList(renderRoutePlan(currentDayOfWeek));
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
        toggleNoItems("No results for \"" + searchKey + "\".",
                simpleRoutePlanRecyclerViewAdapter.updateList(renderRoutePlan(currentDayOfWeek)));
    }


    protected void toggleNoItems(String msg, boolean show) {
        rvRoutePlan.setVisibility(show ? View.VISIBLE : View.GONE);
        tvNoRoutes.setVisibility(show ? View.GONE : View.VISIBLE);
        tvNoRoutes.setText(msg);
    }

    private List<Customer> renderRoutePlan(int dayOfWeek) {
        List<Customer> routes = new ArrayList<>();
        Day day = days.get(days.indexOf(new Day(dayOfWeek)));
        try {
            RoutePlan routePlan = getHelper().fetchIntId(RoutePlan.class).queryBuilder().where().isNull("status").and().eq("user_id", getSession().getUser()).queryForFirst();
            if(routePlan == null)
                return routes;
            List<RoutePlanDetail> routePlanDetails = getHelper().fetchForeignCollection(routePlan.getRoutePlanDetails().closeableIterator());
            for(RoutePlanDetail routePlanDetail : routePlanDetails) {
                if(!day.getShortname().equals(routePlanDetail.getRoute_day()))
                    continue;
                if(!searchKey.trim().isEmpty() && !routePlanDetail.getCustomer().generateFullName().contains(searchKey)) {
                    Log.e("searchKey", searchKey+"----");
                    continue;
                }
                routes.add(routePlanDetail.getCustomer());
                Log.e("frequency", routePlanDetail.getFrequency());
                Log.e("route day", routePlanDetail.getRoute_day());
                Log.e("sequence", routePlanDetail.getSequence()+"");
                if(routePlanDetail.getCustomer() == null)
                    Log.e("Customer", "is null");
                else
                    Log.e("Customer", "is not null");

                Log.e("Customer", routePlanDetail.getCustomer().getName()+" -- "+routes.size());
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

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }
}
