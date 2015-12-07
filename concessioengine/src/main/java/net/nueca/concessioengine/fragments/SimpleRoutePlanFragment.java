package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleRoutePlanRecyclerViewAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.customer.Customer;

import java.util.ArrayList;

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

    private SimpleRoutePlanRecyclerViewAdapter simpleRoutePlanRecyclerViewAdapter;
    private ArrayAdapter<String> daysAdapter;
    private SetupActionBar setupActionBar;

    private ArrayList<String> days = new ArrayList<String>(){{
        add("Sunday");
        add("Monday");
        add("Tuesday");
        add("Wednesday");
        add("Thursday");
        add("Friday");
        add("Saturday");
    }};
    private ArrayList<String> routes = new ArrayList<String>(){{
        add("a");
        add("a");
        add("a");
        add("a");
        add("a");
        add("a");
        add("a");
        add("a");
        add("a");
        add("a");
        add("a");
        add("a");
        add("a");
        add("a");
    }};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        simpleRoutePlanRecyclerViewAdapter = new SimpleRoutePlanRecyclerViewAdapter(getActivity(), routes);
        daysAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_dark, days);
        daysAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_light);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_route_plan_fragment_rv, container, false);

        rvRoutePlan = (RecyclerView) view.findViewById(R.id.rvRoutePlan);
        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        spDays = (Spinner) view.findViewById(R.id.spDays);

        simpleRoutePlanRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvRoutePlan);
        simpleRoutePlanRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                if(routePlanListener != null)
                    routePlanListener.itemClicked(null); // pass the customer
            }
        });
        rvRoutePlan.setAdapter(simpleRoutePlanRecyclerViewAdapter);
        spDays.setAdapter(daysAdapter);

        return view;
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
}
