package net.nueca.imonggosdk.objects.invoice;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.customer.Customer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gama on 19/04/2016.
 */
public class RewardPayment {

    protected int paymentTypeId = 0;
    protected double pointsConversion = 0d;
    protected String reference;

    protected transient Invoice invoice;
    protected transient Customer customer;

    public RewardPayment() {}

    public RewardPayment(Invoice invoice, Customer customer, double pointsConversion, PaymentType rewardsPaymentType) {
        this.invoice = invoice;
        this.reference = invoice.getReference() + "_RPt" + invoice.getReturnId();
        this.customer = customer;
        this.pointsConversion = pointsConversion;
        this.paymentTypeId = rewardsPaymentType.getId();
    }

    public int getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(int paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public double getPointsConversion() {
        return pointsConversion;
    }

    public void setPointsConversion(double pointsConversion) {
        this.pointsConversion = pointsConversion;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("customer_id", customer.getReturnId());
        jsonObject.put("reference", reference);
        jsonObject.put("points", getPointsUsed());
        return jsonObject;
    }

    public String toJSONString() throws JSONException {
        return toJSONObject().toString();
    }

    protected List<Integer> getPaymentBatches() {
        List<Integer> paymentBatches = new ArrayList<>();
        for(InvoicePayment payment : invoice.getPayments()) {
            if(payment.getPaymentBatchNo() != null && !paymentBatches.contains(payment.getPaymentBatchNo()))
                paymentBatches.add(payment.getPaymentBatchNo());
        }
        Collections.sort(paymentBatches);
        return paymentBatches;
    }

    protected double getPointsUsed() {
        List<Integer> paymentBatches = getPaymentBatches();

        int latestBatch = paymentBatches.get(paymentBatches.size()-1);
        double points = 0d;

        for(InvoicePayment payment : invoice.getPayments()) {
            if(latestBatch == payment.getPaymentBatchNo()) {
                if(payment.getPayment_type_id() == paymentTypeId)
                    points += (payment.getAmount() * pointsConversion);
            }
        }
        return points;
    }

    public String getParentReference() {
        return reference;
    }
}
