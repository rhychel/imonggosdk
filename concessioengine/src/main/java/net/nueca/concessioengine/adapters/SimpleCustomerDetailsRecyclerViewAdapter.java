package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseCustomerDetailsRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.enums.CustomerDetail;

import java.util.List;

/**
 * Created by rhymart on 12/2/15.
 */
public class SimpleCustomerDetailsRecyclerViewAdapter extends BaseCustomerDetailsRecyclerAdapter<SimpleCustomerDetailsRecyclerViewAdapter.ListViewHolder>{

    public SimpleCustomerDetailsRecyclerViewAdapter(Context context, List<CustomerDetail> customerDetails) {
        super(context, customerDetails);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.simple_customer_details_item, parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        holder.ivIcon.setVisibility(View.INVISIBLE);
        if(getItem(position) == CustomerDetail.MOBILE_NO) {
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(R.drawable.ic_phone_orange);
        }
        if(getItem(position) == CustomerDetail.COMPANY_NAME) {
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(R.drawable.ic_branch_orange);
        }
        holder.tvLabel.setText(getItem(position).toString());

    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {

        ImageView ivIcon;
        TextView tvDetail;
        TextView tvLabel;

        public ListViewHolder(View itemView) {
            super(itemView);

            ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
            tvDetail = (TextView) itemView.findViewById(R.id.tvDetail);
            tvLabel = (TextView) itemView.findViewById(R.id.tvLabel);
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

}
