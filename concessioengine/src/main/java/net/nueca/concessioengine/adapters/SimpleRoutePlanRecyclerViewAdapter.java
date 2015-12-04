package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRoutePlanRecyclerAdapter;

import java.util.List;

/**
 * Created by rhymart on 12/2/15.
 */
public class SimpleRoutePlanRecyclerViewAdapter extends BaseRoutePlanRecyclerAdapter<SimpleRoutePlanRecyclerViewAdapter.ListViewHolder> {

    public SimpleRoutePlanRecyclerViewAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_route_plan_item, parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {

        public ListViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(v, getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }
}
