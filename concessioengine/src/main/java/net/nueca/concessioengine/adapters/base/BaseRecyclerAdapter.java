package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tonicartos.superslim.LayoutManager;

import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.interfaces.OnItemLongClickListener;
import net.nueca.concessioengine.adapters.tools.DividerItemDecoration;

import java.util.List;

/**
 * Created by rhymart on 8/7/15.
 * imonggosdk2 (c)2015
 */
public abstract class BaseRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder, Obj> extends RecyclerView.Adapter<T>{

    private Context context;
    private List<Obj> objectList;

    protected ListingType listingType = ListingType.BASIC;

    protected OnItemClickListener onItemClickListener = null;
    protected OnItemLongClickListener onItemLongClickListener = null;

    public RecyclerView.LayoutManager layoutManager;
    protected LinearLayoutManager linearLayoutManager;
    protected GridLayoutManager gridLayoutManager;

    public BaseRecyclerAdapter() {}

    public BaseRecyclerAdapter(Context context) {
        this.context = context;
    }

    public BaseRecyclerAdapter(Context context, List<Obj> list) {
        this.context = context;
        this.objectList = list;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }

    public boolean updateList(List<Obj> objList) {
        this.objectList.clear();
        this.objectList = objList;
        notifyDataSetChanged();
        return objectList.size() > 0;
    }

    public void add(Obj obj) {
        this.objectList.add(obj);
        notifyDataSetChanged();
    }

    public void addAll(List<Obj> objList) {
        this.objectList.addAll(objList);
        notifyDataSetChanged();
    }

    public void remove(Obj obj) {
        int removedPosition = this.objectList.indexOf(obj);
        this.objectList.remove(obj);
        notifyItemRemoved(removedPosition);
    }

    public void remove(int index) {
        this.objectList.remove(index);
        notifyItemRemoved(index);
    }

    public void removeAll() {
        this.objectList.clear();
        notifyDataSetChanged();
    }

    public List<Obj> getList() {
        return objectList;
    }

    public Obj getItem(int position) {
        return objectList.get(position);
    }

    public void setList(List<Obj> objectList) {
        this.objectList = objectList;
    }

    protected Context getContext() {
        return context;
    }

    protected int getCount() {
        return objectList.size();
    }

    public void initializeRecyclerView(Context context, RecyclerView rvProducts) {
        linearLayoutManager = new LinearLayoutManager(context);
        rvProducts.setLayoutManager(linearLayoutManager);
        rvProducts.setHasFixedSize(true);
        rvProducts.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
    }

    public void initializeGridRecyclerView(Context context, RecyclerView rvProducts, int span) {
        gridLayoutManager = new GridLayoutManager(context, span);

        rvProducts.setLayoutManager(gridLayoutManager);
        rvProducts.setHasFixedSize(true);
    }

    public GridLayoutManager getGridLayoutManager() {
        return gridLayoutManager;
    }

    public LinearLayoutManager getLinearLayoutManager() {
        return linearLayoutManager;
    }

    public LayoutManager getLayoutManager() {
        return (LayoutManager)layoutManager;
    }

    /**
     * View Handlers for the RecyclerAdapter.
     */
    public abstract class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
