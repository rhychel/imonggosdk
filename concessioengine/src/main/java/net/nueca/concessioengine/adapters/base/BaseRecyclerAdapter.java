package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.tonicartos.superslim.LayoutManager;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.interfaces.OnItemLongClickListener;
import net.nueca.concessioengine.adapters.tools.DividerItemDecoration;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.widgets.AutofitRecyclerView;

import java.util.List;

/**
 * Created by rhymart on 8/7/15.
 * imonggosdk2 (c)2015
 */
public abstract class BaseRecyclerAdapter<T extends BaseRecyclerAdapter.ViewHolder, Obj> extends RecyclerView.Adapter<T>{
    /**
     * These are for the sticky header letter.
     */
    protected int headerDisplay;
    protected boolean marginsFixed;
    protected static final int VIEW_TYPE_HEADER = 0x01;
    protected static final int VIEW_TYPE_CONTENT = 0x00;
    /**
     * ---------- STICKY HEADER ----------
     */

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

    public void add(int position, Obj obj) {
        this.objectList.add(position, obj);
        notifyDataSetChanged();
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

    public void set(int position, Obj object) {
        this.objectList.set(position, object);
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

    public int getPosition(Obj object) {
        return this.objectList.indexOf(object);
    }

    protected Context getContext() {
        return context;
    }

    protected int getCount() {
        return objectList.size();
    }

    public void initializeRecyclerView(Context context, RecyclerView rvProducts, boolean hasDivider) {
        linearLayoutManager = new LinearLayoutManager(context);
        rvProducts.setLayoutManager(linearLayoutManager);
        rvProducts.setHasFixedSize(true);
        if(hasDivider)
            rvProducts.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
    }

    public void initializeRecyclerView(Context context, RecyclerView rvProducts) {
        initializeRecyclerView(context, rvProducts, true);
    }

    public void initializeGridRecyclerView(Context context, RecyclerView rvProducts, int span, boolean hasFixedSize) {
        gridLayoutManager = new GridLayoutManager(context, span);

        rvProducts.setLayoutManager(gridLayoutManager);
        rvProducts.setHasFixedSize(hasFixedSize);
    }

    public void initializeGridRecyclerView(final RecyclerView rvProducts, boolean hasFixedSize) {
        rvProducts.setHasFixedSize(hasFixedSize);
        gridLayoutManager = (GridLayoutManager) rvProducts.getLayoutManager();
        rvProducts.addItemDecoration(new RecyclerView.ItemDecoration() {
            private Integer padding;

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if(padding == null) {
                    int totalWidth = parent.getWidth();
                    int column = ((GridLayoutManager) rvProducts.getLayoutManager()).getSpanCount();
                    int itemWidth = ((AutofitRecyclerView)rvProducts).getColumnWidth();
                    padding = ((totalWidth / column) - itemWidth) / 2;
                    padding = Math.max(0, padding);
                }
                outRect.set(padding, 0, padding * -1, 0);
            }
        });
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
