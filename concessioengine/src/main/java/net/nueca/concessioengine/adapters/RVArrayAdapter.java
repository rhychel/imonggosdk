package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;

import java.util.List;

/**
 * Created by rhymart on 8/11/15.
 * imonggosdk (c)2015
 */
public class RVArrayAdapter<Obj> extends BaseRecyclerAdapter<RVArrayAdapter.ListViewHolder, Obj> {

    private int layout;
    public RVArrayAdapter(Context context, List<Obj> list) {
        super(context, list);
    }

    public RVArrayAdapter(Context context, int layout, List<Obj> list) {
        super(context, list);
        this.layout = layout;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(layout, parent, false);

        ListViewHolder listViewHolder = new ListViewHolder(view);
        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(RVArrayAdapter.ListViewHolder holder, int position) {
        holder.text1.setText(getItem(position).toString());
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {

        private TextView text1;

        public ListViewHolder(View itemView) {
            super(itemView);
            text1 = (TextView)itemView.findViewById(android.R.id.text1);
        }

        @Override
        public void onClick(View view) {

        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }

}
