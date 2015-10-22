package net.nueca.concessioengine.adapters.base;

import android.content.Context;

import net.nueca.imonggosdk.objects.PaymentType;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gama on 20/10/2015.
 */
public abstract class BaseSplitPaymentAdapter<CheckoutPayment extends BaseRecyclerAdapter.ViewHolder>
        extends BaseRecyclerAdapter<CheckoutPayment, InvoicePayment> {
    private int listItemRes;
    protected OnPaymentUpdateListener paymentUpdateListener;
    private HashMap<Integer, PaymentType> paymentTypes;

    protected boolean isFullyPaid = false;

    public BaseSplitPaymentAdapter(Context context, int listItemRes) {
        super(context, new ArrayList<InvoicePayment>());
        this.listItemRes = listItemRes;
        this.paymentTypes = new HashMap<>();
        PaymentType cash = new PaymentType();
        cash.setName("CASH");
        cash.setCode("CASH");
        cash.setId(1);
        this.paymentTypes.put(1, cash);
    }

    public BaseSplitPaymentAdapter(Context context, int listItemRes, List<InvoicePayment> payments) {
        super(context, payments);
        this.listItemRes = listItemRes;
        this.paymentTypes = new HashMap<>();
        PaymentType cash = new PaymentType();
        cash.setName("CASH");
        cash.setCode("CASH");
        cash.setId(1);
        this.paymentTypes.put(1, cash);
    }

    public BaseSplitPaymentAdapter(Context context, int listItemRes, List<InvoicePayment> payments,
                                   List<PaymentType> paymentTypes) {
        super(context, payments);
        this.listItemRes = listItemRes;
        this.paymentTypes = new HashMap<>();
        for(PaymentType paymentType : paymentTypes)
            this.paymentTypes.put(paymentType.getId(), paymentType);
    }

    public HashMap<Integer, PaymentType> getPaymentTypes() {
        return paymentTypes;
    }

    public PaymentType getPaymentTypeWithId(int id) {
        return paymentTypes.get(id);
    }

    public void setListItemResource(int resource) {
        listItemRes = resource;
    }
    public int getListItemResource() {
        return listItemRes;
    }

    public interface OnPaymentUpdateListener {
        void onAddPayment(InvoicePayment invoicePayment);
        void onDeletePayment(int location);
    }

    public OnPaymentUpdateListener getPaymentUpdateListener() {
        return paymentUpdateListener;
    }

    public void setPaymentUpdateListener(OnPaymentUpdateListener paymentUpdateListener) {
        this.paymentUpdateListener = paymentUpdateListener;
    }

    public boolean isFullyPaid() {
        return isFullyPaid;
    }

    public void setIsFullyPaid(boolean isFullyPaid) {
        this.isFullyPaid = isFullyPaid;
    }

    public void deletePayment(int position) {
        remove(position);
        notifyDataSetChanged();

        if(paymentUpdateListener != null)
            paymentUpdateListener.onDeletePayment(position);
    }
}
