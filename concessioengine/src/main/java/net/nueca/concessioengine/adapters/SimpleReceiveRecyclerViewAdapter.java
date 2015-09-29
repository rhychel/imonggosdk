package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseReceiveRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.fragments.SimpleReceiveReviewFragment;
import net.nueca.concessioengine.lists.ValuesList;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.ExtendedAttributes;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 9/9/15.
 */
public class SimpleReceiveRecyclerViewAdapter extends BaseReceiveRecyclerAdapter<SimpleReceiveRecyclerViewAdapter
        .ListViewHolder> {

    public SimpleReceiveRecyclerViewAdapter(Context context, ImonggoDBHelper dbHelper) {
        super(context, R.layout.simple_receive_listitem, dbHelper);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(getListItemResource(), parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder lvh, int position) {
        SelectedProductItem selectedProductItem = getDisplayProductListItem().get(position);
        Product product = selectedProductItem.getProduct();

        lvh.tvProductName.setText(product.getName() + (!isManual && product.getUnit() != null?
                " (" + product.getUnit() + ")" : "" ));
        if(!isReview)
            lvh.tvNum.setText("" + getItem(position).getLine_no());
        else
            lvh.tvNum.setText("" + selectedProductItem.getValues().get(0).getLine_no());

        lvh.tvQuantity.setText(NumberTools.separateInCommas(selectedProductItem.getOriginalQuantity()));
        lvh.tvReceive.setText(NumberTools.separateInCommas(selectedProductItem.getQuantity()));
        lvh.tvReturn.setText(NumberTools.separateInCommas(selectedProductItem.getReturn()));
        lvh.tvDiscrepancy.setText(NumberTools.separateInCommas(selectedProductItem.getDiscrepancy()));

        lvh.tvQuantity.setVisibility(isManual? View.GONE : View.VISIBLE);
        lvh.tvDiscrepancy.setVisibility(isManual? View.GONE : View.VISIBLE);
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

    public boolean updateList(List<DocumentLine> documentLines) {
        updateSelectedProduct(documentLines);
        notifyDataSetChanged();
        return getCount() > 0;
    }

    public boolean updateReceivedList(List<SelectedProductItem> selectedProductItems) {
        updateReceivedProduct(selectedProductItems);
        notifyDataSetChanged();
        return getCount() > 0;
    }
}
