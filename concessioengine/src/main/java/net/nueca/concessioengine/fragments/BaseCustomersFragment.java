package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.adapters.SimpleCustomerListAdapter;
import net.nueca.concessioengine.adapters.SimpleCustomerRecyclerViewAdapter;
import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
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

    protected SimpleCustomerListAdapter simpleCustomerListAdapter;
    protected SimpleCustomerRecyclerViewAdapter simpleCustomerRecyclerViewAdapter;

    private String searchKey;

    protected ListView lvCustomers;
    protected RecyclerView rvCustomers;
    protected Toolbar tbActionBar;
    protected SetupActionBar setupActionBar;

    protected ListScrollListener listScrollListener;

    protected abstract void toggleNoItems(String msg, boolean show);

    protected List<Customer> getCustomers() {
        List<Customer> customers = new ArrayList<>();
        boolean hasSearchKey = searchKey != null && !searchKey.isEmpty();

        try {
            Where<Customer, Integer> whereCustomers = getHelper().getCustomers().queryBuilder().where();
            whereCustomers.isNull("status");
            if(hasSearchKey) {
                whereCustomers.and();
                whereCustomers.like("name", "%" + searchKey + "%");
                whereCustomers.or().like("alternate_code", "%" + searchKey + "%");
            }

            QueryBuilder<Customer, Integer> resultCustomers = getHelper().getCustomers().queryBuilder()
                    .orderByRaw("name COLLATE NOCASE ASC").limit(LIMIT).offset(offset);
            resultCustomers.setWhere(whereCustomers);

            customers = resultCustomers.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
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

    protected abstract void whenListEndReached(List<Customer> customers);

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
            int totalItemCount = simpleCustomerRecyclerViewAdapter.getLinearLayoutManager().getItemCount();
            int firstVisibleItem = simpleCustomerRecyclerViewAdapter.getLinearLayoutManager()
                    .findFirstVisibleItemPosition();

            int lastItem = firstVisibleItem + visibleItemCount;

            if(lastItem == totalItemCount) {
                if(prevLast != lastItem) {
                    offset += LIMIT;
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
        if(useRecyclerView)
            return simpleCustomerRecyclerViewAdapter.getSelectedCustomers();
        else
            return simpleCustomerListAdapter.getSelectedCustomers();
    }
}
