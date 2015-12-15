package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.adapters.SimpleCustomerListAdapter;
import net.nueca.concessioengine.adapters.SimpleCustomerRecyclerViewAdapter;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.customer.Customer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 8/11/15.
 */
public abstract class BaseCustomersFragment extends ImonggoFragment {
    protected static final long LIMIT = 100l;
    protected long offset = 0l;
    private int prevLast = -1;
    protected boolean useRecyclerView = true;
    protected ListingType listingType = ListingType.BASIC;

    protected SimpleCustomerListAdapter simpleCustomerListAdapter;
    protected SimpleCustomerRecyclerViewAdapter simpleCustomerRecyclerViewAdapter;

    private String searchKey;

    protected ListView lvCustomers;
    protected RecyclerView rvCustomers;
    protected Toolbar tbActionBar;
    protected SetupActionBar setupActionBar;

    protected ListScrollListener listScrollListener;

    protected abstract void toggleNoItems(String msg, boolean show);
    protected abstract void whenListEndReached(List<Customer> customers);

    protected List<Customer> getCustomers() {
        List<Customer> customers = new ArrayList<>();
        boolean hasSearchKey = searchKey != null && !searchKey.isEmpty();

        try {
            Where<Customer, Integer> whereCustomers = getHelper().fetchIntId(Customer.class).queryBuilder().where();
            whereCustomers.isNull("status");
            if(hasSearchKey) {
                whereCustomers.and();
                whereCustomers.like("name", "%" + searchKey + "%");
                whereCustomers.or().like("alternate_code", "%" + searchKey + "%");
            }

            QueryBuilder<Customer, Integer> resultCustomers = getHelper().fetchIntId(Customer.class).queryBuilder()
                    .orderByRaw("name COLLATE NOCASE ASC").limit(LIMIT).offset(offset);
            resultCustomers.setWhere(whereCustomers);

            customers = resultCustomers.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    protected List<Customer> processCustomersForLetterHeader(List<Customer> newCustomers) {
        return processCustomersForLetterHeader(newCustomers, null, 0);
    }

    protected List<Customer> processCustomersForLetterHeader(List<Customer> newCustomers, Customer lastCustomer, int lastIndex) {

        ArrayList<Customer> finalCustomers = new ArrayList<>();
        String lastHeader = "";
        int sectionFirstPosition = 0;
        if(lastCustomer != null) {
            sectionFirstPosition = lastCustomer.getSectionFirstPosition();
            lastHeader = lastCustomer.getLetterHeader();
        }
        int sectionItemCount = lastIndex;
        int headerCtr = 0;
        for (int i = 0; i < newCustomers.size(); i++) {
            Customer customer = newCustomers.get(i);
            String name;
            if(customer.getName() != null && customer.getName().length() > 0)
                name = customer.getName();
            else
                name = customer.getFirst_name() + " " + customer.getLast_name();
            name = name.trim();
            String header = name.substring(0, 1);

            if(customer.isHeader() || customer.getSectionFirstPosition() > -1) {
                sectionItemCount++;
                continue;
            }

            if (!TextUtils.equals(lastHeader, header)) {
                sectionFirstPosition = sectionItemCount;
                lastHeader = header;

                Customer customerHeader = new Customer();
                customerHeader.setIsHeader(true);
                customerHeader.setSectionFirstPosition(sectionFirstPosition);
                customerHeader.setLetterHeader(header);

                finalCustomers.add(customerHeader);
                sectionItemCount++;
            }
            customer.setIsHeader(false);
            customer.setSectionFirstPosition(sectionFirstPosition);
            customer.setLetterHeader(header);
            finalCustomers.add(customer);
            sectionItemCount++;
        }

        return finalCustomers;
    }

    protected void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public SetupActionBar getSetupActionBar() {
        return setupActionBar;
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    public Toolbar getActionBar() {
        return tbActionBar;
    }

    public void setActionBar(Toolbar tbActionBar) {
        this.tbActionBar = tbActionBar;
    }

    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }

    public void setUseRecyclerView(boolean useRecyclerView) {
        this.useRecyclerView = useRecyclerView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    public void setListScrollListener(ListScrollListener listScrollListener) {
        this.listScrollListener = listScrollListener;
    }

    protected AbsListView.OnScrollListener lvScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch(scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    if (listScrollListener != null)
                        listScrollListener.onScrollStopped();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    if (listScrollListener != null)
                        listScrollListener.onScrolling();
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int lastItem = firstVisibleItem + visibleItemCount;
            if (lastItem == totalItemCount) {
                if (prevLast != lastItem) {
                    offset += LIMIT;
                    whenListEndReached(getCustomers());
                    prevLast = lastItem;
                }
            }
        }
    };

    protected RecyclerView.OnScrollListener rvScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView view, int scrollState) {
            switch(scrollState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    Log.e("SCROLL IDLE","---");
                    if (listScrollListener != null)
                        listScrollListener.onScrollStopped();
                    break;
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    if (listScrollListener != null)
                        listScrollListener.onScrolling();
                    break;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleItemCount = rvCustomers.getChildCount();
            int totalItemCount = 0;
            int firstVisibleItem = 0;
            if(listingType == ListingType.LETTER_HEADER) {
                totalItemCount = simpleCustomerRecyclerViewAdapter.getLayoutManager().getItemCount();
                firstVisibleItem = simpleCustomerRecyclerViewAdapter.getLayoutManager().findFirstVisibleItemPosition();

//                totalItemCount = simpleCustomerRecyclerViewAdapter2.getLayoutManager().getItemCount();
//                firstVisibleItem = simpleCustomerRecyclerViewAdapter2.getLayoutManager().findFirstVisibleItemPosition();
            }
            else {
                totalItemCount = simpleCustomerRecyclerViewAdapter.getLinearLayoutManager().getItemCount();
                firstVisibleItem = simpleCustomerRecyclerViewAdapter.getLinearLayoutManager()
                        .findFirstVisibleItemPosition();
            }
            int lastItem = firstVisibleItem + visibleItemCount;

            if(lastItem == totalItemCount) {
                if(prevLast != lastItem) {
                    offset += LIMIT;
                    if(listingType == ListingType.LETTER_HEADER) {
                        Customer lastCustomer = simpleCustomerRecyclerViewAdapter.getItem(lastItem-1);
                        List<Customer> customersToBeAdded = getCustomers();

                        whenListEndReached(processCustomersForLetterHeader(customersToBeAdded, lastCustomer, lastItem));
                    }
                    else
                        whenListEndReached(getCustomers());
                    prevLast = lastItem;
                }
            }
        }
    };

    public SimpleCustomerListAdapter getListAdapter() {
        return simpleCustomerListAdapter;
    }
    public SimpleCustomerRecyclerViewAdapter getRecyclerAdapter() {
        return simpleCustomerRecyclerViewAdapter;
    }

    public List<Customer> getSelectedCustomers() {
        if(useRecyclerView) {
            return simpleCustomerRecyclerViewAdapter.getSelectedCustomers();
        }
        else
            return simpleCustomerListAdapter.getSelectedCustomers();
    }
}
