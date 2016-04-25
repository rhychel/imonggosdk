package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.util.Log;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.imonggosdk.objects.invoice.PaymentType;
import net.nueca.imonggosdk.objects.invoice.InvoicePayment;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gama on 20/10/2015.
 */
public abstract class BaseSplitPaymentAdapter<CheckoutPayment extends BaseRecyclerAdapter.ViewHolder>
        extends BaseRecyclerAdapter<CheckoutPayment, InvoicePayment> {
    private int listItemRes;
    protected OnPaymentUpdateListener paymentUpdateListener;
    private HashMap<Integer, PaymentType> paymentTypes;
    private List<PaymentType> paymentTypeList;

    protected String totalAmount = "";
    protected double balance = 0.0;

    protected boolean isFullyPaid = false;
    protected boolean isDefaultCash = false;
    protected boolean isLayaway = false;

    protected Double availablePoints = 0.0;
    protected Double pointsInPeso = 0.0;
    protected Double consumedPoints = 0.0;

    protected ListingType listingType = ListingType.BASIC_PAYMENTS;

    protected InvoiceTools.PaymentsComputation computation;

    public BaseSplitPaymentAdapter(Context context, int listItemRes, InvoiceTools.PaymentsComputation computation) {
        super(context, computation == null || computation.getPayments() == null?
                new ArrayList<InvoicePayment>() : computation.getPayments());
        this.listItemRes = listItemRes;
        this.paymentTypes = new HashMap<>();
        if(isDefaultCash) {
            PaymentType cash = new PaymentType();
            cash.setName("CASH");
            cash.setCode("CASH");
            cash.setId(1);
            this.paymentTypes.put(1, cash);
        }
        this.computation = computation != null? computation : new InvoiceTools.PaymentsComputation();
    }

    public BaseSplitPaymentAdapter(Context context, int listItemRes, InvoiceTools.PaymentsComputation computation,
                                   List<PaymentType> paymentTypes) {
        super(context, computation == null || computation.getPayments() == null?
                new ArrayList<InvoicePayment>() : computation.getPayments());
        this.listItemRes = listItemRes;
        this.paymentTypeList = paymentTypes;
        this.paymentTypes = new HashMap<>();
        for(PaymentType paymentType : paymentTypes)
            this.paymentTypes.put(paymentType.getId(), paymentType);
        this.computation = computation != null? computation : new InvoiceTools.PaymentsComputation();
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
        void onUpdatePayment(int location, double prevValue, InvoicePayment invoicePayment);
        void onDeletePayment(int location, double prevValue);
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

    public List<PaymentType> getPaymentTypeList() {
        return paymentTypeList;
    }

    public void setPaymentTypeList(List<PaymentType> paymentTypeList) {
        this.paymentTypeList = paymentTypeList;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void deletePayment(int position) {
        double prevValue = -1;
        if(getItem(position).getPayment_type_name().toLowerCase().equals("rewards"))
            prevValue = getItem(position).getTender();

        remove(position);
        notifyDataSetChanged();

        computation.removePayment(position);

        setIsFullyPaid(NumberTools.formatDouble(computation.getRemaining().doubleValue(),computation.getDecimalPlace()) <= 0d);

        if(paymentUpdateListener != null)
            paymentUpdateListener.onDeletePayment(position, prevValue);
    }

    public void addPayment(InvoicePayment payment) {
        Log.e("ADDING PAYMENT " + payment.getPaymentBatchNo(), payment.getTender() + " " + computation.getRemaining().doubleValue());
        add(payment);
        notifyItemInserted(getItemCount());

        computation.addPayment(payment);

        setIsFullyPaid(NumberTools.formatDouble(computation.getRemaining().doubleValue(),computation.getDecimalPlace()) <= 0d);

        if(paymentUpdateListener != null)
            paymentUpdateListener.onAddPayment(payment);
    }

    public void updatePayment(int position, InvoicePayment payment) {
        double prevValue = getItem(position).getTender();
        InvoicePayment thisPayment = getItem(position);
        thisPayment.setAmount(payment.getAmount());
        thisPayment.setTender(payment.getTender());
        thisPayment.setPayment_type_id(payment.getPayment_type_id());
        thisPayment.setExtras(payment.getExtras());

        notifyDataSetChanged();

        computation.setPayment(position, thisPayment);

        setIsFullyPaid(NumberTools.formatDouble(computation.getRemaining().doubleValue(),computation.getDecimalPlace()) <= 0d);

        if(paymentUpdateListener != null)
            paymentUpdateListener.onUpdatePayment(position, prevValue, thisPayment);
    }

    public InvoiceTools.PaymentsComputation getComputation() {
        return computation;
    }

    public void setComputation(InvoiceTools.PaymentsComputation computation) {
        this.computation = computation;
        if(this.computation.getPayments() != null) {
            for(InvoicePayment payment : this.computation.getPayments())
                add(payment);
        }
    }

    public boolean isLayaway() {
        return isLayaway;
    }

    public void setLayaway(boolean layaway) {
        isLayaway = layaway;
    }

    public void setConsumedPoints(Double consumedPoints) {
        this.consumedPoints = consumedPoints;
    }

    public void setPointsInPeso(Double pointsInPeso) {
        this.pointsInPeso = pointsInPeso;
    }

    public void setAvailablePoints(Double availablePoints) {
        this.availablePoints = availablePoints;
    }
}
