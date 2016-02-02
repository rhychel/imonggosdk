package net.nueca.imonggosdk.objects.invoice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.base.Extras;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/1/15.
 */
public class InvoicePayment extends BaseTable2 implements Extras.DoOperationsForExtras {
    @Expose
    @DatabaseField
    protected int payment_type_id;
    @Expose
    @DatabaseField
    protected String payment_type_code;
    @Expose
    @DatabaseField
    protected String payment_type_name;
    @Expose
    @DatabaseField
    protected double amount;
    @Expose
    @DatabaseField
    protected double tender;

    @DatabaseField
    protected transient Integer paymentBatchNo = null;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "invoice_id")
    protected transient Invoice invoice;

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

    public String getPayment_type_code() {
        return payment_type_code;
    }

    public void setPayment_type_code(String payment_type_code) {
        this.payment_type_code = payment_type_code;
    }

    public String getPayment_type_name() {
        return payment_type_name;
    }

    public void setPayment_type_name(String payment_type_name) {
        this.payment_type_name = payment_type_name;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public InvoicePayment() {}

    public InvoicePayment(Builder builder) {
        payment_type_id = builder.payment_type_id;
        payment_type_code = builder.payment_type_code;
        payment_type_name = builder.payment_type_name;
        amount = builder.amount;
        tender = builder.tender;
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
        protected String payment_type_name, payment_type_code;

        public Builder paymentType(PaymentType paymentType) {
            this.payment_type_name = paymentType.getName();
            this.payment_type_code = paymentType.getCode();
            this.payment_type_id = paymentType.getId();
            return this;
        }

        public Builder payment_type_name(String payment_type_name) {
            this.payment_type_name = payment_type_name;
            return this;
        }
        public Builder payment_type_code(String payment_type_code) {
            this.payment_type_code = payment_type_code;
            return this;
        }
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

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        insertExtrasTo(dbHelper);
        try {
            dbHelper.insert(InvoicePayment.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateExtrasTo(dbHelper);
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(InvoicePayment.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        deleteExtrasTo(dbHelper);
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(InvoicePayment.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateExtrasTo(dbHelper);
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            extras.setInvoicePayment(this);
            extras.setId(getClass().getName().toUpperCase(), id);
            extras.insertTo(dbHelper);
        }
    }

    @Override
    public void deleteExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null)
            extras.deleteTo(dbHelper);
    }

    @Override
    public void updateExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            String idstr = getClass().getName().toUpperCase() + "_" + id;
            if (idstr.equals(extras.getId()))
                extras.updateTo(dbHelper);
            else {
                extras.deleteTo(dbHelper);
                extras.setId(getClass().getName().toUpperCase(), id);
                extras.insertTo(dbHelper);
            }
        }
    }

    public Integer getPaymentBatchNo() {
        return paymentBatchNo;
    }

    public void setPaymentBatchNo(Integer paymentBatchNo) {
        this.paymentBatchNo = paymentBatchNo;
    }
}
