package net.nueca.imonggosdk.objects.invoice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
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

    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "invoice_id")
    private transient Invoice invoice;

    public InvoiceTaxRate() {}

    public InvoiceTaxRate(Invoice invoice, String tax_rate_id, double amount, double rate) {
        this.invoice = invoice;
        this.tax_rate_id = tax_rate_id;
        this.amount = amount;
        this.rate = rate;
    }

    public String getTaxRateId() {
        return tax_rate_id;
    }

    public void setTaxRateId(String tax_rate_id) {
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

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.INVOICE_TAX_RATES, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.INVOICE_TAX_RATES, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.INVOICE_TAX_RATES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new JSONObject(gson.toJson(this));
        /*JSONObject jsonObject = new JSONObject();
        jsonObject.put("tax_rate_id",tax_rate_id);
        jsonObject.put("amount",amount);
        jsonObject.put("rate",rate);
        return jsonObject;*/
    }
}
