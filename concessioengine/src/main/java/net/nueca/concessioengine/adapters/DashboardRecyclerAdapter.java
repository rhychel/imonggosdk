package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.objects.DashboardTile;
import net.nueca.imonggosdk.enums.ConcessioModule;

import java.util.List;

/**
 * Created by rhymart on 11/24/15.
 */
public class DashboardRecyclerAdapter extends BaseRecyclerAdapter<DashboardRecyclerAdapter.DashboardViewHolder, DashboardTile> {

    public DashboardRecyclerAdapter(Context context, List<DashboardTile> list) {
        super(context, list);
    }

    @Override
    public DashboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dashboard_tile, parent, false);

        DashboardViewHolder dvh = new DashboardViewHolder(v);
        return dvh;
    }

    @Override
    public void onBindViewHolder(DashboardViewHolder holder, int position) {
        holder.concessioModule = getItem(position).getConcessioModule();
        holder.tvTitle.setText(getItem(position).getLabel());
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class DashboardViewHolder extends BaseRecyclerAdapter.ViewHolder{

        ImageView ivLogo;
        TextView tvTitle;
        ConcessioModule concessioModule;

        public DashboardViewHolder(View itemView) {
            super(itemView);

            ivLogo = (ImageView) itemView.findViewById(R.id.ivLogo);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            v.setTag(concessioModule);
            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(v, getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

}
