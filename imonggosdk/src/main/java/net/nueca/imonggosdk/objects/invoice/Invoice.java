package net.nueca.imonggosdk.objects.invoice;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.base.BaseTransactionTable2;
import net.nueca.imonggosdk.objects.base.BatchList;
import net.nueca.imonggosdk.objects.customer.Customer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 7/1/15.
 */
public class Invoice extends BaseTransactionTable2 {
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
    protected List<InvoiceLine> invoice_lines;
    @Expose
    protected List<InvoicePayment> payments;
    @Expose
    protected List<InvoiceTaxRate> invoice_tax_rates;

    @ForeignCollectionField
    private transient ForeignCollection<InvoiceLine> invoice_lines_fc;
    @ForeignCollectionField
    private transient ForeignCollection<InvoicePayment> payments_fc;
    @ForeignCollectionField
    private transient ForeignCollection<InvoiceTaxRate> invoice_tax_rates_fc;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "offlinedata_id")
    protected transient OfflineData offlineData;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "customer_id")
    protected transient Customer customer;

    public Invoice() {}

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
        customer = builder.customer;
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
        refresh();
        if(invoice_lines == null)
            invoice_lines = new BatchList<>(DatabaseOperation.INSERT);
        return invoice_lines;
    }

    public void setInvoiceLines(BatchList<InvoiceLine> invoice_lines) {
        this.invoice_lines = invoice_lines;
    }

    public List<InvoicePayment> getPayments() {
        refresh();
        return payments;
    }

    public void setPayments(BatchList<InvoicePayment> invoicePayments) {
        this.payments = invoicePayments;
    }

    public List<InvoiceTaxRate> getInvoiceTaxRates() {
        refresh();
        return invoice_tax_rates;
    }

    public void setInvoiceTaxRates(BatchList<InvoiceTaxRate> invoice_tax_rates) {
        this.invoice_tax_rates = invoice_tax_rates;
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

    public void setOfflineData(OfflineData offlineData) {
        this.offlineData = offlineData;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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
        refresh();
        return false;
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public String toJSONString() {
        refresh();
        return super.toJSONString();
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        refresh();
        return super.toJSONObject();
    }

    public static class Builder extends BaseTransactionTable2.Builder<Builder> {
        protected String invoice_date;
        protected String status;
        protected String email;
        protected int user_id;
        protected boolean tax_inclusive = false;
        protected String remark;
        protected List<InvoiceLine> invoice_lines = new ArrayList<>();
        protected List<InvoicePayment> payments = new ArrayList<>();
        protected List<InvoiceTaxRate> invoice_tax_rates = new ArrayList<>();
        protected Customer customer;

        public Builder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

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

    @Override
    public void refresh() {
        if(invoice_lines_fc != null && invoice_lines == null) {
            invoice_lines = new ArrayList<>(invoice_lines_fc);
        }
        if(payments_fc != null && payments == null) {
            payments = new ArrayList<>(payments_fc);
        }
        if(invoice_tax_rates_fc != null && invoice_tax_rates == null) {
            invoice_tax_rates = new ArrayList<>(invoice_tax_rates_fc);
        }
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        insertExtrasTo(dbHelper);
        try {
            dbHelper.insert(Invoice.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refresh();
        if(invoice_lines != null) {
            for (InvoiceLine invoiceLine : invoice_lines) {
                invoiceLine.setInvoice(this);
                invoiceLine.insertTo(dbHelper);
            }
        }
        if(invoice_tax_rates != null) {
            for (InvoiceTaxRate invoiceTaxRate : invoice_tax_rates) {
                invoiceTaxRate.setInvoice(this);
                invoiceTaxRate.insertTo(dbHelper);
            }
        }
        if(payments != null) {
            for (InvoicePayment payment : payments) {
                payment.setInvoice(this);
                payment.insertTo(dbHelper);
            }
        }

        updateExtrasTo(dbHelper);
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(Invoice.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refresh();
        if(invoice_lines != null) {
            for (InvoiceLine invoiceLine : invoice_lines) {
                invoiceLine.deleteTo(dbHelper);
            }
        }
        if(invoice_tax_rates != null) {
            for (InvoiceTaxRate invoiceTaxRate : invoice_tax_rates) {
                invoiceTaxRate.deleteTo(dbHelper);
            }
        }
        if(payments != null) {
            for (InvoicePayment payment : payments) {
                payment.deleteTo(dbHelper);
            }
        }

        deleteExtrasTo(dbHelper);
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(Invoice.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refresh();
        if(invoice_lines != null) {
            for (InvoiceLine invoiceLine : invoice_lines) {
                invoiceLine.setInvoice(this);
                invoiceLine.updateTo(dbHelper);
            }
        }
        if(invoice_tax_rates != null) {
            for (InvoiceTaxRate invoiceTaxRate : invoice_tax_rates) {
                invoiceTaxRate.setInvoice(this);
                invoiceTaxRate.updateTo(dbHelper);
            }
        }
        if(payments != null) {
            for (InvoicePayment payment : payments) {
                payment.setInvoice(this);
                payment.updateTo(dbHelper);
            }
        }

        updateExtrasTo(dbHelper);
    }

    @Override
    public void insertExtrasTo(ImonggoDBHelper2 dbHelper) {
        if(extras != null) {
            extras.setInvoice(this);
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

}