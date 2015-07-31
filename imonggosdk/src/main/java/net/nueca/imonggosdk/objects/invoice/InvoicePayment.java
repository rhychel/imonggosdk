package net.nueca.imonggosdk.objects.invoice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gama on 7/1/15.
 */
public class InvoicePayment {
    protected int payment_type_id;
    protected double amount;
    protected double tender;

    public int getPayment_type_id() {
        return payment_type_id;
    }

    public void setPayment_type_id(int payment_type_id) {
        this.payment_type_id = payment_type_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getTender() {
        return tender;
    }

    public void setTender(double tender) {
        this.tender = tender;
    }

    public InvoicePayment(Builder builder) {
        payment_type_id = builder.payment_type_id;
        amount = builder.amount;
        tender = builder.amount;
    }

    public InvoicePayment(int payment_type_id, double amount, double tender) {
        this.payment_type_id = payment_type_id;
        this.amount = amount;
        this.tender = tender;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new JSONObject(gson.toJson(this));
    }

    public static class Builder {
        protected int payment_type_id;
        protected double amount;
        protected double tender;

        public Builder payment_type_id(int payment_type_id) {
            this.payment_type_id = payment_type_id;
            return this;
        }
        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }
        public Builder tender(double tender) {
            this.tender = tender;
            return this;
        }

        public InvoicePayment build() {
            return new InvoicePayment(this);
        }
    }
}
