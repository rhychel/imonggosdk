package net.nueca.concessioengine.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleOrderReceiveRecyclerAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.BaseOrderReceiveFragment;
import net.nueca.concessioengine.objects.ReceivedMultiItem;
import net.nueca.concessioengine.objects.ReceivedProductItem;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.tools.DialogTools;

import java.util.List;

/**
 * Created by gama on 01/06/2016.
 */
public class SimpleOrderReceiveFragment extends BaseOrderReceiveFragment {

    private TextView tvNoProducts;
    private OnItemClickListener itemClickListener;

    private int prevSelectedCategory;

    public static SimpleOrderReceiveFragment newInstance() { return new SimpleOrderReceiveFragment(); }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_products_fragment_rv, container, false);

        rvItems = (RecyclerView) view.findViewById(R.id.rvProducts);
        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        tvNoProducts = (TextView) view.findViewById(R.id.tvNoProducts);
        spCategories = (Spinner) view.findViewById(R.id.spCategories);

        orderReceiveRecyclerAdapter = new SimpleOrderReceiveRecyclerAdapter(getContext(), getHelper());
        if(itemClickListener != null)
            orderReceiveRecyclerAdapter.setOnItemClickListener(itemClickListener);

        if(hasCategories) {
            if(!getCategory().equals("")) {
                prevSelectedCategory = productCategories.indexOf(getCategory().toUpperCase());
                Log.e("prevSelectedCategory", prevSelectedCategory+" -- "+getCategory());
            }
            else if(productCategories.size() > 0)
                setCategory(productCategories.get(0));

            Log.e("prevSelectedCategory", prevSelectedCategory+" -- "+getCategory());

            productCategoriesAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item_dark, productCategories);
            productCategoriesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_list_dark);
            spCategories.setAdapter(productCategoriesAdapter);
            spCategories.setSelection(prevSelectedCategory);
            spCategories.setOnItemSelectedListener(onCategorySelected);
        }
        else
            spCategories.setVisibility(View.GONE);

        rvItems.setAdapter(orderReceiveRecyclerAdapter);
        orderReceiveRecyclerAdapter.initializeRecyclerView(getContext(),rvItems);

        rvItems.addOnScrollListener(rvScrollListener);

        forceUpdateList(getOrderItems());

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
        /*if(showCategoryOnStart)
            new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    if(spCategories != null) {
                        spCategories.performClick();
                    }
                    return false;
                }
            }).sendEmptyMessageDelayed(0, 200);*/

    }

    @Override
    protected void whenListEndReached(List<ReceivedMultiItem> items) {
        orderReceiveRecyclerAdapter.addAll(items);
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                orderReceiveRecyclerAdapter.notifyDataSetChanged();
            }
        };
        handler.sendEmptyMessageDelayed(0, 200);

        toggleNoItems("No products available.", (orderReceiveRecyclerAdapter.getItemCount() > 0));
    }

    public void updateListWhenSearch(String searchKey) {
        setSearchKey(searchKey);
        offset = 0;
        prevLast = -1;

        if(orderReceiveRecyclerAdapter != null)
            Log.e("BaseProducts", "is not null || searchKey="+searchKey);
        toggleNoItems("No results for \"" + searchKey + "\"" + messageCategory() + ".", orderReceiveRecyclerAdapter.updateList(getOrderItems()));
    }
    public void changeCategory(String category) {
        setCategory(category);
        offset = 0;
        prevLast = -1;

        toggleNoItems("No results for \"" + category + "\".", orderReceiveRecyclerAdapter.updateList(getOrderItems()));
    }

    @Override
    protected void toggleNoItems(String msg, boolean show) {
        rvItems.setVisibility(show ? View.VISIBLE : View.GONE);
        tvNoProducts.setVisibility(show ? View.GONE : View.VISIBLE);
        tvNoProducts.setText(msg);
    }

    public void forceUpdateList() {
        offset = 0;
        forceUpdateList(getOrderItems());
    }

    public void forceUpdateList(List<ReceivedMultiItem> items) {
        this.orderReceiveRecyclerAdapter.updateList(items);
        this.orderReceiveRecyclerAdapter.notifyDataSetChanged();

        toggleNoItems("No products available.", (orderReceiveRecyclerAdapter.getItemCount() > 0));
    }

    private AdapterView.OnItemSelectedListener onCategorySelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, final int position, long id) {
            final String category = productCategoriesAdapter.getItem(position).toLowerCase();
            changeCategory(category);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { }
    };

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
