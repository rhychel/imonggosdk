package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
public class CheckoutFragment extends BaseCheckoutFragment {
    private EditText etAmountDue, etBalance;

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

        etAmountDue = (EditText) view.findViewById(R.id.etAmountDue);
        etBalance = (EditText) view.findViewById(R.id.etBalance);
        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);

        rvPayments = (RecyclerView) view.findViewById(R.id.rvPayments);

        etAmountDue.setText(NumberTools.separateInCommas(getAmountDue()));

        if(splitPaymentAdapter == null)
            splitPaymentAdapter = new SimpleSplitPaymentAdapter(getActivity(), getHelper());
        splitPaymentAdapter.initializeRecyclerView(getActivity(), rvPayments);
        splitPaymentAdapter.setPaymentUpdateListener(this);

        rvPayments.setAdapter(splitPaymentAdapter);

        itemTouchHelper.attachToRecyclerView(rvPayments);

        etBalance.setText(NumberTools.separateInCommas(computation.getTotalPayable()));

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
        computation.addPayment(invoicePayment);
        etBalance.setText(NumberTools.separateInCommas(computation.getRemaining()));

        splitPaymentAdapter.setIsFullyPaid(computation.getRemaining().compareTo(BigDecimal.ZERO) <= 0);

        rvPayments.scrollToPosition(splitPaymentAdapter.getItemCount() - 1);
    }

    @Override
    public void onDeletePayment(int location) {
        computation.removePayment(location);
        etBalance.setText(NumberTools.separateInCommas(computation.getRemaining()));

        splitPaymentAdapter.setIsFullyPaid(computation.getRemaining().compareTo(BigDecimal.ZERO) <= 0);
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
    });
}
