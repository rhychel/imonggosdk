package net.nueca.imonggosdk.objects.invoice;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable;
import net.nueca.imonggosdk.objects.base.BatchList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 7/1/15.
 */
public class Invoice extends BaseTable {
    @Expose
    @DatabaseField
    protected String invoice_date;

    @Expose
    @DatabaseField
    protected String status;

    @Expose
    @DatabaseField
    protected String email;

    @Expose
    @DatabaseField
    protected int user_id;

    @Expose
    @DatabaseField
    protected boolean tax_inclusive;

    @Expose
    @DatabaseField
    protected String remark;

    @Expose
    @DatabaseField
    protected String reference;

    @Expose
    protected BatchList<InvoiceLine> invoice_lines = new BatchList<>(DatabaseOperation.INSERT);
    @Expose
    protected BatchList<Payment> payments = new BatchList<>(DatabaseOperation.INSERT);
    @Expose
    protected BatchList<InvoiceTaxRate> invoice_tax_rates = new BatchList<>(DatabaseOperation.INSERT);

    @ForeignCollectionField
    private transient ForeignCollection<InvoiceLine> invoice_lines_fc;
    @ForeignCollectionField
    private transient ForeignCollection<Payment> payments_fc;
    @ForeignCollectionField
    private transient ForeignCollection<InvoiceTaxRate> invoice_tax_rates_fc;

    public void setInvoiceDate(String date) { invoice_date = date; }

    public String getInvoiceDate() { return invoice_date; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public boolean isTaxInclusive() {
        return tax_inclusive;
    }

    public void setTaxInclusive(boolean tax_inclusive) {
        this.tax_inclusive = tax_inclusive;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<InvoiceLine> getInvoiceLines() {
        if(invoice_lines == null)
            invoice_lines = new BatchList<>(DatabaseOperation.INSERT);
        if(invoice_lines.size() <= 0)
            refreshListObjects();
        return invoice_lines;
    }

    public void setInvoiceLines(BatchList<InvoiceLine> invoice_lines) {
        this.invoice_lines = invoice_lines;
    }

    public JSONArray getInvoiceLinesJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        refreshListObjects();
        for(InvoiceLine invoiceLine : invoice_lines)
            jsonArray.put(invoiceLine.toJSONObject());
        return jsonArray;
    }

    public List<Payment> getPayments() {
        if(payments == null || payments.size() <= 0)
            refreshListObjects();
        return payments;
    }

    public void setPayments(BatchList<Payment> payments) {
        this.payments = payments;
    }

    public JSONArray getPaymentsJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        refreshListObjects();
        for(Payment payment : payments)
            jsonArray.put(payment.toJSONObject());
        return jsonArray;
    }

    public List<InvoiceTaxRate> getInvoiceTaxRates() {
        if(invoice_tax_rates == null || invoice_tax_rates.size() <= 0)
            refreshListObjects();
        return invoice_tax_rates;
    }

    public void setInvoiceTaxRates(BatchList<InvoiceTaxRate> invoice_tax_rates) {
        this.invoice_tax_rates = invoice_tax_rates;
    }

    public JSONArray getInvoiceTaxRatesJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        refreshListObjects();
        for(InvoiceTaxRate invoiceTaxRate : invoice_tax_rates)
            jsonArray.put(invoiceTaxRate.toJSONObject());
        return jsonArray;
    }

    private transient boolean didRefresh = false;
    // call this to save the InvoiceLine, Payment and InvoiceTaxRates objects to database
    public Invoice refresh(ImonggoDBHelper dbHelper) {
        if(didRefresh) {
            Log.e("Invoice", "refresh : called more than once");
            return this;
        }
        didRefresh = true;

        for(InvoiceLine invoiceLine : invoice_lines)
            invoiceLine.setInvoice(this);

        for(InvoiceTaxRate invoiceTaxRate : invoice_tax_rates)
            invoiceTaxRate.setInvoice(this);

        for(Payment payment : payments)
            payment.setInvoice(this);

        invoice_lines.doOperation(dbHelper);
        invoice_tax_rates.doOperation(dbHelper);
        payments.doOperation(dbHelper);
        return this;
    }

    // call this when using getHelper().getInvoices() to re-initialize the list that
    // was lost when Invoice was saved to the database
    public Invoice refreshListObjects() {
        if( (invoice_lines == null || invoice_lines.size() <= 0) &&
                (invoice_lines_fc != null && invoice_lines_fc.size() > 0)) {
            for(InvoiceLine invoiceLine : invoice_lines_fc) {
                invoice_lines.add(invoiceLine);
            }
        }
        if( (invoice_tax_rates == null || invoice_tax_rates.size() <= 0) &&
                (invoice_tax_rates_fc != null && invoice_tax_rates_fc.size() > 0)) {
            for(InvoiceTaxRate invoiceTaxRate : invoice_tax_rates_fc) {
                invoice_tax_rates.add(invoiceTaxRate);
            }
        }
        if( (payments == null || payments.size() <= 0) &&
                (payments_fc != null && payments_fc.size() > 0)) {
            for(Payment payment : payments_fc) {
                payments.add(payment);
            }
        }
        return this;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        refreshListObjects();
        return new JSONObject(gson.toJson(this));
    }

    public String toJSONString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        refreshListObjects();
        return (gson.toJson(this));
    }

    private Invoice() {}

    public static Invoice fromJSONString(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        return fromJSONObject(jsonObject);
    }

    public static Invoice fromJSONObject(JSONObject jsonObject) throws JSONException {
        Gson gson = new Gson();
        if(jsonObject.has("invoice")) {
            jsonObject = jsonObject.getJSONObject("invoice");
        }
        Invoice invoice = gson.fromJson(jsonObject.toString(),Invoice.class);
        return invoice;
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        if(id < 0) {
            Log.e("Invoice", "insertTo : Invalid ID : use returned ID from server after sending as this Invoice's ID");
            return;
        }

        refresh(dbHelper);
        try {
            dbHelper.dbOperations(this, Table.INVOICES, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        refresh(dbHelper);
        try {
            dbHelper.dbOperations(this, Table.INVOICES, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        refresh(dbHelper);
        try {
            dbHelper.dbOperations(this, Table.INVOICES, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
