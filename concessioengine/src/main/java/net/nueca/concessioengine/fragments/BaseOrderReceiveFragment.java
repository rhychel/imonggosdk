package net.nueca.concessioengine.fragments;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.Gson;

import net.nueca.concessioengine.adapters.base.BaseOrderReceiveRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.objects.ReceivedMultiItem;
import net.nueca.concessioengine.objects.ReceivedProductItem;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.Product;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 01/06/2016.
 */
public abstract class BaseOrderReceiveFragment extends ImonggoFragment {
    protected static final int LIMIT = 50;
    protected int offset = 0;
    protected int prevLast = 0;

    private String searchKey = "", category = "";
    protected boolean hasCategories = true;

    protected RecyclerView rvItems;
    protected Toolbar tbActionBar;
    protected SetupActionBar setupActionBar;
    protected Spinner spCategories;

    protected ArrayAdapter<String> productCategoriesAdapter;
    protected List<String> productCategories = new ArrayList<>();

    protected ListScrollListener listScrollListener;

    protected BaseOrderReceiveRecyclerAdapter orderReceiveRecyclerAdapter;

    protected abstract void whenListEndReached(List<ReceivedMultiItem> items);
    protected abstract void toggleNoItems(String msg, boolean show);

    protected List<ReceivedMultiItem> getOrderItems() {
        List<ReceivedProductItem> list = null;
        try {
            list = ProductsAdapterHelper.getReceivedProductItems().toList(getHelper(), searchKey, category);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int remaining = list.size() - offset;
        Log.e("getOrderItems", "offset " + offset + " size " + list.size());

        if(remaining < 0)
            return new ArrayList<>();

        return ReceivedMultiItem.generateReceivedMultiItem(list.subList(offset, remaining >= LIMIT? LIMIT : remaining));
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isHasCategories() {
        return hasCategories;
    }

    public void setHasCategories(boolean hasCategories) {
        this.hasCategories = hasCategories;
    }

    public BaseOrderReceiveRecyclerAdapter getOrderReceiveRecyclerAdapter() {
        return orderReceiveRecyclerAdapter;
    }

    public void setOrderReceiveRecyclerAdapter(BaseOrderReceiveRecyclerAdapter orderReceiveRecyclerAdapter) {
        this.orderReceiveRecyclerAdapter = orderReceiveRecyclerAdapter;
    }

    public void refreshList() {
        this.orderReceiveRecyclerAdapter.notifyDataSetChanged();
    }

    public SetupActionBar getSetupActionBar() {
        return setupActionBar;
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    public void setProductCategories(List<String> productCategories) {
        this.productCategories = productCategories;
    }

    public String messageCategory() {
        return category.toLowerCase().equals("All") ? "" : " in \""+category+"\" category";
    }

    protected RecyclerView.OnScrollListener rvScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (listScrollListener != null)
                    listScrollListener.onScrollStopped();
            }
            else if(newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                if(listScrollListener != null)
                    listScrollListener.onScrolling();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleItemCount = rvItems.getChildCount();
            int totalItemCount;
            int firstVisibleItem;

            totalItemCount = orderReceiveRecyclerAdapter.getLinearLayoutManager().getItemCount();
            firstVisibleItem = orderReceiveRecyclerAdapter.getLinearLayoutManager().findFirstVisibleItemPosition();

            int lastItem = firstVisibleItem + visibleItemCount;

            //Log.e("BaseProducts", "lastItem ="+lastItem+" | totalItemCount = "+totalItemCount+" | prevLast = "+prevLast);
            if(lastItem == totalItemCount) {
                if(prevLast != lastItem) {
                    offset += LIMIT;
                    prevLast = lastItem;
                    if(offset <= prevLast)
                        whenListEndReached(getOrderItems());
                }
            }
        }
    };
}
