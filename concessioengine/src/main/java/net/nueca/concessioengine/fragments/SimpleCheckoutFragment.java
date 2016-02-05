package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleSplitPaymentAdapter;
import net.nueca.concessioengine.adapters.base.BaseSplitPaymentAdapter;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;

/**
 * Created by rhymart on 5/14/15.
 * imonggosdk (c)2015
 */
public class SimpleCheckoutFragment extends BaseCheckoutFragment {
    private TextView tvAmountDue, tvBalance;
    private LinearLayout llAmountDue, llBalance;

    private RecyclerView rvPayments;

    private BaseSplitPaymentAdapter splitPaymentAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_checkout_fragment, container, false);

        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);

        llAmountDue = (LinearLayout) view.findViewById(R.id.llCheckoutAmountDue);
        llBalance = (LinearLayout) view.findViewById(R.id.llCheckoutBalance);

        llAmountDue.setVisibility(View.GONE);
        llBalance.setVisibility(View.GONE);

        if(tvAmountDue == null) {
            tvAmountDue = (EditText) view.findViewById(R.id.etAmountDue);
            llAmountDue.setVisibility(View.VISIBLE);
        }
        if(tvBalance == null) {
            tvBalance = (EditText) view.findViewById(R.id.etBalance);
            llBalance.setVisibility(View.VISIBLE);
        }

        rvPayments = (RecyclerView) view.findViewById(R.id.rvPayments);

        tvAmountDue.setText(NumberTools.separateInCommas(getAmountDue()));

        if(splitPaymentAdapter == null) {
            splitPaymentAdapter = new SimpleSplitPaymentAdapter(getActivity(), getHelper());
            splitPaymentAdapter.setPaymentUpdateListener(this);
        }
        splitPaymentAdapter.initializeRecyclerView(getActivity(), rvPayments);

        rvPayments.setAdapter(splitPaymentAdapter);

        itemTouchHelper.attachToRecyclerView(rvPayments);

        tvBalance.setText(NumberTools.separateInCommas(computation.getTotalPayable()));

        return view;
    }

    public BaseSplitPaymentAdapter getSplitPaymentAdapter() {
        return splitPaymentAdapter;
    }

    public void setSplitPaymentAdapter(BaseSplitPaymentAdapter splitPaymentAdapter) {
        this.splitPaymentAdapter = splitPaymentAdapter;
    }

    @Override
    public void onAddPayment(InvoicePayment invoicePayment) {
        //computation.addPayment(invoicePayment);
        tvBalance.setText(NumberTools.separateInCommas(computation.getRemaining()));

        //splitPaymentAdapter.setIsFullyPaid(computation.getRemaining().compareTo(BigDecimal.ZERO) <= 0);

        rvPayments.scrollToPosition(splitPaymentAdapter.getItemCount() - 1);
    }

    @Override
    public void onDeletePayment(int location) {
        //computation.removePayment(location);
        tvBalance.setText(NumberTools.separateInCommas(computation.getRemaining()));

        //splitPaymentAdapter.setIsFullyPaid(computation.getRemaining().compareTo(BigDecimal.ZERO) <= 0);
    }

    private ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper
            .SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) {
                SimpleSplitPaymentAdapter.ListViewHolder lvh = (SimpleSplitPaymentAdapter.ListViewHolder) viewHolder;

                if(lvh.isAdd())
                    splitPaymentAdapter.notifyDataSetChanged();
                else
                    splitPaymentAdapter.deletePayment(lvh.getItemIndex());
            }
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if(viewHolder instanceof SimpleSplitPaymentAdapter.ListViewHolder)
                if(!((SimpleSplitPaymentAdapter.ListViewHolder) viewHolder).isEditable())
                    return 0;
            return super.getSwipeDirs(recyclerView, viewHolder);
        }
    });

    public void setAmountDueTextView(TextView tvAmountDue) {
        this.tvAmountDue = tvAmountDue;
    }

    public void setBalanceTextView(TextView tvBalance) {
        this.tvBalance = tvBalance;
    }
}
