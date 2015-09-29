package net.nueca.concessioengine.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 9/22/15.
 */
public class SimpleReceiveMultiInputAdapter extends BaseRecyclerAdapter<SimpleReceiveMultiInputAdapter
        .ListViewHolder, Values> {

    private boolean isManual = false;
    private SelectedProductItem selectedProductItem;

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_receive_listitem, parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder lvh, int position) {
        Product product = selectedProductItem.getProduct();

        lvh.tvProductName.setText(product.getName() + (!isManual && product.getUnit() != null?
                " (" + product.getUnit() + ")" : "" ));
        lvh.tvNum.setText("" + selectedProductItem.getValues().get(position).getLine_no());

        lvh.tvQuantity.setText(NumberTools.separateInCommas(selectedProductItem.getOriginalQuantity(position)));
        lvh.tvReceive.setText(NumberTools.separateInCommas(selectedProductItem.getQuantity(position)));
        lvh.tvReturn.setText(NumberTools.separateInCommas(selectedProductItem.getReturn(position)));
        lvh.tvDiscrepancy.setText(NumberTools.separateInCommas(selectedProductItem.getDiscrepancy(position)));

        lvh.tvQuantity.setVisibility(isManual? View.GONE : View.VISIBLE);
        lvh.tvDiscrepancy.setVisibility(isManual? View.GONE : View.VISIBLE);
    }

    public void setIsManual(boolean isManual) {
        this.isManual = isManual;
    }

    public void setSelectedProductItem(SelectedProductItem selectedProductItem) {
        this.selectedProductItem = selectedProductItem;

    }

    @Override
    public int getItemCount() {
        return selectedProductItem.getValues().size();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        public AutofitTextView tvProductName, tvNum, tvQuantity,
                tvReceive, tvReturn, tvDiscrepancy;

        public ListViewHolder(View itemView) {
            super(itemView);
            tvProductName = (AutofitTextView) itemView.findViewById(R.id.tvProductName);
            tvNum = (AutofitTextView) itemView.findViewById(R.id.tvNum);
            tvQuantity = (AutofitTextView) itemView.findViewById(R.id.tvQuantity);
            tvReceive = (AutofitTextView) itemView.findViewById(R.id.tvReceive);
            tvReturn = (AutofitTextView) itemView.findViewById(R.id.tvReturn);
            tvDiscrepancy = (AutofitTextView) itemView.findViewById(R.id.tvDiscrepancy);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(v, getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if(onItemLongClickListener != null)
                onItemLongClickListener.onItemLongClicked(v, getLayoutPosition());
            return true;
        }
    }
}
