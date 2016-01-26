package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseTransactionsRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.TransactionsAdapterHelper;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.tools.DateTimeTools;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymart on 8/7/15.
 * imonggosdk2 (c)2015
 */
public class SimpleTransactionRecyclerViewAdapter extends BaseTransactionsRecyclerAdapter<SimpleTransactionRecyclerViewAdapter.ListViewHolder>{

    public SimpleTransactionRecyclerViewAdapter(Context context) {
        super(context);
    }

    public SimpleTransactionRecyclerViewAdapter(Context context, List<OfflineData> offlineDataList) {
        super(context, offlineDataList);
    }

    public SimpleTransactionRecyclerViewAdapter(Context context, List<OfflineData> list, ListingType listingType) {
        super(context, list, listingType);
    }

    @Override
    public SimpleTransactionRecyclerViewAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if(listingType == ListingType.DETAILED_HISTORY)
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_transaction_listitem2, parent, false);
        else
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_transaction_listitem, parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(SimpleTransactionRecyclerViewAdapter.ListViewHolder viewHolder, int position) {
        OfflineData offlineData = getItem(position);

        viewHolder.tvTransactionRefNo.setText(offlineData.getReference_no());
        if(listingType == ListingType.DETAILED_HISTORY) {
            viewHolder.tvTransactionDate.setText(DateTimeTools.convertFromTo(offlineData.getDate(), "MMMM d, yyyy, EEEE, h:mma", TimeZone.getTimeZone("UTC"), Calendar.getInstance().getTimeZone()));
            viewHolder.tvTransactionType.setText(TransactionsAdapterHelper.getTransactionType(dbHelper, offlineData));
        }
        else {
            viewHolder.tvTransactionDate.setText(DateTimeTools.convertFromTo(offlineData.getDate(), TimeZone.getTimeZone("UTC"), Calendar.getInstance().getTimeZone()));
            viewHolder.tvTransactionType.setText(TransactionsAdapterHelper.getTransactionType(offlineData.getType()));
        }
        viewHolder.ivStatus.setImageResource(TransactionsAdapterHelper.getStatus(offlineData, listingType == ListingType.BASIC));
        viewHolder.tvTransactionBranch.setText(offlineData.getBranchName());

        if(viewHolder.ivStatus.getAnimation() != null)
            viewHolder.ivStatus.getAnimation().cancel();
        if(offlineData.isSyncing() && !offlineData.isSynced())
            viewHolder.ivStatus.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forever));
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        public ImageView ivStatus;
        public AutofitTextView tvTransactionRefNo, tvTransactionBranch;
        public TextView tvTransactionDate, tvTransactionType;
        public LinearLayout llCash;
        public TextView tvCash;
        public View root;

        public ListViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            ivStatus = (ImageView) itemView.findViewById(R.id.ivStatus);
            tvTransactionRefNo = (AutofitTextView) itemView.findViewById(R.id.tvTransactionRefNo);
            tvTransactionBranch = (AutofitTextView) itemView.findViewById(R.id.tvTransactionBranch);
            tvTransactionDate = (TextView) itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionType = (TextView) itemView.findViewById(R.id.tvTransactionType);
            if(listingType == ListingType.DETAILED_HISTORY) {
                llCash = (LinearLayout) itemView.findViewById(R.id.llCash);
                tvCash = (TextView) itemView.findViewById(R.id.tvCash);
            }

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
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
