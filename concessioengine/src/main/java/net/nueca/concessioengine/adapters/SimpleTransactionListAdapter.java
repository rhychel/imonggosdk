package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseTransactionsAdapter;
import net.nueca.concessioengine.adapters.tools.TransactionsAdapterHelper;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.tools.DateTimeTools;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymart on 8/4/15.
 * imonggosdk2 (c)2015
 */
public class SimpleTransactionListAdapter extends BaseTransactionsAdapter {

    public SimpleTransactionListAdapter(Context context, List<OfflineData> objects) {
        super(context, R.layout.simple_transaction_listitem, objects);
    }

    private static class ListViewHolder {
        ImageView ivStatus;
        AutofitTextView tvTransactionRefNo, tvTransactionBranch;
        TextView tvTransactionDate, tvTransactionType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListViewHolder lvh = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_transaction_listitem, null);
            lvh = new ListViewHolder();

            lvh.ivStatus = (ImageView) convertView.findViewById(R.id.ivStatus);
            lvh.tvTransactionRefNo = (AutofitTextView) convertView.findViewById(R.id.tvTransactionRefNo);
            lvh.tvTransactionBranch = (AutofitTextView) convertView.findViewById(R.id.tvTransactionBranch);
            lvh.tvTransactionType = (TextView) convertView.findViewById(R.id.tvTransactionType);
            lvh.tvTransactionDate = (TextView) convertView.findViewById(R.id.tvTransactionDate);

            convertView.setTag(lvh);
        }
        else
            lvh = (ListViewHolder) convertView.getTag();

        OfflineData offlineData = getItem(position);

        lvh.tvTransactionRefNo.setText(offlineData.getReference_no());
        lvh.tvTransactionDate.setText(DateTimeTools.convertFromTo(offlineData.getDate(), TimeZone.getTimeZone("UTC"), Calendar.getInstance().getTimeZone()));
        lvh.tvTransactionType.setText(TransactionsAdapterHelper.getTransactionType(offlineData.getType()));
        lvh.ivStatus.setImageResource(TransactionsAdapterHelper.getStatus(offlineData, true));
        if(lvh.ivStatus.getAnimation() != null)
            lvh.ivStatus.getAnimation().cancel();
        if(offlineData.isSyncing() && !offlineData.isSynced())
            lvh.ivStatus.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forever));

        return convertView;
    }

}
