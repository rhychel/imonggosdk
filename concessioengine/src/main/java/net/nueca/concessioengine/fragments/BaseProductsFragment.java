package net.nueca.concessioengine.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import net.nueca.concessioengine.adapters.SimpleProductRecyclerViewAdapter;
import net.nueca.imonggosdk.database.ImonggoDBHelper;

/**
 * Created by rhymart on 7/15/15.
 * imonggosdk (c)2015
 */
public abstract class BaseProductsFragment extends Fragment {

    private ImonggoDBHelper dbHelper;

    protected LinearLayoutManager linearLayoutManager;
    protected RecyclerView rvProducts;
    protected ListView lvProducts;

    protected SimpleProductRecyclerViewAdapter simpleProductRecyclerViewAdapter;

    public ImonggoDBHelper getHelper() {
        return dbHelper;
    }

    public void setHelper(ImonggoDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
}
