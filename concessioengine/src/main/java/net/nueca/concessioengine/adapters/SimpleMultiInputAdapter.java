package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymart on 8/13/15.
 * imonggosdk (c)2015
 */
public class SimpleMultiInputAdapter extends BaseRecyclerAdapter<SimpleMultiInputAdapter.ListViewHolder, Values> {

    private boolean hasDeliveryDate = true, hasBrand = true, hasBatchNo = true;

    public SimpleMultiInputAdapter(Context context) {
        super(context);
    }

    @Override
    public SimpleMultiInputAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.simple_multiinput_listitem, parent, false);
        ListViewHolder lvh = new ListViewHolder(view);
        return lvh;
    }

    @Override
    public void onBindViewHolder(SimpleMultiInputAdapter.ListViewHolder holder, int position) {
        holder.tvQuantity.setText(getItem(position).getQuantity()+" "+getItem(position).getUnit_name());
        holder.tvBrand.setText(getItem(position).getExtendedAttributes().getBrand());
        holder.tvDeliveryDate.setText(getItem(position).getExtendedAttributes().getDelivery_date());
        holder.tvBatchNo.setText(getItem(position).getExtendedAttributes().getBatch_no());
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public void setHasBrand(boolean hasBrand) {
        this.hasBrand = hasBrand;
    }

    public void setHasDeliveryDate(boolean hasDeliveryDate) {
        this.hasDeliveryDate = hasDeliveryDate;
    }

    public void setHasBatchNo(boolean hasBatchNo) {
        this.hasBatchNo = hasBatchNo;
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {

        private AutofitTextView tvQuantity;
        private TextView tvBrand, tvDeliveryDate, tvBatchNo;

        public ListViewHolder(View itemView) {
            super(itemView);

            tvBrand = (TextView) itemView.findViewById(R.id.tvBrand);
            tvDeliveryDate = (TextView) itemView.findViewById(R.id.tvDeliveryDate);
            tvBatchNo = (TextView) itemView.findViewById(R.id.tvBatchNo);
            tvQuantity = (AutofitTextView) itemView.findViewById(R.id.tvQuantity);

            if(!hasDeliveryDate)
                tvDeliveryDate.setVisibility(View.GONE);
            if(!hasBrand)
                tvBrand.setVisibility(View.GONE);
            if(!hasBatchNo)
                tvBatchNo.setVisibility(View.GONE);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(view, getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }
}
