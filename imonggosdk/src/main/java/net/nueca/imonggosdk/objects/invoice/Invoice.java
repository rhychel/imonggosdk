package net.nueca.imonggosdk.objects.invoice;

import android.content.Context;

import com.google.gson.Gson;

import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.objects.base.BaseTransaction;
import net.nueca.imonggosdk.objects.base.BatchList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 7/1/15.
 */
public class Invoice extends BaseTransaction {
    protected String invoice_date;

    protected String status;

    protected String email;

    protected int user_id;

    protected boolean tax_inclusive;

    protected String remark;

    protected List<InvoiceLine> invoice_lines;

    protected List<InvoicePayment> payments;

    protected List<InvoiceTaxRate> invoice_tax_rates;

    public Invoice(Builder builder) {
        super(builder);
        invoice_date = builder.invoice_date;
        status = builder.status;
        email = builder.email;
        user_id = builder.user_id;
        tax_inclusive = builder.tax_inclusive;
        remark = builder.remark;
        invoice_lines = builder.invoice_lines;
        invoice_tax_rates = builder.invoice_tax_rates;
        payments = builder.payments;
    }

    public void setInvoice_date(String date) { invoice_date = date; }

    public String getInvoice_date() { return invoice_date; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public boolean isTax_inclusive() {
        return tax_inclusive;
    }

    public void setTax_inclusive(boolean tax_inclusive) {
        this.tax_inclusive = tax_inclusive;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<InvoiceLine> getInvoiceLines() {
        if(invoice_lines == null)
            invoice_lines = new BatchList<>(DatabaseOperation.INSERT);
        return invoice_lines;
    }

    public void setInvoiceLines(BatchList<InvoiceLine> invoice_lines) {
        this.invoice_lines = invoice_lines;
    }

    public JSONArray getInvoiceLinesJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(InvoiceLine invoiceLine : invoice_lines)
            jsonArray.put(invoiceLine.toJSONObject());
        return jsonArray;
    }

    public List<InvoicePayment> getPayments() {
        return payments;
    }

    public void setPayments(BatchList<InvoicePayment> invoicePayments) {
        this.payments = invoicePayments;
    }

    public JSONArray getPaymentsJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(InvoicePayment invoicePayment : payments)
            jsonArray.put(invoicePayment.toJSONObject());
        return jsonArray;
    }

    public List<InvoiceTaxRate> getInvoiceTaxRates() {
        return invoice_tax_rates;
    }

    public void setInvoiceTaxRates(BatchList<InvoiceTaxRate> invoice_tax_rates) {
        this.invoice_tax_rates = invoice_tax_rates;
    }

    public JSONArray getInvoiceTaxRatesJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(InvoiceTaxRate invoiceTaxRate : invoice_tax_rates)
            jsonArray.put(invoiceTaxRate.toJSONObject());
        return jsonArray;
    }

    public void addInvoiceLine(InvoiceLine invoiceLine) {
        invoice_lines.add(invoiceLine);
    }
    public void addInvoiceTaxRate(InvoiceTaxRate invoiceTaxRate) {
        invoice_tax_rates.add(invoiceTaxRate);
    }
    public void addPayment(InvoicePayment invoicePayment) {
        payments.add(invoicePayment);
    }

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
    public boolean shouldPageRequest() {
        return false;
    }

    public static class Builder extends BaseTransaction.Builder<Builder> {
        protected String invoice_date = "";
        protected String status = "";
        protected String email = "";
        protected int user_id = 0;
        protected boolean tax_inclusive = false;
        protected String remark = "";
        protected List<InvoiceLine> invoice_lines = new ArrayList<>();
        protected List<InvoicePayment> payments = new ArrayList<>();
        protected List<InvoiceTaxRate> invoice_tax_rates = new ArrayList<>();

        public Builder invoice_date(String invoice_date) {
            this.invoice_date = invoice_date;
            return this;
        }
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        public Builder user_id(int user_id) {
            this.user_id = user_id;
            return this;
        }
        public Builder tax_inclusive(boolean tax_inclusive) {
            this.tax_inclusive = tax_inclusive;
            return this;
        }
        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }
        public Builder invoice_lines(List<InvoiceLine> invoice_lines) {
            this.invoice_lines = invoice_lines;
            return this;
        }
        public Builder invoice_tax_rates(List<InvoiceTaxRate> invoice_tax_rates) {
            this.invoice_tax_rates = invoice_tax_rates;
            return this;
        }
        public Builder payments(List<InvoicePayment> payments) {
            this.payments = payments;
            return this;
        }

        public Builder addInvoiceLine(InvoiceLine invoiceLine) {
            if(invoice_lines == null)
                invoice_lines = new ArrayList<>();
            invoice_lines.add(invoiceLine);
            return this;
        }
        public Builder addInvoiceTaxRate(InvoiceTaxRate invoiceTaxRate) {
            if(invoice_tax_rates == null)
                invoice_tax_rates = new ArrayList<>();
            invoice_tax_rates.add(invoiceTaxRate);
            return this;
        }
        public Builder addPayment(InvoicePayment payment) {
            if(payments == null)
                payments = new ArrayList<>();
            payments.add(payment);
            return this;
        }

        public Invoice build() {
            return new Invoice(this);
        }
    }
}
