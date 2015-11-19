package net.nueca.imonggosdk.objects.invoice;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.base.BaseTransactionLine;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by gama on 7/1/15.
 */
public class InvoiceLine extends BaseTransactionLine {
    @Expose
    @DatabaseField
    protected String discount_text;
    @Expose
    @DatabaseField
    protected String subtotal;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "invoice_id")
    protected transient Invoice invoice;

    public InvoiceLine() {}

    public InvoiceLine (Builder builder) {
        super(builder);
        discount_text = builder.discount_text;
        subtotal = builder.subtotal;
    }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
    }

    public static class Builder extends BaseTransactionLine.Builder<Builder> {
        protected String discount_text;
        protected String subtotal;

        public Builder discount_text(String discount_text) {
            this.discount_text = discount_text;
            return this;
        }
        public Builder subtotal(String subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public InvoiceLine build() {
            return new InvoiceLine(this);
        }
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(InvoiceLine.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(InvoiceLine.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.update(InvoiceLine.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
