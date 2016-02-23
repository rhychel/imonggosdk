package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRoutePlanRecyclerAdapter;
import net.nueca.imonggosdk.objects.customer.Customer;

import java.util.List;

/**
 * Created by rhymart on 12/2/15.
 */
public class SimpleRoutePlanRecyclerViewAdapter extends BaseRoutePlanRecyclerAdapter<SimpleRoutePlanRecyclerViewAdapter.ListViewHolder> {

    public SimpleRoutePlanRecyclerViewAdapter(Context context, List<Customer> list) {
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
        Customer customer = getItem(position);

        if(customer != null) {
            Log.e("Customer", "is not null -- adapter");
            holder.tvCustomerName.setText(customer.getName());
            holder.tvCompany.setText(customer.getCompany_name());
            holder.tvLastTransaction.setText(customer.getLastPurchase());
        }
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {

        public ImageView ivStatus;
        public TextView tvCustomerName, tvCompany, tvLastTransaction, tvTransactionBranch, tvSubtotal;

        public ListViewHolder(View itemView) {
            super(itemView);

            ivStatus = (ImageView) itemView.findViewById(R.id.ivStatus);
            tvCustomerName = (TextView) itemView.findViewById(R.id.tvCustomerName);
            tvCompany = (TextView) itemView.findViewById(R.id.tvCompany);
            tvLastTransaction = (TextView) itemView.findViewById(R.id.tvLastTransaction);
            tvTransactionBranch = (TextView) itemView.findViewById(R.id.tvTransactionBranch);
            tvSubtotal = (TextView) itemView.findViewById(R.id.tvSubtotal);

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
