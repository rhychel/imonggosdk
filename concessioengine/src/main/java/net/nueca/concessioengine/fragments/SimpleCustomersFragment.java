package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.imonggosdk.objects.customer.Customer;

import java.util.List;

/**
 * Created by gama on 8/11/15.
 */
public class SimpleCustomersFragment extends BaseCustomersFragment {

    public interface OnCustomerSelectedListener {
        void onCustomerSelected(Customer customer, int position);
    }

    private TextView tvNoCustomers;
    private boolean isMultiSelect = false;
    private boolean hasSelected = false;
    private Integer color, highlightColor;

    private OnCustomerSelectedListener onCustomerSelectedListener;

    public void setMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
    }
    public void setColor(Integer color) {
        this.color = color;
    }
    public void setHighlightColor(Integer highlightColor) {
        this.highlightColor = highlightColor;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(useRecyclerView) {
            if(listingType == ListingType.BASIC)
                simpleCustomerRecyclerViewAdapter = new SimpleCustomerRecyclerViewAdapter(getActivity(), getCustomers(),
                        isMultiSelect, highlightColor);
            else if(listingType == ListingType.LETTER_HEADER) {
                simpleCustomerRecyclerViewAdapter = new SimpleCustomerRecyclerViewAdapter(getActivity(), processCustomersForLetterHeader(getCustomers()),
                        isMultiSelect, highlightColor);
                simpleCustomerRecyclerViewAdapter.setListingType(listingType);
            }
            if (color != null) {
                simpleCustomerRecyclerViewAdapter.setCircleColor(color);
                tbActionBar.setBackgroundColor(color);
            }
        }
        else {
            simpleCustomerListAdapter = new SimpleCustomerListAdapter(getActivity(), getCustomers(), isMultiSelect,
                    highlightColor);

            if (color != null) {
                simpleCustomerListAdapter.setCircleColor(color);
                tbActionBar.setBackgroundColor(color);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(useRecyclerView ?
                        R.layout.simple_customers_fragment_rv : R.layout.simple_customers_fragment_lv,
                container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        tvNoCustomers = (TextView) view.findViewById(R.id.tvNoCustomers);

        if(useRecyclerView) {
            rvCustomers = (RecyclerView) view.findViewById(R.id.rvCustomers);
            /*simpleCustomerRecyclerViewAdapter = new SimpleCustomerRecyclerViewAdapter(getActivity(), getCustomers(),
                    isMultiSelect, highlightColor);

            if (color != null) {
                simpleCustomerRecyclerViewAdapter.setCircleColor(color);
                tbActionBar.setBackgroundColor(color);
            }*/
            simpleCustomerRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    if(isMultiSelect) {

                    }
                    else {
                        if(onCustomerSelectedListener != null)
                            onCustomerSelectedListener.onCustomerSelected(simpleCustomerRecyclerViewAdapter.getSelectedCustomers().get(0), position);
                        hasSelected = true;
                    }
                }
            });
            simpleCustomerRecyclerViewAdapter.initializeRecyclerView(getActivity(), rvCustomers);
            rvCustomers.setAdapter(simpleCustomerRecyclerViewAdapter);

            rvCustomers.addOnScrollListener(rvScrollListener);

            toggleNoItems("No customers in the list.", (simpleCustomerRecyclerViewAdapter.getItemCount() > 0));
        }
        else {
            lvCustomers = (ListView) view.findViewById(R.id.lvCustomers);
            /*simpleCustomerListAdapter = new SimpleCustomerListAdapter(getActivity(), getCustomers(), isMultiSelect,
                    highlightColor);

            if (color != null) {
                simpleCustomerListAdapter.setCircleColor(color);
                tbActionBar.setBackgroundColor(color);
            }*/

            lvCustomers.setAdapter(simpleCustomerListAdapter);
            lvCustomers.setOnItemClickListener(simpleCustomerListAdapter.getOnItemClickListener());
            lvCustomers.setOnScrollListener(lvScrollListener);

            toggleNoItems("No customers in the list.", (simpleCustomerListAdapter.getCount() > 0));
        }

        return view;
    }

    public void deselectCustomers() {
        simpleCustomerRecyclerViewAdapter.getSelectedCustomers().clear();
        simpleCustomerRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void deselectCustomer(Customer customer) {
        deselectCustomer(customer, -1);
    }

    public void deselectCustomer(Customer customer, int index) {
        if(useRecyclerView) {
            simpleCustomerRecyclerViewAdapter.removeCustomer(customer);
            if(index > -1)
                simpleCustomerRecyclerViewAdapter.notifyItemChanged(index);
            else
                simpleCustomerRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {
        tvNoCustomers.setVisibility(show ? View.GONE : View.VISIBLE);
        tvNoCustomers.setText(msg);

        if(useRecyclerView) {
            rvCustomers.setVisibility(show ? View.VISIBLE : View.GONE);
//            rvCustomers.smoothScrollToPosition(0);
        }
        else {
            lvCustomers.setVisibility(show ? View.VISIBLE : View.GONE);
            lvCustomers.smoothScrollToPosition(0);
        }
    }

    @Override
    protected void whenListEndReached(List<Customer> customers) {
        if(useRecyclerView) {
            simpleCustomerRecyclerViewAdapter.addAll(customers);
            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    simpleCustomerRecyclerViewAdapter.notifyDataSetChanged();
                }
            };
            handler.sendEmptyMessageDelayed(0, 100);
        }
        else
            simpleCustomerListAdapter.addAll(customers);
    }

    public void reinitializeList() {
        offset = 0l;
        prevLast = -1;

        simpleCustomerRecyclerViewAdapter.removeAll();
        simpleCustomerRecyclerViewAdapter.notifyDataSetChanged();
        simpleCustomerRecyclerViewAdapter.setList(processCustomersForLetterHeader(getCustomers()));
    }

    public void refreshList() {
        if(useRecyclerView) {
            simpleCustomerRecyclerViewAdapter.notifyDataSetChanged();
        }
        else
            simpleCustomerListAdapter.notifyDataSetChanged();
    }

    public void updateListWhenSearch(String searchKey) {
        setSearchKey(searchKey);
        offset = 0l;
        headerCount = 0;

        if(useRecyclerView) {
            if(listingType == ListingType.BASIC)
                toggleNoItems("No results for \"" + searchKey + "\"" + ".", simpleCustomerRecyclerViewAdapter.updateList(getCustomers()));
            else if(listingType == ListingType.LETTER_HEADER)
                toggleNoItems("No results for \"" + searchKey + "\"" + ".", simpleCustomerRecyclerViewAdapter.updateList(processCustomersForLetterHeader(getCustomers())));
        }
        else
            toggleNoItems("No results for \"" + searchKey + "\"" + ".",
                    simpleCustomerListAdapter.updateList(getCustomers()));
    }

    public void setOnCustomerSelectedListener(OnCustomerSelectedListener onCustomerSelectedListener) {
        this.onCustomerSelectedListener = onCustomerSelectedListener;
    }

    public boolean isHasSelected() {
        return hasSelected;
    }

    public void setHasSelected(boolean hasSelected) {
        this.hasSelected = hasSelected;
    }
}