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

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/1/15.
 */
public class InvoiceTaxRate extends BaseTable2 {
    @Expose
    @DatabaseField
    protected String tax_rate_id;
    @Expose
    @DatabaseField
    protected double amount;
    @Expose
    @DatabaseField
    protected double rate;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "invoice_id")
    protected transient Invoice invoice;

    public InvoiceTaxRate() {}

    public InvoiceTaxRate(Builder builder) {
        tax_rate_id = builder.tax_rate_id;
        amount = builder.amount;
        rate = builder.rate;
    }

    public InvoiceTaxRate(String tax_rate_id, double amount, double rate) {
        this.tax_rate_id = tax_rate_id;
        this.amount = amount;
        this.rate = rate;
    }

    public String getTax_rate_id() {
        return tax_rate_id;
    }

    public void setTax_rate_id(String tax_rate_id) {
        this.tax_rate_id = tax_rate_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new JSONObject(gson.toJson(this));
    }

    public static class Builder {
        protected String tax_rate_id;
        protected double amount;
        protected double rate;

        public Builder tax_rate_id(String tax_rate_id) {
            this.tax_rate_id = tax_rate_id;
            return this;
        }
        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }
        public Builder rate(double rate) {
            this.rate = rate;
            return this;
        }

        public InvoiceTaxRate build() {
            return new InvoiceTaxRate(this);
        }
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(InvoiceTaxRate.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(InvoiceTaxRate.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(InvoiceTaxRate.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
