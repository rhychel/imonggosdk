package net.nueca.dizonwarehouse.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.dizonwarehouse.R;

import java.util.List;

/**
 * Created by gama on 31/03/2016.
 */
public class CategoryRecyclerAdapter extends BaseRecyclerAdapter<CategoryRecyclerAdapter.ListViewHolder, String> {
    private int selected = 0;

    public CategoryRecyclerAdapter(Context context) {
        super(context);
    }

    public CategoryRecyclerAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_tab, parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        holder.root.setSelected(position == selected);
        holder.tvName.setText(getItem(position));
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        public View root;
        public TextView tvName;

        public ListViewHolder(View itemView) {
            super(itemView);

            root = itemView;
            tvName = (TextView) itemView.findViewById(R.id.tvName);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            selected = getLayoutPosition();
            notifyDataSetChanged();
            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(view, getLayoutPosition());
        }


        @Override
        public boolean onLongClick(View view) {
            if(onItemLongClickListener != null)
                onItemLongClickListener.onItemLongClicked(view, getLayoutPosition());
            return true;
        }
    }
}
